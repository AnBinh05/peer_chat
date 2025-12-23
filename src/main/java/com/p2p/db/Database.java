package com.p2p.db;

import java.sql.Connection;
import java.sql.DriverManager;

public class Database {

    private static final String URL =
            "jdbc:mysql://localhost:3306/peertalk?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC";

    private static final String USER = "root";
    private static final String PASS = "binh07042005";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
