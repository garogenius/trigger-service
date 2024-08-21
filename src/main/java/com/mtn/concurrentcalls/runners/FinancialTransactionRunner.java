package com.mtn.concurrentcalls.runners;
import java.sql.*;
import java.util.concurrent.CompletableFuture;
import com.mtn.concurrentcalls.enums.BatchType;
import com.mtn.concurrentcalls.helpers.DatabaseHelper;
import com.mtn.concurrentcalls.helpers.UniqueIdGenerator;
import com.mtn.concurrentcalls.services.FinancialServiceCaller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FinancialTransactionRunner {


	@Autowired
	FinancialServiceCaller serviceCaller;
	@Autowired
	DatabaseHelper databaseHelper;
	@Autowired
	Connection con;

	@Value("${dbReal_Schema}")
	private String dbRealSchema;
	@Value("${db_Link}")
	private String db_Link;
	@Value("${batch_Size}")
	private int batchSize;


	public void execute(long threadId) {
		databaseHelper.persistLogTableIfNotExist();
		try {
			log.info("01. Execution started");
			Timestamp startDateTimestamp = null;
			//getting start datetime
			PreparedStatement startTimeStatement = con.prepareStatement("SELECT max(FINALIZEDTIME) from FRAUDAPIPROCESSINGSTATUS WHERE BATCHTYPE=?", ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY);

			startTimeStatement.setString(1, BatchType.FINANCIALBATCH.toString());
			ResultSet rs1 = startTimeStatement.executeQuery();
			log.info("01. Getting start date time...");
			if (rs1 != null && rs1.next()) {
				startDateTimestamp = rs1.getTimestamp(1);
				startTimeStatement.close();
				rs1.close();
			}


			if (startDateTimestamp == null) {
				log.info("02. Getting start date time from RDS$FINANCIALRECEIPT...");
				PreparedStatement maxStartTimeStatement = con.prepareStatement("SELECT max (FINALIZEDTIME) from " + dbRealSchema + ".RDS$FINANCIALRECEIPT" + db_Link, ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY);
				ResultSet rs2 = maxStartTimeStatement.executeQuery();
				if (rs2 != null && rs2.next()) {
					startDateTimestamp = rs2.getTimestamp(1);
					maxStartTimeStatement.close();
					rs2.close();
				}
			}
			System.out.println("Datetime: "+startDateTimestamp);

			log.info("03. Get count of transaction by transfer type..");
			PreparedStatement categoryStatement = con.prepareStatement("SELECT TRANSFERTYPE, COUNT(*) AS COUNT FROM " + dbRealSchema + ".RDS$FINANCIALRECEIPT" + db_Link + " WHERE FINALIZEDTIME >=? GROUP BY TRANSFERTYPE", ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY);
			categoryStatement.setTimestamp(1, startDateTimestamp);
			ResultSet rs3 = categoryStatement.executeQuery();
			String transactionType;
			int count;
			if (rs3 != null) {
				while (rs3.next()) {
					log.info("04. Getting transaction type and count");
					transactionType = rs3.getString(1);
					count = rs3.getInt(2);

					if (count > 0) {
						log.info("05: Processing " + transactionType);
						buildAndProcessBatch(threadId, transactionType, startDateTimestamp);
					}
				}

				categoryStatement.close();
				rs3.close();
			}
		} catch (Exception ex) {
			log.error(ex.getMessage());
			ex.printStackTrace();
		}
	}

	public void buildAndProcessBatch(Long threadId, String transactionType, Timestamp startDateTimestamp) {
		CompletableFuture<String> completedObj;
		long batchId = threadId + new UniqueIdGenerator().generateLongId();

		try {
			log.info("############### Processing financial batch "+batchId+" ################## ");
			PreparedStatement preparedStatement;
			log.info("06: Preparing next "+batchSize+" record for "+transactionType+"  batch "+batchId);

			String query = "INSERT INTO FRAUDAPIPROCESSINGSTATUS SELECT FINANCIALTRANSACTIONID AS TRANSACTIONID,TRANSFERTYPE AS TRANSACTIONTYPE,? AS BATCHTYPE, ? AS BATCHID,'PENDING' AS STATUS, FINALIZEDTIME  FROM RDS_UG.RDS$FINANCIALRECEIPT" + db_Link +"  WHERE TRANSFERTYPE =? AND FINALIZEDTIME >=? ORDER BY FINALIZEDTIME ASC";
		    log.info("06.1: Prepared  SQL query "+query);
			preparedStatement = con.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_UPDATABLE);

			preparedStatement.setString(1,BatchType.FINANCIALBATCH.toString());
			preparedStatement.setLong(2, batchId);
			preparedStatement.setString(3, transactionType);
			preparedStatement.setTimestamp(4, startDateTimestamp);
			boolean dataResponse= preparedStatement.execute();
			log.info("07: Preparing status: "+dataResponse);
			log.info("08. Calling Financial SASFMS for batch "+batchId);
			completedObj = serviceCaller.callSASFMServiceBatchAPI(batchId);
			if (completedObj != null) {
				String response = completedObj.get();
				log.info("09: Financial batch request processed with batch " + batchId);
				log.info(response);
			} else {
				log.error("10: Unable to process system layer API call");
			}
			log.info("############### Processing end for batch "+batchId+" ################## ");

		} catch (Exception ex) {
			log.error(ex.getMessage());
			ex.printStackTrace();
		}
	}
}
