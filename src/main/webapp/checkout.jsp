<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<html>
<head>
    <title>Checkout - Cartify</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css" />
</head>
<body class="font-body bg-surface">
<jsp:include page="partials/header.jsp" />
<main class="container mx-auto px-6 py-8">
    <section class="max-w-5xl mx-auto grid gap-6 md:grid-cols-[2fr,1.5fr]">
        <div class="card rounded-xl space-y-4">
            <h1 class="font-header text-primary text-xl">Shipping & payment</h1>
            <form method="post" action="${pageContext.request.contextPath}/checkout" class="space-y-4 text-sm">
                <div class="space-y-1">
                    <label class="text-muted block">Full name</label>
                    <input type="text" name="name" required class="w-full border border-(--color-border) rounded-md px-3 py-2" />
                </div>
                <div class="space-y-1">
                    <label class="text-muted block">Address</label>
                    <input type="text" name="address1" required class="w-full border border-(--color-border) rounded-md px-3 py-2" />
                </div>
                <div class="grid grid-cols-2 gap-3">
                    <div class="space-y-1">
                        <label class="text-muted block">City</label>
                        <input type="text" name="city" required class="w-full border border-(--color-border) rounded-md px-3 py-2" />
                    </div>
                    <div class="space-y-1">
                        <label class="text-muted block">Postal code</label>
                        <input type="text" name="postal_code" required class="w-full border border-(--color-border) rounded-md px-3 py-2" />
                    </div>
                </div>
                <div class="space-y-1">
                    <label class="text-muted block">Country</label>
                    <input type="text" name="country_code" required class="w-full border border-(--color-border) rounded-md px-3 py-2" />
                </div>
                <div class="space-y-2">
                    <span class="text-muted block">Payment method</span>
                    <label class="flex items-center gap-2 text-sm">
                        <input type="radio" name="payment_method" value="card" checked />
                        <span>Credit / debit card (test)</span>
                    </label>
                    <label class="flex items-center gap-2 text-sm">
                        <input type="radio" name="payment_method" value="cod" />
                        <span>Cash on delivery</span>
                    </label>
                </div>
                <button type="submit" class="inline-flex items-center justify-center rounded-lg bg-primary text-white px-5 py-2 text-sm mt-2">Place order</button>
            </form>
        </div>

        <aside class="card rounded-xl space-y-3">
            <h2 class="font-header text-primary text-lg">Order summary</h2>
            <c:if test="${empty cartItems}">
                <p class="text-muted text-sm">Your cart is empty.</p>
            </c:if>
            <c:if test="${not empty cartItems}">
                <div class="space-y-2 max-h-64 overflow-auto pr-1">
                    <c:forEach items="${cartItems}" var="item" varStatus="loop">
                        <c:set var="p" value="${cartProducts[loop.index]}" />
                        <div class="flex items-center justify-between text-xs">
                            <div class="flex-1 mr-2">
                                <p class="text-default line-clamp-1">${p.name}</p>
                                <p class="text-muted">Qty: ${item.quantity}</p>
                            </div>
                            <p class="text-default font-medium">$${p.price * item.quantity}</p>
                        </div>
                    </c:forEach>
                </div>
                <div class="border-t border-(--color-border) pt-3 space-y-1 text-sm">
                    <div class="flex items-center justify-between">
                        <span class="text-muted">Subtotal</span>
                        <span class="text-default">$${subtotal}</span>
                    </div>
                    <div class="flex items-center justify-between">
                        <span class="text-muted">Shipping</span>
                        <span class="text-default">$${shipping}</span>
                    </div>
                    <div class="flex items-center justify-between">
                        <span class="text-muted">Tax</span>
                        <span class="text-default">$${tax}</span>
                    </div>
                    <div class="flex items-center justify-between pt-2">
                        <span class="font-header text-primary">Total</span>
                        <span class="font-header text-primary">$${subtotal + shipping + tax}</span>
                    </div>
                </div>
            </c:if>
        </aside>
    </section>
</main>
<jsp:include page="partials/footer.jsp" />
</body>
</html>
