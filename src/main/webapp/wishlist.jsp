<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html>
<head>
    <title>Your Wishlist - Cartify</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css" />
</head>
<body class="font-body bg-surface">
<jsp:include page="partials/header.jsp" />
<main class="container mx-auto px-6 py-8">
    <section class="max-w-5xl mx-auto space-y-4">
        <div class="flex items-center justify-between">
            <h1 class="font-header text-primary text-xl">Wishlist</h1>
        </div>
        <c:if test="${empty wishlistItems}"><p class="text-muted text-sm">No items yet.</p></c:if>
        <div class="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
            <c:forEach items="${wishlistProducts}" var="p" varStatus="loop">
                <article class="card rounded-xl shadow-sm flex flex-col gap-2 hover:-translate-y-1 hover:shadow-md transition-transform duration-150">
                    <h3 class="font-header text-sm text-primary">${p.name}</h3>
                    <p class="text-muted text-xs">$${p.price}</p>
                    <div class="mt-1 flex items-center gap-2">
                        <form method="post" action="${pageContext.request.contextPath}/wishlist" class="inline">
                            <input type="hidden" name="action" value="remove" />
                            <input type="hidden" name="id" value="${wishlistItems[loop.index].id}" />
                            <button class="btn btn-accent rounded-lg px-3 py-1.5 text-xs transition-transform hover:-translate-y-0.5" type="submit">Remove</button>
                        </form>
                        <a class="btn btn-secondary rounded-lg px-3 py-1.5 text-xs transition-transform hover:-translate-y-0.5" href="${pageContext.request.contextPath}/product?id=${p.id}">View</a>
                    </div>
                </article>
            </c:forEach>
        </div>
    </section>
</main>
<jsp:include page="partials/footer.jsp" />
</body>
</html>
