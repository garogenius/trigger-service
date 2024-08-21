package com.mtn.concurrentcalls;

import com.mtn.concurrentcalls.runners.FinancialTransactionRunner;
import com.mtn.concurrentcalls.runners.NonFinancialTransactionRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
@Slf4j
public class ThreadManager {

	@Autowired
	private FinancialTransactionRunner financialRunner;
	@Autowired
	private NonFinancialTransactionRunner nonFinancialRunner;

	Thread financialRunnerThread = null;
	Thread nonFinancialRunnerThread = null;


	@EventListener(ApplicationReadyEvent.class)
	public void run() {
		if (financialRunnerThread == null) {
			financialRunnerThread = new Thread(() -> {
				try {
					while (true) {
						log.info("0. Executing financial thread with id: " + financialRunnerThread.getId());
						nonFinancialRunner.execute(financialRunnerThread.getId());
						Thread.sleep(600000);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			financialRunnerThread.start();

		} else {
			log.info("I'm alive inside financialRunnerThread");
		}

		if (nonFinancialRunnerThread == null) {
			nonFinancialRunnerThread = new Thread(() -> {
				try {
					while (true) {
						log.info("0. Executing non financial thread with id: " + nonFinancialRunnerThread.getId());
						nonFinancialRunner.execute(nonFinancialRunnerThread.getId());
						Thread.sleep(600000);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			nonFinancialRunnerThread.start();
		} else {
			log.info("I'm alive inside nonFinancialRunnerThread");
		}
	}
}