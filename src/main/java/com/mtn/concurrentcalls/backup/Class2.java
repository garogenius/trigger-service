//package com.mtn.concurrentcalls;
//
//import java.sql.Connection;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestTemplate;
//
//@Component
//public class Class2 {
//	
//	@Autowired
//	private RestTemplate restTemplate;
//	
//	@Autowired
//	private Connection con;
//		
//	public void execute() throws InterruptedException{
//		System.out.println("Test Class 2");
//		Thread.sleep(5000);
//	}
//
//}
