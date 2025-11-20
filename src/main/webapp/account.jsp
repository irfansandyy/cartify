<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html>
<head>
    <title>Account Settings - Cartify</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css" />
</head>
<body class="font-body bg-surface">
<jsp:include page="partials/header.jsp" />
<main class="container mx-auto px-6 py-8 space-y-8">
    <c:if test="${not empty sessionScope.accountError}">
        <div class="max-w-xl mx-auto rounded-md border border-red-300 bg-red-50 text-red-700 text-xs px-4 py-3">
            ${sessionScope.accountError}
        </div>
        <c:remove var="accountError" scope="session" />
    </c:if>
    <c:if test="${not empty sessionScope.accountSuccess}">
        <div class="max-w-xl mx-auto rounded-md border border-green-300 bg-green-50 text-green-700 text-xs px-4 py-3">
            ${sessionScope.accountSuccess}
        </div>
        <c:remove var="accountSuccess" scope="session" />
    </c:if>
    <section class="card rounded-xl p-6 space-y-4 max-w-xl">
        <h1 class="font-header text-primary text-xl">Profile</h1>
        <form method="post" action="${pageContext.request.contextPath}/account" class="space-y-3 text-sm">
            <input type="hidden" name="action" value="updateProfile" />
            <div class="space-y-1">
                <label class="text-muted">Email (read-only)</label>
                <input type="text" readonly value="${sessionScope.currentUser.email}" class="w-full border border-(--color-border) rounded-md px-3 py-2 bg-surface-alt" />
            </div>
            <div class="space-y-1">
                <label class="text-muted">Name</label>
                <input type="text" name="name" required value="${sessionScope.currentUser.name}" class="w-full border border-(--color-border) rounded-md px-3 py-2" />
            </div>
            <div class="space-y-1">
                <label class="text-muted">Phone</label>
                <input type="text" name="phone" value="${sessionScope.currentUser.phone}" class="w-full border border-(--color-border) rounded-md px-3 py-2" />
            </div>
            <button type="submit" class="inline-flex items-center justify-center rounded-lg bg-primary text-white px-5 py-2 text-sm">Save profile</button>
        </form>
    </section>

    <section class="space-y-4">
        <div class="flex items-center justify-between">
            <h2 class="font-header text-primary text-lg">Addresses</h2>
        </div>
        <div class="grid md:grid-cols-2 gap-5">
            <c:forEach items="${addresses}" var="addr">
                <div class="card rounded-lg p-4 space-y-2 text-xs">
                    <div class="flex items-center justify-between">
                        <strong class="text-default">${addr.name != null ? addr.name : 'Unnamed'}</strong>
                        <div class="flex gap-1">
                            <c:if test="${addr.defaultShipping}"><span class="px-2 py-0.5 rounded-full bg-accent text-[10px] uppercase">Ship</span></c:if>
                            <c:if test="${addr.defaultBilling}"><span class="px-2 py-0.5 rounded-full bg-secondary text-[10px] uppercase">Bill</span></c:if>
                        </div>
                    </div>
                    <p class="text-muted leading-snug">${addr.address1}<c:if test="${not empty addr.address2}">, ${addr.address2}</c:if>, ${addr.city} ${addr.postalCode}, ${addr.countryCode}</p>
                    <p class="text-muted">${addr.phone}</p>
                    <div class="flex flex-wrap gap-2 pt-2">
                        <form method="post" action="${pageContext.request.contextPath}/account" class="m-0">
                            <input type="hidden" name="action" value="setDefaultShipping" />
                            <input type="hidden" name="address_id" value="${addr.id}" />
                            <button class="text-[10px] px-2 py-1 rounded border border-(--color-border) hover:bg-accent" type="submit">Set shipping</button>
                        </form>
                        <form method="post" action="${pageContext.request.contextPath}/account" class="m-0">
                            <input type="hidden" name="action" value="setDefaultBilling" />
                            <input type="hidden" name="address_id" value="${addr.id}" />
                            <button class="text-[10px] px-2 py-1 rounded border border-(--color-border) hover:bg-secondary" type="submit">Set billing</button>
                        </form>
                        <form method="post" action="${pageContext.request.contextPath}/account" class="m-0">
                            <input type="hidden" name="action" value="deleteAddress" />
                            <input type="hidden" name="address_id" value="${addr.id}" />
                            <button class="text-[10px] px-2 py-1 rounded border border-red-300 text-red-600 hover:bg-red-50" type="submit">Delete</button>
                        </form>
                    </div>
                    <details class="mt-2">
                        <summary class="cursor-pointer text-[11px] text-primary">Edit</summary>
                        <form method="post" action="${pageContext.request.contextPath}/account" class="space-y-1 mt-2">
                            <input type="hidden" name="action" value="editAddress" />
                            <input type="hidden" name="address_id" value="${addr.id}" />
                            <input type="text" name="address_name" value="${addr.name}" placeholder="Name" class="w-full border border-(--color-border) rounded px-2 py-1" />
                            <input type="text" name="address_phone" value="${addr.phone}" placeholder="Phone" class="w-full border border-(--color-border) rounded px-2 py-1" />
                            <input type="text" name="address1" value="${addr.address1}" placeholder="Address line 1" class="w-full border border-(--color-border) rounded px-2 py-1" />
                            <input type="text" name="address2" value="${addr.address2}" placeholder="Address line 2" class="w-full border border-(--color-border) rounded px-2 py-1" />
                            <div class="grid grid-cols-2 gap-2">
                                <input type="text" name="city" value="${addr.city}" placeholder="City" class="border border-(--color-border) rounded px-2 py-1" />
                                <input type="text" name="region" value="${addr.region}" placeholder="Region" class="border border-(--color-border) rounded px-2 py-1" />
                            </div>
                            <div class="grid grid-cols-2 gap-2">
                                <input type="text" name="postal_code" value="${addr.postalCode}" placeholder="Postal" class="border border-(--color-border) rounded px-2 py-1" />
                                <input type="text" name="country_code" value="${addr.countryCode}" placeholder="Country" maxlength="2" pattern="[A-Za-z]{2}" class="border border-(--color-border) rounded px-2 py-1" />
                            </div>
                            <button type="submit" class="inline-flex items-center justify-center rounded bg-primary text-white text-[11px] px-3 py-1">Save</button>
                        </form>
                    </details>
                </div>
            </c:forEach>
            <div class="card rounded-lg p-4 space-y-3 text-xs">
                <h3 class="font-header text-sm text-primary">Add new address</h3>
                <form method="post" action="${pageContext.request.contextPath}/account" class="space-y-2">
                    <input type="hidden" name="action" value="addAddress" />
                    <input type="text" name="address_name" placeholder="Name" class="w-full border border-(--color-border) rounded px-2 py-1" />
                    <input type="text" name="address_phone" placeholder="Phone" class="w-full border border-(--color-border) rounded px-2 py-1" />
                    <input type="text" name="address1" placeholder="Address line 1" required class="w-full border border-(--color-border) rounded px-2 py-1" />
                    <input type="text" name="address2" placeholder="Address line 2" class="w-full border border-(--color-border) rounded px-2 py-1" />
                    <div class="grid grid-cols-2 gap-2">
                        <input type="text" name="city" placeholder="City" required class="border border-(--color-border) rounded px-2 py-1" />
                        <input type="text" name="region" placeholder="Region" class="border border-(--color-border) rounded px-2 py-1" />
                    </div>
                    <div class="grid grid-cols-2 gap-2">
                        <input type="text" name="postal_code" placeholder="Postal code" required class="border border-(--color-border) rounded px-2 py-1" />
                        <input type="text" name="country_code" placeholder="Country (2 letters)" required maxlength="2" pattern="[A-Za-z]{2}" class="border border-(--color-border) rounded px-2 py-1" />
                    </div>
                    <label class="flex items-center gap-2 text-[11px]"><input type="checkbox" name="default_shipping" /> <span>Default shipping</span></label>
                    <label class="flex items-center gap-2 text-[11px]"><input type="checkbox" name="default_billing" /> <span>Default billing</span></label>
                    <button type="submit" class="inline-flex items-center justify-center rounded bg-primary text-white text-[11px] px-3 py-1">Add address</button>
                </form>
            </div>
        </div>
    </section>
</main>
<jsp:include page="partials/footer.jsp" />
</body>
</html>
