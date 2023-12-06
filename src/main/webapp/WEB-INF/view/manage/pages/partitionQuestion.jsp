<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
  Created by IntelliJ IDEA.
  User: o_345
  Date: 2023/10/30
  Time: 13:21
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:useBean id="partition" scope="request" type="indi.etern.checkIn.entities.question.interfaces.Partition"/>
<c:set var="partition" value="${partition}"/>
<div style="display: flex;flex-direction: column;" onload="initQuestionViewImages()">
    <label id="partitionLabel${partition.id}" titleLabel text>
        ${fn:escapeXml(partition.name)}
    </label>
    <div class="questions" text>
        <c:forEach var="question" items="${partition.questions}">
            <div class="question" id="${question.md5}" rounded clickable
                 onclick="md5ToQuestionFormDataMap = new Map();editQuestion('${question.md5}')">
                <div class="l1">
                    <div class="t1">
                        <div class="questionContent" rounded><c:out value="${fn:escapeXml(question.content)}"/></div>
                    </div>
                    <div class="t2">
                        <div class="questionMD5" rounded><c:out value="${question.md5}"/></div>
                        <div class="questionType" rounded><c:out value="${question.getClass().getSimpleName()}"/></div>
                    </div>
                    <div class="t3">
                        <div class="questionChoices">
                            <c:forEach var="choice" items="${question.getChoices()}">
                                <div class="questionChoice"
                                     <c:if test="${choice.correct}">correct</c:if> rounded><c:out
                                        value="${fn:escapeXml(choice.getContent())}"/></div>
                            </c:forEach>
                        </div>
                    </div>
                </div>
            </div>
        </c:forEach>
    </div>
    <div class="addButtons">
        <button type="button" text style="font-size: 20px"
                onclick="addQuestion('${partition.id}')">
            +
        </button>
        <button type="button" text style="font-size: 20px">↑</button>
    </div>
</div>
