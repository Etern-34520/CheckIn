<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
  Created by IntelliJ IDEA.
  User: o_345
  Date: 2023/11/3
  Time: 18:39
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<jsp:useBean id="partitionInfo" type="indi.etern.checkIn.beans.PartitionInfo" scope="request"/>
<jsp:useBean id="permission_create_and_edit_owns_question" scope="request" type="java.lang.Boolean"/>
<div id="partitionDivs">
    <c:forEach var="partition" items="${partitionInfo.partitions}">
        <div style="background:var(--input-background-color);margin: 0 0 8px;" rounded clickable
             id="partition${partition.id}" class="partitionDiv">
            <div style="display: flex;flex-direction: row" class="partitionTop">
                <div style="margin: 0;flex: 1" rounded clickable onclick="togglePartition(this)">${fn:escapeXml(partition.name)}</div>
                <c:if test="${permission_create_and_edit_owns_question}">
                    <button style="min-width: 20px;margin: 0 0 0 2px;font-size: 18px" onclick="newQuestionOf('${partition.id}')">+</button>
                </c:if>
            </div>
            <div style="display: none" class="partitionQuestionsList">
                <c:choose>
                    <c:when test="${empty partition.questions}">
                        <div rounded style="cursor: auto;background: none;" class="empty">empty</div>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="question" items="${partition.questions}">
                            <div rounded clickable class="question${question.md5}"
                                 style="height: 21px;overflow: hidden;display: flex;flex-direction: row"
                                 onclick="switchToQuestion('${question.md5}',this)">
                                <div class="pointer">•</div>
                                <div class="questionContentOverview">${question.content}</div>
                            </div>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </c:forEach>
</div>
<button style="height: 30px;margin: 0" highlight onclick="updateAllQuestions()">全部更新</button>