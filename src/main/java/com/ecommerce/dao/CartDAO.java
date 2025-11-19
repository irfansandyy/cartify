package com.ecommerce.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.ecommerce.model.CartItem;
import com.ecommerce.util.DB;

public class CartDAO {
    public List<CartItem> listByUser(String userId) {
        String sql = "SELECT id,user_id,product_id,quantity,price_at_add,created_at,updated_at FROM cart_items WHERE user_id=?";
        List<CartItem> items = new ArrayList<>();
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) items.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list cart items", e);
        }
        return items;
    }

    public void addOrIncrement(String userId, int productId, int quantity) {
        String sql = "INSERT INTO cart_items(user_id,product_id,quantity,price_at_add) VALUES(?,?,?,(SELECT price FROM products WHERE id=?)) ON DUPLICATE KEY UPDATE quantity=quantity+VALUES(quantity)";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            ps.setInt(4, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add to cart", e);
        }
    }

    public void updateQuantity(int id, int quantity) {
        String sql = "UPDATE cart_items SET quantity=? WHERE id=?";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update quantity", e);
        }
    }

    public void remove(int id) {
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM cart_items WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove cart item", e);
        }
    }

    public void clearForUser(String userId) {
        String sql = "DELETE FROM cart_items WHERE user_id=?";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clear cart for user", e);
        }
    }

    private CartItem map(ResultSet rs) throws SQLException {
        CartItem c = new CartItem();
        c.setId(rs.getInt("id"));
        c.setUserId(rs.getString("user_id"));
        c.setProductId(rs.getInt("product_id"));
        c.setQuantity(rs.getInt("quantity"));
        c.setPriceAtAdd(rs.getBigDecimal("price_at_add"));
        return c;
    }
}
