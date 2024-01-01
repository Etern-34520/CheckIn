<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="components" tagdir="/WEB-INF/tags" %>
<%--
  Created by IntelliJ IDEA.
  User: Etern
  Date: 2023/12/26
  Time: 17:44
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<jsp:useBean id="role" scope="request" type="indi.etern.checkIn.entities.user.Role"/>
<jsp:useBean id="permissionInfo" scope="request" type="indi.etern.checkIn.beans.PermissionInfo"/>
<div style="padding: 8px;width: 800px;height: 100%;display: flex;flex-direction: column">
    <label titleLabel>权限编辑 ${role.type}</label>
    <div style="display:flex;flex-direction: column">
        <label titleLabel>权限列表</label>
        <%@ include file="permissionGroupList.jspf" %>
    </div>
    <label id="tip"></label>
    <button id="saveButton" highlight rounded onclick="sendEditing('${role.type}')">保存</button>
</div>