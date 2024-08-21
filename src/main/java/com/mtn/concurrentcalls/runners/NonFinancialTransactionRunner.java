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
	@Value("${dbReal_Schema}")
	private String dbRealSchema;
	@Value("${db_Link}")
	private String db_Link;
	@Value("${batch_Size}")
	private int batchSize;

	public void execute(long threadId) {
		System.out.println("01. Execution started...");

		DatabaseHelper databaseHelper = new DatabaseHelper();
		Connection firstCon= databaseHelper.openConnection();
		try {
			Timestamp startDateTimestamp = null;

			//getting start datetime
			PreparedStatement startTimeStatement = firstCon.prepareStatement("SELECT max(FINALIZEDTIME) from FRAUDAPIPROCESSINGSTATUS WHERE BATCHTYPE=?", ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY);
			startTimeStatement.setString(1, BatchType.NONFINANCIALBATCH.toString());
			ResultSet rs1 = databaseHelper.executeStatement(startTimeStatement);
			System.out.println("01. Getting start date time...");
			if (rs1 != null && rs1.next()) {
				startDateTimestamp = rs1.getTimestamp(1);

				startTimeStatement.close();
				rs1.close();
			}
			System.out.println("Datetime: "+startDateTimestamp);

			//if previous finalizedtime is not there, get from audit rail
			if (startDateTimestamp == null) {
				System.out.println("02. Getting start date time from RDS$AUDITTRAILLOGEVENT...");
				PreparedStatement maxStartTimeStatement = firstCon.prepareStatement("SELECT max (LOGGINGTIME) from " + dbRealSchema + ".RDS$AUDITTRAILLOGEVENT" + db_Link,  ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY);
				ResultSet rs2 = databaseHelper.executeStatement(maxStartTimeStatement);
				if (rs2 != null && rs2.next()) {
					startDateTimestamp = rs2.getTimestamp(1);

					maxStartTimeStatement.close();
					rs2.close();
				}
			}
			System.out.println("Datetime: "+startDateTimestamp);
			//end


			System.out.println("03.Get count of transaction type and count..");
			PreparedStatement categoryStatement = firstCon.prepareStatement("SELECT TRANSACTIONTYPE, COUNT(*) FROM " + dbRealSchema + ".RDS$AUDITTRAILLOGEVENT" + db_Link + " WHERE LOGGINGTIME >=? GROUP BY TRANSACTIONTYPE", ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY);
			categoryStatement.setTimestamp(1, startDateTimestamp);
			ResultSet rs3 = databaseHelper.executeStatement(categoryStatement);

			String transactionType;
			int count;
			if(rs3 != null) {
				while (rs3.next()){
					System.out.println("04.Getting transaction type and count based on column index..");
					transactionType = rs3.getString(1);
					count = rs3.getInt(2);
					System.out.println(transactionType + " is " + count);
					if (count > 0) {
						log.info("Processing (" + count + ") transactions of " + transactionType);
						buildAndProcessBatch(threadId, transactionType, startDateTimestamp);
					}
				}

				categoryStatement.close();
				rs3.close();
				firstCon.close();
			}
		} catch (Exception ex) {
			log.error(ex.getMessage());
			ex.printStackTrace();
		}
	}

	public void buildAndProcessBatch(Long threadId, String transactionType, Timestamp startDateTimestamp) {

		CompletableFuture<String> completedObj;

		DatabaseHelper databaseHelper = new DatabaseHelper();
		Connection secondCon = databaseHelper.openConnection();

		try {
			System.out.println("04.Getting all transaction details..");
			String query = "SELECT TRANSACTIONID,TRANSACTIONTYPE FROM " + dbRealSchema + ".RDS$AUDITTRAILLOGEVENT" + db_Link + "  WHERE LOGGINGTIME >=?"
					+ "AND TRANSACTIONTYPE =?";
			PreparedStatement statement = secondCon.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY);
			statement.setTimestamp(1, startDateTimestamp);
			statement.setString(2, transactionType);

			ResultSet rs1 = databaseHelper.executeStatement(statement);
			String insertQuery;
			PreparedStatement preparedStatement;
			long batchId = threadId + new UniqueIdGenerator().generateLongId();
			if (rs1 != null) {
				int counter=0;
				while (rs1.next()) {
					++counter;

					System.out.println("05.Insert to FRAUDAPIPROCESSINGSTATUS..");
					insertQuery = "INSERT INTO FRAUDAPIPROCESSINGSTATUS(TRANSACTIONID, TRANSACTIONTYPE, BATCHTYPE, BATCHID, STATUS, FINALIZEDTIME) values (?,?,?,?,'PENDING',current_timestamp)";
					preparedStatement = secondCon.prepareStatement(insertQuery, ResultSet.TYPE_FORWARD_ONLY,
							ResultSet.CONCUR_READ_ONLY);
					preparedStatement.setString(1, rs1.getString(1));
					preparedStatement.setString(2, rs1.getString(2));
					preparedStatement.setString(3, BatchType.NONFINANCIALBATCH.toString());
					preparedStatement.setLong(4, batchId);
					databaseHelper.executeStatement(preparedStatement);

					preparedStatement.addBatch();
					if (counter == batchSize) {
						preparedStatement.executeBatch();
						preparedStatement.clearBatch();
						preparedStatement.close();
						secondCon.commit();
						break;
					}
				}
				statement.close();
				rs1.close();
				secondCon.commit();
				secondCon.close();
			}

			System.out.println("06.Calling Non financial SASFMS for batch ("+batchId+")..");
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
