//package com.mtn.concurrentcalls.backup;
//
//import com.mtn.concurrentcalls.services.FinancialServiceCaller;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import java.math.BigDecimal;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.Timestamp;
//import java.time.Duration;
//import java.time.Instant;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.CompletableFuture;
//
//@Component
//public class FinancialTransactionRunnerBackup {
//
//	@Autowired
//	FinancialServiceCaller slowServiceCaller;
//
//	@Autowired
//	private Connection con;
//
//
//	@Value("${dbReal_Schema}")
//	private String dbRealSchema;
//
//	@Value("${batch_Size}")
//	private int batchSize;
//
//	@Value("${result_Set_Fetch_Size}")
//	private int resultSetFetchSize;
//
//	@Value("${run_Every}")
//	private int runEvery;
//
//	@Value("${run_Every_Unit}")
//	private String runEveryUnit;
//
//	@Value("${sleep_Every}")
//	private int sleepEvery;
//
//	@Value("${accelerate_Run_Every}")
//	private int accelerateRunEvery;
//
//	@Value("${return_Response}")
//	private boolean returnResponse;
//
//	@Value("${db_Link}")
//	private String db_Link;
//
//
//	public void execute(long batchId) throws Exception {
//		Instant start = Instant.now();
//		PreparedStatement pStatement = null;
//		PreparedStatement pStatement2 = null;
//		ResultSet rs = null;
//		String query = null;
//		String response = null;
//		int i = 0;
//		int period = runEvery;
//		CompletableFuture<String> completedObj = null;
//		List<CompletableFuture<String>> allFutures = new ArrayList<>();
//		try {
//			System.out.printf("Financial trigger running...");
//			System.out.println(new Timestamp(System.currentTimeMillis()) + " 4");
//			Timestamp startDateTimestamp = null;
//			Timestamp endDateTimestamp = null;
//			Timestamp accDateTimestamp = null;
//			Timestamp maxTimestamp = null;
//			System.out.println(new Timestamp(System.currentTimeMillis()) + " 41");
//			pStatement = con.prepareStatement("SELECT max (finalizedtime) from " + dbRealSchema + ".RDS$FINANCIALRECEIPT" + db_Link, ResultSet.TYPE_FORWARD_ONLY,
//					ResultSet.CONCUR_READ_ONLY);
//			rs = pStatement.executeQuery();
//			System.out.println(new Timestamp(System.currentTimeMillis()) + " 42");
//			while (rs.next()) {
//				System.out.println(new Timestamp(System.currentTimeMillis()) + " 43");
//				maxTimestamp = rs.getTimestamp(1);
//				System.out.println("maxTimestamp= " + maxTimestamp);
//			}
//			System.out.println(new Timestamp(System.currentTimeMillis()) + " 44");
//			rs.close();
//			pStatement.close();
//			//
//			pStatement = con.prepareStatement("SELECT max (finalizedtime) start_time, max (finalizedtime) + interval '"
//							+ runEvery + "' " + runEveryUnit + " end_time ," + " max (finalizedtime) + interval '"
//							+ accelerateRunEvery + "' " + runEveryUnit + " acc_time from FINANCIAL_API_FINALIZEDTIME", ResultSet.TYPE_FORWARD_ONLY,
//					ResultSet.CONCUR_READ_ONLY);
//			rs = pStatement.executeQuery();
//			System.out.println(new Timestamp(System.currentTimeMillis()) + " 51");
//			while (rs.next()) {
//				System.out.println(new Timestamp(System.currentTimeMillis()) + " 52");
//				startDateTimestamp = rs.getTimestamp(1);
//				System.out.println("startDateTimestamp= " + startDateTimestamp);
//				System.out.println(new Timestamp(System.currentTimeMillis()) + " 53");
//				endDateTimestamp = rs.getTimestamp(2);
//				System.out.println("endDateTimestamp= " + endDateTimestamp);
//				accDateTimestamp = rs.getTimestamp(3);
//				System.out.println("accDateTimestamp= " + accDateTimestamp);
//				System.out.println(new Timestamp(System.currentTimeMillis()) + " 54");
//			}
//			System.out.println(new Timestamp(System.currentTimeMillis()) + " 55");
//
//			long diff = (maxTimestamp.getTime() - startDateTimestamp.getTime()) / (1000 * 60);
//			if (diff >= 5) {
//				period = accelerateRunEvery;
//				endDateTimestamp = accDateTimestamp;
//			}
//			System.out.println("diff:"+diff);
//			if (diff <= 1)
//				return;
//			System.out.println("Max dateTime in transactions is : " + maxTimestamp);
//			System.out.println("Max dateTime of  processing  is : " + startDateTimestamp);
//			System.out.println("Difference between two times is : " + diff + " Minute(s)");
//			rs.close();
//			pStatement.close();
//			pStatement = con.prepareStatement(
//					"select count(1) from " + dbRealSchema + ".RDS$FINANCIALRECEIPT" + db_Link + " where finalizedtime >= ?", ResultSet.TYPE_FORWARD_ONLY,
//					ResultSet.CONCUR_READ_ONLY);
//			pStatement.setTimestamp(1, startDateTimestamp);
//			rs = pStatement.executeQuery();
//			System.out.println(new Timestamp(System.currentTimeMillis()) + " 56");
//			if (rs.next()) {
//				System.out.println(new Timestamp(System.currentTimeMillis()) + " 57");
//				if (rs.getInt(1) <= 0) {
//					System.out.println(new Timestamp(System.currentTimeMillis()) + " 58");
//					rs.close();
//					pStatement.close();
//					return;
//				}
//				rs.close();
//				pStatement.close();
//			}
//			System.out.println(new Timestamp(System.currentTimeMillis()) + " 6");
//			pStatement = con
//					.prepareStatement("Insert into FINANCIAL_API_FINALIZEDTIME Select max(finalizedTime) + Interval '"
//									+ period + "' " + runEveryUnit + " from FINANCIAL_API_FINALIZEDTIME", ResultSet.TYPE_FORWARD_ONLY,
//							ResultSet.CONCUR_READ_ONLY);
//			pStatement.executeUpdate();
//			con.commit();
//			pStatement.close();
//
//			String query1="SELECT /* + PARALLEL (20) */ FINANCIALTRANSACTIONID,TRANSFERTYPE FROM ( "
//					+ "SELECT FINANCIALTRANSACTIONID,TRANSFERTYPE FROM " + dbRealSchema + ".RDS$FINANCIALRECEIPT" + db_Link + " where finalizedtime >= ?" + " and finalizedtime < ?"
//					+ "     and (transfertype in " + "( "
//					+ "'BATCH_TRANSFER', 'CASH_IN', 'CASH_OUT', 'DEBIT', 'FLOAT_TRANSFER', 'PAYMENT', 'TRANSFER', 'TRANSFER_FROM_ANY_BANK', 'WITHDRAWAL', "
//					+ "'ADJUSTMENT','REVERSAL') or FINANCIALINSTRUCTIONTYPE = 'RESOLVE_DEPOSIT') union ALL select FR.financialtransactionid FINANCIALTRANSACTIONID, FRD.transfertype TRANSFERTYPE "
//					+ " from " + dbRealSchema + ".RDS$FINANCIALRECEIPT" + db_Link + " FR "
//					+ "left outer join " + dbRealSchema + ".RDS$FINANCIALRECDETAILS" + db_Link + " FRD on FR.FINANCIALTRANSACTIONID = FRD.FINANCIALTRANSACTIONID "
//					+ "where FR.finalizedtime >= ? and FR.finalizedtime < ? AND FRD.TRANSFERTYPE = 'COMMISSIONING' "
//					+ " ) ";
//			System.out.println(query1);
//			pStatement = con.prepareStatement(query1
//					, ResultSet.TYPE_FORWARD_ONLY,
//					ResultSet.CONCUR_READ_ONLY);
//
//			pStatement.setTimestamp(1, startDateTimestamp);
//			pStatement.setTimestamp(2, endDateTimestamp);
//			pStatement.setTimestamp(3, startDateTimestamp);
//			pStatement.setTimestamp(4, endDateTimestamp);
//			rs = pStatement.executeQuery();
//			rs.setFetchSize(resultSetFetchSize);
//			System.out.println(new Timestamp(System.currentTimeMillis()) + " 7");
//			query = "insert into FRAUDAPISTATUS(TRANSACTIONID,TRANSFERTYPE,REQUEST_SENT) values (?,?,1)";
//			pStatement2 = con.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY,
//					ResultSet.CONCUR_READ_ONLY);
//			int counter = 0;
//
//
//
//
//			System.out.println(new Timestamp(System.currentTimeMillis()) + " 8");
//			BigDecimal tranId;
//			String tranType = null;
//			while (rs.next()) {
//				++counter;
//				tranId = rs.getBigDecimal(1);
//				tranType = rs.getString(2);
//				completedObj = slowServiceCaller.callSASFMService(tranId, tranType);
//				if (completedObj != null)
//					allFutures.add(completedObj);
//
//				pStatement2.setBigDecimal(1, tranId);
//				pStatement2.setString(2, tranType);
//
//				pStatement2.addBatch();
//				if (counter % batchSize == 0) {
//					pStatement2.executeBatch();
//					pStatement2.clearBatch();
//					con.commit();
//				}
//			}
//			rs.close();
//			System.out.println(new Timestamp(System.currentTimeMillis()) + " 9");
//			System.out.println(new Timestamp(System.currentTimeMillis()) + " # of requests within this round is : " + counter);
//			pStatement2.executeBatch();
//			pStatement2.clearBatch();
//			con.commit();
//
//			pStatement.close();
//			pStatement2.close();
//			System.out.println(new Timestamp(System.currentTimeMillis()) + " 90");
//			if (returnResponse) {
//				CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0]));
//				System.out.println(new Timestamp(System.currentTimeMillis()) + " 91");
//
//				query = "insert into FRAUDAPISTATUS2(response,TRANSACTIONID,GET_response) values (?,?,1)";
//				pStatement2 = con.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY,
//						ResultSet.CONCUR_READ_ONLY);
//
//				for (i = 0; i < allFutures.size(); i++) {
//					pStatement2.setLong(2, (i + 1));
//					try {
//						response = allFutures.get(i).get().toString();
//						System.out.println(response);
//					} catch (Exception ex) {
//						//ex.printStackTrace();
//						response = ex.getMessage();
//						System.out.println(response);
//						pStatement2.setString(1, response);
//						pStatement2.addBatch();
//						if ((i + 1) % batchSize == 0) {
//							pStatement2.executeBatch();
//							pStatement2.clearBatch();
//							con.commit();
//						}
//						continue;
//					}
//					pStatement2.setString(1, response);
//					pStatement2.addBatch();
//					if ((i + 1) % batchSize == 0) {
//						pStatement2.executeBatch();
//						pStatement2.clearBatch();
//						con.commit();
//					}
//				}
//
//				pStatement2.executeBatch();
//				pStatement2.clearBatch();
//				con.commit();
//			}
//			rs.close();
//			pStatement.close();
//			pStatement2.close();
//			System.out.println(new Timestamp(System.currentTimeMillis()) + " Total time to process the round is : "
//					+ Duration.between(start, Instant.now()).getSeconds() + " Second(s)");
//			System.out.println(new Timestamp(System.currentTimeMillis()) + " 12");
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		} finally {
//
//			if (rs != null) rs.close();
//			if (pStatement != null) pStatement.close();
//			if (pStatement2 != null) pStatement2.close();
//		}
//	}
//}
