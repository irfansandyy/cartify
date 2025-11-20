<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<header class="bg-surface/70 border-b border-(--color-border) sticky-header">
  <div class="container mx-auto flex items-center justify-between gap-4 px-6 py-2">
    <div class="flex items-center gap-2">
      <a href="${pageContext.request.contextPath}/products" class="flex items-center gap-2">
        <img src="${pageContext.request.contextPath}/assets/icons/logo.svg" alt="Cartify" class="text-primary h-8 w-8" />
        <span class="font-header text-primary text-2xl font-bold tracking-tight">Cartify</span>
      </a>
    </div>
    <form method="get" action="${pageContext.request.contextPath}/products" class="hidden md:flex flex-1 max-w-lg items-center gap-2">
      <input type="text" name="q" placeholder="Search for products..." class="flex-1 border border-(--color-border) rounded-md px-3 py-1.5 text-sm outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 transition" />
    </form>
    <nav class="flex items-center gap-4 text-sm text-muted">
      <a href="${pageContext.request.contextPath}/wishlist" class="inline-flex items-center justify-center rounded-lg p-2 hover:bg-gray-100 transition">
      <img src="${pageContext.request.contextPath}/assets/icons/heart-solid-full.svg" alt="Wishlist" class="text-primary h-6 w-6" />
      </a>

      <div class="relative group">
        <a href="${pageContext.request.contextPath}/cart"
           class="inline-flex items-center justify-center rounded-lg p-2 hover:bg-gray-100 transition relative">
          <img src="${pageContext.request.contextPath}/assets/icons/shopping-cart.svg" alt="Cart" class="text-primary h-6 w-6" />
        </a>
        <div aria-hidden="true" class="absolute" style="top:100%;right:0;width:3rem;height:12px;background:transparent;"></div>

        <div id="cart-preview" class="hidden group-hover:block absolute right-0 mt-2 w-72 rounded-lg bg-surface border border-(--color-border) shadow-lg p-3 text-sm">
          <c:if test="${empty miniCartItems}">
            <p class="text-muted text-xs">Your cart is empty.</p>
          </c:if>
          <c:forEach items="${miniCartItems}" var="ci" varStatus="loop">
            <div class="flex items-center justify-between py-1">
              <span class="text-xs text-default line-clamp-1">${cartProductsMini[loop.index].name}</span>
              <span class="text-xs text-muted">x${ci.quantity}</span>
            </div>
          </c:forEach>
          <c:if test="${not empty miniCartItems}">
            <div class="mt-2 flex items-center justify-between">
              <span class="text-xs text-muted">Total</span>
              <span class="text-sm font-semibold text-primary">$${miniCartTotal}</span>
            </div>
          </c:if>
          <a href="${pageContext.request.contextPath}/cart"
             class="mt-3 inline-flex w-full items-center justify-center rounded-md bg-primary text-white text-xs py-1.5 hover:bg-primary-hover transition">View full cart</a>
        </div>
      </div>
      <a href="${pageContext.request.contextPath}/orders" class="inline-flex items-center justify-center rounded-lg p-2 hover:bg-gray-100 transition">
      <img src="${pageContext.request.contextPath}/assets/icons/history.svg" alt="Orders" class="text-primary h-6 w-6" />
      </a>
      <c:set var="userEmail" value="${sessionScope.currentUserEmail}" />
      <c:if test="${empty userEmail and not empty sessionScope.currentUser}">
        <c:set var="userEmail" value="${sessionScope.currentUser.email}" />
      </c:if>

      <c:if test="${empty userEmail}">
        <a href="${pageContext.request.contextPath}/register"
           class="inline-flex items-center justify-center rounded-lg bg-primary border border-(--color-primary) text-white mx-2 px-5 py-2 text-md font-semibold hover:text-black hover:bg-white transition">Register</a>
        <a href="${pageContext.request.contextPath}/login"
           class="inline-flex items-center justify-center rounded-lg border border-(--color-primary) mx-2 px-5 py-2 text-md font-semibold text-primary hover:text-white hover:bg-black transition">Login</a>
      </c:if>
      <c:if test="${not empty userEmail}">
        <div class="relative group">
          <button type="button"
                  class="inline-flex items-center justify-center rounded-full bg-surface-alt mx-2 p-1 hover:bg-gray-100 transition">
            <img src="${pageContext.request.contextPath}/assets/icons/user-solid-full.svg" alt="Account" class="h-6 w-6" />
          </button>
          <div aria-hidden="true" class="absolute" style="top:100%;right:0;width:3rem;height:12px;background:transparent;"></div>
          <div class="absolute right-0 mt-2 w-64 rounded-lg bg-surface border border-(--color-border) shadow-lg p-3 text-xs hidden group-hover:block">
            <p class="text-muted mb-1">Signed in as</p>
            <p class="text-default font-medium mb-3 break-all">${userEmail}</p>
            <a href="${pageContext.request.contextPath}/account"
               class="inline-flex w-full items-center justify-between rounded-md px-3 py-1.5 hover:bg-accent text-default mb-1">
              <span>Account settings</span>
            </a>
            <form method="post" action="${pageContext.request.contextPath}/logout" class="mt-1">
              <button type="submit"
                      class="inline-flex w-full items-center justify-between rounded-md px-3 py-1.5 text-red-600 hover:bg-accent">
                <span>Log out</span>
              </button>
            </form>
          </div>
        </div>
      </c:if>
    </nav>
  </div>
</header>

