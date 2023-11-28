<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>login</title>
    <link rel="stylesheet" href="./../manage/css/login.css">
    <link rel="stylesheet" href="./../css/global.css">
    <%--    <script src="https://cdn.staticfile.org/jquery/3.7.1/jquery.min.js"></script>--%>
    <%--    <script src="https://cdn.staticfile.org/jquery-cookie/1.4.1/jquery.cookie.min.js"></script>--%>
    <%--    <script src="https://cdn.staticfile.org/jquery-easing/1.4.1/jquery.easing.min.js"></script>--%>
    <script src="./../lib/http_cdn.staticfile.org_echarts_4.3.0_echarts.js"></script>
    <script src="./../lib/http_cdn.staticfile.org_jquery_3.7.1_jquery.js"></script>
    <script src="./../lib/http_cdn.staticfile.org_jquery-color_2.1.2_jquery.color.js"></script>
    <script src="./../lib/http_cdn.staticfile.org_jquery-cookie_1.4.1_jquery.cookie.js"></script>
    <script src="./../lib/http_cdn.staticfile.org_jquery-easing_1.4.1_jquery.easing.js"></script>
    <script src="./../lib/http_connect.qq.com_qc_jssdk.js"></script>
    <script src="./../lib/http_js.hcaptcha.com_1_api.js"></script>
    <script src="./../manage/js/login.js"></script>
</head>
<body>
<div id="root">
    <div class="blank">
    </div>
    <div id="main" rounded>
        <div class="title">
            <label text style="font-size: 20px">
                CheckIn管理页
            </label>
        </div>
        <div id="loginFormDiv">
            <form id="loginForm">
                <div class="input">
                    <label for="usernameOrQQ"></label>
                    <input type="text" id="usernameOrQQ" name="usernameOrQQ" placeholder="用户名/QQ" required>
                </div>
                <div class="input">
                    <label for="password"></label>
                    <input type="password" id="password" name="password" placeholder="密码" required>
                </div>
                <input class="input" id="loginButton" type="button" onclick="login()" value="登录">
                <div id="tip" text></div>
            </form>
        </div>
    </div>
    <div class="blank">
    </div>
</div>
</body>
</html>