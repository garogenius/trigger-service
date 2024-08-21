package com.mtn.concurrentcalls.services;

import com.mtn.concurrentcalls.helpers.APIConstants;
import com.mtn.concurrentcalls.models.AmlHealthResponse;
import com.mtn.concurrentcalls.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;

@Service
public class ConcurrentService {
    @Autowired
    private Connection con;

    public AmlHealthResponse checkHealth(){
        int statusCode = HttpStatus.OK.value();
        String statusMessage= APIConstants.SUCCESS;
        String statusDesc=APIConstants.CONNECTED;

        try {
            if (con== null || con.isClosed()) {
                statusCode=503;
                statusMessage=APIConstants.FAILURE;
                statusDesc=APIConstants.NOT_CONNECTED;
            }
        }catch (SQLException ex){
            statusMessage=APIConstants.FAILURE;
            statusCode=ex.getErrorCode();
            statusDesc=ex.getMessage();
            ex.printStackTrace();
        }
        return  new AmlHealthResponse(statusCode, statusMessage, statusDesc, Utils.getLocalZonedDateTime());
    }

    public AmlHealthResponse checkHealthTest(){
        int statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        String statusMessage= APIConstants.FAILURE;
        String statusDesc=APIConstants.NOT_CONNECTED;

        return  new AmlHealthResponse(statusCode, statusMessage, statusDesc, Utils.getLocalZonedDateTime());
    }
}
