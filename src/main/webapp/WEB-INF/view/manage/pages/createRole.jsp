<%--
  Created by IntelliJ IDEA.
  User: Etern
  Date: 2024/1/1
  Time: 4:55
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<div style="padding: 8px;width: 800px;height: 100%;display: flex;flex-direction: column">
    <label titleLabel>创建用户组</label>
    <label titleLabel for="roleNameInput">名称</label>
    <input id="roleNameInput" type="text">
    <label titleLabel>权限列表</label>
    <%@ include file="permissionGroupList.jspf" %>
    <button rounded highlight id="confirmCreateRoleButton">确定</button>
    <label id="tip"></label>
</div>
