package com.ecommerce.dao;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

import com.ecommerce.model.User;
import com.ecommerce.util.DB;

public class UserDAO {

    public User findByEmail(String email) {
        String sql = "SELECT id,name,email,password_hash,role,phone,last_login_at FROM users WHERE email=?";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return map(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by email", e);
        }
    }

    public User create(String name, String email, String rawPassword, String phone) {
        String sql = "INSERT INTO users (name,email,password_hash,phone) VALUES (?,?,?,?)";
        String hash = hashPassword(rawPassword);
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, hash);
            ps.setString(4, phone);
            ps.executeUpdate();
            
            User u = findByEmail(email);
            if (u != null) {
                return u;
            }

            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create user", e);
        }
    }

    public boolean verifyPassword(String rawPassword, String hash) {
        return hashPassword(rawPassword).equals(hash);
    }

    private String hashPassword(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(raw.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getString("id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(rs.getString("role"));
        u.setPhone(rs.getString("phone"));
        Timestamp last = rs.getTimestamp("last_login_at");
        if (last != null) {
            u.setLastLoginAt(last.toLocalDateTime());
        }
        return u;
    }

    public User findById(String id) {
        String sql = "SELECT id,name,email,password_hash,role,phone,last_login_at FROM users WHERE id=?";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return map(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by id", e);
        }
    }

    public void updateProfile(String id, String name, String phone) {
        String sql = "UPDATE users SET name=?, phone=? WHERE id=?";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setString(3, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update profile", e);
        }
    }
}

