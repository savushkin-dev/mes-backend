package com.host.SpringBootAutomationProduction.service;

import com.host.SpringBootAutomationProduction.model.LuMove;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Service
public class LuMoveService {


    public List<LuMove> getLuMoveDay() {

        // Параметры соединения
        String url = "jdbc:sqlserver://10.164.35.215;database=naswms;encrypt=true;trustServerCertificate=true";
        String user = "nas";
        String password = "Nas2024$";

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        List<LuMove> luMoveList = new ArrayList<>();

        try {
            connection = DriverManager.getConnection(url, user, password);
            statement = connection.createStatement();

            String sql = "SELECT * FROM BD_LUMOVE WHERE CAST(CONVERT(DATETIME, \n" +
                    "                   STUFF(STUFF(STUFF(DATETIME, 9, 0, ' '), 12, 0, ':'), 15, 0, ':')) \n" +
                    "              AS DATE) = CAST(GETDATE() AS DATE)";
            resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {

                LuMove luMove = new LuMove();

                luMove.setFId(resultSet.getInt("F_ID"));
                luMove.setMsgId(resultSet.getString("MSGID"));
                luMove.setOrderNo(resultSet.getString("ORDERNO"));
                luMove.setMovementId(resultSet.getInt("MOVEMENTID"));
                luMove.setSscc(resultSet.getString("SSCC"));
                luMove.setFromLoc(resultSet.getString("FROMLOC"));
                luMove.setToLoc(resultSet.getString("TOLOC"));
                luMove.setReason(resultSet.getString("REASON"));
                luMove.setUserCode(resultSet.getString("USERCODE"));
                luMove.setDateTime(resultSet.getString("DATETIME"));

                luMove.trimStringFields();
                luMoveList.add(luMove);

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return luMoveList;

    }


}
