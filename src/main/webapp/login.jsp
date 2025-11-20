<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html>
<head>
    <title>Login - Cartify</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css" />
</head>
<body class="font-body bg-surface">
<main class="container mx-auto px-6 py-10 flex justify-center items-center" style="flex:1;min-height:90%;">
    <section class="card rounded-xl w-full max-w-md space-y-6" style="width:100%;">
        <div>
            <h1 class="font-header text-primary text-xl mb-1">Welcome back</h1>
            <p class="text-muted text-sm">Sign in to continue shopping.</p>
        </div>
        <c:if test="${not empty error}">
            <div class="rounded-md bg-accent px-3 py-2 text-xs text-red-700">${error}</div>
        </c:if>
        <form method="post" action="${pageContext.request.contextPath}/login" class="space-y-4 text-sm">
            <div class="space-y-1">
                <label class="text-muted block">Email</label>
                <input type="email" name="email" required class="w-full border border-(--color-border) rounded-md px-3 py-2" />
            </div>
            <div class="space-y-1">
                <label class="text-muted block">Password</label>
                <input type="password" name="password" required class="w-full border border-(--color-border) rounded-md px-3 py-2" />
            </div>
            <button type="submit" class="w-full inline-flex items-center justify-center rounded-lg bg-primary text-white px-4 py-2 text-sm">Sign in</button>
        </form>
        <p class="text-xs text-muted">Don't have an account?
            <a href="${pageContext.request.contextPath}/register" class="text-primary">Register</a>
        </p>
    </section>
</main>
<jsp:include page="partials/footer.jsp" />
</body>
</html>
