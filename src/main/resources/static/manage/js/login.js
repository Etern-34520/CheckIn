$(function () {//页面加载完成后执行
    setTimeout(function () {
        $("#main").animate({
            opacity: 1
        }, 300, "easeInCubic");
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
    const userNameOrQQ = $("input[name=usernameOrQQ]").val();
    const password = $("input[name=password]").val();
    $.removeCookie("qq", {path: "/checkIn"});
    $.removeCookie("password", {path: "/checkIn"});
    $.ajax({
        url: "./",
        type: "post",
        data: {
            usernameOrQQ: userNameOrQQ,
            password: password
        },
        success: function (result) {
            console.log(result);
            if (result.startsWith("passed")) {
                $.cookie("qq", result.toString().replace("passed", ""), {expires: 7, path: "/checkIn"});
                $.cookie("password", password, {expires: 7, path: "/checkIn"});
                $.cookie('page', 0,{path: "/checkIn/manage"});
                $.cookie('pageClass', 'server',{path: "/checkIn/manage"});
                $("#main").animate({
                    opacity: 0,
                    height: 0,
                    maxHeight: 0,
                    minHeight: 0,
                    border: 0,
                }, 300, "easeOutCubic", function () {
                    window.location.href = "./../manage/";
                });
            } else {
                tip("用户名或密码错误");
            }
        },
        error: function (res) {
            tip(res);
        }
    });
}