<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
  Created by IntelliJ IDEA.
  User: o_345
  Date: 2023/10/9
  Time: 18:19
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<jsp:useBean id="manageMenuItemInfo"
             class="indi.etern.checkIn.beans.ManageMenuItemInfo"
             scope="application"/>
<jsp:useBean id="currentUser"
             class="indi.etern.checkIn.entities.user.User"
             scope="application"/>
<head>
    <title>CheckIn</title>
    <script src="https://cdn.staticfile.org/echarts/5.4.3/echarts.min.js"></script>
    <script src="https://cdn.staticfile.org/jquery/3.7.1/jquery.min.js"></script>
    <script src="https://cdn.staticfile.org/jquery-cookie/1.4.1/jquery.cookie.min.js"></script>
    <script src="https://cdn.staticfile.org/jquery-easing/1.4.1/jquery.easing.min.js"></script>
    <script src="https://cdn.staticfile.org/jquery-color/2.1.2/jquery.color.min.js"></script>

<%--    <script src="./../lib/http_cdn.staticfile.org_echarts_4.3.0_echarts.js"></script>--%>
<%--    <script src="./../lib/http_cdn.staticfile.org_jquery_3.7.1_jquery.js"></script>--%>
<%--    <script src="./../lib/http_cdn.staticfile.org_jquery-color_2.1.2_jquery.color.js"></script>--%>
<%--    <script src="./../lib/http_cdn.staticfile.org_jquery-cookie_1.4.1_jquery.cookie.js"></script>--%>
<%--    <script src="./../lib/http_cdn.staticfile.org_jquery-easing_1.4.1_jquery.easing.js"></script>--%>

    <%--    https://cdn.staticfile.org/jquery-contextmenu/2.9.2/font/context-menu-icons.ttf--%>
    <script src="https://cdn.staticfile.org/jquery-contextmenu/2.9.2/jquery.contextMenu.min.js"></script>
    <script src="https://cdn.staticfile.org/jquery-contextmenu/2.9.2/jquery.ui.position.min.js"></script>

<%--    <script src="./../lib/http_connect.qq.com_qc_jssdk.js"></script>--%>
<%--    <script src="./../lib/http_js.hcaptcha.com_1_api.js"></script>--%>
    <script src="./../lib/md5.min.js"></script>

    <script src="./../js/webSocket.js"></script>
    <script src="./../js/manage.js"></script>
    <script src="./../js/echart.js"></script>
    <script src="./../js/server_2.js"></script>
    <script src="./../js/user_0.js"></script>
    <script src="./../js/user_1.js"></script>
    <script src="./../js/user_2.js"></script>
    <script src="./../js/components.js"></script>
    <script src="./../js/question_form_data.js"></script>
    <%--    <script src="./js/server_2_editForm.js"></script>--%>
    <link rel="stylesheet" href="https://cdn.staticfile.org/jquery-contextmenu/2.9.2/jquery.contextMenu.min.css">
    <link rel="stylesheet" href="./../css/global.css">
    <link rel="stylesheet" href="./../css/manage.css">
    <link rel="stylesheet" href="./../css/server_0.css">
    <link rel="stylesheet" href="./../css/server_2.css">
    <link rel="stylesheet" href="./../css/user_0.css">
    <link rel="stylesheet" href="./../css/user_2.css">
    <link rel="stylesheet" href="./../css/questions.css">
</head>
<script>
    const Permission = { <c:forEach var="permissionBooleanEntries" items="${requestScope.entrySet()}"><c:if test="${permissionBooleanEntries.key.startsWith(\"permission\")}">
        ${permissionBooleanEntries.key}: ${permissionBooleanEntries.value},</c:if></c:forEach>
    }
</script>
<body>
<div id="tipsMask">
</div>
<div id="contentRoot">
    <div id="top" rounded border style="margin-top: -50px">
        <button type="button" id="menuButton" class="button" style="font-size: 20px" onclick="showMenu()">=</button>
        <div id="pagePath">
            <div class="path" undeleted>
                CheckIn
            </div>
        </div>
        <%--    <button type="button" id="addQuestion" class="button" style="font-size: 20px" onclick="addQuestion()">+</button>
            <button type="button" id="upload" class="button" style="font-size: 20px"></button>
            <button type="button" id="save" class="button" style="font-size: 20px"></button>
        --%>
        <div clickable rounded style="display:flex;margin: 0;padding-top: 6px" onclick="switchToPageWithAnimate('user',2)">
            <div class="userAvatar"
                 style="background-image: url('https://q1.qlogo.cn/g?b=qq&nk=${cookie.get('qq').getValue()}&s=640');
                         background-size: 100% 100%;border-radius: 50%;
                         margin-top: -4px;margin-left: -2px;width: 31px;height: 31px;"></div>
            <div class="currentUserName" style="margin-left: 4px;">${fn:escapeXml(currentUser.name)}</div>
            <div class="currentUserQQ" style="margin-left: 4px;">${currentUser.QQNumber}</div>
        </div>
    </div>
    <div id="content"></div>
</div>
<div id="topMask" onclick="closeMenu()">
    <div id="menu" rounded>
        <div id="menuTop">
            <button type="button" id="menuInsideButton" class="button" style="font-size: 20px" onclick="closeMenu()"><
            </button>
        </div>
        <div class="forScroll">
            <div id="menuButtons">
                <div id="serverMenu" class="menuButtons">
                    <div style="display:flex;flex-direction: column">
                        <label style="margin: 4px">服务器</label>
                        <div style="min-height:2px;height: 2px;flex:1;background: rgba(128,128,128,0.7);margin: 0 4px 0 4px"></div>
                    </div>
                    <c:forEach var="menuString" items="${manageMenuItemInfo.menuItemServerStrings}" varStatus="stat">
                        <button type="button" class="button" blockString="${menuString}"
                                onclick="switchToPage('server',${stat.index})">${menuString}<span style="color: rgb(127,127,127);margin-left: 8px;font-size: 12px">(Ctrl+Alt+${stat.index+1})</span></button>
                    </c:forEach>
                </div>
                <div id="userMenu" class="menuButtons">
                    <div style="display:flex;flex-direction: column">
                        <label style="margin: 4px">用户</label>
                        <div style="min-height:2px;height: 2px;flex:1;background: rgba(128,128,128,0.7);margin: 0 4px 0 4px;"></div>
                    </div>
                    <c:forEach var="menuString" items="${manageMenuItemInfo.menuItemUserStrings}" varStatus="stat">
                        <button type="button" class="button" blockString="${menuString}"
                                onclick="switchToPage('user',${stat.index})">${menuString}<span style="color: rgb(127,127,127);margin-left: 8px;font-size: 12px">(Ctrl+Alt+${stat.index+manageMenuItemInfo.menuItemServerStrings.size()+1})</span></button>
                    </c:forEach>
                </div>
            </div>
            <div class="blank"></div>
            <div id="menuBottom">
                <div id="userPanel" rounded clickable onclick="switchToPage('user',2)">
                    <div class="userAvatar"
                         style="background-image: url('https://q1.qlogo.cn/g?b=qq&nk=${currentUser.QQNumber}&s=640');border-radius: 50%;width: 52px;height: 52px;background-size: 100% 100%;"></div>
                    <div style="margin-left: 10px;display: flex;flex-direction: column;">
                        <div class="currentUserName">
                            ${fn:escapeXml(currentUser.name)}
                        </div>
                        <div class="currentUserQQ">
                            ${currentUser.QQNumber}
                        </div>
                    </div>
                </div>
                <button type="button" id="logoutButton" class="button" onclick="logout()">退出</button>
            </div>
        </div>
    </div>
</div>
</body>
</html>
