package com.ecommerce.servlet;

import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.IOException;

import com.ecommerce.dao.UserDAO;
import com.ecommerce.model.User;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            req.setAttribute("error", "Please enter email and password.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        User user = userDAO.findByEmail(email);
        if (user == null || !userDAO.verifyPassword(password, user.getPasswordHash())) {
            req.setAttribute("error", "Invalid email or password.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

    HttpSession session = req.getSession(true);
    session.setAttribute("currentUser", user);
    session.setAttribute("currentUserEmail", user.getEmail());

    AuthFilter.createSessionToken(user, req, resp);

        resp.sendRedirect(req.getContextPath() + "/products");
    }
}
