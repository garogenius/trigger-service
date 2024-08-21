//package com.mtn.concurrentcalls;
//
//import java.util.concurrent.Executor;
//import java.util.concurrent.ThreadPoolExecutor;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.annotation.AsyncConfigurer;
//import org.springframework.scheduling.annotation.EnableAsync;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//
////@Data
//@EnableAsync
//@Configuration
//@ConfigurationProperties(prefix = "async.thread.pool")
//public class NewExecuter implements AsyncConfigurer {
//
//  private int coreSize;
//  private int maxSize;
//  private int queueCapacity;
//
//  @Override
//  public Executor getAsyncExecutor() {
//    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//    executor.setCorePoolSize(coreSize);
//    executor.setMaxPoolSize(maxSize);
//    executor.setQueueCapacity(queueCapacity);
//    executor.setThreadNamePrefix("worker-exec-");
//    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
//    return executor;
//  }
//
//  @Override
//  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
//    return (ex, method, params) -> {
//      Class<?> targetClass = method.getDeclaringClass();
//      Logger logger = LoggerFactory.getLogger(targetClass);
//      logger.error(ex.getMessage(), ex);
//    };
//  }
//
//}
