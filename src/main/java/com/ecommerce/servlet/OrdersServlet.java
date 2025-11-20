package com.ecommerce.servlet;

import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

import com.ecommerce.dao.OrderDAO;
import com.ecommerce.model.User;

@WebServlet("/orders")
public class OrdersServlet extends HttpServlet {
    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = session != null ? (User) session.getAttribute("currentUser") : null;
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        var orders = orderDAO.listByUser(user.getId());
        Map<Integer, List<OrderDAO.OrderItemRow>> itemsMap = new HashMap<>();
        for (var o : orders) {
            itemsMap.put(o.getId(), orderDAO.listItemsWithProducts(o.getId()));
        }

        String placedParam = req.getParameter("placed");
        Integer placedId = null;
        if (placedParam != null) {
            try { placedId = Integer.valueOf(placedParam); } catch (NumberFormatException ignored) {}
        }

        req.setAttribute("orders", orders);
        req.setAttribute("orderItemsMap", itemsMap);
        req.setAttribute("placedId", placedId);
        req.getRequestDispatcher("/orders.jsp").forward(req, resp);
    }
}