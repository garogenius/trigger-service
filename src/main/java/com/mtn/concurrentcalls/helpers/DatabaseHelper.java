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

    public Connection openConnection() {
        return con;
    }
    public ResultSet executeStatement(PreparedStatement statement) throws SQLException {
        try {
            return statement.executeQuery();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
