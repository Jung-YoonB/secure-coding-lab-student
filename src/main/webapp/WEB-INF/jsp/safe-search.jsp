<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="ko">
<head><meta charset="UTF-8"><title>JSP 검색 (안전)</title></head>
<body>
<h1>JSP 검색 (안전 버전)</h1>
<!-- 안전: JSTL <c:out> 또는 fn:escapeXml 은 HTML 이스케이프 처리 -->
<p>c:out 출력: <span><c:out value="${q}" /></span></p>
<p>fn:escapeXml 출력: <span>${fn:escapeXml(q)}</span></p>

<!-- 안전: JavaScript DOM 조작 시 textContent 사용 -->
<p>DOM 조작(textContent): <span id="dom-result"></span></p>
<script>
    const params = new URLSearchParams(location.search);
    const q = params.get("q") || "";
    const element = document.getElementById("dom-result");
    element.textContent = q;
</script>
</body>
</html>
