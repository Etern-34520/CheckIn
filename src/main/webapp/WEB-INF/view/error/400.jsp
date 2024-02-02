<%--
  Created by IntelliJ IDEA.
  User: o_345
  Date: 2023/10/12
  Time: 18:04
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<html>
<head>
    <title>404</title>
    <link rel="stylesheet" href="/checkIn/css/global.css">
</head>
<body>
<h1>400</h1>
<hr/>
<div>
    ${pageContext.errorData.throwable.message}
</div>
<br>
<div>
    ${pageContext.errorData.throwable.cause}
</div>
</body>
</html>
