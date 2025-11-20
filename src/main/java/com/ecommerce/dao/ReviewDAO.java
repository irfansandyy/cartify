package com.ecommerce.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.ecommerce.util.DB;

public class ReviewDAO {

    public static class Row {
        private int id;
        private int productId;
        private String userId;
        private int rating;
        private String title;
        private String body;
        private String status;
        private boolean verifiedPurchase;
        private String userEmail;

        public int getId() { return id; }
        public int getProductId() { return productId; }
        public String getUserId() { return userId; }
        public int getRating() { return rating; }
        public String getTitle() { return title; }
        public String getBody() { return body; }
        public String getStatus() { return status; }
        public boolean isVerifiedPurchase() { return verifiedPurchase; }
        public String getUserEmail() { return userEmail; }
    }

    public List<Row> listRowsForProduct(int productId) {
        String sql = "SELECT r.id,r.product_id,r.user_id,r.rating,r.title,r.body,r.status,r.verified_purchase,r.created_at,r.updated_at,u.email AS user_email " +
                "FROM reviews r LEFT JOIN users u ON r.user_id=u.id WHERE r.product_id=? AND r.status='approved' ORDER BY r.created_at DESC";
        List<Row> list = new ArrayList<>();
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list reviews", e);
        }
        return list;
    }

    public void addReview(int productId, String userId, int rating, String title, String body, String status, boolean verifiedPurchase) {
        String sql = "INSERT INTO reviews(product_id,user_id,rating,title,body,status,verified_purchase) VALUES(?,?,?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE rating=VALUES(rating), title=VALUES(title), body=VALUES(body), status=VALUES(status), verified_purchase=VALUES(verified_purchase), updated_at=CURRENT_TIMESTAMP";
        String st = (status != null && !status.isEmpty()) ? status : "approved";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            if (userId == null) ps.setNull(2, Types.CHAR); else ps.setString(2, userId);
            ps.setInt(3, rating);
            ps.setString(4, title);
            ps.setString(5, body);
            ps.setString(6, st);
            ps.setBoolean(7, verifiedPurchase);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add review", e);
        }
    }

    public void delete(int productId, String userId) {
        String sql = "DELETE FROM reviews WHERE product_id=? AND user_id=?";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setString(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete review", e);
        }
    }

    private Row mapRow(ResultSet rs) throws SQLException {
        Row r = new Row();
        // Package-private field accessors since inner class; use reflection-like setters avoided for simplicity
        r.id = rs.getInt("id");
        r.productId = rs.getInt("product_id");
        r.userId = rs.getString("user_id");
        r.rating = rs.getInt("rating");
        r.title = rs.getString("title");
        r.body = rs.getString("body");
        r.status = rs.getString("status");
        r.verifiedPurchase = rs.getBoolean("verified_purchase");
        r.userEmail = rs.getString("user_email");
        return r;
    }
}
