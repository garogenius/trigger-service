//package com.mtn.concurrentcalls;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.time.Duration;
//import java.time.Instant;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ExecutionException;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//@Component
//public class ConcurrentRunner implements CommandLineRunner {
//
//    @Autowired
//    SlowServiceCaller slowServiceCaller;
//    
//    static Connection con= null;
//    //static boolean tryAnotherRound = true;
//    
//    public void JDBCInit() throws Exception
//    {
//    	System.out.println("Before JDBC Open");
//    	Class.forName("oracle.jdbc.driver.OracleDriver");  
//    	
//    	
//    	con=DriverManager.getConnection(  
//    	"jdbc:oracle:thin:@localhost:1521:orcl","RDS_UG","RDS_UG");
//    	
//    	
//    	/*
//    	con=DriverManager.getConnection(  
//    	"jdbc:oracle:thin:@10.156.195.67:1522/RDSASAML","SASAML_API","Sasap#9867");
//    	*/
//    	
//    	con.setAutoCommit(false);
//    	System.out.println("After JDBC Open");
//    }
//
//    @Override
//    public void run(String... args)throws Exception  {
//    	JDBCInit();
//        Instant start = Instant.now();
//        
//        while (true /*&& tryAnotherRound*/)
//        {
//        	//tryAnotherRound = false;
//        	System.out.println("1");
//        	DoProcess(start);
//        	System.out.println("2");
//        	Thread.sleep(40000);
//        	System.out.println("3");
//        }
//    }
//
//	private void DoProcess(Instant start) throws SQLException, InterruptedException, ExecutionException {
//		PreparedStatement pStatement2 = null;
//		String query = null;
//		String response = null;
//		int batchSize = 1000;
//        int i = 0;
//        List<CompletableFuture<String>> allFutures = new ArrayList<>();
//		try {
//		//List<CompletableFuture<JsonNode>> allFutures = new ArrayList<>();
//        System.out.println("4");
//        Statement stmt=con.createStatement();
//        String startDateTimestamp = null;
//        String endDateTimestamp = null;
//        System.out.println("5");
//        ResultSet rs=stmt.executeQuery("SELECT to_number(to_char(max (finalizedtime),'YYYYMMDDHHMISSFF9')) start_time, to_number(to_char(max (finalizedtime) + interval '1' minute,'YYYYMMDDHHMISSFF9')) end_time from FINANCIAL_API_FINALIZEDTIME") ;
//        System.out.println("51");
//        while (rs.next()) {
//        	System.out.println("52");
//			startDateTimestamp = rs.getString(1);
//			System.out.println(startDateTimestamp);
//			System.out.println("53");
//			endDateTimestamp = rs.getString(2);
//			System.out.println(endDateTimestamp);
//			System.out.println("54");
//		}
//        System.out.println("55");
//		rs = stmt.executeQuery(
//				"select count(id) from RDS_UG.RDS$FINANCIALRECEIPT where to_number(to_char(finalizedtime,'YYYYMMDDHHMISSFF9')) >= "
//						+ startDateTimestamp);
//		System.out.println("56");
//		if (rs.next()) {
//			System.out.println("57");
//			if (rs.getInt(1) <= 0) {
//				System.out.println("58");
//				rs.close();
//				stmt.close();
//				return;
//			}
//		}
//		System.out.println("6");
//		//stmt.executeUpdate("Insert into RDS_UG.FINANCIAL_API_FINALIZEDTIME Select max(finalizedTime) + Interval '10' minute from RDS_UG.FINANCIAL_API_FINALIZEDTIME");
//		stmt.executeUpdate("Insert into FINANCIAL_API_FINALIZEDTIME Select max(finalizedTime) + Interval '1' minute from FINANCIAL_API_FINALIZEDTIME");
//		    
//    	  
//    	 rs=stmt.executeQuery("SELECT /* + PARALLEL (20) */ FINANCIALTRANSACTIONID,TRANSFERTYPE FROM   RDS_UG.RDS$FINANCIALRECEIPT where TO_NUMBER(TO_CHAR(finalizedtime,'YYYYMMDDHHMISSFF9')) >= "
//    			+ startDateTimestamp + " and TO_NUMBER(TO_CHAR(finalizedtime,'YYYYMMDDHHMISSFF9')) < " + endDateTimestamp 
//    			+ "     and transfertype in "
//    			+ "( "
//    			+ "'BATCH_TRANSFER', 'CASH_IN', 'CASH_OUT', 'DEBIT', 'FLOAT_TRANSFER', 'PAYMENT', 'TRANSFER', 'TRANSFER_FROM_ANY_BANK', 'WITHDRAWAL', "
//    			+ "'ADJUSTMENT','REVERSAL','COMMISSIONING','RESOLVE_DEPOSIT' "
//    			+ ") ");
//    	 
//    	Statement stmt2=con.createStatement();
//    	System.out.println("7");
//    	//String query = "insert into RDS_UG.FRAUDAPISTATUS(TRANSACTIONID,TRANSFERTYPE,REQUEST_SENT) values (?,?,1)";
//    	query = "insert into FRAUDAPISTATUS(TRANSACTIONID,TRANSFERTYPE,REQUEST_SENT) values (?,?,1)";
//        PreparedStatement pStatement = con.prepareStatement(query);
//        int counter = 0;
//        System.out.println("8");
//		while (rs.next()) {
//			++counter;
//			allFutures.add(slowServiceCaller.callOtherService(rs.getString(1), rs.getString(2)));
//			pStatement.setString(1,rs.getString(1));
//			pStatement.setString(2,rs.getString(2));
//			pStatement.addBatch();
//			if (counter % batchSize == 0) {
//                pStatement.executeBatch();
//                pStatement.clearBatch();
//                con.commit();
//            }
//		}
//		System.out.println("9");
//		pStatement.executeBatch();
//		pStatement.clearBatch();
//		con.commit();
//		pStatement.close();
//        
//		CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0]));
//        
//        //query = "update RDS_UG.FRAUDAPISTATUS set GET_response = 1,response=? where transactionID =?";
//        
//        query = "insert into FRAUDAPISTATUS2(response,TRANSACTIONID,GET_response) values (?,?,1)";
//        pStatement2 = con.prepareStatement(query);
//        
//        System.out.println("Count of Responses: "+allFutures.size());
//        System.out.println("10");
//        for (i = 0; i < allFutures.size(); i++) {
//        	/*if (allFutures.get(i).isCompletedExceptionally())
//        		response = allFutures.get(i).toString();
//        	else
//        		response = allFutures.get(i).get().toString();
//        	*/	 
//        	pStatement2.setLong(2, (i+1));
//        	try {
//        	response = allFutures.get(i).get().toString();
//        	}
//        	catch (Exception ex)
//    		{
//    			//ex.printStackTrace();
//    			response = ex.getMessage();
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
//        	/*if ((i+1) == allFutures.size())
//        		tryAnotherRound = true;
//        		*/
//        }
//        //tryAnotherRound = true;
//        
//        pStatement2.executeBatch();
//        pStatement2.clearBatch();
//        con.commit();
//        
//        System.out.println("11");
//        rs.close();
//		stmt.close();
//		stmt2.close();
//		pStatement.close();
//        System.out.println("Total time: " + Duration.between(start, Instant.now()).getSeconds());
//        System.out.println("12");
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
//	}
//}
