//package com.mtn.concurrentcalls;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.annotation.AnnotationConfigApplicationContext;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.task.AsyncListenableTaskExecutor;
//import org.springframework.core.task.SimpleAsyncTaskExecutor;
//import org.springframework.util.concurrent.ListenableFuture;
//import org.springframework.util.concurrent.ListenableFutureCallback;
//
//import java.util.concurrent.Callable;
//
//public class AsyncListableTaskExecutorExample {
//
//    public static void main (String[] args) throws Exception {
//        ApplicationContext context =
//                  new AnnotationConfigApplicationContext(MyConfig.class);
//        MyBean bean = context.getBean(MyBean.class);
//        bean.runTasks();
//    }
//
//    @Configuration
//    public static class MyConfig {
//
//        @Bean
//        MyBean myBean () {
//            return new MyBean();
//        }
//
//        @Bean
//        AsyncListenableTaskExecutor taskExecutor () {
//            SimpleAsyncTaskExecutor t = new SimpleAsyncTaskExecutor();
//            t.setConcurrencyLimit(100);
//            return t;
//        }
//
//        @Bean
//        ListenableFutureCallback<String> taskCallback () {
//            return new MyListenableFutureCallback();
//        }
//    }
//
//    private static class MyBean {
//        @Autowired
//        private AsyncListenableTaskExecutor executor;
//        @Autowired
//        private ListenableFutureCallback threadListenableCallback;
//
//
//        public void runTasks () throws Exception {
//
//            for (int i = 0; i < 10; i++) {
//                ListenableFuture<String> f =
//                          executor.submitListenable(getTask(i));
//                f.addCallback(threadListenableCallback);
//            }
//        }
//
//        private Callable<String> getTask (int i) {
//            return () -> {
//                System.out.printf("running task %d. Thread: %s%n",
//                                  i,
//                                  Thread.currentThread().getName());
//                return String.format("Task finished %d", i);
//            };
//        }
//    }
//
//    private static class MyListenableFutureCallback
//              implements ListenableFutureCallback<String> {
//        @Override
//        public void onFailure (Throwable ex) {
//            System.out.println("faliure message: " + ex.getMessage());
//            ex.printStackTrace();
//        }
//
//        @Override
//        public void onSuccess (String result) {
//            System.out.println("success object: " + result);
//        }
//    }
//}