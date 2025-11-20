package com.ecommerce.servlet;

import com.ecommerce.dao.UserDAO;
import com.ecommerce.dao.AddressDAO;
import com.ecommerce.dao.AddressDAO.Address;
import com.ecommerce.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/account")
public class AccountServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();
    private final AddressDAO addressDAO = new AddressDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = session != null ? (User) session.getAttribute("currentUser") : null;
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        List<Address> addresses = addressDAO.listByUser(user.getId());
        req.setAttribute("addresses", addresses);
        req.getRequestDispatcher("/account.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = session != null ? (User) session.getAttribute("currentUser") : null;
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String action = req.getParameter("action");
        if (action == null) {
            resp.sendRedirect(req.getContextPath() + "/account");
            return;
        }

        List<String> errors = new ArrayList<>();

        switch (action) {
            case "updateProfile" -> handleUpdateProfile(req, user, session, errors);
            case "addAddress" -> handleAddAddress(req, user, errors);
            case "editAddress" -> handleEditAddress(req, user, errors);
            case "deleteAddress" -> handleDeleteAddress(req, user, errors);
            case "setDefaultShipping" -> handleSetDefaultShipping(req, user);
            case "setDefaultBilling" -> handleSetDefaultBilling(req, user);
            default -> {}
        }

        if (!errors.isEmpty()) {
            session.setAttribute("accountError", String.join("; ", errors));
        } else {
            if (!"deleteAddress".equals(action) && !"setDefaultShipping".equals(action) && !"setDefaultBilling".equals(action)) {
                session.setAttribute("accountSuccess", "Changes saved successfully.");
            }
        }
        resp.sendRedirect(req.getContextPath() + "/account");
    }

    private void handleUpdateProfile(HttpServletRequest req, User user, HttpSession session, List<String> errors) {
        String name = safe(req.getParameter("name"));
        String phone = safe(req.getParameter("phone"));
        if (name == null || name.isBlank()) {
            errors.add("Name is required");
            return;
        }
        if (name.length() > 100) errors.add("Name exceeds 100 characters");
        if (phone != null && phone.length() > 30) errors.add("Phone exceeds 30 characters");
        if (errors.isEmpty()) {
            userDAO.updateProfile(user.getId(), name, phone);
            user.setName(name);
            user.setPhone(phone);
            session.setAttribute("currentUser", user);
            session.setAttribute("currentUserEmail", user.getEmail());
        }
    }

    private void handleAddAddress(HttpServletRequest req, User user, List<String> errors) {
        Address a = buildAddressFromRequest(req, errors);
        if (!errors.isEmpty()) return;
        addressDAO.create(user.getId(), a);
        int latestId = lastInsertedId(user.getId());
        if (a.defaultShipping && latestId > 0) addressDAO.setDefaultShipping(user.getId(), latestId);
        if (a.defaultBilling && latestId > 0) addressDAO.setDefaultBilling(user.getId(), latestId);
    }

    private void handleEditAddress(HttpServletRequest req, User user, List<String> errors) {
        String idStr = req.getParameter("address_id");
        if (idStr == null) { errors.add("Missing address id"); return; }
        int id = parseId(idStr, errors);
        if (!errors.isEmpty()) return;
        Address a = buildAddressFromRequest(req, errors);
        if (!errors.isEmpty()) return;
        addressDAO.updateForUser(id, user.getId(), a);
    }

    private void handleDeleteAddress(HttpServletRequest req, User user, List<String> errors) {
        String idStr = req.getParameter("address_id");
        if (idStr == null) { errors.add("Missing address id"); return; }
        int id = parseId(idStr, errors);
        if (!errors.isEmpty()) return;
        addressDAO.deleteForUser(id, user.getId());
    }

    private void handleSetDefaultShipping(HttpServletRequest req, User user) {
        String idStr = req.getParameter("address_id");
        if (idStr == null) return;
        int id = Integer.parseInt(idStr);
        addressDAO.setDefaultShipping(user.getId(), id);
    }

    private void handleSetDefaultBilling(HttpServletRequest req, User user) {
        String idStr = req.getParameter("address_id");
        if (idStr == null) return;
        int id = Integer.parseInt(idStr);
        addressDAO.setDefaultBilling(user.getId(), id);
    }

    private Address buildAddressFromRequest(HttpServletRequest req, List<String> errors) {
        Address a = new Address();
        a.name = truncate(safe(req.getParameter("address_name")), 100, "Address name", errors);
        a.phone = truncate(safe(req.getParameter("address_phone")), 30, "Phone", errors);
        a.address1 = requireAndTruncate(safe(req.getParameter("address1")), 200, "Address line 1", errors);
        a.address2 = truncate(safe(req.getParameter("address2")), 200, "Address line 2", errors);
        a.city = requireAndTruncate(safe(req.getParameter("city")), 100, "City", errors);
        a.region = truncate(safe(req.getParameter("region")), 100, "Region", errors);
        a.postalCode = requireAndTruncate(safe(req.getParameter("postal_code")), 20, "Postal code", errors);
        String cc = requireAndTruncate(safe(req.getParameter("country_code")), 2, "Country code", errors);
        if (cc != null && cc.length() != 2) errors.add("Country code must be exactly 2 letters");
        a.countryCode = cc != null ? cc.toUpperCase() : null;
        a.defaultShipping = "on".equals(req.getParameter("default_shipping"));
        a.defaultBilling = "on".equals(req.getParameter("default_billing"));
        return a;
    }

    private String safe(String v) { return v == null ? null : v.trim(); }
    private String truncate(String v, int max, String label, List<String> errors) {
        if (v == null || v.isBlank()) return v; // optional
        if (v.length() > max) errors.add(label + " exceeds " + max + " chars");
        return v;
    }
    private String requireAndTruncate(String v, int max, String label, List<String> errors) {
        if (v == null || v.isBlank()) { errors.add(label + " is required"); return null; }
        if (v.length() > max) errors.add(label + " exceeds " + max + " chars");
        return v;
    }
    private int parseId(String raw, List<String> errors) {
        try { return Integer.parseInt(raw); } catch (NumberFormatException e) { errors.add("Invalid id"); return -1; }
    }

    private int lastInsertedId(String userId) {
        List<Address> list = addressDAO.listByUser(userId);
        return list.isEmpty() ? 0 : list.get(0).id;
    }
}
