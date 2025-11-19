package com.ecommerce.dao;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

import com.ecommerce.model.CartItem;
import com.ecommerce.util.DB;

public class OrderDAO {

    public int createOrderWithItems(String userId, List<CartItem> items, BigDecimal subtotal, BigDecimal shipping, BigDecimal tax, String currency, String paymentMethod) {
        String orderSql = "INSERT INTO orders(user_id,total_price,status,order_number,currency,shipping_total,tax_total) " +
                "VALUES(?, ?, 'paid', ?, ?, ?, ?)";
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
}
