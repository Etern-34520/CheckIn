<%@ taglib prefix="components" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%--<jsp:useBean id="user" class="indi.etern.checkIn.entities.user.User"/>--%>
<c:set var="isOnline" value="${webSocketService.isOnline(user)}"/>
<c:set var="currentUser" value="${requestScope.get(\"currentUser\")}"/>
<components:shrinkPane clazz="userDiv" id="userDiv${user.QQNumber}">
    <jsp:attribute name="title">
            <div style="font-size: 36px;color: var(--highlight-component-background-color-hover);margin-right: 4px;${!user.equals(requestScope.get("currentUser"))?"opacity: 0":""}">
                ·
            </div>
            <div class="userAvatar"
                 style="background-image: url('https://q1.qlogo.cn/g?b=qq&nk=${user.QQNumber}&s=640');"></div>
            <div class="userInfoDiv" style="display: flex;flex-direction: column;font-size: 18px">
                <div class="userName">${user.name}</div>
                <div style="display: flex;flex-direction: row">
                    <div class="userQQ">${user.QQNumber}</div>
                    <div class="blank"></div>
                    <c:if test="${isOnline||user.equals(currentUser)}">
                        <div class="userOnLine"
                             style="margin-right: 4px;color: var(--highlight-component-background-color-hover)">在线
                        </div>
                    </c:if>
                    <div class="userRole"
                         style="margin-right: 8px;color: var(--highlight-component-background-color-hover)">${user.role.type}</div>
                </div>
            </div>
    </jsp:attribute>
    <jsp:attribute name="content">
        <div class="userOperation" style="display:flex;flex-direction: row">
            <div style="display:flex;flex-direction: row">
            <c:if test="${!user.equals(currentUser)}">
                <c:if test="${isOnline}">
                    <button highlight class="optionButton" onclick="offLine(${user.QQNumber})">强制下线</button>
                </c:if>
                <button highlight class="optionButton" onclick="changeRole(${user.QQNumber})" ${isOnline?"disabled":""}>
                    修改用户组
                </button>
                <button highlight class="optionButton" onclick="deleteUser(${user.QQNumber})" ${isOnline?"disabled":""}>
                    删除
                </button>
                <div style="display:flex;flex-direction: row;align-items: center">
                    <label style="margin-left: 8px;margin-right: 4px;">启用</label>
                    <components:toggleSwitch
                            toggleSwitchId="switchEnable${user.QQNumber}"
                            toggleSwitchOn="function (){enableUser(${user.QQNumber})}"
                            toggleSwitchOff="function (){disableUser(${user.QQNumber})}"
                            toggleSwitchState="${user.enabled}"
                            toggleSwitchDisabled="${webSocketService.isOnline(user)}"
                    />
                </div>
            </c:if>
                <c:if test="${user.equals(currentUser)}">
                    <button highlight class="optionButton" onclick="changePassword()">修改密码</button>
                    <button highlight class="optionButton" onclick="deleteUser(${user.QQNumber});logout()">删除</button>
                </c:if>
            </div>
        </div>
    </jsp:attribute>
</components:shrinkPane>