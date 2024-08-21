package com.mtn.concurrentcalls.services;

import java.util.concurrent.CompletableFuture;
import java.math.BigDecimal;

import com.mtn.concurrentcalls.enums.BatchType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NonFinancialServiceCaller {

	@Autowired
	private RestTemplate restTemplate;

	@Value("${CSNM1}")
    private String CSNM1;

	@Value("${CSNM2}")
	private String CSNM2;

	@Value("${CSNM3}")
    private String CSNM3;

	@Value("${CSNM3}")
	private String CSNM4;

	@Value("${CSNM5}")
    private String CSNM5;

	@Value("${CSNM6}")
    private String CSNM6;

	@Value("${CSNM7}")
    private String CSNM7;

	@Value("${CSNM8}")
    private String CSNM8;

	@Value("${CSNM9}")
    private String CSNM9;

	@Value("${CSNM10}")
    private String CSNM10;

	@Value("${CSNM11}")
    private String CSNM11;

	@Value("${NONFINANCIALBATCH}")
	private String NONFINANCIALBATCHAPI;

	@Value("${BASEURL}")
	private String BASEURL;


	@Async
	public  CompletableFuture<String> callSASFMServiceBatchAPI(long batchId) {
		System.out.println("Before Non financial API Calling batch("+batchId+")");
		String serviceEndpoint =BASEURL+"/"+NONFINANCIALBATCHAPI+"?batchId=" + batchId;
        System.out.println("Endpoint: "+serviceEndpoint);
        String responseObj = restTemplate.getForObject(serviceEndpoint, String.class);
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
            case "ActivateUser":
                endpoint = CSNM1 + "?transactionId=" + finTranId;
                break;
            case "AppLogin":
                endpoint = CSNM2 + "?transactionId=" + finTranId;
                break;
            case "ActivateEreTree":
                endpoint = CSNM3 + "?transactionId=" + finTranId;
                break;
            case "ParameterChange":
                endpoint = CSNM4 + "?transactionId=" + finTranId;
                break;
            case "UpdateCredential":
            case "ChangePassword":
                endpoint = CSNM6 + "?transactionId=" + finTranId;
                break;
            case "RegisterAccountHolder":
                endpoint = CSNM7 + "?transactionId=" + finTranId;
                break;
            case "ChangeProfile":
                endpoint = CSNM9 + "?transactionId=" + finTranId;
                break;
            case "SetIMSI":
                endpoint = CSNM10 + "?transactionId=" + finTranId;
                break;
            case "SetMSISDN":
                endpoint = CSNM11 + "?transactionId=" + finTranId;
                break;
        }
		return BASEURL+"/"+endpoint;
	}
}
