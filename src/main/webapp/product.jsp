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
                <form method="post" action="${pageContext.request.contextPath}/cart" class="flex items-center gap-3 mt-2">
                    <input type="hidden" name="action" value="add" />
                    <input type="hidden" name="productId" value="${product.id}" />
                    <input type="number" name="qty" value="1" min="1" class="w-20 border border-(--color-border) rounded-md px-2 py-1 text-sm" />
                    <button class="inline-flex items-center justify-center rounded-md bg-primary text-white px-3 py-2"
                            type="submit">
                        <img src="${pageContext.request.contextPath}/assets/icons/shopping-cart.svg" alt="Add to cart" class="h-4 w-4" />
                    </button>
                </form>
                <form method="post" action="${pageContext.request.contextPath}/wishlist" class="mt-2">
                    <input type="hidden" name="action" value="add" />
                    <input type="hidden" name="productId" value="${product.id}" />
                    <button class="inline-flex items-center justify-center rounded-md bg-surface-alt px-3 py-2 hover:bg-accent"
                            type="submit"
                            onmouseover="this.querySelector('img').src='${pageContext.request.contextPath}/assets/icons/heart-solid-full.svg'"
                            onmouseout="this.querySelector('img').src='${pageContext.request.contextPath}/assets/icons/heart-regular-full.svg'">
                        <img src="${pageContext.request.contextPath}/assets/icons/heart-regular-full.svg" alt="Wishlist" class="h-4 w-4" />
                    </button>
                </form>
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
                        <span class="text-muted text-xs">${r.createdAt}</span>
                    </div>
                    <p class="text-default text-sm leading-snug">${r.body}</p>
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
</script>
</body>
</html>
