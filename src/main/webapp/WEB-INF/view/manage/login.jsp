<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>login</title>
    <link rel="stylesheet" href="./../manage/css/login.css">
    <link rel="stylesheet" href="./../css/global.css">
    <script src="https://cdn.staticfile.org/jquery/3.7.1/jquery.min.js"></script>
    <script src="https://cdn.staticfile.org/jquery-cookie/1.4.1/jquery.cookie.min.js"></script>
    <script src="https://cdn.staticfile.org/jquery-easing/1.4.1/jquery.easing.min.js"></script>
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
                <div id="tip" text>
                </div>
                <div class="blank"></div>
                <div id="buttons" class="input">
                    <div class="blank"></div>
                    <input id="loginButton" type="button" onclick="login()" value="登录">
                </div>
            </form>
        </div>
    </div>
    <div class="blank">
    </div>
</div>
</body>
</html>