package com.ecommerce.servlet;

import com.ecommerce.dao.WishlistDAO;
import com.ecommerce.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/wishlist-add")
public class WishlistAddServlet extends HttpServlet {
    private final WishlistDAO wishlistDAO = new WishlistDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = session != null ? (User) session.getAttribute("currentUser") : null;
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\":\"login required\"}");
            return;
        }
        String pidRaw = req.getParameter("productId");
        if (pidRaw == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\":\"missing productId\"}");
            return;
        }
        int productId = Integer.parseInt(pidRaw);
        int wishlistId = wishlistDAO.ensureDefaultForUser(user.getId());
        wishlistDAO.add(wishlistId, productId);
        resp.setContentType("application/json");
        resp.getWriter().write("{\"status\":\"added\"}");
    }
}