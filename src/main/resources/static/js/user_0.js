function debounce(fn, delay) {
    let timer = null;
    return function () {
        const context = this;
        const args = arguments;
        clearTimeout(timer);
        timer = setTimeout(function () {
            fn.apply(context, args);
        }, delay);
    };
}

function deleteUser(userQQ) {//TODO webSocket
    sendMessage({type: "deleteUser", QQ: userQQ.toString()});
}

function offLine(userQQ) {
    sendMessage({type: "offLine", QQ: userQQ.toString()});
}

function removeUserDiv(QQ) {
    const $userDiv = $("#userDiv" + QQ);
    $userDiv.slideUp(200, "easeInCubic", function () {
        $userDiv.remove()
    });
}

function updateUserDiv(user) {
    const $userDiv = $("#userDiv" + user.QQ);
    $userDiv.find(".userQQ").text(user.QQ);
    $userDiv.find(".userName").text(user.name);
    $userDiv.find(".userRole").text(user.role);
    $("#userSelect" + user.QQ).text(user.name)
    // $userDiv.find(".userStatus").text(user.status);
    switchToIgnoreHandler($userDiv.find("span[component_type='toggleSwitch']"), user.enabled);
    if (user.QQ == $.cookie("qq")) {
        $(".currentUserName").text(user.name);
        $(".currentUserRole").text(user.role);
    }
}


function addUserDiv(user) {//TODO
    $.ajax({
        url: "./page/userPane",
        type: "get",
        data: {
            QQ: user.qq
        },
        success: function (res) {
            const $userDiv = $(res);
            $userDiv.hide();
            const $userDivs = $(".waterfallColumn");
            let $usersDiv;
            {
                let childrenCount = null;
                for (const userDiv of $userDivs) {
                    const size = $(userDiv).children().length;
                    if (childrenCount === null || size < childrenCount) {
                        $usersDiv = $(userDiv);
                        childrenCount = size;
                    }
                }
            }
            if ($usersDiv === undefined) {
                $usersDiv = $userDivs.children().eq(0);
            }
            $usersDiv.append($userDiv);
            $userDiv.show(200, "easeOutCubic");
        }
    })
}

let newUserLock = false;

function newUser() {//TODO
    if (newUserLock) return;
    newUserLock = true;
    $.ajax({
        url: "./page/newUser",
        data: {},
        success: function (res) {
            popDialog(res, function () {
                const $userQQInput = $("#userQQInput");
                $userQQInput.on("blur", function () {
                    let qq = $userQQInput.val();
                    const $userAvatarView = $("#userAvatarView");
                    if (qq !== "")
                        $userAvatarView.css("background-image", "url(https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640)");
                })
                $("#okButton").on("click", sendNewUser);
            });
            // transitionPage($("#right"),res,"添加用户",function (){},function (){})
        },
        error: function () {
        }
    });
}

function sendNewUser() {
    const $okButton = $("#okButton");
    const $userQQInput = $("#userQQInput");
    if ($userQQInput.val().length < 5 || $userQQInput.val().length > 15) {
        tip("QQ号码长度不正确");
        return;
    }
    const $userNameInput = $("#userNameInput");
    if ($userNameInput.val().length === 0) {
        tip("用户名为空");
        return;
    }
    $okButton.attr("disabled", true);
    sendMessage({
        type: "newUser",
        QQ: $userQQInput.val(),
        name: $userNameInput.val(),
        role: $("#userRoleSelection").val()
    }, function (res) {
        // $okButton.off("click");
        $okButton.removeAttr("disabled");
        const dataObj = JSON.parse(res.data);
        if (dataObj.type === "error") {
            let reason = dataObj.message;
            if (reason === "user already exists") reason = "用户已存在";
            tip(reason);
        } else {
            $okButton.off("click");
            $okButton.on("click", closeMenu);
            // $okButton.attr("filter", "");
            $okButton.text("关闭");
            tip("初始密码（仅显示一次）：" + dataObj.initPassword, false);
        }
    }, function () {
        return false;
    });
    //TODO
}

//used in user page
// noinspection JSUnusedGlobalSymbols
function enableUser(qqNumber, $toggleSwitch) {
    sendMessage({
        type: "enableUser",
        QQ: qqNumber.toString()
    }, undefined, function () {
        switchOffIgnoreHandler($toggleSwitch);
    });
}

// noinspection JSUnusedGlobalSymbols

function disableUser(qqNumber, $toggleSwitch) {
    sendMessage({
        type: "disableUser",
        QQ: qqNumber.toString()
    }, undefined, function () {
        switchOnIgnoreHandler($toggleSwitch);
    });
}

function tip(content, hide = true) {
    let $tip = $("#tip");
    $tip.stop(true, true);
    const callback = function () {
        $tip.html(content);
        $tip.animate({
            opacity: 1
        }, 200, "easeInQuad", function () {
            if (hide)
                setTimeout(function () {
                    $tip.animate({
                        opacity: 0
                    }, 200, "easeOutQuad")
                }, 2000);
        })
    };
    $tip.animate({
        opacity: 0
    }, 200, "easeOutQuad", callback);
}

function initChangePassword() {
    $("#okButton").on("click", function () {
        const newPassword = $("#newPassword").val();
        if ($("#repeat").val() === newPassword) {
            if (newPassword.length < 6) {
                tip("新密码长度过短");
            } else {
                sendMessage({
                    type: "changePassword",
                    QQ: $.cookie("qq"),
                    oldPassword: $("#currentPassword").val(),
                    newPassword: newPassword,
                }, function (event) {
                    if (JSON.parse(event.data).type === "success") {
                        closeMenu();
                    }
                }, function () {
                    tip("密码错误");
                });
            }
        } else {
            tip("新密码不一致");
        }
    });
}

function changePassword() {
    popDialog(`
    <div style="display: flex;flex-direction: column;height: 450px;width: 400px;margin: 8px">
    <label titleLabel>修改密码</label>
    <label for="currentPassword">当前密码</label>
    <input id="currentPassword" name="currentPassword" type="password" preText="当前密码"/> 
    <label for="newPassword">新密码</label>
    <input id="newPassword" name="newPassword" type="password" preText="新密码"/>
    <label for="repeat">重复新密码</label>
    <input id="repeat" name="repeat" type="password" preText="重复新密码"/> 
    <button id="okButton" rounded highlight>确定</button>
    <label id="tip"></label>
    </div>
    `, initChangePassword);
}

function changeRole(QQ) {
    $.ajax({
        url: "./page/changeRole",
        data: {
            QQ: QQ
        },
        success: function (res) {
            popDialog(res, function () {
                const $userRoleSelection = $("#userRoleSelection");
                const $okButton = $("#okButton");
                $okButton.on("click", function () {
                    $okButton.attr("disabled", "disabled");
                    sendMessage({
                            type: "changeRole",
                            QQ: QQ.toString(),
                            role: $userRoleSelection.val()
                        }, function (res) {
                            $okButton.removeAttr("disabled");
                            const dataObj = JSON.parse(res.data);
                            if (dataObj.type === "error") {
                                let reason = dataObj.reason;
                                // if (reason === "user already exists") reason = "用户已存在";
                                tip(reason);
                            } else {
                                closeMenu();
                            }
                        }
                    );
                })
            });
        }
    });
}

let isSearching = false;
let display = "flex";

function searchUser() {
    const $userSearch = $("#userSearch");
    const $userDivs = $(".userDiv");
    $userDivs.css("display", display);
    display = $userDivs.css("display");
    const value = $userSearch.val().toLowerCase();
    if (value === "") {
        isSearching = false;
    } else {
        isSearching = true;
        if (!isNaN(Number(value))) { //是数字
            $userDivs.css("display", "none");
            for (const userDiv of $userDivs) {
                const $userDiv = $(userDiv);
                if ($userDiv.find(".userQQ").text().toLowerCase().includes(value)) {
                    $userDiv.css("display", display);
                } else if ($userDiv.find(".userName").text().toLowerCase().includes(value)) {
                    $userDiv.css("display", display);
                }
            }
        } else {
            $userDivs.css("display", "none");
            for (const userDiv of $userDivs) {
                const $userDiv = $(userDiv);
                if ($userDiv.find(".userName").text().toLowerCase().includes(value)) {
                    $userDiv.css("display", display);
                } else if ($userDiv.find(".userRole").text().toLowerCase().includes(value)) {
                    $userDiv.css("display", display);
                } else {
                    const userOnline = $userDiv.find(".userOnline");
                    if (userOnline.text().toLowerCase().includes(value)&&userOnline.css("opacity")!=0) {
                        $userDiv.css("display", display);
                    }
                }
            }
        }
    }
}