package com.mtn.concurrentcalls.services;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class FinancialServiceCaller {

	@Autowired
	private RestTemplate restTemplate;
	
	@Value("${CSBF1}")
    private String CSBF1;
	
	@Value("${CSBF2}")
    private String CSBF2;
	
	@Value("${CSBF3}")
    private String CSBF3;
	
	@Value("${CSBF4}")
    private String CSBF4;

	@Value("${BASEURL}")
	private String BASEURL;

	@Value("${FINANCIALBATCH}")
	private String FINANCIALBATCHAPI;


	@Async
	public  CompletableFuture<String> callSASFMServiceBatchAPI(long batchId) {
		System.out.println("11: Before financial API Calling batch("+batchId+")");
		String endpoint =BASEURL+"/"+FINANCIALBATCHAPI+"?batchId=" + batchId;
        System.out.println("12: Calling system endpoint:"+endpoint);
		String responseObj = restTemplate.getForObject(endpoint, String.class);
		return CompletableFuture.completedFuture(responseObj);
	}

	@Async
	public  CompletableFuture<String> callSASFMService(BigDecimal finTranId, String tranType) throws Exception {
		System.out.println("Before API Calling");

		String serviceEndpoint=getLocalServiceEndpoint(finTranId,tranType);
		System.out.println("Endpoint: "+serviceEndpoint);

        String responseObj = restTemplate.getForObject(serviceEndpoint, String.class);
        return CompletableFuture.completedFuture(responseObj);
    }


	private String getLocalServiceEndpoint(BigDecimal finTranId,String tranType) {
		String endpoint = null;
        switch (tranType) {
            case "BATCH_TRANSFER":
            case "CASH_IN":
            case "CASH_OUT":
            case "DEBIT":
            case "FLOAT_TRANSFER":
            case "PAYMENT":
            case "TRANSFER":
            case "TRANSFER_FROM_ANY_BANK":
            case "WITHDRAWAL":
                endpoint = CSBF1 + "?transactionId=" + finTranId;
                break;
            case "ADJUSTMENT":
            case "REVERSAL":
                endpoint = CSBF2 + "?transactionId=" + finTranId;
                break;
            case "COMMISSIONING":
                endpoint = CSBF3 + "?transactionId=" + finTranId;
                break;
            case "RESOLVE_DEPOSIT":
                endpoint = CSBF4 + "?transactionId=" + finTranId;
                break;
        }
		return BASEURL+"/"+endpoint;
	}
}
