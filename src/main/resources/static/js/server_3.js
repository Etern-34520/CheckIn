function saveSetting(saveButton) {
    const $form = $(saveButton).parent().find("form");
    const formData = new FormData($form[0]);
    const formDataObj = {};
    for (let [key, value] of formData.entries()) {
        formDataObj[key] = value;
    }
    sendMessage({
        type: "saveSetting",
        data: formDataObj,
        settingName: $form.attr("name")
    }, function (event) {
        if (JSON.parse(event.data).type === "success") {
            showTip("success", "保存成功");
        }
    });
}