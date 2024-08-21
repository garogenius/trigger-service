package com.mtn.concurrentcalls.runners;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.concurrent.CompletableFuture;
import com.mtn.concurrentcalls.enums.BatchType;
import com.mtn.concurrentcalls.helpers.DatabaseHelper;
import com.mtn.concurrentcalls.helpers.UniqueIdGenerator;
import com.mtn.concurrentcalls.services.NonFinancialServiceCaller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NonFinancialTransactionRunner {

	@Autowired
	NonFinancialServiceCaller serviceCaller;
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
			System.out.println("01. Execution started...");
			Timestamp startDateTimestamp = null;

			//getting start datetime
			PreparedStatement startTimeStatement = con.prepareStatement("SELECT max(FINALIZEDTIME) from FRAUDAPIPROCESSINGSTATUS WHERE BATCHTYPE=?", ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY);
			startTimeStatement.setString(1, BatchType.NONFINANCIALBATCH.toString());
			ResultSet rs1 = startTimeStatement.executeQuery();
			System.out.println("01. Getting start date time...");
			if (rs1 != null && rs1.next()) {
				startDateTimestamp = rs1.getTimestamp(1);

				startTimeStatement.close();
				rs1.close();
			}
			System.out.println("Datetime: "+startDateTimestamp);

			if (startDateTimestamp == null) {
				System.out.println("02. Getting start date time from RDS$AUDITTRAILLOGEVENT...");
				PreparedStatement maxStartTimeStatement = con.prepareStatement("SELECT max (LOGGINGTIME) from " + dbRealSchema + ".RDS$AUDITTRAILLOGEVENT" + db_Link,  ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY);
				ResultSet rs2 = maxStartTimeStatement.executeQuery();
				if (rs2 != null && rs2.next()) {
					startDateTimestamp = rs2.getTimestamp(1);

					maxStartTimeStatement.close();
					rs2.close();
				}
			}
			System.out.println("Datetime: "+startDateTimestamp);
			System.out.println("03.Get count of transaction type and count..");
			PreparedStatement categoryStatement = con.prepareStatement("SELECT TRANSACTIONTYPE, COUNT(*) FROM " + dbRealSchema + ".RDS$AUDITTRAILLOGEVENT" + db_Link + " WHERE LOGGINGTIME >=? GROUP BY TRANSACTIONTYPE", ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY);
			categoryStatement.setTimestamp(1, startDateTimestamp);
			ResultSet rs3 = categoryStatement.executeQuery();

			String transactionType;
			int count;
			if(rs3 != null) {
				while (rs3.next()){
					System.out.println("04.Getting transaction type and count");
					transactionType = rs3.getString(1);
					count = rs3.getInt(2);
					System.out.println(transactionType + " is " + count);
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
			log.info("############### Processing batch "+batchId+" ################## ");
			PreparedStatement preparedStatement;
			log.info("06: Preparing next " + batchSize + " record for " + transactionType + "  batch " + batchId);

			String query = "INSERT INTO FRAUDAPIPROCESSINGSTATUS SELECT TRANSACTIONID, TRANSACTIONTYPE,'" + BatchType.NONFINANCIALBATCH.toString() + "' AS BATCHTYPE, " + batchId + " AS BATCHID,'PENDING' AS STATUS, FINALIZEDTIME  FROM RDS_UG.RDS$AUDITTRAILLOGEVENT" + db_Link +"  WHERE TRANSFERTYPE =? AND LOGGINGTIME >=? ORDER BY LOGGINGTIME ASC";
			log.info("06.1: Prepared  SQL query" + query);
			preparedStatement = con.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_UPDATABLE);
			preparedStatement.setString(1, transactionType);
			preparedStatement.setTimestamp(2, startDateTimestamp);
			preparedStatement.setInt(3, batchSize);
			boolean dataResponse = preparedStatement.execute();
			log.info("07: Preparing status: " + dataResponse);
			log.info("08. Calling Financial SASFMS for batch " + batchId);
			completedObj = serviceCaller.callSASFMServiceBatchAPI(batchId);
			if (completedObj != null) {
				String response = completedObj.get();
				log.info("09: Non financial batch request processed with batch " + batchId);
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
