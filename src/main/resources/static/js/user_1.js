function editingPermissionOf(roleType) {
    $.ajax({
        url: "./page/editingPermission",
        type: "get",
        data: {
            roleType: roleType
        },
        success: function (res) {
            popDialog(res);
        },
        error: function (data) {
            showTip("error", "加载页面时发生错误" + data);
        }
    });
}

function sendEditing(roleType) {
    $("#saveButton").attr("disabled", "disabled");
    const enabledPermission = [];
    for (const permissionInput of $("#permissions input.toggleSwitchInput")) {
        const $input = $(permissionInput);
        if ($input.val() === "true") {
            enabledPermission.push($input.attr("id").substring(16));
        }
    }
    sendMessage({
        type: "savePermission",
        role: roleType,
        enable: enabledPermission,
        // disable: disabledPermission
    }, function () {
        closeMenu();
    }, function (data) {
        $("#saveButton").removeAttr("disabled");
        // showTip("error",data.message);
        // return false;
    })
    //TODO
}

function deleteRole(roleType) {
    sendMessage({
        type: "deleteRole",
        role: roleType
    },function (message) {
        if (JSON.parse(message.data).type === "success") {
            const $rolePane = $("#role_"+roleType.replace(' ','_'));
            $rolePane.hide(200,"easeInCubic",function () {
                $rolePane.remove();
            });
        }
    });
}

function createRole() {
    $.ajax({
        url: "./page/createRole",
        method: "get",
        success: function (res) {
            popDialog(res, function () {
                const $confirmCreateRoleButton = $("#confirmCreateRoleButton");
                const enabledPermission = [];
                for (const permissionInput of $("#permissions input.toggleSwitchInput")) {
                    const $input = $(permissionInput);
                    if ($input.val() === "true") {
                        enabledPermission.push($input.attr("id").substring(16));
                    }
                }
                $confirmCreateRoleButton.on("click", function () {
                    const roleName = $("#roleNameInput").val();
                    if (roleName === "") {
                        tip("名称不能为空");
                        return;
                    }
                    $confirmCreateRoleButton.attr("disabled","disabled");
                    sendMessage({
                        type: "createRole",
                        role: roleName,
                        enable: enabledPermission,
                    }, function (dataObj) {
                        if (dataObj.type === "error") {
                            let reason = dataObj.reason;
                            tip(reason);
                        } else {
                            switchToPage("user",1, false)
                            closeMenu();
                        }
                        $confirmCreateRoleButton.removeAttr("disabled");
                    });
                });
            });
        },
        error: function (data) {
            showTip("error", data.code);
        }
    })
}