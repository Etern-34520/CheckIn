<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<jsp:useBean id="roleInfo" type="indi.etern.checkIn.beans.RoleInfo" scope="request"/>
<div id="newUserPage" style="display: flex;flex-direction: column;width: 650px;height: 300px">
    <label text titleLabel>添加用户</label>
    <div class="subContentRoot" style="margin: 12px;display: flex;flex-direction: row">
        <div id="userAvatarView" style="border-radius: 50%;width: 64px;height: 64px;background-size: calc(100% + 2px) calc(100% + 2px);background-position: -1px -1px;margin: 8px" border></div>
        <div style="display:flex;flex-direction: column;flex: 1">
            <form id="newUserForm">
                <div style="display: flex;flex-direction: column">
                    <label for="userNameInput">用户名</label>
                    <input id="userNameInput" name="name" type="text">
                </div>
                <div style="display:flex">
                    <div style="display:flex;flex-direction: column;flex:1">
                        <label for="userQQInput">QQ</label>
                        <input id="userQQInput" name="qq" type="number" min="1" max="9999999999">
                    </div>
                    <div style="display:flex;flex-direction: column;width: 100px">
                        <label for="userRoleSelection">用户组</label>
                        <select id="userRoleSelection">
                            <c:forEach var="role" items="${roleInfo.roles}">
                                <option>${role.type}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>
            </form>
            <button id="okButton" highlight rounded style="height: 30px">确定</button>
            <label id="tip"></label>
        </div>
    </div>
</div>