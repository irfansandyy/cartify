package com.ecommerce.servlet;

import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

import com.ecommerce.dao.ProductDAO;
import com.ecommerce.dao.ReviewDAO;
import com.ecommerce.model.Product;
import com.ecommerce.model.Review;

@WebServlet("/product")
public class ProductDetailServlet extends HttpServlet {
    private final ProductDAO productDAO = new ProductDAO();
    private final ReviewDAO reviewDAO = new ReviewDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idStr = req.getParameter("id");
        if (idStr == null) {
            resp.sendRedirect(req.getContextPath() + "/products");
            return;
        }
        int id = Integer.parseInt(idStr);
        Product product = productDAO.findById(id);
        if (product == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        List<Review> reviews = reviewDAO.listForProduct(id);
            req.setAttribute("product", product);
            req.setAttribute("reviews", reviews);
            req.getRequestDispatcher("/product.jsp").forward(req, resp);
    }
}
