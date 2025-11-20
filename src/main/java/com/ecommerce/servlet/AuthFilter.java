package com.ecommerce.servlet;

import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.IOException;

import java.sql.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import com.ecommerce.dao.UserDAO;
import com.ecommerce.dao.CartDAO;
import com.ecommerce.dao.ProductDAO;
import com.ecommerce.dao.WishlistDAO;
import com.ecommerce.model.User;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Product;
import com.ecommerce.util.DB;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebFilter("/*")
public class AuthFilter implements Filter {

    private final UserDAO userDAO = new UserDAO();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        User currentUser = session != null ? (User) session.getAttribute("currentUser") : null;

        if (currentUser == null) {
            currentUser = tryRestoreFromToken(req, resp);
            if (currentUser != null) {
                session = req.getSession(true);
                session.setAttribute("currentUser", currentUser);
                session.setAttribute("currentUserEmail", currentUser.getEmail());
            }
        }

        String path = req.getRequestURI().substring(req.getContextPath().length());
        boolean requiresAuth = path.startsWith("/cart") || path.startsWith("/wishlist") || path.startsWith("/orders") || path.startsWith("/account") || path.startsWith("/checkout");

        if (requiresAuth && currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Inject cart preview data for header if user logged in
        if (currentUser != null) {
            try {
                CartDAO cartDAO = new CartDAO();
                ProductDAO productDAO = new ProductDAO();
                WishlistDAO wishlistDAO = new WishlistDAO();
                List<CartItem> miniCartItems = cartDAO.listByUser(currentUser.getId());
                List<Product> cartProductsMini = new ArrayList<>();
                BigDecimal total = BigDecimal.ZERO;
                for (CartItem ci : miniCartItems) {
                    Product p = productDAO.findById(ci.getProductId());
                    if (p != null) {
                        cartProductsMini.add(p);
                        if (p.getPrice() != null) {
                            total = total.add(p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
                        }
                    }
                }
                request.setAttribute("miniCartItems", miniCartItems);
                request.setAttribute("cartProductsMini", cartProductsMini);
                request.setAttribute("miniCartTotal", total);
                request.setAttribute("cartCount", miniCartItems.size());

                // Inject wishlist presence map keyed by productId
                int wishlistId = wishlistDAO.ensureDefaultForUser(currentUser.getId());
                var wishItems = wishlistDAO.listItems(wishlistId);
                Map<Integer, Boolean> wishlistMap = new HashMap<>();
                for (var wi : wishItems) wishlistMap.put(wi.getProductId(), Boolean.TRUE);
                request.setAttribute("wishlistMap", wishlistMap);
            } catch (Exception ignored) { /* fail silently so page still renders */ }
        }

        chain.doFilter(request, response);
    }

    private User tryRestoreFromToken(HttpServletRequest req, HttpServletResponse resp) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;

        String token = null;
        for (Cookie c : cookies) {
            if ("cartify_auth".equals(c.getName())) {
                token = c.getValue();
                break;
            }
        }
        if (token == null || token.isEmpty()) return null;

        String sql = "SELECT user_id, expires_at FROM user_tokens WHERE token=?";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;

            Instant expires = rs.getTimestamp("expires_at").toInstant();
            if (expires.isBefore(Instant.now())) {
                deleteToken(token);
                Cookie expired = new Cookie("cartify_auth", "");
                expired.setPath(req.getContextPath().isEmpty() ? "/" : req.getContextPath());
                expired.setMaxAge(0);
                resp.addCookie(expired);
                return null;
            }

            String userId = rs.getString("user_id");
            return userDAO.findById(userId);
        } catch (SQLException e) {
            return null;
        }
    }

    public static void createSessionToken(User user, HttpServletRequest req, HttpServletResponse resp) {
        String token = UUID.randomUUID().toString();
        Instant expires = Instant.now().plus(3, ChronoUnit.DAYS);

        String sql = "INSERT INTO user_tokens (user_id, token, expires_at) VALUES (?,?,?)";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getId());
            ps.setString(2, token);
            ps.setTimestamp(3, java.sql.Timestamp.from(expires));
            ps.executeUpdate();
        } catch (SQLException e) {
            
        }

        Cookie cookie = new Cookie("cartify_auth", token);
        cookie.setHttpOnly(true);
        cookie.setPath(req.getContextPath().isEmpty() ? "/" : req.getContextPath());
        cookie.setMaxAge(3 * 24 * 60 * 60);
        resp.addCookie(cookie);
    }

    public static void clearSessionToken(User user, HttpServletRequest req, HttpServletResponse resp) {
        String sql = "DELETE FROM user_tokens WHERE user_id=?";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            
        }

        Cookie cookie = new Cookie("cartify_auth", "");
        cookie.setPath(req.getContextPath().isEmpty() ? "/" : req.getContextPath());
        cookie.setMaxAge(0);
        resp.addCookie(cookie);
    }

    private void deleteToken(String token) {
        String sql = "DELETE FROM user_tokens WHERE token=?";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.executeUpdate();
        } catch (SQLException ignored) { }
    }
}
