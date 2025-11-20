package com.ecommerce.dao;

import com.ecommerce.util.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AddressDAO {

    public static class Address {
        public int id;
        public String userId;
        public String name;
        public String phone;
        public String address1;
        public String address2;
        public String city;
        public String region;
        public String postalCode;
        public String countryCode;
        public boolean defaultShipping;
        public boolean defaultBilling;

        public int getId() { return id; }
        public String getUserId() { return userId; }
        public String getName() { return name; }
        public String getPhone() { return phone; }
        public String getAddress1() { return address1; }
        public String getAddress2() { return address2; }
        public String getCity() { return city; }
        public String getRegion() { return region; }
        public String getPostalCode() { return postalCode; }
        public String getCountryCode() { return countryCode; }
        public boolean isDefaultShipping() { return defaultShipping; }
        public boolean isDefaultBilling() { return defaultBilling; }
    }

    public List<Address> listByUser(String userId) {
        String sql = "SELECT * FROM addresses WHERE user_id=? ORDER BY id DESC";
        List<Address> list = new ArrayList<>();
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public Address findByIdForUser(int id, String userId) {
        String sql = "SELECT * FROM addresses WHERE id=? AND user_id=?";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void create(String userId, Address a) {
        String sql = "INSERT INTO addresses(user_id,name,phone,address1,address2,city,region,postal_code,country_code,is_default_shipping,is_default_billing) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, a.name);
            ps.setString(3, a.phone);
            ps.setString(4, a.address1);
            ps.setString(5, a.address2);
            ps.setString(6, a.city);
            ps.setString(7, a.region);
            ps.setString(8, a.postalCode);
            ps.setString(9, a.countryCode);
            ps.setBoolean(10, a.defaultShipping);
            ps.setBoolean(11, a.defaultBilling);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateForUser(int id, String userId, Address a) {
        String sql = "UPDATE addresses SET name=?,phone=?,address1=?,address2=?,city=?,region=?,postal_code=?,country_code=? " +
                "WHERE id=? AND user_id=?";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.name);
            ps.setString(2, a.phone);
            ps.setString(3, a.address1);
            ps.setString(4, a.address2);
            ps.setString(5, a.city);
            ps.setString(6, a.region);
            ps.setString(7, a.postalCode);
            ps.setString(8, a.countryCode);
            ps.setInt(9, id);
            ps.setString(10, userId);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void deleteForUser(int id, String userId) {
        String sql = "DELETE FROM addresses WHERE id=? AND user_id=?";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void setDefaultShipping(String userId, int addressId) {
        String reset = "UPDATE addresses SET is_default_shipping=0 WHERE user_id=?";
        String set = "UPDATE addresses SET is_default_shipping=1 WHERE id=? AND user_id=?";
        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(reset)) {
                ps1.setString(1, userId);
                ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = conn.prepareStatement(set)) {
                ps2.setInt(1, addressId);
                ps2.setString(2, userId);
                ps2.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void setDefaultBilling(String userId, int addressId) {
        String reset = "UPDATE addresses SET is_default_billing=0 WHERE user_id=?";
        String set = "UPDATE addresses SET is_default_billing=1 WHERE id=? AND user_id=?";
        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(reset)) {
                ps1.setString(1, userId);
                ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = conn.prepareStatement(set)) {
                ps2.setInt(1, addressId);
                ps2.setString(2, userId);
                ps2.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private Address map(ResultSet rs) throws SQLException {
        Address a = new Address();
        a.id = rs.getInt("id");
        a.userId = rs.getString("user_id");
        a.name = rs.getString("name");
        a.phone = rs.getString("phone");
        a.address1 = rs.getString("address1");
        a.address2 = rs.getString("address2");
        a.city = rs.getString("city");
        a.region = rs.getString("region");
        a.postalCode = rs.getString("postal_code");
        a.countryCode = rs.getString("country_code");
        a.defaultShipping = rs.getBoolean("is_default_shipping");
        a.defaultBilling = rs.getBoolean("is_default_billing");
        return a;
    }
}
