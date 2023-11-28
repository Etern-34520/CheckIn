<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
<head>
    <title>CheckIn</title>
    <%--<script src="https://cdn.staticfile.org/echarts/5.4.3/echarts.min.js"></script>
    <script src="https://cdn.staticfile.org/jquery/3.7.1/jquery.min.js"></script>
    <script src="https://cdn.staticfile.org/jquery-cookie/1.4.1/jquery.cookie.min.js"></script>
    <script src="https://cdn.staticfile.org/jquery-easing/1.4.1/jquery.easing.min.js"></script>
    --%>
    <script src="./../lib/http_cdn.staticfile.org_echarts_4.3.0_echarts.js"></script>
    <script src="./../lib/http_cdn.staticfile.org_jquery_3.7.1_jquery.js"></script>
    <script src="./../lib/http_cdn.staticfile.org_jquery-color_2.1.2_jquery.color.js"></script>
    <script src="./../lib/http_cdn.staticfile.org_jquery-cookie_1.4.1_jquery.cookie.js"></script>
    <script src="./../lib/http_cdn.staticfile.org_jquery-easing_1.4.1_jquery.easing.js"></script>
    <%--    https://cdn.staticfile.org/jquery-contextmenu/2.9.2/font/context-menu-icons.ttf--%>
    <script src="https://cdn.staticfile.org/jquery-contextmenu/2.9.2/jquery.contextMenu.min.js"></script>
    <script src="https://cdn.staticfile.org/jquery-contextmenu/2.9.2/jquery.ui.position.min.js"></script>

    <script src="./../lib/http_connect.qq.com_qc_jssdk.js"></script>
    <script src="./../lib/http_js.hcaptcha.com_1_api.js"></script>
    <script src="./../lib/md5.min.js"></script>

    <script src="./../js/question.js"></script>
    <script src="./../js/webSocket.js"></script>
    <script src="./js/manage.js"></script>
    <script src="./js/echart.js"></script>
    <script src="./js/server_2.js"></script>
    <script src="./js/question_form_data.js"></script>
<%--    <script src="./js/server_2_editForm.js"></script>--%>
    <link rel="stylesheet" href="https://cdn.staticfile.org/jquery-contextmenu/2.9.2/jquery.contextMenu.min.css">
    <link rel="stylesheet" href="./../css/global.css">
    <link rel="stylesheet" href="./css/manage.css">
    <link rel="stylesheet" href="./css/server_0.css">
    <link rel="stylesheet" href="./css/server_2.css">
</head>
<body>
<div id="tipsMask">
</div>
<div id="contentRoot">
    <div id="top" rounded>
        <button type="button" id="menuButton" class="button" style="font-size: 20px" onclick="showMenu()">=</button>
        <div id="pagePath">
            <div class="path" undeleted>
                CheckIn
            </div>
        </div>
        <button type="button" id="addQuestion" class="button" style="font-size: 20px" onclick="addQuestion()">+</button>
        <button type="button" id="upload" class="button" style="font-size: 20px"></button>
        <button type="button" id="save" class="button" style="font-size: 20px"></button>
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
                        <label style="margin: 4px">Server</label>
                        <div style="min-height:2px;height: 2px;flex:1;background: rgba(128,128,128,0.7);margin: 0 4px 0 4px"></div>
                    </div>
                    <c:forEach var="menuString" items="${manageMenuItemInfo.menuItemServerStrings}" varStatus="stat">
                        <button type="button" class="button"
                                onclick="switchToPage('server',${stat.index})">${menuString}</button>
                    </c:forEach>
                </div>
                <div id="userMenu" class="menuButtons">
                    <div style="display:flex;flex-direction: column">
                        <label style="margin: 4px">User</label>
                        <div style="min-height:2px;height: 2px;flex:1;background: rgba(128,128,128,0.7);margin: 0 4px 0 4px;"></div>
                    </div>
                    <c:forEach var="menuString" items="${manageMenuItemInfo.menuItemUserStrings}" varStatus="stat">
                        <button type="button" class="button"
                                onclick="switchToPage('user',${stat.index})">${menuString}</button>
                    </c:forEach>
                </div>
            </div>
            <div class="blank"></div>
            <div id="menuBottom">
                <div id="userPanel" rounded clickable>
                    ${cookie.get("qq").getValue()}
                </div>
                <button type="button" id="logoutButton" class="button" onclick="logout()">Logout</button>
            </div>
        </div>
    </div>
</div>
</body>
</html>
