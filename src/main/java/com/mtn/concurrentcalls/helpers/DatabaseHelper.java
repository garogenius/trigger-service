package com.mtn.concurrentcalls.helpers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
@Slf4j
public class DatabaseHelper {

    @Autowired
    private Connection con;

    public void  persistLogTableIfNotExist() {
        try {
            log.info("01. Log table check started started...");
            PreparedStatement tableCheckStatement = con.prepareStatement("SELECT * FROM USER_TABLES WHERE TABLE_NAME =?", ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);
            tableCheckStatement.setString(1, "FRAUDAPIPROCESSINGSTATUS");
            ResultSet resultSet = tableCheckStatement.executeQuery();
            log.info("01. Getting start date time...");
            if (resultSet == null || !resultSet.next()) {
                //create table if not exist
                con.prepareStatement("CREATE TABLE FRAUDAPIPROCESSINGSTATUS (TRANSACTIONID NUMBER,TRANSACTIONTYPE VARCHAR2(64),BATCHTYPE VARCHAR2(50),BATCHID NUMBER,STATUS VARCHAR2(50),FINALIZEDTIME TIMESTAMP(6))", ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_UPDATABLE).execute();
                tableCheckStatement.close();
            }
            log.info("02. Log table is ready...");
        } catch (Exception ex) {
            log.error(ex.getMessage());
            ex.printStackTrace();
        }
    }
}
