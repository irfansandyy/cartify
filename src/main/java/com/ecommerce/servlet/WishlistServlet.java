package com.ecommerce.servlet;

import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.ecommerce.dao.ProductDAO;
import com.ecommerce.dao.WishlistDAO;
import com.ecommerce.model.Product;
import com.ecommerce.model.WishlistItem;

@WebServlet("/wishlist")
public class WishlistServlet extends HttpServlet {
    private final WishlistDAO wishlistDAO = new WishlistDAO();
    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int wishlistId = ensureWishlist(req.getSession());
        List<WishlistItem> items = wishlistDAO.listItems(wishlistId);
        List<Product> products = items.stream().map(i -> productDAO.findById(i.getProductId())).collect(Collectors.toList());
        req.setAttribute("wishlistItems", items);
        req.setAttribute("wishlistProducts", products);
        req.getRequestDispatcher("/wishlist.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int wishlistId = ensureWishlist(req.getSession());
        String action = req.getParameter("action");
        if ("add".equals(action)) {
            int productId = Integer.parseInt(req.getParameter("productId"));
            wishlistDAO.add(wishlistId, productId);
        } else if ("remove".equals(action)) {
            int id = Integer.parseInt(req.getParameter("id"));
            wishlistDAO.remove(id);
        }
        resp.sendRedirect(req.getContextPath() + "/wishlist");
    }

    private int ensureWishlist(HttpSession session) {
        Integer wishlistId = (Integer) session.getAttribute("wishlistId");
        if (wishlistId == null) {
            wishlistId = 1;
            session.setAttribute("wishlistId", wishlistId);
        }
        return wishlistId;
    }
}
