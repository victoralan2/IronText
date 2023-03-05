package org.irontext;

import java.sql.*;

public class SQL {
    private final Connection conn;
    public SQL(){
        conn = getConnection();
    }

    public ResultSet executeQuery(String sqlCode){
        try {
            return conn.createStatement().executeQuery(sqlCode);

        } catch (SQLException e){
            return null;
        }
    }
    public PreparedStatement prepareStatement(String sqlCode){
        try {
            return conn.prepareStatement(sqlCode);

        } catch (SQLException e){
            return null;
        }
    }
    public String executeQueryString(String sqlCode){
        ResultSet result = null;
        try {
            result = conn.createStatement().executeQuery(sqlCode);
            String data = "";
            while (result.next()) {
                String row = "";
                int i = 1;
                while (true) {
                    try {
                        data += result.getString(i) + "  :  ";
                        i++;
                    } catch (SQLException e) {
                        data = data.substring(0, data.length() -5);
                        break;
                    }
                }
                data += row;
                if (!result.isLast()) {
                    data += "\n\n";
                }
            }
            return data;
        } catch (SQLException e){
                e.printStackTrace();
        }
        return null;
    }
    public void executeUpdate(String sqlCode){
        try {
            conn.createStatement().executeUpdate(sqlCode);
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    private Connection getConnection(){
        String url = "jdbc:mysql://localhost:3306/mydb";

        String uname = "user";
        String password = "password";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            return DriverManager.getConnection(url, uname, password);
        }
        // Handle any errors that may have occurred.
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
