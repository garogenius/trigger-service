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

	@Value("${dbReal_Schema}")
	private String dbRealSchema;
	@Value("${db_Link}")
	private String db_Link;
	@Value("${batch_Size}")
	private int batchSize;
	@Autowired
	private Connection con;

	public void execute(long threadId) {
		System.out.println("01. Execuation started...");
		try {
			Timestamp startDateTimestamp = null;
			//getting start datetime
			System.out.println(con.isReadOnly());
			PreparedStatement startTimeStatement = con.prepareStatement("SELECT max(FINALIZEDTIME) from FRAUDAPIPROCESSINGSTATUS WHERE BATCHTYPE=?", ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY);
			System.out.println(startTimeStatement);

			startTimeStatement.setString(1, BatchType.FINANCIALBATCH.toString());
			ResultSet rs1 = startTimeStatement.executeQuery();
			System.out.println("01. Getting start date time...");
			if (rs1 != null && rs1.next()) {
				startDateTimestamp = rs1.getTimestamp(1);
				startTimeStatement.close();
				rs1.close();
			}

			//if previous finalizedtime is not there, get from audit rail
			if (startDateTimestamp == null) {
				System.out.println("02. Getting start date time from RDS$FINANCIALRECEIPT...");
				PreparedStatement maxStartTimeStatement = con.prepareStatement("SELECT max (FINALIZEDTIME) from " + dbRealSchema + ".RDS$FINANCIALRECEIPT" + db_Link, ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY);
				ResultSet rs2 = maxStartTimeStatement.executeQuery();
				if (rs2 != null && rs2.next()) {
					startDateTimestamp = rs2.getTimestamp(1);
					maxStartTimeStatement.close();
					rs2.close();
				}
			}
			//end

			System.out.println("03.Get count of transaction by transfer type..");
			PreparedStatement categoryStatement = con.prepareStatement("SELECT TRANSFERTYPE, COUNT(*) AS COUNT FROM " + dbRealSchema + ".RDS$FINANCIALRECEIPT" + db_Link + " WHERE FINALIZEDTIME >=? GROUP BY TRANSFERTYPE", ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY);
			categoryStatement.setTimestamp(1, startDateTimestamp);
			ResultSet rs3 = categoryStatement.executeQuery();
			String transactionType;
			int count;
			if (rs3 != null) {
				while (rs3.next()) {
					System.out.println("04.Getting transaction type and count based on column index..");
					transactionType = rs3.getString(1);
					count = rs3.getInt(2);

					if (count > 0) {
						log.info("Processing " + count + " transactions of " + transactionType);
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
		try {
			System.out.println("04.Getting all transaction details..");
			String query = "SELECT FINANCIALTRANSACTIONID,TRANSFERTYPE FROM " + dbRealSchema + ".RDS$FINANCIALRECEIPT" + db_Link + "  WHERE FINALIZEDTIME >=?"
					+ "AND TRANSFERTYPE =? AND ROWNUM <?";
			PreparedStatement statement = con.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY);
			statement.setTimestamp(1, startDateTimestamp);
			statement.setString(2, transactionType);
			ResultSet rs1 = statement.executeQuery();

			String insertQuery;
			PreparedStatement preparedStatement;
			long batchId = threadId + new UniqueIdGenerator().generateLongId();
			System.out.println("05.looping through transactions..");

			if (rs1 != null) {
				int counter = 0;
				while (rs1.next()) {
					++counter;
					insertQuery = "INSERT INTO FRAUDAPIPROCESSINGSTATUS(TRANSACTIONID, TRANSACTIONTYPE, BATCHTYPE, BATCHID, STATUS, FINALIZEDTIME) values (?,?,?,?,'PENDING',current_timestamp)";
					preparedStatement = con.prepareStatement(insertQuery, ResultSet.TYPE_FORWARD_ONLY,
							ResultSet.CONCUR_UPDATABLE);
					preparedStatement.setString(1, rs1.getString(1));
					preparedStatement.setString(2, rs1.getString(2));
					preparedStatement.setString(3, BatchType.FINANCIALBATCH.toString());
					preparedStatement.setLong(4, batchId);
					preparedStatement.execute();

					preparedStatement.addBatch();
					if (counter == batchSize) {
						preparedStatement.executeBatch();
						preparedStatement.clearBatch();
						preparedStatement.close();
						con.commit();
						break;
					}
				}
				statement.close();
				rs1.close();
				con.commit();
				con.close();
			}

			System.out.println("06.Calling Financial SASFMS for batch ("+batchId+")..");
			completedObj = serviceCaller.callSASFMServiceBatchAPI(batchId);
			if (completedObj != null) {
				String response = completedObj.get();
				log.info("Financial batch request processed with batch " + batchId);
				log.info(response);
			} else {
				log.error("Unable to process system layer API call");
			}
		} catch (Exception ex) {
			log.error(ex.getMessage());
			ex.printStackTrace();
		}
	}
}
