<%--
  Created by IntelliJ IDEA.
  User: o_345
  Date: 2023/10/16
  Time: 20:30
  To change this template use File | Settings | File Templates.
--%>
<%--for manage——server:home --%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="components" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="questionInfo" type="indi.etern.checkIn.beans.QuestionInfo" scope="request"/>
<div id="managePage" index="0" page-type="server" onload="initChart()">
    <div id="left">
        <div id="leftTop" rounded border onclick="switchToPageWithAnimate('server',1)">
            <div id="chart"></div>
        </div>
        <div id="leftBottom" rounded border>
            <div id="todayTraffics"></div>
        </div>
    </div>
    <div id="right" rounded border>
        <label titleLabel>新增题目</label>
        <c:forEach var="question" items="${questionInfo.getNewQuestionInDays(14)}">
            <components:questionOverview inUser_2="true" question="${question}"/>
        </c:forEach>
    </div>
</div>