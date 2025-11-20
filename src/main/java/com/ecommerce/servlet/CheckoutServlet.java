package com.ecommerce.servlet;

import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import com.ecommerce.dao.CartDAO;
import com.ecommerce.dao.AddressDAO;
import com.ecommerce.dao.OrderDAO;
import com.ecommerce.dao.ProductDAO;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;

@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {
    private final CartDAO cartDAO = new CartDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final AddressDAO addressDAO = new AddressDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (User) (session != null ? session.getAttribute("currentUser") : null);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String userId = user.getId();
        List<CartItem> items = cartDAO.listByUser(userId);
        List<Product> products = items.stream().map(i -> productDAO.findById(i.getProductId())).collect(Collectors.toList());

        BigDecimal subtotal = BigDecimal.ZERO;
        for (int i = 0; i < items.size(); i++) {
            CartItem ci = items.get(i);
            Product p = products.get(i);
            if (p != null && p.getPrice() != null) {
                subtotal = subtotal.add(p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
            }
        }

        req.setAttribute("cartItems", items);
        req.setAttribute("cartProducts", products);
        req.setAttribute("subtotal", subtotal);
        req.setAttribute("shipping", new BigDecimal("0.00"));
        req.setAttribute("tax", new BigDecimal("0.00"));

        // Load saved addresses and prefill with default shipping if available
        var addresses = addressDAO.listByUser(userId);
        req.setAttribute("addresses", addresses);
        AddressDAO.Address prefill = null;
        for (var a : addresses) {
            if (a.isDefaultShipping()) { prefill = a; break; }
        }
        if (prefill == null && !addresses.isEmpty()) {
            prefill = addresses.get(0);
        }
        if (prefill != null) {
            req.setAttribute("shipName", prefill.getName());
            req.setAttribute("shipAddress1", prefill.getAddress1());
            req.setAttribute("shipCity", prefill.getCity());
            req.setAttribute("shipPostal", prefill.getPostalCode());
            req.setAttribute("shipCountry", prefill.getCountryCode());
        }
        req.getRequestDispatcher("/checkout.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (User) (session != null ? session.getAttribute("currentUser") : null);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String userId = user.getId();
        List<CartItem> items = cartDAO.listByUser(userId);
        if (items.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/cart");
            return;
        }

    req.getParameter("name");
    req.getParameter("address1");
    req.getParameter("city");
    req.getParameter("postal_code");
    req.getParameter("country_code");
        String paymentMethod = req.getParameter("payment_method");
        if (paymentMethod == null || paymentMethod.isBlank()) {
            paymentMethod = "cod";
        }
        // Normalize to DB enum values
        String pm = paymentMethod.toLowerCase();
        if (pm.equals("card") || pm.equals("credit") || pm.equals("credit-card") || pm.equals("credit_card")) {
            paymentMethod = "credit_card";
        } else if (pm.equals("bank") || pm.equals("transfer") || pm.equals("bank-transfer") || pm.equals("bank_transfer")) {
            paymentMethod = "bank_transfer";
        } else if (pm.equals("wallet")) {
            paymentMethod = "wallet";
        } else if (pm.equals("cod") || pm.equals("cash") || pm.equals("cash_on_delivery") || pm.equals("cash-on-delivery")) {
            paymentMethod = "cod";
        } else {
            paymentMethod = "cod";
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem ci : items) {
            Product p = productDAO.findById(ci.getProductId());
            if (p != null && p.getPrice() != null) {
                subtotal = subtotal.add(p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
                if (ci.getPriceAtAdd() == null) {
                    ci.setPriceAtAdd(p.getPrice());
                }
            }
        }

        BigDecimal shipping = new BigDecimal("0.00");
        BigDecimal tax = new BigDecimal("0.00");

        int orderId = orderDAO.createOrderWithItems(userId, items, subtotal, shipping, tax, "USD", paymentMethod);

        cartDAO.clearForUser(userId);

        resp.sendRedirect(req.getContextPath() + "/orders?placed=" + orderId);
    }
}
