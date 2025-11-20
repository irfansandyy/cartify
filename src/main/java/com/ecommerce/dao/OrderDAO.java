package com.ecommerce.dao;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;

import com.ecommerce.model.CartItem;
import com.ecommerce.util.DB;

public class OrderDAO {

    public int createOrderWithItems(String userId, List<CartItem> items, BigDecimal subtotal, BigDecimal shipping, BigDecimal tax, String currency, String paymentMethod) {
        String orderSql = "INSERT INTO orders(user_id,total_price,status,order_number,currency,shipping_total,tax_total) " +
            "VALUES(?, ?, 'completed', ?, ?, ?, ?)"; // mark as completed immediately (delivered)
        String itemSql = "INSERT INTO order_items(order_id,product_id,price_at_purchase,quantity) VALUES(?,?,?,?)";
        String paymentSql = "INSERT INTO payments(order_id,amount,method,status,paid_at) VALUES(?,?,?,?,CURRENT_TIMESTAMP)";

        BigDecimal total = subtotal.add(shipping).add(tax);

        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);
            int orderId;

            try (PreparedStatement ps = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, userId);
                ps.setBigDecimal(2, total);
                ps.setString(3, generateOrderNumber());
                ps.setString(4, currency);
                ps.setBigDecimal(5, shipping);
                ps.setBigDecimal(6, tax);
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                if (!rs.next()) {
                    conn.rollback();
                    throw new RuntimeException("Failed to create order (no id)");
                }
                orderId = rs.getInt(1);
            }

            try (PreparedStatement psItem = conn.prepareStatement(itemSql)) {
                for (CartItem ci : items) {
                    psItem.setInt(1, orderId);
                    psItem.setInt(2, ci.getProductId());
                    psItem.setBigDecimal(3, ci.getPriceAtAdd());
                    psItem.setInt(4, ci.getQuantity());
                    psItem.addBatch();
                }
                psItem.executeBatch();
            }

            try (PreparedStatement psPay = conn.prepareStatement(paymentSql)) {
                psPay.setInt(1, orderId);
                psPay.setBigDecimal(2, total);
                psPay.setString(3, paymentMethod);
                psPay.setString(4, "paid");
                psPay.executeUpdate();
            }

            conn.commit();
            return orderId;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create order", e);
        }
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }

    public static class OrderRow {
        public int id;
        public String userId;
        public String orderNumber;
        public BigDecimal totalPrice;
        public String status;
        public Timestamp createdAt;

        public int getId() { return id; }
        public String getUserId() { return userId; }
        public String getOrderNumber() { return orderNumber; }
        public BigDecimal getTotalPrice() { return totalPrice; }
        public String getStatus() { return status; }
        public Timestamp getCreatedAt() { return createdAt; }
    }

    public List<OrderRow> listByUser(String userId) {
        String sql = "SELECT id,user_id,order_number,total_price,status,created_at FROM orders WHERE user_id=? ORDER BY id DESC";
        List<OrderRow> list = new ArrayList<>();
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                OrderRow r = new OrderRow();
                r.id = rs.getInt("id");
                r.userId = rs.getString("user_id");
                r.orderNumber = rs.getString("order_number");
                r.totalPrice = rs.getBigDecimal("total_price");
                r.status = rs.getString("status");
                r.createdAt = rs.getTimestamp("created_at");
                list.add(r);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list orders", e);
        }
        return list;
    }

    public static class OrderItemRow {
        public int productId;
        public String productName;
        public int quantity;
        public BigDecimal priceAtPurchase;
        public int getProductId() { return productId; }
        public String getProductName() { return productName; }
        public int getQuantity() { return quantity; }
        public BigDecimal getPriceAtPurchase() { return priceAtPurchase; }
    }

    public List<OrderItemRow> listItemsWithProducts(int orderId) {
        String sql = "SELECT oi.product_id, p.name AS product_name, oi.quantity, oi.price_at_purchase FROM order_items oi JOIN products p ON oi.product_id=p.id WHERE oi.order_id=?";
        List<OrderItemRow> list = new ArrayList<>();
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                OrderItemRow r = new OrderItemRow();
                r.productId = rs.getInt("product_id");
                r.productName = rs.getString("product_name");
                r.quantity = rs.getInt("quantity");
                r.priceAtPurchase = rs.getBigDecimal("price_at_purchase");
                list.add(r);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list order items", e);
        }
        return list;
    }
}
