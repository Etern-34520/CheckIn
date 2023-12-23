function initUserDetailPage() {
    $("#changePasswordButton").on("click", function () {
        const newPassword = $("#newPassword").val();
        if ($("#repeat").val() === newPassword) {
            if (newPassword.length < 6) {
                showTip("error","新密码长度过短");
            } else {
                sendMessage({
                    type: "changePassword",
                    oldPassword: $("#currentPassword").val(),
                    newPassword: newPassword,
                }, function (event) {
                    if (JSON.parse(event.data).type === "success") {
                        showTip("success","修改成功");
                    }
                }, function () {
                    showTip("error","密码错误");
                });
            }
        } else {
            showTip("error","新密码不一致");
        }
    });
    $("#changeUserNameButton").on("click",function () {
        const newUserName = $("#newUserNameInput").val();
        sendMessage({
            type: "changeUserName",
            newName: newUserName
        }, function (event) {
            if (JSON.parse(event.data).type === "success") {
                showTip("success","修改成功");
            }
        });
    });
}