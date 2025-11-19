package com.ecommerce.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.ecommerce.model.Review;
import com.ecommerce.util.DB;

public class ReviewDAO {
    public List<Review> listForProduct(int productId) {
        String sql = "SELECT id,product_id,user_id,rating,title,body,status,verified_purchase,created_at,updated_at FROM reviews WHERE product_id=? AND status='approved' ORDER BY created_at DESC";
        List<Review> list = new ArrayList<>();
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list reviews", e);
        }
        return list;
    }

    public void add(Review r) {
        String sql = "INSERT INTO reviews(product_id,user_id,rating,title,body,status,verified_purchase) VALUES(?,?,?,?,?,'pending',?)";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, r.getProductId());
            if (r.getUserId() == null) ps.setNull(2, Types.CHAR); else ps.setString(2, r.getUserId());
            ps.setInt(3, r.getRating());
            ps.setString(4, r.getTitle());
            ps.setString(5, r.getBody());
            ps.setBoolean(6, r.isVerifiedPurchase());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add review", e);
        }
    }

    private Review map(ResultSet rs) throws SQLException {
        Review r = new Review();
        r.setId(rs.getInt("id"));
        r.setProductId(rs.getInt("product_id"));
        r.setUserId(rs.getString("user_id"));
        r.setRating(rs.getInt("rating"));
        r.setTitle(rs.getString("title"));
        r.setBody(rs.getString("body"));
        r.setStatus(rs.getString("status"));
        r.setVerifiedPurchase(rs.getBoolean("verified_purchase"));
        return r;
    }
}
