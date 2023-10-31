<%--
  Created by IntelliJ IDEA.
  User: o_345
  Date: 2023/10/12
  Time: 18:04
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<html>
<head>
    <title>404</title>
    <link rel="stylesheet" href="./css/global.css">
</head>
<body>
<h1>404</h1>
<hr/>
<div>
    ${pageContext.exception.message}
</div>
<br>
<div>
    ${pageContext.exception.cause}
</div>
<br>
<div>
    ${pageContext.exception.stackTrace}
</div>
</body>
</html>
