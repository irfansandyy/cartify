<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<html>
<head>
    <title>Your Orders - Cartify</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css" />
</head>
<body class="font-body bg-surface">
<jsp:include page="partials/header.jsp" />
<main class="container mx-auto px-6 py-8">
    <section class="max-w-5xl mx-auto space-y-6">
        <div class="flex items-center justify-between">
            <h1 class="font-header text-primary text-xl">Your Orders</h1>
        </div>
        <c:if test="${empty orders}">
            <p class="text-muted text-sm">You have no orders yet.</p>
        </c:if>
        <c:forEach items="${orders}" var="o" varStatus="loop">
            <div class="card rounded-xl space-y-3 ${placedId != null && placedId == o.id ? 'border-green-300 bg-green-50' : ''}">
                <div class="flex items-center justify-between">
                    <div class="space-y-1">
                        <h2 class="text-sm font-semibold text-primary">Order ${o.orderNumber}</h2>
                        <p class="text-[11px] text-muted">Placed: ${o.createdAt}</p>
                    </div>
                    <div class="text-xs font-medium ${o.status == 'completed' ? 'text-green-700' : (o.status == 'cancelled' ? 'text-red-700' : 'text-black')}">
                        <c:choose>
                            <c:when test="${o.status == 'completed'}">Delivered</c:when>
                            <c:when test="${o.status == 'paid'}">Processing</c:when>
                            <c:when test="${o.status == 'shipped'}">In transit</c:when>
                            <c:when test="${o.status == 'cancelled'}">Cancelled</c:when>
                            <c:otherwise>${o.status}</c:otherwise>
                        </c:choose>
                    </div>
                </div>
                <div class="space-y-2">
                    <c:set var="items" value="${orderItemsMap[o.id]}" />
                    <c:forEach items="${items}" var="it">
                        <div class="flex items-center justify-between text-xs border-b border-(--color-border) py-1">
                            <div class="mr-2 flex-1">
                                <p class="text-default line-clamp-1">${it.productName}</p>
                                <p class="text-muted">Qty: ${it.quantity}</p>
                            </div>
                            <p class="text-default font-medium">$${it.priceAtPurchase * it.quantity}</p>
                        </div>
                    </c:forEach>
                </div>
                <div class="flex items-center justify-between pt-1">
                    <span class="text-sm text-muted">Total</span>
                    <span class="font-header text-primary text-sm">$${o.totalPrice}</span>
                </div>
            </div>
        </c:forEach>
    </section>
</main>
<jsp:include page="partials/footer.jsp" />
</body>
</html>