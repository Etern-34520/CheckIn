<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<jsp:useBean id="roleInfo" type="indi.etern.checkIn.beans.RoleInfo" scope="request"/>
<jsp:useBean id="user" type="indi.etern.checkIn.entities.user.User" scope="request"/>
<div id="newUserPage" style="display: flex;flex-direction: column;width: 500px;height: 250px">
    <label text titleLabel>修改用户组</label>
    <div class="subContentRoot" style="margin: 12px;display: flex;flex-direction: row">
        <div id="userAvatarView" style="border-radius: 50%;width: 64px;height: 64px;background-size: calc(100% + 2px) calc(100% + 2px);background-position: -1px -1px;margin: 8px;background-image: url(https://q1.qlogo.cn/g?b=qq&nk=${user.QQNumber}&s=640);" border></div>
        <div style="display:flex;flex-direction: column;flex: 1">
            <form id="newUserForm">
                <div style="display:flex;flex-direction: row">
                    <div style="display: flex;flex-direction: column;flex:1;margin: 8px;">
                        <label>用户名: ${user.name}</label>
                        <label>QQ: ${user.QQNumber}</label>
                    </div>
                    <div style="display:flex;flex-direction: column;width: 150px">
                        <label for="userRoleSelection">用户组</label>
                        <select id="userRoleSelection">
                            <c:forEach var="role" items="${roleInfo.roles}">
                                <option ${role.equals(user.role)?"selected":""}>${role.type}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>
            </form>
            <button id="okButton" highlight rounded style="height: 30px">确定</button>
        </div>
    </div>
</div>