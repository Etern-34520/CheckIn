function saveSetting(saveButton) {
    const $form = $(saveButton).parent().find("form");
    const formData = new FormData($form[0]);
    const formDataObj = {};
    for (let [key, value] of formData.entries()) {
        formDataObj[key] = value;
    }
    sendMessage({
        type: "saveSetting_"+$form.attr("name"),
        data: formDataObj,
    });
}