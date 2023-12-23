<%@ taglib prefix="components" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
  Created by IntelliJ IDEA.
  User: o_345
  Date: 2023/10/30
  Time: 13:21
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<jsp:useBean id="partition" scope="request" type="indi.etern.checkIn.entities.question.interfaces.Partition"/>
<c:set var="partition" value="${partition}"/>
<div style="display: flex;flex-direction: column;" onload="initQuestionViewImages()">
    <label id="partitionLabel${partition.id}" titleLabel text>
        ${fn:escapeXml(partition.name)}
    </label>
    <div class="questions" text>
        <c:forEach var="question" items="${partition.questions}">
            <components:questionOverview question="${question}"/>
        </c:forEach>
    </div>
    <div class="addButtons">
        <button type="button" text style="font-size: 20px"
                onclick="addQuestion('${partition.id}')">
            +
        </button>
<%--        <button type="button" text style="font-size: 20px">↑</button>--%>
    </div>
</div>
