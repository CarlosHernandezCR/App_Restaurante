package dao;

import common.config.Configuration;
import common.constants.Constants;
import jakarta.inject.Inject;

import java.sql.*;

public class DBConnection {

    private final Configuration config;
    @Inject
    public DBConnection(Configuration config) {
        this.config = config;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager
                .getConnection(config.getProperty(Constants.DATABASE), config.getProperty(Constants.USER), config.getProperty(Constants.PASSWORD));
    }
    public void closeConnection(Connection connArg) {
        try {
            if (connArg != null) {
                connArg.close();
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    public void releaseResource(PreparedStatement pstmt) {
        try {
            if (pstmt != null) {
                pstmt.close();
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    public void releaseResource(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }

        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    public void releaseResource(Statement stmt) {
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }
}
