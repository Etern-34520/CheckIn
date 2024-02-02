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
<jsp:useBean id="permission_create_and_edit_owns_question" type="java.lang.Boolean" scope="request"/>
<c:set var="partition" value="${partition}"/>
<div style="display: flex;flex-direction: column;" onload="initQuestionViewImages()">
    <label id="partitionLabel${partition.id}" titleLabel text>
        ${fn:escapeXml(partition.name)}
    </label>
    <div class="questions" text>
        <c:forEach var="question" items="${partition.sortedQuestion}">
            <components:questionOverview question="${question}"/>
        </c:forEach>
    </div>
    <div class="addButtons" style="margin: 4px">
        <c:if test="${permission_create_and_edit_owns_question}">
            <button type="button" text onclick="addQuestion('${partition.id}')">+</button>
        </c:if>
    </div>
</div>
