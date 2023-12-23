$(function () {//页面加载完成后执行
    setTimeout(function () {
        $("#main").animate({
            opacity: 1
        }, 300, "easeInCubic", function () {
            const logoutReason = $.cookie("logoutReason");
            if (logoutReason !== null && logoutReason !== undefined) {
                tip(logoutReason);
                $.removeCookie("logoutReason",{path: "/checkIn"})
            }
        });
    }, 100);
})

function tip(content) {
    let $tip = $("#tip");
    $tip.stop(true, true);
    $tip.animate({
        opacity: 0
    }, 200, "easeOutQuad");
    $tip.html(content);
    $tip.animate({
        opacity: 1
    }, 200, "easeInQuad", function () {
        setTimeout(function () {
            $tip.animate({
                opacity: 0
            }, 200, "easeOutQuad")
        }, 2000);
    })
}

function login() {
    const usernameOrQQ = $("input[name=usernameOrQQ]").val();
    const password = $("input[name=password]").val();
    $.removeCookie("qq", {path: "/checkIn"});
    $.removeCookie("name", {path: "/checkIn"});
    $.removeCookie("password", {path: "/checkIn"});
    $.ajax({
        url: "./",
        type: "post",
        xhrFields: {
            withCredentials: true
        },
        data: {
            usernameOrQQ: usernameOrQQ,
            password: password
        },
        success: function (result) {
            console.log(result);
            const dataObject = JSON.parse(result);
            if (dataObject.result === "success") {
                $.cookie("qq", dataObject.qq, {expires: 7, path: "/checkIn"});
                $.cookie("name", dataObject.name, {expires: 7, path: "/checkIn"});
                $.cookie("token", dataObject.token, {path: "/checkIn/manage"});
                // $.cookie("password", password, {expires: 7, path: "/checkIn"});
                $.cookie('page', 0, {path: "/checkIn/manage"});
                $.cookie('pageClass', 'server', {path: "/checkIn/manage"});
                $("#main").animate({
                    opacity: 0,
                    height: 0,
                    maxHeight: 0,
                    minHeight: 0,
                    border: 0,
                }, 300, "easeOutCubic", function () {
                    // window.location.href = "./../manage/";
                    const map = new Map();
                    map.set("token", "Bearer " + dataObject.token);
                    window.location.href = "./../manage/";
                });
            } else {
                if (dataObject.reason !== undefined) {
                    tip(dataObject.reason);
                } else {
                    tip("用户名或密码错误");
                }
            }
        },
        error: function (res) {
            tip(res);
        }
    });
}

$(function () {
    const $usernameOrQQ = $("#usernameOrQQ");
    $usernameOrQQ.focus();
    $usernameOrQQ.keydown(function (event) {
        if (event.keyCode === 13) {//enter
            $("#password").focus();
        }
    });
    $("#password").keydown(function (event) {
        if (event.keyCode === 13) {//enter
            login();
        }
    });
});
