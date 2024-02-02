<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="components" tagdir="/WEB-INF/tags" %>
<div rounded id="permissions">
    <c:forEach var="permissionGroup" items="${permissionInfo.permissionGroups}">
        <components:shrinkPane titleMinHeight="30" titlePadding="2">
                    <jsp:attribute name="title">
                            <div style="display: flex;flex-direction: row;align-items: center;margin-left: 8px">
                                <label for="permission${permissionGroup.name}">${permissionGroup.name} —— ${permissionGroup.description}</label>
                                <div class="blank"></div>
                            </div>
                            <div class="blank"></div>
                    </jsp:attribute>
            <jsp:attribute name="content">
                        <c:forEach var="permission" items="${permissionGroup.permissions}">
                            <div style="display: flex;flex-direction: row;align-items: center;background: var(--input-background-color)" rounded>
                                <label for="permission${permission.id}">${permission.name} —— ${permission.description}</label>
                                <div class="blank"></div>
                                <components:toggleSwitch switchID="permission${permission.id}" state="${role.permissions.contains(permission)}"/>
                            </div>
                        </c:forEach>
                    </jsp:attribute>
        </components:shrinkPane>
    </c:forEach>
</div>