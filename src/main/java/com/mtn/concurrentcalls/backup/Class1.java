//package com.mtn.concurrentcalls.backup;
//
//import java.math.BigDecimal;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.Timestamp;
//import java.time.Duration;
//import java.time.Instant;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.CompletableFuture;
//import java.util.stream.Collectors;
//
//import com.mtn.concurrentcalls.services.FinancialServiceCaller;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//@Component
//public class Class1 {
//
//	@Autowired
//    FinancialServiceCaller slowServiceCaller;
//
//	@Autowired
//	private Connection con;
//
//	/*@Autowired
//	ExecutorService executor;
//*/
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
//	/*
//	  public void execute() throws InterruptedException{
//	  System.out.println("Test Class 1"); Thread.sleep(10000); }
//	 */
//
//	public void execute() throws Exception {
//		/* System.out.println("numberOfRounds " + ++numOfRounds); */
//		Instant start = Instant.now();
//		// System.out.println(new Timestamp(System.currentTimeMillis()) + " 1");
//		System.out.println("Test Class 1");
//		doProcess(start);
//		// System.out.println(new Timestamp(System.currentTimeMillis()) + " 2");
//		Thread.sleep(sleepEvery);
//	}
//
//	private void doProcess(Instant start) throws Exception {
//		PreparedStatement pStatement = null;
//		PreparedStatement pStatement2 = null;
//		ResultSet rs = null;
//		String query = null;
//		String response = null;
//		int i = 0;
//		int period = runEvery;
//		CompletableFuture<String> completedObj = null;
//		List<CompletableFuture<String>> allFutures = new ArrayList<>();
//		Map<String, CompletableFuture<String>> requests = new HashMap<>();
//		try {
//			// System.out.println(new Timestamp(System.currentTimeMillis()) + " 4");
//			Timestamp startDateTimestamp = null;
//			Timestamp endDateTimestamp = null;
//			Timestamp accDateTimestamp = null;
//			Timestamp maxTimestamp = null;
//			// System.out.println(new Timestamp(System.currentTimeMillis()) + " 41");
//			//
//			pStatement = con.prepareStatement(
//					"SELECT max (finalizedtime) from " + dbRealSchema + ".RDS$FINANCIALRECEIPT",
//					java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
//			rs = pStatement.executeQuery();
//			// System.out.println(new Timestamp(System.currentTimeMillis()) + " 42");
//			while (rs.next()) {
//				// System.out.println(new Timestamp(System.currentTimeMillis()) + " 43");
//				maxTimestamp = rs.getTimestamp(1);
//				System.out.println("maxTimestamp= " + maxTimestamp);
//			}
//			// System.out.println(new Timestamp(System.currentTimeMillis()) + " 44");
//			rs.close();
//			pStatement.close();
//			pStatement = con.prepareStatement(
//					"SELECT max (finalizedtime) start_time, max (finalizedtime) + interval '" + runEvery + "' "
//							+ runEveryUnit + " end_time ," + " max (finalizedtime) + interval '" + accelerateRunEvery
//							+ "' " + runEveryUnit + " acc_time from FINANCIAL_API_FINALIZEDTIME",
//					java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
//			rs = pStatement.executeQuery();
//			// System.out.println(new Timestamp(System.currentTimeMillis()) + " 51");
//			while (rs.next()) {
//				// System.out.println(new Timestamp(System.currentTimeMillis()) + " 52");
//				startDateTimestamp = rs.getTimestamp(1);
//				System.out.println("startDateTimestamp= " + startDateTimestamp);
//				// System.out.println(new Timestamp(System.currentTimeMillis()) + " 53");
//				endDateTimestamp = rs.getTimestamp(2);
//				System.out.println("endDateTimestamp= " + endDateTimestamp);
//				accDateTimestamp = rs.getTimestamp(3);
//				// System.out.println("accDateTimestamp= " + accDateTimestamp);
//				// System.out.println(new Timestamp(System.currentTimeMillis()) + " 54");
//			}
//			// System.out.println(new Timestamp(System.currentTimeMillis()) + " 55");
//
//			long diff = (maxTimestamp.getTime() - startDateTimestamp.getTime()) / (1000 * 60);
//			if (diff >= 5) {
//				period = accelerateRunEvery;
//				endDateTimestamp = accDateTimestamp;
//			}
//			if (diff <=1)
//				return;
//			System.out.println(maxTimestamp);
//			System.out.println(startDateTimestamp);
//			System.out.println(diff);
//			rs.close();
//			pStatement.close();
//			pStatement = con.prepareStatement(
//					"select count(1) from " + dbRealSchema + ".RDS$FINANCIALRECEIPT where finalizedtime >= ?",
//					java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
//			pStatement.setTimestamp(1, startDateTimestamp);
//			rs = pStatement.executeQuery();
//			// System.out.println(new Timestamp(System.currentTimeMillis()) + " 56");
//			if (rs.next()) {
//				// System.out.println(new Timestamp(System.currentTimeMillis()) + " 57");
//				if (rs.getInt(1) <= 0) {
//					// System.out.println(new Timestamp(System.currentTimeMillis()) + " 58");
//					rs.close();
//					pStatement.close();
//					return;
//				}
//				rs.close();
//				pStatement.close();
//			}
//			// System.out.println(new Timestamp(System.currentTimeMillis()) + " 6");
//
//			pStatement = con
//			.prepareStatement("Insert into FINANCIAL_API_FINALIZEDTIME Select max(finalizedTime) + Interval '"
//					+ period + "' " + runEveryUnit + " from FINANCIAL_API_FINALIZEDTIME",java.sql.ResultSet.TYPE_FORWARD_ONLY,
//		              java.sql.ResultSet.CONCUR_READ_ONLY);
//	pStatement.executeUpdate();
//	con.commit();
//	pStatement.close();
//
//	//System.out.println(new Timestamp(System.currentTimeMillis()) + " 60");
//
//
//			pStatement = con.prepareStatement("SELECT /* + PARALLEL (20) */ FINANCIALTRANSACTIONID,TRANSFERTYPE FROM "
//			+ dbRealSchema + ".RDS$FINANCIALRECEIPT where finalizedtime >= ?" + " and finalizedtime < ?"
//			+ "     and transfertype in " + "( "
//			+ "'BATCH_TRANSFER', 'CASH_IN', 'CASH_OUT', 'DEBIT', 'FLOAT_TRANSFER', 'PAYMENT', 'TRANSFER', 'TRANSFER_FROM_ANY_BANK', 'WITHDRAWAL', "
//			+ "'ADJUSTMENT','REVERSAL','COMMISSIONING','RESOLVE_DEPOSIT') " ,java.sql.ResultSet.TYPE_FORWARD_ONLY,
//              java.sql.ResultSet.CONCUR_READ_ONLY);
//
//	pStatement.setTimestamp(1, startDateTimestamp);
//	pStatement.setTimestamp(2, endDateTimestamp);
//	rs = pStatement.executeQuery();
//	rs.setFetchSize(resultSetFetchSize);
//	//System.out.println(new Timestamp(System.currentTimeMillis()) + " 7");
//	query = "insert into FRAUDAPISTATUS(TRANSACTIONID,TRANSFERTYPE,REQUEST_SENT) values (?,?,1)";
//	pStatement2 = con.prepareStatement(query,java.sql.ResultSet.TYPE_FORWARD_ONLY,
//              java.sql.ResultSet.CONCUR_READ_ONLY);
//	int counter = 0;
//	//System.out.println(new Timestamp(System.currentTimeMillis()) + " 8");
//	BigDecimal tranId;
//	String tranType = null;
//	while (rs.next()) {
//		++counter;
//		tranId = rs.getBigDecimal(1);
//		tranType = rs.getString(2);
//		try
//		{
//
//			 completedObj = slowServiceCaller.callSASFMService(tranId, tranType);
//			 allFutures.add(completedObj);
//			 if (!returnResponse)
//				 completedObj.complete("Future's Result");
//		}
//		catch (Exception ex)
// 		{
//			ex.printStackTrace();
//			System.exit(0);
// 		}
//
//		pStatement2.setBigDecimal(1, tranId);
//		pStatement2.setString(2, tranType);
//
//		pStatement2.addBatch();
//		if (counter % batchSize == 0) {
//			pStatement2.executeBatch();
//			pStatement2.clearBatch();
//			con.commit();
//		}
//	}
//	rs.close();
//	//System.out.println(new Timestamp(System.currentTimeMillis()) + " 9");
//	System.out.println(new Timestamp(System.currentTimeMillis()) + " Counter is " + counter);
//	pStatement2.executeBatch();
//	pStatement2.clearBatch();
//	con.commit();
//
//	pStatement.close();
//	pStatement2.close();
//	//System.out.println(new Timestamp(System.currentTimeMillis()) + " 90");
//
//	if (returnResponse)
//	{
//	CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0]));
//	//System.out.println(new Timestamp(System.currentTimeMillis()) + " 91");
//
//	query = "insert into FRAUDAPISTATUS2(response,GET_response) values (?,1)";
//	pStatement2 = con.prepareStatement(query,java.sql.ResultSet.TYPE_FORWARD_ONLY,
//              java.sql.ResultSet.CONCUR_READ_ONLY);
//
//	/*System.out.println(new Timestamp(System.currentTimeMillis()) + " Count of Responses: "+allFutures.size());*/
//	//System.out.println(new Timestamp(System.currentTimeMillis()) + " 10");
//    /*Iterator<Map.Entry<String, CompletableFuture<String>>> itr = requests.entrySet().iterator();
//    i = 0;
//    while(itr.hasNext())
//    {
//    	++i;
//        Map.Entry<String, CompletableFuture<String>> entry = itr.next();
//        pStatement2.setString(2, entry.getKey());
//     	try {
//     	//response = entry.getValue().get(10, TimeUnit.MILLISECONDS).toString();
//     		response = entry.getValue().get().toString();
//     		//System.out.println(response);
//     	}
//
//     	catch (Exception ex)
// 		{
//     		//ex.printStackTrace();
//     		if (ex instanceof TimeoutException)
//     			response = "java.util.concurrent.TimeoutException";
//     		else
//     			response = ex.getMessage();
//     		//System.out.println(response);
//         	pStatement2.setString(1,response);
//         	pStatement2.addBatch();
//         	if (i % batchSize == 0) {
//                 pStatement2.executeBatch();
//                 pStatement2.clearBatch();
//                 con.commit();
//             }
//         	continue;
// 		}
//     	pStatement2.setString(1,response);
//     	pStatement2.addBatch();
//     	if (i % batchSize == 0) {
//             pStatement2.executeBatch();
//             pStatement2.clearBatch();
//             con.commit();
//         }
//    }*/
//
//	List<String> result = allFutures
//            .stream()
//            .filter(future -> future.isDone() && !future.isCompletedExceptionally())
//            .map(CompletableFuture::join)
//            .collect(Collectors.toList());
//	/*
//	for (int j = 0 ; j < result.size(); j++)
//		System.out.println(result.get(j));
//	*/
//	/*for (int j = 0 ; j < allFutures.size(); j++)
//		System.out.println(allFutures.get(j).get());
//	*/
//    for (i = 0; i < allFutures.size(); i++) {
//    	//if (allFutures.get(i).isCompletedExceptionally())
//    	//	response = allFutures.get(i).toString();
//    	//else
//    	//	response = allFutures.get(i).get().toString();
//
//    	try {
//    	response = allFutures.get(i).get().toString();
//    	System.out.println(response);
//    	}
//    	catch (Exception ex)
//		{
//			//ex.printStackTrace();
//			response = ex.getMessage();
//			System.out.println(response);
//        	pStatement2.setString(1,response);
//        	pStatement2.addBatch();
//        	if ((i+1) % batchSize == 0) {
//                pStatement2.executeBatch();
//                pStatement2.clearBatch();
//                con.commit();
//            }
//        	continue;
//		}
//    	pStatement2.setString(1,response);
//    	pStatement2.addBatch();
//    	if ((i+1) % batchSize == 0) {
//            pStatement2.executeBatch();
//            pStatement2.clearBatch();
//            con.commit();
//        }
//    }
//
//    pStatement2.executeBatch();
//    pStatement2.clearBatch();
//    con.commit();
//	}
//	//System.out.println(new Timestamp(System.currentTimeMillis()) + " 11");
//	rs.close();
//	pStatement.close();
//	pStatement2.close();
//	System.out.println(new Timestamp(System.currentTimeMillis()) + " Total time: "
//			+ Duration.between(start, Instant.now()).getSeconds());
//	//System.out.println(new Timestamp(System.currentTimeMillis()) + " 12");
//	//executor.shutdown();
//
//
//
//
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//		finally {
//
//			if (rs != null)
//				rs.close();
//			if (pStatement != null)
//				pStatement.close();
//			if (pStatement2 != null)
//				pStatement2.close();
//		}
//	}
//}