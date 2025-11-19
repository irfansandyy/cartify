package com.ecommerce.seed;

import java.math.BigDecimal;
import java.sql.*;
import java.text.Normalizer;
import java.util.Locale;

import com.ecommerce.util.DB;

public class Seeder {
    public static class Result {
        public boolean skipped;
        public int categoriesInserted;
        public int productsInserted;
        public String message;
    }

    public Result run() {
        Result r = new Result();
        try (Connection conn = DB.getConnection()) {
            if (tableHasData(conn, "products")) {
                r.skipped = true;
                r.message = "Products already present; seeding skipped.";
                return r;
            }

            conn.setAutoCommit(false);
            try {
                int catElectronics = upsertCategory(conn, "Electronics");
                int catBooks = upsertCategory(conn, "Books");
                int catHome = upsertCategory(conn, "Home & Kitchen");
                int catFashion = upsertCategory(conn, "Fashion");
                r.categoriesInserted = countCategories(conn);

                r.productsInserted += upsertProduct(conn, "Wireless Headphones",
                        "Comfortable over-ear wireless headphones with noise isolation.",
                        new BigDecimal("79.99"), 50,
                        "https://via.placeholder.com/600x400?text=Wireless+Headphones",
                        catElectronics, "ELEC-HEAD-001");

                r.productsInserted += upsertProduct(conn, "Smartwatch",
                        "Track fitness and receive notifications with a sleek smartwatch.",
                        new BigDecimal("129.99"), 40,
                        "https://via.placeholder.com/600x400?text=Smartwatch",
                        catElectronics, "ELEC-WATCH-002");

                r.productsInserted += upsertProduct(conn, "Coffee Maker",
                        "Programmable drip coffee maker with reusable filter.",
                        new BigDecimal("49.99"), 60,
                        "https://via.placeholder.com/600x400?text=Coffee+Maker",
                        catHome, "HOME-COFFEE-003");

                r.productsInserted += upsertProduct(conn, "Vacuum Cleaner",
                        "Lightweight bagless vacuum cleaner with HEPA filter.",
                        new BigDecimal("149.99"), 25,
                        "https://via.placeholder.com/600x400?text=Vacuum+Cleaner",
                        catHome, "HOME-VAC-004");

                r.productsInserted += upsertProduct(conn, "Novel: The Lost City",
                        "A thrilling adventure novel set in a mysterious ancient city.",
                        new BigDecimal("14.99"), 100,
                        "https://via.placeholder.com/600x400?text=Novel",
                        catBooks, "BOOK-LOST-005");

                r.productsInserted += upsertProduct(conn, "Pullover Hoodie",
                        "Soft cotton-blend hoodie with front pocket.",
                        new BigDecimal("29.99"), 80,
                        "https://via.placeholder.com/600x400?text=Hoodie",
                        catFashion, "FASH-HOOD-006");

                r.productsInserted += upsertProduct(conn, "Running Sneakers",
                        "Breathable running shoes with cushioned sole.",
                        new BigDecimal("69.99"), 70,
                        "https://via.placeholder.com/600x400?text=Sneakers",
                        catFashion, "FASH-SHOE-007");

                r.productsInserted += upsertProduct(conn, "Bluetooth Speaker",
                        "Portable Bluetooth speaker with deep bass.",
                        new BigDecimal("39.99"), 90,
                        "https://via.placeholder.com/600x400?text=Bluetooth+Speaker",
                        catElectronics, "ELEC-SPKR-008");

                conn.commit();
                r.message = "Seeding complete.";
            } catch (Exception ex) {
                try { conn.rollback(); } catch (SQLException ignored) {}
                throw ex;
            } finally {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        } catch (Exception e) {
            Result err = new Result();
            err.skipped = true;
            err.message = "Seeding failed: " + e.getMessage();
            return err;
        }
        return r;
    }

    private boolean tableHasData(Connection conn, String table) throws SQLException {
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT 1 FROM " + table + " LIMIT 1")) {
            return rs.next();
        }
    }

    private int countCategories(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM categories")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private int upsertCategory(Connection conn, String name) throws SQLException {
        String slug = slugify(name);
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO categories (name, slug) VALUES (?, ?) " +
                        "ON DUPLICATE KEY UPDATE name=VALUES(name)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, slug);
            ps.executeUpdate();
        }
        
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM categories WHERE slug=?")) {
            ps.setString(1, slug);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Failed to retrieve category id for slug: " + slug);
    }

    private int upsertProduct(Connection conn, String name, String description, BigDecimal price, int stock,
                              String imageUrl, int categoryId, String sku) throws SQLException {
        String slug = slugify(name);
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO products (name, description, price, stock, image_url, category_id, sku, slug, status) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'active') " +
                        "ON DUPLICATE KEY UPDATE name=VALUES(name), description=VALUES(description), price=VALUES(price), " +
                        "stock=VALUES(stock), image_url=VALUES(image_url), category_id=VALUES(category_id), status='active'")) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setBigDecimal(3, price);
            ps.setInt(4, stock);
            ps.setString(5, imageUrl);
            ps.setInt(6, categoryId);
            ps.setString(7, sku);
            ps.setString(8, slug);
            return ps.executeUpdate();
        }
    }

    private String slugify(String input) {
        String nowhitespace = input.trim().replaceAll("[\\s_]+", "-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD)
                .replaceAll("[^\\w-]", "")
                .toLowerCase(Locale.ROOT);
        return normalized.replaceAll("-+", "-");
    }
}
