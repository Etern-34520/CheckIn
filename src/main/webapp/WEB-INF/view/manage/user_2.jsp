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
<jsp:useBean id="currentUser" type="indi.etern.checkIn.entities.user.User" scope="request"/>
<div id="managePage" index="2" page-type="user" onload="initUserDetailPage()">
    <div rounded style="display: flex;flex: 1;flex-direction: column;padding: 16px;overflow: auto;" border>
        <div style="flex: 1;padding: 16px">
            <label titleLabel text>账户设置</label>
            <div style="display: flex;flex-direction: row;background: var(--input-background-color);padding: 16px"
                 rounded>
                <div class="userAvatar"
                     style="background-image: url('https://q1.qlogo.cn/g?b=qq&nk=${currentUser.QQNumber}&s=640')"></div>
                <div style="display: flex;flex-direction: column">
                    <label class="currentUserName" style="margin-left: 16px;font-size: 28px">${fn:escapeXml(currentUser.name)}</label>
                    <label class="currentUserQQ" style="margin-left: 16px;font-size: 20px">${currentUser.QQNumber}</label>
                    <label class="currentUserRole" style="margin-left: 16px;font-size: 20px;color: var(--highlight-component-background-color-hover)">${fn:escapeXml(currentUser.role.type)}</label>
                </div>
            </div>
            <components:shrinkPane title="我的题目" useLabelTitle="true">
                <jsp:attribute name="content">
                    <c:choose>
                        <c:when test="${!empty currentUser.multiPartitionableQuestions}">
                            <c:forEach var="question" items="${currentUser.multiPartitionableQuestions}">
                                <components:questionOverview inUser_2="true" question="${question}"/>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <div style="margin: 8px">
                                空
                            </div>
                        </c:otherwise>
                    </c:choose>
                </jsp:attribute>
            </components:shrinkPane>
            <br>
            <label titleLabel text>操作</label>
            <components:shrinkPane title="用户名" useLabelTitle="true">
                <jsp:attribute name="content">
                    <div style="display: flex;flex-direction: row;overflow: hidden">
                        <label for="newUserNameInput" style="margin-left: 8px;line-height: 48px">新用户名</label>
                        <input style="flex:1" id="newUserNameInput" type="text" value="${fn:escapeXml(currentUser.name)}">
                        <button id="changeUserNameButton" highlight rounded style="margin: 4px;padding: 8px">确定</button>
                    </div>
                </jsp:attribute>
            </components:shrinkPane>
            <components:shrinkPane title="密码" useLabelTitle="true">
                <jsp:attribute name="content">
                    <div style="display: flex;flex-direction: row">
                        <label for="currentPassword" style="line-height: 48px">当前密码</label>
                        <input id="currentPassword" style="flex:1" name="currentPassword" type="password"
                               preText="当前密码"/>
                        <label for="newPassword" style="line-height: 48px">新密码</label>
                        <input id="newPassword" style="flex:1" name="newPassword" type="password" preText="新密码"/>
                        <label for="repeat" style="line-height: 48px">重复新密码</label>
                        <input id="repeat" style="flex:1" name="repeat" type="password" preText="重复新密码"/>
                        <button id="changePasswordButton" rounded highlight style="margin: 4px;padding: 8px">确定</button>
                        <label id="tip"></label>
                    </div>
                </jsp:attribute>
            </components:shrinkPane>
            <components:shrinkPane title="其他操作" useLabelTitle="true">
                <jsp:attribute name="content">
                    <div style="display: flex;flex-direction: row;overflow: hidden">
                        <button rounded highlight style="margin: 4px;padding: 8px" onclick="logout()">退出登录</button>
                        <button rounded highlight style="margin: 4px;padding: 8px" onclick="deleteUser(${currentUser.QQNumber});logout()">删除账户</button>
                    </div>
                </jsp:attribute>
            </components:shrinkPane>
        </div>
    </div>
</div>