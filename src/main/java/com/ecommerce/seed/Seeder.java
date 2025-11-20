package com.ecommerce.seed;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import com.ecommerce.util.DB;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

public class Seeder {
    public static class Result {
        public boolean skipped;
        public int categoriesInserted;
        public int productsInserted;
        public String message;
    }

    private static class ProductSeed {
        String name;
        String category;
        String description;
        BigDecimal price;
        int stock;
        String sku;
        String imageUrl;
    }

    public Result run() {
        Result r = new Result();
        try (Connection conn = DB.getConnection()) {
            if (tableHasData(conn, "products")) {
                r.skipped = true;
                r.message = "Products already present; seeding skipped.";
                return r;
            }

            List<ProductSeed> seeds = generateWithGemini(100);

            conn.setAutoCommit(false);
            try {
                // Insert categories on-the-fly while inserting products
                for (ProductSeed p : seeds) {
                    int catId = upsertCategory(conn, p.category);
                    r.productsInserted += upsertProduct(conn, p.name, p.description, p.price, p.stock, p.imageUrl, catId, p.sku);
                }
                r.categoriesInserted = countCategories(conn);

                conn.commit();
                r.message = "Seeding complete via Gemini.";
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

    private List<ProductSeed> generateWithGemini(int count) {
        try {
            Client client = Client.builder().build();

            String prompt = "You are generating realistic e-commerce seed data. " +
                    "Output exactly " + count + " lines, each representing one product. " +
                    "Use this strict pipe-delimited format without quotes: " +
                    "name|category|short_description|price|stock|sku\n" +
                    "Constraints: " +
                    "- Do not include the '|' character in any field.\n" +
                    "- Keep descriptions under 140 characters, plain text.\n" +
                    "- price: a realistic USD price with 2 decimals (e.g., 19.99).\n" +
                    "- stock: integer between 20 and 150.\n" +
                    "- sku: Uppercase alphanumeric with hyphens, 8-16 chars, unique per item.\n" +
                    "- Use diverse consumer categories like Electronics, Home & Kitchen, Books, Fashion, Beauty, Sports, Toys, Office, Automotive, Pet Supplies.\n" +
                    "- Vary products across categories.\n" +
                    "Only output the data lines, no headers, no explanations.";

            GenerateContentResponse response = client.models.generateContent(
                    "gemini-2.5-flash",
                    prompt,
                    null
            );

            String text = response.text();
            if (text == null) throw new RuntimeException("Empty response from Gemini");
            String[] lines = text.split("\r?\n");

            List<ProductSeed> list = new ArrayList<>();
            int i = 0;
            for (String raw : lines) {
                if (list.size() >= count) break;
                String line = raw.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length < 6) continue; // skip malformed

                ProductSeed p = new ProductSeed();
                p.name = parts[0].trim();
                p.category = parts[1].trim();
                p.description = parts[2].trim();
                try { p.price = new BigDecimal(parts[3].trim()); } catch (Exception ex) { continue; }
                try { p.stock = Integer.parseInt(parts[4].trim()); } catch (Exception ex) { p.stock = 50; }
                String sku = parts[5].trim();
                if (sku.isEmpty()) sku = generateSku(p.category, p.name, i);
                p.sku = sku;

                String slug = slugify(p.name);
                String seed = URLEncoder.encode(slug, StandardCharsets.UTF_8);
                // 4:3 fallback for consistency with generated images
                p.imageUrl = "https://picsum.photos/seed/" + seed + "/1024/768";

                list.add(p);
                i++;
            }

            // If Gemini returned fewer than requested, top up with fallback synthetic items
            if (list.size() < count) {
                list.addAll(fallbackSynthetic(count - list.size()));
            }
            return list;
        } catch (Exception e) {
            // Fallback entirely if Gemini fails
            return fallbackSynthetic(count);
        }
    }

    private List<ProductSeed> fallbackSynthetic(int count) {
        String[] cats = {"Electronics", "Home & Kitchen", "Books", "Fashion", "Beauty", "Sports", "Toys", "Office", "Automotive", "Pet Supplies"};
        String[] names = {"Wireless Earbuds", "Smartwatch", "Espresso Maker", "Air Purifier", "Desk Lamp", "Yoga Mat", "RC Drone", "Notebook Set", "Car Vacuum", "Pet Grooming Kit"};
        String[] descs = {"High quality and reliable.", "Compact and user-friendly.", "Durable with modern design.", "Great value for everyday use.", "Lightweight and portable."};
        Random rnd = new Random();
        List<ProductSeed> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ProductSeed p = new ProductSeed();
            String name = names[i % names.length] + " " + (100 + i);
            p.name = name;
            p.category = cats[i % cats.length];
            p.description = descs[i % descs.length];
            p.price = new BigDecimal(String.format(Locale.ROOT, "%.2f", 19.0 + (i % 80)));
            p.stock = 20 + rnd.nextInt(100);
            p.sku = generateSku(p.category, p.name, i);
            String slug = slugify(p.name);
            String seed = URLEncoder.encode(slug, StandardCharsets.UTF_8);
            // 4:3 fallback for consistency with generated images
            p.imageUrl = "https://picsum.photos/seed/" + seed + "/1024/768";
            list.add(p);
        }
        return list;
    }

    private String generateSku(String category, String name, int i) {
        String cat = slugify(category).replaceAll("[^a-z0-9]", "").toUpperCase(Locale.ROOT);
        if (cat.length() > 4) cat = cat.substring(0, 4);
        String nm = slugify(name).replaceAll("[^a-z0-9]", "").toUpperCase(Locale.ROOT);
        if (nm.length() > 6) nm = nm.substring(0, 6);
        String idx = String.format(Locale.ROOT, "%03d", (i % 1000));
        return (cat + "-" + nm + "-" + idx).replaceAll("-+", "-");
    }
}
