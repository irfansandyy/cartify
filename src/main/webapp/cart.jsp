<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<html>
<head>
    <title>Your Cart - Cartify</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css" />
</head>
<body class="font-body bg-surface">
<jsp:include page="partials/header.jsp" />
<main class="container mx-auto px-6 py-8">
    <section class="max-w-4xl mx-auto space-y-4">
        <div class="flex items-center justify-between">
            <h1 class="font-header text-primary text-xl">Shopping Cart</h1>
        </div>
        <c:if test="${empty cartItems}"><p class="text-muted text-sm">Your cart is empty.</p></c:if>
        <c:if test="${not empty cartItems}">
            <div class="overflow-hidden rounded-xl border border-(--color-border) bg-surface shadow-sm">
                <table class="w-full border-collapse text-sm">
                    <thead class="bg-surface-alt">
                    <tr class="text-left border-b border-(--color-border)">
                        <th class="px-4 py-3">Product</th>
                        <th class="px-4 py-3">Price</th>
                        <th class="px-4 py-3">Qty</th>
                        <th class="px-4 py-3">Subtotal</th>
                        <th class="px-4 py-3"></th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:set var="grand" value="0" scope="page" />
                    <c:forEach items="${cartItems}" var="item" varStatus="loop">
                        <c:set var="p" value="${cartProducts[loop.index]}" />
                        <tr class="border-b border-(--color-border) hover:bg-surface-alt/70 transition-colors">
                            <td class="px-4 py-3">${p.name}</td>
                            <td class="px-4 py-3">$${p.price}</td>
                            <td class="px-4 py-3">
                                <form method="post" action="${pageContext.request.contextPath}/cart" class="inline-flex items-center gap-2">
                                    <input type="hidden" name="action" value="update" />
                                    <input type="hidden" name="id" value="${item.id}" />
                                    <input type="number" name="qty" value="${item.quantity}" min="1" class="w-16 border border-(--color-border) rounded-lg px-2 py-1 text-sm" />
                                    <button class="btn btn-primary rounded-lg px-3 py-1 text-xs transition-transform hover:-translate-y-0.5" type="submit">Update</button>
                                </form>
                            </td>
                            <td class="px-4 py-3">
                                <c:set var="rowTotal" value="${p.price * item.quantity}" />
                                $${rowTotal}
                                <c:set var="grand" value="${grand + rowTotal}" />
                            </td>
                            <td class="px-4 py-3">
                                <form method="post" action="${pageContext.request.contextPath}/cart">
                                    <input type="hidden" name="action" value="remove" />
                                    <input type="hidden" name="id" value="${item.id}" />
                                    <button class="btn btn-secondary rounded-lg px-3 py-1 text-xs transition-transform hover:-translate-y-0.5" type="submit" style="color: #000;">Remove</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
            <div class="mt-4 flex justify-end">
                <strong class="text-primary text-base">Total: $${grand}</strong>
            </div>
            <div class="mt-6 flex justify-end">
                <a href="${pageContext.request.contextPath}/checkout" class="btn btn-primary rounded-lg px-5 py-2 text-sm inline-flex items-center gap-2 hover:-translate-y-0.5 transition-transform">
                    <span>Proceed to checkout</span>
                </a>
            </div>
        </c:if>
    </section>
</main>
<jsp:include page="partials/footer.jsp" />
</body>
</html>
