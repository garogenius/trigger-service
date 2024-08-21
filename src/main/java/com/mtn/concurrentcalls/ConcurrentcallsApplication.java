package com.mtn.concurrentcalls;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Timestamp;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableAsync
public class ConcurrentcallsApplication {
	


	@Value("${dbBridge_Schema}")
	private String dbBridgeSchema;

	@Value("${dbBridge_Password}")
	private String dbBridgePassword;
	
	@Value("${core_Pool_Size}")
    private int corePoolSize;
	
	@Value("${max_Core_Pool_Size}")
    private int maxCorePoolSize;
	
	@Value("${queue_Capacity}")
    private int queueCapacity;

    @Value("${dbConnection_String}")
    private String dbConnectionString;
	
    public static void main(String[] args) throws Exception {
    	System.out.println("Starting");
        SpringApplication.run(ConcurrentcallsApplication.class, args);
    }

    /*@Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }*/
    
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setBufferRequestBody(false);
        return new RestTemplate(rf);
    }
    

    @Bean
    public Executor executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();        
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxCorePoolSize);
        executor.setThreadNamePrefix("worker-exec-");
        //executor.setQueueCapacity(queueCapacity);
        executor.initialize();
        
        return executor;
    }
       
    @Bean
	public Connection JDBCInit() throws Exception {
		System.out.println(new Timestamp(System.currentTimeMillis()) + " Before JDBC Open");
		Class.forName("oracle.jdbc.driver.OracleDriver");

		Connection con=DriverManager.getConnection(  
    			dbConnectionString,dbBridgeSchema,dbBridgePassword);

		con.setAutoCommit(false);
		System.out.println(new Timestamp(System.currentTimeMillis()) + " After JDBC Open");
		return con;
	}
    
}
