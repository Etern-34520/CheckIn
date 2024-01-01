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
<jsp:useBean id="permission_delete_role" type="java.lang.Boolean" scope="request"/>
<jsp:useBean id="permission_edit_permission" type="java.lang.Boolean" scope="request"/>
<jsp:useBean id="permission_create_role" type="java.lang.Boolean" scope="request"/>
<div id="managePage" index="0" page-type="user">
    <div class="forScroll" rounded style="display: flex;flex: 1;flex-direction: column" border>
        <div id="roles">
            <c:forEach var="role" items="${roleInfo.roles}">
                <components:shrinkPane id="role_${role.type.replace(' ','_')}">
                    <jsp:attribute name="title">
                        <div style="margin: 8px">${fn:escapeXml(role.type)}</div>
                        <div class="blank"></div>
                        <c:if test="${permission_delete_role}">
                        <button highlight rounded onclick="deleteRole('${role.type}')">删除</button>
                        </c:if>
                        <c:if test="${permission_edit_permission}">
                        <button highlight rounded style="margin-right: 16px" onclick="editingPermissionOf('${role.type}')">修改权限</button>
                        </c:if>
                    </jsp:attribute>
                    <jsp:attribute name="content">
                        <div style="display: flex;flex-direction: row">
                            <div style="display: flex;flex-direction: row;overflow: auto;flex-wrap: wrap">
                                <c:forEach var="user" items="${role.users}">
                                    <%@ include file="pages/userPane.jsp" %>
                                </c:forEach>
                            </div>
                        </div>
                    </jsp:attribute>
                </components:shrinkPane>
            </c:forEach>
        </div>
        <c:if test="${permission_create_role}">
            <button rounded style="margin: 6px;font-size: 28px" onclick="createRole()" id="createRoleButton">+</button>
        </c:if>
    </div>
</div>