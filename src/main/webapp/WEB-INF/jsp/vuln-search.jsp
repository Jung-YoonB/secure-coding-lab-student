<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head><meta charset="UTF-8"><title>JSP 검색 (취약)</title></head>
<body>
<h1>JSP 검색 (취약 버전)</h1>
<!-- 취약: 기본 EL 표현식이나 스크립틀릿은 HTML 특수문자를 이스케이프하지 않음 -->
<p>EL 출력: <span>${q}</span></p>
<p>스크립틀릿 출력: <span><%= request.getParameter("q") %></span></p>

<!-- 취약: JavaScript DOM 조작 시 innerHTML 사용 -->
<p>DOM 조작(innerHTML): <span id="dom-result"></span></p>
<script>
    const params = new URLSearchParams(location.search);
    const q = params.get("q") || "";
    const element = document.getElementById("dom-result");
    element.innerHTML = q;
</script>
</body>
</html>
