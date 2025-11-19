package com.ecommerce.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.ecommerce.model.WishlistItem;
import com.ecommerce.util.DB;

public class WishlistDAO {
    public List<WishlistItem> listItems(int wishlistId) {
        String sql = "SELECT id,wishlist_id,product_id,created_at FROM wishlist_items WHERE wishlist_id=?";
        List<WishlistItem> items = new ArrayList<>();
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, wishlistId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) items.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list wishlist items", e);
        }
        return items;
    }

    public void add(int wishlistId, int productId) {
        String sql = "INSERT IGNORE INTO wishlist_items(wishlist_id,product_id) VALUES(?,?)";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, wishlistId);
            ps.setInt(2, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add wishlist item", e);
        }
    }

    public void remove(int id) {
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM wishlist_items WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove wishlist item", e);
        }
    }

    private WishlistItem map(ResultSet rs) throws SQLException {
        WishlistItem w = new WishlistItem();
        w.setId(rs.getInt("id"));
        w.setWishlistId(rs.getInt("wishlist_id"));
        w.setProductId(rs.getInt("product_id"));
        return w;
    }
}
