<%--
  Created by IntelliJ IDEA.
  User: o_345
  Date: 2023/10/12
  Time: 18:07
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<html>
<head>
    <title>Error</title>
    <link rel="stylesheet" href="./css/global.css">
</head>
<body>
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
