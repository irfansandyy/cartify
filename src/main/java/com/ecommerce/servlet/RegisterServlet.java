package com.ecommerce.servlet;

import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.IOException;

import com.ecommerce.dao.UserDAO;
import com.ecommerce.model.User;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String confirm = req.getParameter("confirmPassword");
        String phone = req.getParameter("phone");

        if (name == null || name.isBlank() || email == null || email.isBlank() ||
                password == null || password.isBlank()) {
            req.setAttribute("error", "Please fill in all required fields.");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }
        if (!password.equals(confirm)) {
            req.setAttribute("error", "Passwords do not match.");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }

        if (userDAO.findByEmail(email) != null) {
            req.setAttribute("error", "Email is already registered.");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }

        User user = userDAO.create(name, email, password, phone);
        if (user == null) {
            req.setAttribute("error", "Could not create account. Please try again.");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }

    HttpSession session = req.getSession(true);
    session.setAttribute("currentUser", user);
    session.setAttribute("currentUserEmail", user.getEmail());
    AuthFilter.createSessionToken(user, req, resp);
        resp.sendRedirect(req.getContextPath() + "/products");
    }
}

