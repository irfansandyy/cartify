package com.ecommerce.servlet;

import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.ecommerce.dao.CartDAO;
import com.ecommerce.dao.ProductDAO;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;

@WebServlet("/cart")
public class CartServlet extends HttpServlet {
    private final CartDAO cartDAO = new CartDAO();
    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userId = resolveUserId(req.getSession());
        List<CartItem> items = cartDAO.listByUser(userId);
        
        List<Product> products = items.stream().map(i -> productDAO.findById(i.getProductId())).collect(Collectors.toList());
        req.setAttribute("cartItems", items);
        req.setAttribute("cartProducts", products);
        
        req.setAttribute("miniCartItems", items.stream().limit(3).collect(Collectors.toList()));
        req.setAttribute("cartProductsMini", products.stream().limit(3).collect(Collectors.toList()));
        int cartCount = items.stream().mapToInt(CartItem::getQuantity).sum();
        req.setAttribute("cartCount", cartCount);
        java.math.BigDecimal miniTotal = java.math.BigDecimal.ZERO;
        for (int i = 0; i < items.size() && i < products.size() && i < 3; i++) {
            CartItem ci = items.get(i);
            Product p = products.get(i);
            if (p.getPrice() != null) {
                miniTotal = miniTotal.add(p.getPrice().multiply(new java.math.BigDecimal(ci.getQuantity())));
            }
        }
        req.setAttribute("miniCartTotal", miniTotal);
        req.getRequestDispatcher("/cart.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        String userId = resolveUserId(req.getSession());
        if ("add".equals(action)) {
            int productId = Integer.parseInt(req.getParameter("productId"));
            int qty = Integer.parseInt(req.getParameter("qty"));
            cartDAO.addOrIncrement(userId, productId, qty);
        } else if ("update".equals(action)) {
            int id = Integer.parseInt(req.getParameter("id"));
            int qty = Integer.parseInt(req.getParameter("qty"));
            cartDAO.updateQuantity(id, qty);
        } else if ("remove".equals(action)) {
            int id = Integer.parseInt(req.getParameter("id"));
            cartDAO.remove(id);
        }
        resp.sendRedirect(req.getContextPath() + "/cart");
    }

    private String resolveUserId(HttpSession session) {
        if (session == null) {
            throw new IllegalStateException("No HTTP session for cart operation");
        }
        User user = (User) session.getAttribute("currentUser");
        if (user == null || user.getId() == null) {
            throw new IllegalStateException("No authenticated user in session for cart");
        }
        return user.getId();
    }
}
