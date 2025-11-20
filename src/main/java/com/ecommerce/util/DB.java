package com.ecommerce.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DB {
    private static String url;
    private static String user;
    private static String password;
    private static Map<String,String> localEnv;

    static {
        // Prefer real environment variables (container-friendly)
        url = System.getenv("DB_URL");
        user = System.getenv("DB_USER");
        password = System.getenv("DB_PASS");

        // Fallback: attempt to read .env in working directory if any are missing
        if (url == null || user == null || password == null) {
            loadLocalDotEnv();
            if (url == null) url = getLocal("DB_URL");
            if (user == null) user = getLocal("DB_USER");
            if (password == null) password = getLocal("DB_PASS");
        }

        // Provide safe defaults (development convenience) if still missing
        if (url == null) {
            url = "jdbc:mysql://localhost:3306/cartify?useSSL=false&serverTimezone=UTC";
        }
        if (user == null) {
            user = "root";
        }
        if (password == null) {
            password = ""; // empty password dev default
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) { }
    }

    private static void loadLocalDotEnv() {
        Path p = Paths.get(System.getProperty("user.dir"), ".env");
        if (!Files.exists(p)) return;
        Map<String,String> map = new HashMap<>();
        try (BufferedReader br = Files.newBufferedReader(p)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eq = line.indexOf('=');
                if (eq <= 0) continue;
                String k = line.substring(0, eq).trim();
                String v = line.substring(eq + 1).trim();
                if ((v.startsWith("\"") && v.endsWith("\"")) || (v.startsWith("'") && v.endsWith("'"))) {
                    v = v.substring(1, v.length() - 1);
                }
                map.put(k, v);
            }
        } catch (IOException ignored) { }
        localEnv = map;
    }

    private static String getLocal(String key) {
        if (localEnv == null) return null;
        return localEnv.get(key);
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
