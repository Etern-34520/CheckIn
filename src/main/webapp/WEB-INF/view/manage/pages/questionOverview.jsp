<%@ taglib prefix="components" tagdir="/WEB-INF/tags" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%--
  Created by IntelliJ IDEA.
  User: etern
  Date: 2023/12/23
  Time: 15:07
  To change this template use File | Settings | File Templates.
--%>
<jsp:useBean id="question" scope="request" type="indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion"/>
<components:questionOverview question="${question}"/>