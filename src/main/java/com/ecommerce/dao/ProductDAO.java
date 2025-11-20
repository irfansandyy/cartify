package com.ecommerce.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.ecommerce.model.Product;
import com.ecommerce.util.DB;

public class ProductDAO {
    public List<Product> listAll() {
    String sql = "SELECT p.id,p.name,p.description,p.price,p.stock,p.image_url,p.category_id,p.sku,p.slug,p.status,p.created_at,p.updated_at, " +
        "c.name AS category_name, COUNT(r.id) AS review_count, AVG(r.rating) AS avg_rating " +
        "FROM products p " +
        "LEFT JOIN reviews r ON r.product_id = p.id AND r.status = 'approved' " +
        "LEFT JOIN categories c ON c.id = p.category_id " +
        "WHERE p.deleted_at IS NULL " +
        "GROUP BY p.id,p.name,p.description,p.price,p.stock,p.image_url,p.category_id,p.sku,p.slug,p.status,p.created_at,p.updated_at,c.name " +
        "ORDER BY p.created_at DESC LIMIT 100";
        List<Product> list = new ArrayList<>();
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list products", e);
        }
        return list;
    }

    public List<Product> search(String term) {
        String sql = "SELECT p.id,p.name,p.description,p.price,p.stock,p.image_url,p.category_id,p.sku,p.slug,p.status,p.created_at,p.updated_at, " +
                "c.name AS category_name, COUNT(r.id) AS review_count, AVG(r.rating) AS avg_rating " +
                "FROM products p " +
                "LEFT JOIN reviews r ON r.product_id = p.id AND r.status='approved' " +
                "LEFT JOIN categories c ON c.id = p.category_id " +
                "WHERE p.deleted_at IS NULL AND (LOWER(p.name) LIKE ? OR LOWER(p.description) LIKE ? OR LOWER(c.name) LIKE ?) " +
                "GROUP BY p.id,p.name,p.description,p.price,p.stock,p.image_url,p.category_id,p.sku,p.slug,p.status,p.created_at,p.updated_at,c.name " +
                "ORDER BY p.created_at DESC LIMIT 100";
        List<Product> list = new ArrayList<>();
        String like = "%" + term.toLowerCase() + "%";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search products", e);
        }
        return list;
    }

    public Product findById(int id) {
    String sql = "SELECT p.id,p.name,p.description,p.price,p.stock,p.image_url,p.category_id,p.sku,p.slug,p.status,p.created_at,p.updated_at, " +
        "c.name AS category_name, COUNT(r.id) AS review_count, AVG(r.rating) AS avg_rating " +
        "FROM products p " +
        "LEFT JOIN reviews r ON r.product_id = p.id AND r.status = 'approved' " +
        "LEFT JOIN categories c ON c.id = p.category_id " +
        "WHERE p.id = ? " +
        "GROUP BY p.id,p.name,p.description,p.price,p.stock,p.image_url,p.category_id,p.sku,p.slug,p.status,p.created_at,p.updated_at,c.name";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find product" , e);
        }
    }

    private Product map(ResultSet rs) throws SQLException {
    Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setStock(rs.getInt("stock"));
        p.setImageUrl(rs.getString("image_url"));
        int catId = rs.getInt("category_id");
        p.setCategoryId(rs.wasNull()?null:catId);
        p.setSku(rs.getString("sku"));
        p.setSlug(rs.getString("slug"));
    p.setStatus(rs.getString("status"));
    p.setCategoryName(rs.getString("category_name"));
    int rc = rs.getInt("review_count");
    p.setReviewCount(rs.wasNull() ? null : rc);
    double avg = rs.getDouble("avg_rating");
    p.setAverageRating(rs.wasNull() ? null : avg);
        return p;
    }
}
