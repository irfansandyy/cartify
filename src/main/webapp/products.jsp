<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<html>
<head>
    <title>Products - Cartify</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css" />
</head>
<body class="font-body bg-surface">
<jsp:include page="partials/header.jsp" />

<section class="hero">
    <div class="container mx-auto flex flex-col gap-2 px-6">
        <h1 class="font-header text-primary text-2xl tracking-tight">Welcome to Cartify</h1>
        <p class="text-muted text-sm">Browse our latest products and add your favorites to the cart or wishlist.</p>
    </div>
</section>

<main class="container mx-auto px-6 py-8 space-y-4">
    <div class="card card-alt flex items-center gap-3 rounded-lg shadow-sm md:hidden">
        <form method="get" action="${pageContext.request.contextPath}/products" class="flex w-full items-center gap-3">
            <input type="text" name="q" placeholder="Search products..." value="${fn:escapeXml(param.q)}"
                   class="flex-1 border border-(--color-border) rounded-lg px-3 py-2 text-sm outline-none focus:border-secondary focus:ring-2 focus:ring-secondary/10 transition" />
            <button class="btn btn-secondary rounded-lg px-4 py-2 text-sm transition-transform hover:-translate-y-0.5" type="submit">Search</button>
        </form>
    </div>

    <c:choose>
        <c:when test="${empty products and param.seed eq 'true'}">
            <div class="card shadow-md rounded-xl text-center py-10 space-y-3">
                <h2 class="font-header text-primary text-xl">No products yet</h2>
                <p class="text-muted text-sm">Seed the database with sample items.</p>
                <a class="btn btn-primary rounded-full px-6 py-2 text-sm inline-flex items-center justify-center gap-2 hover:-translate-y-0.5 transition-transform" href="${pageContext.request.contextPath}/seed">Run Seeder</a>
            </div>
        </c:when>
        <c:when test="${empty products}">
            <div class="card shadow-md rounded-xl text-center py-10 space-y-3">
                <h2 class="font-header text-primary text-xl">No products found</h2>
                <p class="text-muted text-sm">Try adjusting your search or reset the query.</p>
                <a class="btn btn-primary rounded-full px-6 py-2 text-sm inline-flex items-center justify-center gap-2 hover:-translate-y-0.5 transition-transform" href="${pageContext.request.contextPath}/products">Reset Query</a>
            </div>
        </c:when>
    </c:choose>

    <div class="grid gap-5 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
        <c:forEach items="${products}" var="p">
            <article class="card shadow-sm rounded-xl overflow-hidden flex flex-col hover:-translate-y-1 hover:shadow-md transition-transform duration-150">
                <a href="${pageContext.request.contextPath}/product?id=${p.id}" class="flex flex-col flex-1">
                    <div class="h-44 overflow-hidden bg-surface-alt">
                        <c:if test="${not empty p.imageUrl}">
                            <img src="${p.imageUrl}" alt="${p.name}" class="w-full h-full object-cover" />
                        </c:if>
                    </div>
                    <div class="p-4 flex flex-col gap-2">
                        <div class="flex items-center justify-between mb-1">
                            <div class="flex flex-wrap gap-1">
                                <c:if test="${not empty p.categoryName}">
                                    <span class="inline-flex items-center rounded-full bg-accent px-2 py-0.5 text-[10px] font-medium text-muted uppercase tracking-wide">${p.categoryName}</span>
                                </c:if>
                            </div>
                            <div class="flex items-center gap-1 text-[11px]">
                                <c:choose>
                                    <c:when test="${not empty p.reviewCount and p.reviewCount gt 0}">
                                        <span class="text-[10px] text-muted">${fn:substringBefore(p.averageRating + 0.05, '.')}.0</span>
                                        <span class="text-[11px]" aria-hidden="true">&#9733;</span>
                                        <span class="text-[10px] text-muted">(${p.reviewCount})</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="text-[10px] text-muted">No reviews</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                        <h3 class="font-header text-sm text-primary line-clamp-2">${p.name}</h3>
                        <p class="text-muted text-xs leading-snug">
                            <c:choose>
                                <c:when test="${fn:length(p.description) > 80}">
                                    ${fn:substring(p.description,0,77)}...
                                </c:when>
                                <c:otherwise>
                                    ${p.description}
                                </c:otherwise>
                            </c:choose>
                        </p>
                        <strong class="price mt-1">$${p.price}</strong>
                    </div>
                </a>
                <div class="px-4 pb-4 pt-0">
                    <c:choose>
                        <c:when test="${p.stock le 0}">
                            <button class="w-full text-xs rounded-lg px-4 py-2 bg-secondary text-muted cursor-not-allowed opacity-70">Out of stock</button>
                        </c:when>
                        <c:otherwise>
                            <div class="flex items-center gap-2">
                                <button data-product="${p.id}" class="add-cart-grid inline-flex items-center justify-center gap-2 rounded-lg flex-1 text-xs bg-primary text-white px-4 py-2 transition-transform hover:-translate-y-0.5 hover:bg-primary-hover" type="button">
                                    <svg class="h-3.5 w-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                        <circle cx="9" cy="21" r="1"></circle>
                                        <circle cx="20" cy="21" r="1"></circle>
                                        <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
                                    </svg>
                                    <span>Add to cart</span>
                                </button>
                                <button data-product="${p.id}" class="wishlist-grid inline-flex items-center justify-center rounded-lg bg-surface-alt px-3 py-2 hover:bg-accent" type="button"
                                    onmouseover="this.querySelector('img').src='${pageContext.request.contextPath}/assets/icons/heart-solid-full.svg'"
                                    onmouseout="this.querySelector('img').dataset.state!=='added' && (this.querySelector('img').src='${pageContext.request.contextPath}/assets/icons/heart-regular-full.svg')">
                                    <c:choose>
                                        <c:when test="${not empty wishlistMap and wishlistMap[p.id]}">
                                            <img src="${pageContext.request.contextPath}/assets/icons/heart-solid-full.svg" alt="Wishlist" class="h-3.5 w-3.5" data-state="added" />
                                        </c:when>
                                        <c:otherwise>
                                            <img src="${pageContext.request.contextPath}/assets/icons/heart-regular-full.svg" alt="Wishlist" class="h-3.5 w-3.5" />
                                        </c:otherwise>
                                    </c:choose>
                                </button>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </article>
        </c:forEach>
    </div>
</main>
<jsp:include page="partials/footer.jsp" />
<script>
    const ctx = '${pageContext.request.contextPath}';
    document.querySelectorAll('.add-cart-grid').forEach(btn => {
        const originalHtml = btn.innerHTML;
        btn.addEventListener('click', async () => {
            if (btn.dataset.state === 'added') return;
            const pid = btn.getAttribute('data-product');
            try {
                const res = await fetch(ctx + '/cart-add', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: 'productId=' + encodeURIComponent(pid) + '&qty=1'
                });
                if (res.ok) {
                    btn.dataset.state = 'added';
                    btn.innerHTML = '<svg class="h-3.5 w-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12l5 5L20 7" /></svg><span>Added</span>';
                    const badge = document.getElementById('nav-cart-count');
                    if (badge) {
                        const current = parseInt(badge.textContent || '0');
                        badge.textContent = current + 1;
                        badge.style.transform = 'scale(1.2)';
                        setTimeout(()=> badge.style.transform = '', 300);
                    }
                    setTimeout(() => { window.location.reload(); }, 350);
                }
            } catch(e) { console.error('Add to cart failed', e); }
        });
    });

    document.querySelectorAll('.wishlist-grid').forEach(btn => {
        btn.addEventListener('click', async () => {
            if (btn.dataset.state === 'added') return;
            const pid = btn.getAttribute('data-product');
            try {
                const res = await fetch(ctx + '/wishlist-add', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: 'productId=' + encodeURIComponent(pid)
                });
                if (res.ok) {
                    btn.dataset.state = 'added';
                    const img = btn.querySelector('img');
                    img.src = ctx + '/assets/icons/heart-solid-full.svg';
                    img.dataset.state = 'added';
                    btn.style.transform = 'scale(1.05)';
                    setTimeout(()=> btn.style.transform = '', 200);
                }
            } catch(e) { console.error('Wishlist add failed', e); }
        });
    });
</script>
</body>
</html>
