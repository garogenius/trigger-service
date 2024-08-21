//package com.nickolasfisher.concurrentcalls;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.ResultSet;
//import java.sql.Statement;
//import java.time.Duration;
//import java.time.Instant;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.CompletableFuture;
//
//@Component
//public class ConcurrentRunner2 implements CommandLineRunner {
//
//    @Autowired
//    SlowServiceCaller slowServiceCaller;
//
//    @Override
//    public void run(String... args) throws Exception {
//        Instant start = Instant.now();
//        //List<CompletableFuture<JsonNode>> allFutures = new ArrayList<>();
//        List<CompletableFuture<String>> allFutures = new ArrayList<>();
//        
//      	Class.forName("oracle.jdbc.driver.OracleDriver");  
//  	  
//    	Connection con=DriverManager.getConnection(  
//    	"jdbc:oracle:thin:@localhost:1521:orcl","RDS_UG","RDS_UG");  
//    	  
//    	Statement stmt=con.createStatement();  
//    	  
//    	ResultSet rs=stmt.executeQuery("SELECT /* + PARALLEL (20) */ FINANCIALTRANSACTIONID,TRANSFERTYPE FROM   RDS_UG.RDS$FINANCIALRECEIPT where TO_NUMBER(TO_CHAR(finalizedtime,'YYYYMMDDHHMISSFF9')) >= "
//    			+ "(select to_number(to_char(min (finalizedtime),'YYYYMMDDHHMISSFF9')) from FINANCIAL_API_FINALIZEDTIME) and TO_NUMBER(TO_CHAR(finalizedtime,'YYYYMMDDHHMISSFF9')) < (select to_number(to_char(min (finalizedtime) + interval '70' minute,'YYYYMMDDHHMISSFF9')) from FINANCIAL_API_FINALIZEDTIME) "
//    			+ "     and transfertype in "
//    			+ "( "
//    			+ "'BATCH_TRANSFER', 'CASH_IN', 'CASH_OUT', 'DEBIT', 'FLOAT_TRANSFER', 'PAYMENT', 'TRANSFER', 'TRANSFER_FROM_ANY_BANK', 'WITHDRAWAL', "
//    			+ "'ADJUSTMENT','REVERSAL','COMMISSIONING','RESOLVE_DEPOSIT' "
//    			+ ") ");  
//		while (rs.next()) {
//			allFutures.add(slowServiceCaller.callOtherService(rs.getString(1), rs.getString(2)));
//			//System.out.println(rs.getInt(1) + "  " + rs.getString(2));
//		}
//
///*
//        for (int i = 0; i < 150000; i++) {
//            allFutures.add(slowServiceCaller.callOtherService());
//        }
//*/
//        CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0]));
//
//        for (int i = 0; i < allFutures.size(); i++) {
//            System.out.println("response: " + allFutures.get(i).get().toString());
//        }
//
//        System.out.println("Total time: " + Duration.between(start, Instant.now()).getSeconds());
//    }
//}
