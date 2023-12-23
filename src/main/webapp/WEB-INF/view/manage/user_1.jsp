<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="components" tagdir="/WEB-INF/tags" %>
<%--
  Created by IntelliJ IDEA.
  User: o_345
  Date: 2023/10/16
  Time: 20:30
  To change this template use File | Settings | File Templates.
--%>
<%--for manage——server:home --%>
<%@ page contentType="text/html;charset=UTF-8" %>
<jsp:useBean id="roleInfo" scope="request" type="indi.etern.checkIn.beans.RoleInfo"/>
<div id="managePage" index="0" page-type="user">
    <div rounded style="display: flex;flex: 1;flex-direction: column" border>
        <c:forEach var="role" items="${roleInfo.roles}">
            <components:shrinkPane useLabelTitle="true">
                <jsp:attribute name="title">
                    ${fn:escapeXml(role.type)}
                </jsp:attribute>
                <jsp:attribute name="content">
                    <div style="display: flex;flex-direction: row">
                        <c:choose>
                            <c:when test="${empty role.users}">
                                <span style="margin-left: 8px;">空</span>
                            </c:when>
                            <c:otherwise>
                                <div style="display: flex;flex-direction: row;overflow: auto">
                                    <c:forEach var="user" items="${role.users}">
                                        <%@ include file="pages/userPane.jsp" %>
                                    </c:forEach>
                                </div>
                            </c:otherwise>
                        </c:choose>
                        <div class="blank"></div>
                        <button highlight rounded style="margin-right: 0;">删除</button>
                    </div>
                </jsp:attribute>
            </components:shrinkPane>
        </c:forEach>
        <button rounded style="margin: 6px;font-size: 28px">
            +
        </button>
    </div>
</div>