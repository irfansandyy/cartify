package com.ecommerce.servlet;

import com.ecommerce.dao.CartDAO;
import com.ecommerce.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/cart-add")
public class CartAddServlet extends HttpServlet {
    private final CartDAO cartDAO = new CartDAO();

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
        String qtyRaw = req.getParameter("qty");
        if (pidRaw == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\":\"missing productId\"}");
            return;
        }
        int productId = Integer.parseInt(pidRaw);
        int qty = 1;
        if (qtyRaw != null) {
            try { qty = Math.max(1, Integer.parseInt(qtyRaw)); } catch (NumberFormatException ignored) {}
        }
        cartDAO.addOrIncrement(user.getId(), productId, qty);
        resp.setContentType("application/json");
        resp.getWriter().write("{\"status\":\"added\",\"productId\":" + productId + ",\"qty\":" + qty + "}" );
    }
}