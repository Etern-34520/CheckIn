<%@ tag pageEncoding="utf-8" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="components" tagdir="/WEB-INF/tags" %>
<%@attribute name="question"
             type="indi.etern.checkIn.entities.question.impl.multipleQuestion.MultipleChoiceQuestion" %>
<%@attribute name="inUser_2" type="java.lang.Boolean" %>
<components:shrinkPane id="${question.md5}" clazz="question">
    <jsp:attribute name="title">
        <div style="flex:1">
            <div style="display: flex;flex-direction: row">
                <div class="questionContent" rounded><c:out value="${fn:escapeXml(question.content)}"/></div>
            </div>
            <div>
                <div class="questionChoices">
            <c:forEach var="choice" items="${question.choices}">
                <div class="questionChoice"
                     <c:if test="${choice.correct}">correct</c:if> rounded>${fn:escapeXml(choice.content)}</div>
            </c:forEach>
                </div>
            </div>
        </div>
        <%--        <div class="blank"></div>--%>
    </jsp:attribute>
    <jsp:attribute name="content">
        <div class="questionOverviewImages" style="flex-wrap: wrap;display: flex;flex-direction: row"></div>
        <div style="display: flex;flex-direction: row;flex-wrap: wrap">
            <div class="questionMD5" rounded>${question.md5}</div>
            <div class="questionType" rounded>${question.getClass().getSimpleName()}</div>
            <div class="questionAuthor" rounded>
                <c:choose>
                    <c:when test="${question.author!=null}">
                        ${fn:escapeXml(question.author.name)}(${question.author.QQNumber})
                    </c:when>
                    <c:otherwise>
                        unknown
                    </c:otherwise>
                </c:choose>
            </div>
            <div class="questionEditTime" rounded style="flex: none"><c:out value="${empty question.lastEditTime.toString()?\"修改时间未知\":question.lastEditTime.toString()}"/></div>
            <c:choose>
                <c:when test="${inUser_2}">
                    <button rounded highlight style="padding: 4px 6px;margin: 2px"
                            onclick="switchToPageWithAnimate('server',2,false,function () {md5ToQuestionFormDataMap = new Map();editQuestion('${question.md5}')})">
                        编辑
                    </button>
                    <button rounded highlight style="padding: 4px 6px;margin: 2px"
                            onclick="deleteQuestion('${question.md5}')">删除
                    </button>
                </c:when>
                <c:otherwise>
                    <button rounded highlight style="padding: 4px 6px;margin: 2px"
                            onclick="md5ToQuestionFormDataMap = new Map();editQuestion('${question.md5}')">编辑
                    </button>
                    <button rounded highlight style="padding: 4px 6px;margin: 2px"
                            onclick="deleteQuestion('${question.md5}')">删除
                    </button>
                </c:otherwise>
            </c:choose>
        </div>
    </jsp:attribute>
</components:shrinkPane>
<%--
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
</div>--%>
