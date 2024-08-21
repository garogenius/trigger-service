package com.mtn.concurrentcalls;

import com.mtn.concurrentcalls.runners.FinancialTransactionRunner;
import com.mtn.concurrentcalls.runners.NonFinancialTransactionRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
public class ThreadManager {

	@Autowired
	private FinancialTransactionRunner financialRunner;
	@Autowired
	private NonFinancialTransactionRunner nonFinancialRunner;

	Thread financialRunnerThread = null;
	Thread nonFinancialRunnerThread = null;


    @EventListener(ApplicationReadyEvent.class)
	public void run() {
//		if (financialRunnerThread == null) {
//			financialRunnerThread = new Thread(() -> {
//				try {
//					while (true) {
//						System.out.println("0. Executing thread with id: " + financialRunnerThread.getId());
						financialRunner.execute(11);
//						Thread.sleep(40000);
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			});
//			financialRunnerThread.start();
//
//		} else {
//			System.out.println("I'm alive inside financialRunnerThread");
//		}
//
//		if (nonFinancialRunnerThread == null) {
//			nonFinancialRunnerThread = new Thread(() -> {
//				try {
//					while (true) {
//						System.out.println("0. Executing thread with id: " + nonFinancialRunnerThread.getId());
//						nonFinancialRunner.execute(nonFinancialRunnerThread.getId());
//						Thread.sleep(40000);
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			});
//			nonFinancialRunnerThread.start();
//		} else {
//			System.out.println("I'm alive inside nonFinancialRunnerThread");
//		}
	}
}