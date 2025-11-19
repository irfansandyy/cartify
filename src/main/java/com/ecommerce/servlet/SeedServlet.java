package com.ecommerce.servlet;

import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

import com.ecommerce.seed.Seeder;

@WebServlet("/seed")
public class SeedServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Seeder seeder = new Seeder();
        Seeder.Result result = seeder.run();

        resp.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html lang=\"en\">");
            out.println("<head><meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                    + "<title>Seeder</title>"
                    + "<link rel=\"stylesheet\" href=\"" + req.getContextPath() + "/assets/css/style.css\"></head>");
            out.println("<body>");
            out.println("<div class=\"container\" style=\"max-width:800px;margin:2rem auto;\">");
            out.println("<div class=\"card\" style=\"padding:1.25rem;\">");
            out.println("<h1 class=\"font-header text-primary\">Database Seeder</h1>");
            out.printf("<p class=\"text-muted\">%s</p>", escape(result.message));
            if (!result.skipped) {
                out.printf("<p>Categories present: <strong>%d</strong></p>", result.categoriesInserted);
                out.printf("<p>Products inserted/updated: <strong>%d</strong></p>", result.productsInserted);
            }
            out.println("<div style=\"margin-top:1rem;display:flex;gap:.5rem;\">");
            out.println("<a class=\"btn btn-primary\" href=\"" + req.getContextPath() + "/products\">Go to Products</a>");
            out.println("<a class=\"btn btn-secondary\" href=\"" + req.getContextPath() + "/seed\">Run Again</a>");
            out.println("</div>");
            out.println("</div></div>");
            out.println("</body></html>");
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
