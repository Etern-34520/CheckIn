<%@ taglib prefix="components" tagdir="/WEB-INF/tags" %>
<%--
  Created by IntelliJ IDEA.
  User: Etern
  Date: 2023/12/30
  Time: 4:29
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:useBean id="title" scope="request" type="java.lang.String"/>
<jsp:useBean id="content" scope="request" type="java.lang.String"/>
<components:shrinkPane clazz="${empty clazz?null:clazz}" id="${empty id?null:id}">
    <jsp:attribute name="title">
        ${title}
    </jsp:attribute>
    <jsp:attribute name="content">
        ${content}
    </jsp:attribute>
</components:shrinkPane>
