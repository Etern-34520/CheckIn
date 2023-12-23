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
<div id="managePage" index="0" page-type="user">
    <div rounded style="display: flex;flex: 1;" border>
        <div id="searchAndFilterDiv">
            <label titleLabel text>用户</label>
            <div style="display: flex">
                <label for="userSearch"></label>
                <input id="userSearch" type="text" style="flex: 1" preText="搜索">
                <button>search</button>
            </div>
            <button rounded style="font-size: 30px;" onclick="newUser()">+</button>
        </div>
        <div style="width: 2px;margin: 4px;border-radius: 1px;background: rgba(128,128,128,0.2)"></div>
        <div class="userDivs">
            <c:forEach var="user1" items="${userInfo.users}" varStatus="userStat">
                <c:set var="user" value="${user1}" scope="page"/>
                <c:if test="${userStat.index%2==0}">
                    <%@ include file="pages/userPane.jsp" %>
                </c:if>
            </c:forEach>
        </div>
        <div class="userDivs" style="padding-right: 2%;">
            <c:forEach var="user2" items="${userInfo.users}" varStatus="userStat">
                <c:set var="user" value="${user2}" scope="page"/>
                <c:if test="${userStat.index%2==1}">
                    <%@ include file="pages/userPane.jsp" %>
                </c:if>
            </c:forEach>
        </div>
    </div>
</div>