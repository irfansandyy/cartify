package com.ecommerce.servlet;

import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.IOException;

import com.ecommerce.dao.ReviewDAO;
import com.ecommerce.model.Review;
import com.ecommerce.model.User;

@WebServlet("/review")
public class ReviewServlet extends HttpServlet {
    private final ReviewDAO reviewDAO = new ReviewDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userId = resolveUserId(req.getSession());
        int productId = Integer.parseInt(req.getParameter("productId"));
        int rating = Integer.parseInt(req.getParameter("rating"));
        String title = req.getParameter("title");
        String body = req.getParameter("body");
        Review r = new Review();
        r.setProductId(productId);
        r.setUserId(userId);
        r.setRating(rating);
        r.setTitle(title);
        r.setBody(body);
        r.setVerifiedPurchase(false);
        reviewDAO.add(r);
        resp.sendRedirect(req.getContextPath() + "/product?id=" + productId);
    }

    private String resolveUserId(HttpSession session) {
        if (session == null) {
            throw new IllegalStateException("No HTTP session for review operation");
        }
        User user = (User) session.getAttribute("currentUser");
        if (user == null || user.getId() == null) {
            throw new IllegalStateException("No authenticated user in session for review");
        }
        return user.getId();
    }
}
