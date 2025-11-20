<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html>
<head>
    <title>${product.name} - Cartify</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css" />
</head>
<body class="font-body bg-surface">
<jsp:include page="partials/header.jsp" />
<main class="container mx-auto px-6 py-8">
    <section class="max-w-5xl mx-auto space-y-6">
        <div class="card rounded-xl flex flex-col md:flex-row gap-6">
            <div class="flex-1">
                <c:if test="${not empty product.imageUrl}">
                    <img src="${product.imageUrl}" alt="${product.name}" class="w-full max-h-96 object-cover rounded-lg" />
                </c:if>
            </div>
            <div class="flex-1 flex flex-col gap-3">
                <h1 class="font-header text-primary text-2xl">${product.name}</h1>
                <p class="text-muted text-sm">${product.description}</p>
                <strong class="price text-xl">$${product.price}</strong>
                <div class="flex items-center gap-3 mt-2" id="cart-add-wrap">
                    <input type="number" id="cart-qty" value="1" min="1" class="w-20 border border-(--color-border) rounded-md px-2 py-1 text-sm" />
                    <button id="add-cart-btn" class="inline-flex items-center justify-center rounded-md text-white hover:bg-gray-100 px-3 py-2" type="button" aria-label="Add to cart">
                        <img src="${pageContext.request.contextPath}/assets/icons/shopping-cart-black.svg" alt="Wishlist" class="h-4 w-4" />
                    </button>
                </div>
                <c:if test="${not empty product.categoryName}">
                    <span class="text-[11px] text-muted uppercase tracking-wide">${product.categoryName}</span>
                </c:if>
                <div class="mt-2">
                    <button id="wishlist-btn" data-product="${product.id}" class="inline-flex items-center justify-center rounded-md bg-surface-alt px-3 py-2 hover:bg-accent"
                            type="button"
                            onmouseover="this.querySelector('img').src='${pageContext.request.contextPath}/assets/icons/heart-solid-full.svg'"
                            onmouseout="this.querySelector('img').dataset.state!=='added' && (this.querySelector('img').src='${pageContext.request.contextPath}/assets/icons/heart-regular-full.svg')">
                        <c:choose>
                            <c:when test="${not empty wishlistMap and wishlistMap[product.id]}">
                                <img src="${pageContext.request.contextPath}/assets/icons/heart-solid-full.svg" alt="Wishlist" class="h-4 w-4" data-state="added" />
                            </c:when>
                            <c:otherwise>
                                <img src="${pageContext.request.contextPath}/assets/icons/heart-regular-full.svg" alt="Wishlist" class="h-4 w-4" />
                            </c:otherwise>
                        </c:choose>
                    </button>
                </div>
            </div>
        </div>

        <div class="card card-alt rounded-xl space-y-4">
            <div class="flex items-center justify-between">
                <h2 class="font-header text-primary text-lg">Reviews</h2>
                <span class="text-muted text-xs">Share your experience</span>
            </div>
            <c:if test="${empty reviews}"><p class="text-muted text-sm">No reviews yet. Be the first to review this product.</p></c:if>
            <c:forEach items="${reviews}" var="r">
                <div class="border-t border-(--color-border) py-3 space-y-1">
                    <div class="flex items-center justify-between">
                        <div class="flex items-center gap-2">
                            <div class="flex items-center gap-0.5 text-xs">
                                <c:forEach begin="1" end="5" var="i">
                                    <c:choose>
                                        <c:when test="${i le r.rating}">
                                            <span class="text-[13px]" aria-hidden="true">&#9733;</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-[13px] text-muted" aria-hidden="true">&#9734;</span>
                                        </c:otherwise>
                                    </c:choose>
                                </c:forEach>
                            </div>
                            <span class="text-default text-sm font-medium">${r.title}</span>
                        </div>
                        <span class="text-muted text-xs">
                            <c:out value="${r.userEmail}" />
                        </span>
                    </div>
                    <p class="text-default text-sm leading-snug">${r.body}</p>
                    <c:if test="${sessionScope.currentUser != null and sessionScope.currentUser.id == r.userId}">
                        <div class="flex items-center gap-2 mt-2">
                            <button type="button" class="btn btn-primary px-2 py-1 text-[10px] rounded" onclick="toggleEditReview('${r.id}')">Edit</button>
                            <form method="post" action="${pageContext.request.contextPath}/review" onsubmit="return confirm('Delete your review?');" class="inline">
                                <input type="hidden" name="action" value="delete" />
                                <input type="hidden" name="productId" value="${product.id}" />
                                <button type="submit" class="btn btn-secondary px-2 py-1 text-[10px] rounded" style="color: black;">Delete</button>
                            </form>
                        </div>
                        <form id="edit-review-${r.id}" method="post" action="${pageContext.request.contextPath}/review" style="display:none;" class="mt-3 space-y-2 text-xs">
                            <input type="hidden" name="action" value="edit" />
                            <input type="hidden" name="productId" value="${product.id}" />
                            <label class="text-muted">Rating</label>
                            <input type="number" name="rating" value="${r.rating}" min="1" max="5" class="w-20 border border-(--color-border) rounded px-2 py-1" />
                            <label class="text-muted">Title</label>
                            <input type="text" name="title" value="${r.title}" class="w-full border border-(--color-border) rounded px-2 py-1" />
                            <label class="text-muted">Body</label>
                            <textarea name="body" rows="3" class="w-full border border-(--color-border) rounded px-2 py-1">${r.body}</textarea>
                            <div class="flex gap-2">
                                <button type="submit" class="btn btn-primary px-3 py-1 rounded">Save</button>
                                <button type="button" onclick="toggleEditReview('${r.id}')" class="btn btn-secondary px-3 py-1 rounded">Cancel</button>
                            </div>
                        </form>
                    </c:if>
                </div>
            </c:forEach>
            <div class="border-t border-(--color-border) pt-4 space-y-3">
                <h3 class="font-header text-sm">Add review</h3>
                <form method="post" action="${pageContext.request.contextPath}/review" class="space-y-3 text-sm" onsubmit="syncStarRating()">
                    <input type="hidden" name="productId" value="${product.id}" />
                    <input type="hidden" name="rating" id="rating-input" value="5" />
                    <div class="flex items-center gap-3">
                        <span class="text-muted text-sm">Rating</span>
                        <div class="flex items-center gap-1" id="rating-stars">
                            <button type="button" data-value="1" class="star-btn text-[18px]">&#9733;</button>
                            <button type="button" data-value="2" class="star-btn text-[18px]">&#9733;</button>
                            <button type="button" data-value="3" class="star-btn text-[18px]">&#9733;</button>
                            <button type="button" data-value="4" class="star-btn text-[18px]">&#9733;</button>
                            <button type="button" data-value="5" class="star-btn text-[18px]">&#9733;</button>
                        </div>
                    </div>
                    <div class="space-y-1">
                        <label class="text-muted block">Title</label>
                        <input type="text" name="title" class="w-full border border-(--color-border) rounded-md px-3 py-2" />
                    </div>
                    <div class="space-y-1">
                        <label class="text-muted block">Body</label>
                        <textarea name="body" rows="3" class="w-full border border-(--color-border) rounded-md px-3 py-2"></textarea>
                    </div>
                    <button class="btn btn-primary rounded-lg px-4 py-2 text-sm" type="submit">Submit review</button>
                </form>
            </div>
        </div>
    </section>
</main>
<jsp:include page="partials/footer.jsp" />
<script>
    const starButtons = document.querySelectorAll('#rating-stars .star-btn');
    const ratingInput = document.getElementById('rating-input');
    const wishlistBtn = document.getElementById('wishlist-btn');
    const ctx = '${pageContext.request.contextPath}';
    const addCartBtn = document.getElementById('add-cart-btn');
    const cartQtyInput = document.getElementById('cart-qty');

    function setStarRating(value) {
        ratingInput.value = value;
        starButtons.forEach(btn => {
            const v = parseInt(btn.getAttribute('data-value'));
            btn.style.color = v <= value ? '#facc15' : '#d1d5db';
        });
    }

    function syncStarRating() {
        if (!ratingInput.value) {
            ratingInput.value = 5;
        }
    }

    starButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const value = parseInt(btn.getAttribute('data-value'));
            setStarRating(value);
        });
    });

    setStarRating(parseInt(ratingInput.value || '5'));

    if (wishlistBtn) {
        wishlistBtn.addEventListener('click', async () => {
            if (wishlistBtn.dataset.state === 'added') return;
            const pid = wishlistBtn.getAttribute('data-product');
            try {
                const res = await fetch(ctx + '/wishlist-add', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: 'productId=' + encodeURIComponent(pid)
                });
                if (res.ok) {
                    wishlistBtn.dataset.state = 'added';
                    const img = wishlistBtn.querySelector('img');
                    img.src = ctx + '/assets/icons/heart-solid-full.svg';
                    img.dataset.state = 'added';
                }
            } catch (e) { console.error('Wishlist add failed', e); }
        });
    }

    const originalAddCartHTML = addCartBtn ? addCartBtn.innerHTML : '';
    if (addCartBtn) {
        addCartBtn.addEventListener('click', async () => {
            if (addCartBtn.dataset.state === 'added') return;
            const pid = '${product.id}';
            const qty = cartQtyInput ? cartQtyInput.value : '1';
            try {
                const res = await fetch(ctx + '/cart-add', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: 'productId=' + encodeURIComponent(pid) + '&qty=' + encodeURIComponent(qty)
                });
                if (res.ok) {
                    addCartBtn.dataset.state = 'added';
                    addCartBtn.classList.add('bg-secondary');
                    addCartBtn.classList.remove('hover:bg-gray-100');
                    addCartBtn.innerHTML = '<svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12l5 5L20 7" /></svg><span class="ml-1">Added</span>';
                    const badge = document.getElementById('nav-cart-count');
                    if (badge) {
                        const current = parseInt(badge.textContent || '0');
                        badge.textContent = current + parseInt(qty);
                        badge.style.transform = 'scale(1.2)';
                        setTimeout(()=> badge.style.transform = '', 300);
                    }
                    setTimeout(() => { window.location.reload(); }, 350);
                }
            } catch (e) { console.error('Cart add failed', e); }
        });
    }

    function toggleEditReview(id) {
        const form = document.getElementById('edit-review-' + id);
        if (!form) return;
        form.style.display = form.style.display === 'none' ? 'block' : 'none';
    }
</script>
</body>
</html>
