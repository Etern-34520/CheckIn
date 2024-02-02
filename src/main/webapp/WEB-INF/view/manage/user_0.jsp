<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
  Created by IntelliJ IDEA.
  User: o_345
  Date: 2023/10/16
  Time: 20:30
  To change this template use File | Settings | File Templates.
--%>
<%--for manage——server:home --%>
<%@ page contentType="text/html;charset=UTF-8" %>
<jsp:useBean id="userInfo" scope="request" type="indi.etern.checkIn.beans.UserInfo"/>
<jsp:useBean id="webSocketService" scope="request" type="indi.etern.checkIn.service.web.WebSocketService"/>
<div id="managePage" index="0" page-type="user" onload="initWaterfall()" onclose="destroyWaterfall()">
    <div rounded style="display: flex;flex: 1;" border>
        <div id="searchDiv">
            <label titleLabel text>用户</label>
            <div style="display: flex;margin-top: 8px;">
                <label for="userSearch" titleLabel>搜索</label>
                <input id="userSearch" type="text" style="flex: 1" preText="搜索" oninput="searchUser()">
<%--                <button style="margin: 4px" onclick="searchUser()">搜索</button>--%>
            </div>
            <c:if test="${permission_create_user}">
                <button rounded style="font-size: 30px" onclick="newUser()">+</button>
            </c:if>
        </div>
        <div style="width: 2px;margin: 4px;border-radius: 1px;background: rgba(128,128,128,0.2)"></div>
        <div class="waterfallInfo" style="display: none">
            <c:forEach var="user1" items="${userInfo.users}" varStatus="userStat" begin="0" step="1">
                <c:set var="user" value="${user1}" scope="page"/>
                <%@ include file="pages/userPane.jsp" %>
            </c:forEach>
        </div>
        <div class="waterfallBasic" style="display: flex;flex: 1;flex-direction: row;overflow: hidden;margin-right: 8px;"></div>
    </div>
</div>