package com.ecommerce.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import io.github.cdimascio.dotenv.Dotenv;

public class DB {
    private static String url;
    private static String user;
    private static String password;

    static {
        Dotenv dotenv = Dotenv.configure().directory("/home/sandy/code/website/cartify").load();

        url = dotenv.get("DB_URL");
        user = dotenv.get("DB_USER");
        password = dotenv.get("DB_PASS");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException ex) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException ignored) { }
            return DriverManager.getConnection(url, user, password);
        }
    }
}
