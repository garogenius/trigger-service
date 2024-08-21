//package com.mtn.concurrentcalls;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
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
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
////@Order(value=1)
//@Component
//public class ConcurrentRunner implements CommandLineRunner {
//
//    @Autowired
//    FinancialServiceCaller slowServiceCaller;
//        
//    @Value("${dbConnection_String}")
//    private String dbConnectionString;
//    
//    @Value("${dbReal_Schema}")
//    private String dbRealSchema;
//    
//    @Value("${dbBridge_Schema}")
//    private String dbBridgeSchema;
//
//    @Value("${dbBridge_Password}")
//    private String dbBridgePassword;
//    
//    @Value("${batch_Size}")
//    private int batchSize;
//    
//    @Value("${result_Set_Fetch_Size}")
//    private int resultSetFetchSize;
//    
//    @Value("${run_Every}")
//    private int runEvery;
//    
//    @Value("${run_Every_Unit}")
//    private String runEveryUnit;
//    
//    @Value("${sleep_Every}")
//    private int sleepEvery;
//    
//    @Value("${accelerate_Run_Every}")
//    private int accelerateRunEvery;
//    
//    
//    
//    static Connection con= null;
//    
//    public void JDBCInit() throws Exception
//    {
//    	System.out.println(new Timestamp(System.currentTimeMillis()) + " Before JDBC Open");
//    	Class.forName("oracle.jdbc.driver.OracleDriver");  
//    	
//    	/*
//    	con=DriverManager.getConnection(  
//    	"jdbc:oracle:thin:@localhost:1521:orcl","RDS_UG","RDS_UG");
//    	*/
//    	
//    	
//    	con=DriverManager.getConnection(  
//    			dbConnectionString,dbBridgeSchema,dbBridgePassword);
//    	
//    	
//    	con.setAutoCommit(false);
//    	System.out.println(new Timestamp(System.currentTimeMillis()) + " After JDBC Open");
//    }
//
//    @Override
//    public void run(String... args)throws Exception  {
//    	JDBCInit();
//        Instant start = Instant.now();
//        while (true)
//        {
//        	System.out.println(new Timestamp(System.currentTimeMillis()) + " 1");
//        	doProcess(start);
//        	System.out.println(new Timestamp(System.currentTimeMillis()) + " 2");
//        	Thread.sleep(sleepEvery);
//        	System.out.println(new Timestamp(System.currentTimeMillis()) + " 3");
//        	//return;
//        }
//    }
//
//	private void doProcess(Instant start) throws Exception {
//		PreparedStatement pStatement = null;
//		PreparedStatement pStatement2 = null;
//		ResultSet rs = null;
//		String query = null;
//		String response = null;
//        int i = 0;
//        int period = runEvery;
//        CompletableFuture<String> completedObj = null;
//        List<CompletableFuture<String>> allFutures = new ArrayList<>();
//        Map<String, CompletableFuture<String>> requests = new HashMap<>();
//		try {
//        System.out.println(new Timestamp(System.currentTimeMillis()) + " 4");
//        Timestamp startDateTimestamp = null;
//        Timestamp endDateTimestamp = null;
//        Timestamp accDateTimestamp = null;
//        Timestamp maxTimestamp = null;
//        System.out.println(new Timestamp(System.currentTimeMillis()) + " 41");
//        //
//        pStatement = con.prepareStatement("SELECT max (finalizedtime) from " + dbRealSchema + ".RDS$FINANCIALRECEIPT");
//        rs = pStatement.executeQuery();
//        System.out.println(new Timestamp(System.currentTimeMillis()) + " 42");
//        while (rs.next()) {
//        	System.out.println(new Timestamp(System.currentTimeMillis()) + " 43");
//        	maxTimestamp = rs.getTimestamp(1);
//			System.out.println("maxTimestamp= " + maxTimestamp);
//		}
//        System.out.println(new Timestamp(System.currentTimeMillis()) + " 44");
//        rs.close();
//        pStatement.close();
//        //
//        pStatement = con.prepareStatement("SELECT max (finalizedtime) start_time, max (finalizedtime) + interval '" + runEvery +"' " + runEveryUnit +" end_time ,"
//        		+ " max (finalizedtime) + interval '" + accelerateRunEvery +"' " + runEveryUnit +" acc_time from FINANCIAL_API_FINALIZEDTIME");
//        rs = pStatement.executeQuery();
//        System.out.println(new Timestamp(System.currentTimeMillis()) + " 51");
//        while (rs.next()) {
//        	System.out.println(new Timestamp(System.currentTimeMillis()) + " 52");
//        	startDateTimestamp = rs.getTimestamp(1);
//			System.out.println("startDateTimestamp= " + startDateTimestamp);
//			System.out.println(new Timestamp(System.currentTimeMillis()) + " 53");
//			endDateTimestamp = rs.getTimestamp(2);
//			System.out.println("endDateTimestamp= " + endDateTimestamp);
//			accDateTimestamp = rs.getTimestamp(3);
//			System.out.println("accDateTimestamp= " + accDateTimestamp);
//			System.out.println(new Timestamp(System.currentTimeMillis()) + " 54");
//		}
//        System.out.println(new Timestamp(System.currentTimeMillis()) + " 55");
//        
//        long diff = (maxTimestamp.getTime() - startDateTimestamp.getTime())/(1000 * 60);
//		if (diff >= 5) {
//			period = accelerateRunEvery;
//			endDateTimestamp = accDateTimestamp;
//		}
//        System.out.println(maxTimestamp);
//        System.out.println(startDateTimestamp);
//        System.out.println(diff);
//        rs.close();
//        pStatement.close();
//        pStatement = con.prepareStatement("select count(1) from " + dbRealSchema + ".RDS$FINANCIALRECEIPT where finalizedtime >= ?");
//        pStatement.setTimestamp(1,startDateTimestamp);
//        rs = pStatement.executeQuery();
//		System.out.println(new Timestamp(System.currentTimeMillis()) + " 56");
//		if (rs.next()) {
//			System.out.println(new Timestamp(System.currentTimeMillis()) + " 57");
//			if (rs.getInt(1) <= 0) {
//				System.out.println(new Timestamp(System.currentTimeMillis()) + " 58");
//				rs.close();
//				pStatement.close();
//				return;
//			}
//			rs.close();
//			pStatement.close();
//		}
//		System.out.println(new Timestamp(System.currentTimeMillis()) + " 6");
//		pStatement = con.prepareStatement("Insert into FINANCIAL_API_FINALIZEDTIME Select max(finalizedTime) + Interval '" + period + "' " + runEveryUnit +" from FINANCIAL_API_FINALIZEDTIME");
//		pStatement.executeUpdate();
//		con.commit();
//		pStatement.close();
//		System.out.println(new Timestamp(System.currentTimeMillis()) + " 60");  
//		
//		pStatement = con.prepareStatement("SELECT /* + PARALLEL (20) */ FINANCIALTRANSACTIONID,TRANSFERTYPE FROM " + dbRealSchema + ".RDS$FINANCIALRECEIPT where finalizedtime >= ?"
//    			+ " and finalizedtime < ?" 
//    			+ "     and transfertype in "
//    			+ "( "
//    			+ "'BATCH_TRANSFER', 'CASH_IN', 'CASH_OUT', 'DEBIT', 'FLOAT_TRANSFER', 'PAYMENT', 'TRANSFER', 'TRANSFER_FROM_ANY_BANK', 'WITHDRAWAL', "
//    			+ "'ADJUSTMENT','REVERSAL','COMMISSIONING','RESOLVE_DEPOSIT' "
//    			+ ") ");
//    	
//		pStatement.setTimestamp(1,startDateTimestamp);
//		pStatement.setTimestamp(2,endDateTimestamp);
//        rs = pStatement.executeQuery();
//        rs.setFetchSize(resultSetFetchSize);
//    	System.out.println(new Timestamp(System.currentTimeMillis()) + " 7");
//    	query = "insert into FRAUDAPISTATUS(TRANSACTIONID,TRANSFERTYPE,REQUEST_SENT) values (?,?,1)";
//        pStatement2 = con.prepareStatement(query);
//        int counter = 0;
//        System.out.println(new Timestamp(System.currentTimeMillis()) + " 8");
//        
//        //for (i = 0 ; i < 100 ; i++)
//         
//		while (rs.next())  
//		{
//			++counter;
//			completedObj = 
//					slowServiceCaller.callOtherService(rs.getString(1), rs.getString(2));
//			/*completedObj = slowServiceCaller.callOtherService("17206120014", "TRANSFER");*/
//			/*
//			allFutures.add(completedObj);
//			requests.put("17206120014", completedObj);
//			pStatement2.setString(1,"17206120014");
//			pStatement2.setString(2,"TRANSFER");
//			*/
//			/*requests.put(rs.getString(1), completedObj);*/
//			pStatement2.setString(1,rs.getString(1));
//			pStatement2.setString(2,rs.getString(2));
//			
//			pStatement2.addBatch();
//			if (counter % batchSize == 0) {
//                pStatement2.executeBatch();
//                pStatement2.clearBatch();
//                con.commit();
//            }
//		}
//		rs.close();
//		System.out.println(new Timestamp(System.currentTimeMillis()) + " 9");
//		System.out.println(new Timestamp(System.currentTimeMillis()) + " Counter is " + counter);
//		pStatement2.executeBatch();
//		pStatement2.clearBatch();
//		con.commit();
//		
//		pStatement.close();
//		pStatement2.close();
//		System.out.println(new Timestamp(System.currentTimeMillis()) + " 90");
//		//CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0]));
//		System.out.println(new Timestamp(System.currentTimeMillis()) + " 91");
//        
//        query = "insert into FRAUDAPISTATUS2(response,TRANSACTIONID,GET_response) values (?,?,1)";
//        pStatement2 = con.prepareStatement(query);
//        
//        /*System.out.println(new Timestamp(System.currentTimeMillis()) + " Count of Responses: "+allFutures.size());*/
//        System.out.println(new Timestamp(System.currentTimeMillis()) + " 10");
//        /*Iterator<Map.Entry<String, CompletableFuture<String>>> itr = requests.entrySet().iterator();
//        i = 0;
//        while(itr.hasNext())
//        {
//        	++i;
//            Map.Entry<String, CompletableFuture<String>> entry = itr.next();
//            pStatement2.setString(2, entry.getKey());
//         	try {
//         	//response = entry.getValue().get(10, TimeUnit.MILLISECONDS).toString();
//         		response = entry.getValue().get().toString();
//         		//System.out.println(response);
//         	}
//
//         	catch (Exception ex)
//     		{
//         		//ex.printStackTrace();
//         		if (ex instanceof TimeoutException)
//         			response = "java.util.concurrent.TimeoutException";
//         		else
//         			response = ex.getMessage();
//         		//System.out.println(response);
//             	pStatement2.setString(1,response);
//             	pStatement2.addBatch();
//             	if (i % batchSize == 0) {
//                     pStatement2.executeBatch();
//                     pStatement2.clearBatch();
//                     con.commit();
//                 }
//             	continue;
//     		}
//         	pStatement2.setString(1,response);
//         	pStatement2.addBatch();
//         	if (i % batchSize == 0) {
//                 pStatement2.executeBatch();
//                 pStatement2.clearBatch();
//                 con.commit();
//             }
//        }*/
//        /*for (i = 0; i < allFutures.size(); i++) {
//        	//if (allFutures.get(i).isCompletedExceptionally())
//        	//	response = allFutures.get(i).toString();
//        	//else
//        	//	response = allFutures.get(i).get().toString();
//        		 
//        	pStatement2.setLong(2, (i+1));
//        	try {
//        	response = allFutures.get(i).get().toString();
//        	System.out.println(response);
//        	}
//        	catch (Exception ex)
//    		{
//    			//ex.printStackTrace();
//    			response = ex.getMessage();
//    			System.out.println(response);
//            	pStatement2.setString(1,response);
//            	pStatement2.addBatch();
//            	if ((i+1) % batchSize == 0) {
//                    pStatement2.executeBatch();
//                    pStatement2.clearBatch();
//                    con.commit();
//                }
//            	continue;
//    		}
//        	pStatement2.setString(1,response);
//        	pStatement2.addBatch();
//        	if ((i+1) % batchSize == 0) {
//                pStatement2.executeBatch();
//                pStatement2.clearBatch();
//                con.commit();
//            }
//        }*/
//        
//        pStatement2.executeBatch();
//        pStatement2.clearBatch();
//        con.commit();
//        
//        System.out.println(new Timestamp(System.currentTimeMillis()) + " 11");
//        rs.close();
//		pStatement.close();
//		pStatement2.close();
//        System.out.println(new Timestamp(System.currentTimeMillis()) + " Total time: " + Duration.between(start, Instant.now()).getSeconds());
//        System.out.println(new Timestamp(System.currentTimeMillis()) + " 12");
//		}
//		catch (Exception ex)
//		{
//			ex.printStackTrace();
//			/*response = ex.getMessage();
//        	pStatement2.setString(1,response);
//        	pStatement2.addBatch();
//        	if ((i+1) % batchSize == 0) {
//                pStatement2.executeBatch();
//                pStatement2.clearBatch();
//                con.commit();
//            }*/
//		}
//		
//		finally {
//
//			if (rs != null) rs.close();
//			if (pStatement != null) pStatement.close();
//			if (pStatement2 != null) pStatement2.close();
//		}
//	}
//}
