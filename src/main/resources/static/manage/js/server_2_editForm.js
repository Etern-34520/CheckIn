let questionMD5ToFormDataMap = new Map();
let questionMD5ToChangedBooleanMap = new Map();
let questionMD5ToOriginalFormDataMap = new Map();
let currentFormData;

function initQuestionForm() {
    let $partitionSelections = $("#partitionSelectionsDiv");
    for (const partitionSelection of $partitionSelections.children()) {
        let $partitionSelection = $(partitionSelection);
        if ($partitionSelection.attr("selected") === "selected") {
            $partitionSelection.removeAttr("selected");
            $partitionSelection.trigger("click");
        }
    }

    let questionMD5 = $('#md5').val();
    let oriFormData = new FormData(document.getElementById("questionEditForm"));
    if (!questionMD5ToFormDataMap.has(questionMD5)) {
        questionMD5ToFormDataMap.set(questionMD5, oriFormData);
    }
    if (!questionMD5ToOriginalFormDataMap.has(questionMD5)) {
        questionMD5ToOriginalFormDataMap.set(questionMD5, new FormData(document.getElementById("questionEditForm")));
    }
    if (!questionMD5ToOriginalFormDataMap.has(questionMD5)) {
        questionMD5ToOriginalFormDataMap.set(questionMD5, oriFormData);
    }
    currentFormData = new FormData(document.getElementById("questionEditForm"));
    let $questionEditForm = $("#questionEditForm");
    $questionEditForm.find("input").on("input", function (){
        checkChange();
    });
    $questionEditForm.find("select").on("input", function (){
        checkChange();
    });
    questionMD5ToChangedBooleanMap.set(questionMD5, false);
}
function switchToQuestion(questionMD5) {
    let data = "new=true";
    let url = "page/newQuestion";
    currentFormData = questionMD5ToFormDataMap.get(questionMD5);
    if (currentFormData === undefined) {
        url = "page/editQuestion";
        data = "md5=" + questionMD5;
    }
    $.ajax({
        url: url, method: "post", data: data, success: function (res) {
            let $right = $("#right");
            // removePathContainsAfter("Edit Question")
            transitionPage($right, res, undefined, undefined, function () {
                if (currentFormData === undefined) {
                    currentFormData = new FormData(document.getElementById("questionEditForm"));
                } else {
                    currentFormData = questionMD5ToFormDataMap.get(questionMD5);
                }
                let $questionEditForm = $("#questionEditForm");
                let formDataMap = new Map();
                for (let key of currentFormData.keys()) {
                    formDataMap.set(key, currentFormData.get(key));
                }
                for (const key of formDataMap.keys()) {
                    let $formElement = $questionEditForm.find("*[name=" + key + "]");
                    if ((!isNaN(Number(key)) || key.startsWith("correct")) && $questionEditForm.find("*[name=" + key + "]").length === 0) {
                        addOption(false);
                        $formElement = $questionEditForm.find("*[name=" + key + "]");
                    } else if (key.startsWith("question_partition_")) {
                        if (formDataMap.get(key) === "true") {
                            $formElement.parent().attr("selected", "selected");
                        }
                    }
                    if (key.startsWith("correct")) {
                        $formElement.attr("checked", "checked");
                    }
                    if (key.startsWith("image")) {
                        setTimeout(function () {
                            addImage(formDataMap.get(key));
                        }, 200);
                    }
                    $formElement.val(formDataMap.get(key));
                }
            });
        }, error: function (res) {
            showTip("error", res.status);
        }
    });

}
function addQuestion() {
    editQuestion("");
}
function editQuestion(questionMD5) {
    let data = "md5=" + questionMD5 + "";
    if (questionMD5 === undefined) {
        questionMD5 = "";
    }
    let url = "page/editQuestion";
    if (questionMD5 === "") {//new question
        data = data + "new=true";
        url = "page/newQuestion";
    }
    let $left = $("#left");
    $.ajax({
        url: "page/partitionQuestionLeft", method: "post", data: "", success: function (res) {
            transitionPage($left, res);
        },
    });
    $.ajax({
        url: url, method: "post", data: data, success: function (res) {
            let $right = $("#right");
            // removePathContainsAfter("Edit Question")
            let pathBlock = "Edit Question";
            removePathAfter("Questions", false, false);
            transitionPage($right, res, pathBlock, function () {
                // switchToPage("server",2);
            });
            // $right.html(res);
        }, error: function (res) {
            showTip("error", res.status);
        }
    });

}

function updateAllQuestions() {
    for (let formData of questionMD5ToFormDataMap.values()){
        let questionMD5 = formData.get("md5");
        if (questionMD5ToChangedBooleanMap.get(questionMD5)){
            $.ajax({
                url: "data/updateQuestion/"+ questionMD5,
                method: "post",
                data: formData,
                processData: false,
                contentType: false,
                success: function (){
                    showTip("info","upload success: "+questionMD5);
                    questionMD5ToOriginalFormDataMap.set(questionMD5,formData);
                    checkChange(formData,formData);
                    questionMD5ToOriginalFormDataMap.delete(questionMD5);
                    questionMD5ToFormDataMap.delete(questionMD5);
                    questionMD5ToChangedBooleanMap.delete(questionMD5);
                },
                error: function (res){
                    showTip("error","upload failed: "+questionMD5+";"+res.responseJSON.message,false);
                }
            });
        }
    }
    transitionPage($("#right"), "");
}
let imageNameAndSizeArray;
function addImages(uploadInput) {

    const fileList = uploadInput.files; // 获取FileList
    for (let i = 0; i < fileList.length; i++) {
        let image = fileList[i];
        if (!imageNameAndSizeArray.includes(image.name + image.size)) {
            addImage(image);
        } else {
            showTip("info", "图片已存在");
        }
    }
    $('#imageInput')[0].value = "";
    checkChange();

}
function showDeleteDiv(div) {
    let $childDiv = $(div).children().eq(0);
    $childDiv.animate({
        opacity: 1
    }, 200, 'easeInOutCubic');

}
function addImage(image) {//FIXME
    let imageNameAndSize = image.name + image.size;
    imageNameAndSizeArray.push(imageNameAndSize);
    const reader = new FileReader();
    reader.readAsDataURL(image);
    currentFormData.append("image_" + imageNameAndSizeArray.indexOf(imageNameAndSize), image);
    reader.onload = function (e) {
        const img = new Image();
        img.src = reader.result;
        img.onload = function () {
            // images.push(img);
            const width = (img.width / (img.height / 100));
            let $imageDiv = $(`
<div id="${imageNameAndSize}" class="imageDiv" style="background-image: url('${reader.result}');width: ${width + 'px'}" rounded onmouseenter="showDeleteDiv(this);">
<div rounded text onclick="removeImage(this);"
onmouseout="hideDeleteDiv(this);">-
</div>
</div>`);
            $imageDiv.css({
                marginLeft: -5, opacity: 0
            });
            $("#imagesDiv").append($imageDiv);
            $imageDiv.animate({
                marginLeft: 0, opacity: 1
            }, 300, 'easeInOutCubic');
            checkChange();
        }
    }

}
function removeImage(div) {
    let $parent = $(div).parent();
    let parentId = $parent.attr("id");
    let index = imageNameAndSizeArray.indexOf(parentId);
    $parent.animate({
        opacity: 0
    }, 200, 'easeInOutCubic', function () {
        $parent.animate({
            width: 0, margin: 0,
        }, 200, 'easeInOutCubic', function () {
            setTimeout(function () {
                $parent.remove();
                currentFormData.delete("image_" + index);
                let imageParts = [];
                let imageKeys = [];
                for (const key of currentFormData.keys()) {
                    if (key.startsWith("image_")) {
                        imageParts.push(currentFormData.get(key));
                        imageKeys.push(key);
                    }
                }
                for (const index in imageParts) {
                    currentFormData.delete(imageKeys[index]);
                    currentFormData.append("image_" + index, imageParts[index]);
                }
                checkChange();
            }, 100);
        })
    });
    imageNameAndSizeArray.splice(imageNameAndSizeArray.indexOf(parentId), 1);

}
function hideDeleteDiv(childDiv) {
    let $childDiv = $(childDiv);
    $childDiv.animate({
        opacity: 0
    }, 200, 'easeInOutCubic');

}
function addOption(animate = true) {
    var optionsDiv = document.getElementById('optionsDiv');
    var div = document.createElement('div');
    div.className = "optionDiv"
    var index = optionsDiv.children.length;
    let $optionDiv = $(div);
    $optionDiv.slideUp(0);
    div.innerHTML = `
<input type="checkbox" name="correct` + (index + 1) + `" id="correct` + (index + 1) + `" value="true">
<input type="text" style="margin-left: 8px" name="` + (index + 1) + `"  id="` + (index + 1) + `" value="">
<button class="deleteOptionButton" type="button" style="height: 32px;width: 32px;margin: 4px;font-size: 24px" onclick="deleteOption(this)">-</button>
`
    optionsDiv.append(div);
    if (animate) {
        $optionDiv.slideDown(150);
    } else {
        $optionDiv.slideDown(0);
    }
    $optionDiv.children("input").on("input", function () {
        checkChange();
    });
    let $deleteOptionButton = $(".deleteOptionButton");
    if (document.getElementById("optionsDiv").children.length <= 2) {
        $deleteOptionButton.fadeOut(200, "easeInOutCubic");
    } else {
        $deleteOptionButton.fadeIn(200, "easeInOutCubic");
        $deleteOptionButton.removeAttr("disabled");
    }
    if (animate) {
        checkChange();
    }

}
function deleteOption(e) {
    let parentNode = e.parentNode;
    const $parentNode = $(parentNode);
    $parentNode.slideUp(150, function () {
        const children = parentNode.parentNode.children;
        parentNode.parentNode.removeChild(parentNode);
        if (children.length <= 2) {
            let $deleteOptionButton = $(".deleteOptionButton");
            $deleteOptionButton.attr("disabled", "disabled");
            $deleteOptionButton.fadeOut(200, "easeInOutCubic");
        }
        checkChange();
    });

}
function togglePartition(titleDiv) {
    let $brothers = $(titleDiv).parent().children();
    $brothers.eq(1).toggle(200, function () {
        if ($brothers.eq(0).attr('selected') === 'selected') {
            $brothers.eq(0).removeAttr('selected');
        } else {
            $brothers.eq(0).attr('selected', true);
        }
    })
}
function selectQuestionPartition(partitionDiv) {
    let $partitionSelection = $(partitionDiv);
    let hiddenPartitionInput = $partitionSelection.find("input[type='hidden']");

    let partitionId = partitionDiv.id.replace("partition_select_", "");
    let selected = $partitionSelection.attr("selected") === "selected";
    if (selected) {
        $partitionSelection.removeAttr("selected");
        hiddenPartitionInput.attr("value", "false");
    } else {
        hiddenPartitionInput.attr("value", "true");
        $partitionSelection.attr("selected", "");

    }
    let $md5 = $('#md5');
    // let questionMd5 = md5("new");
    // $md5.val(questionMd5);
    let questionMd5 = $md5.val();
    let $questionContent = $("#questionContent");
    let $partitionDetail = $("#partition" + partitionId);
    let $questions = $partitionDetail.find(".question" + questionMd5);
    let $questionsOfPartitions = $partitionDetail.children().eq(1);
    if (selected) {
        let $questionsOfPartitionByMd5 = $questionsOfPartitions.find(".question" + questionMd5);
        $questionsOfPartitionByMd5.animate({
            opacity: 0
        }, 200, "easeInOutQuad", function () {
            $questionsOfPartitionByMd5.animate({
                height: 0,
                margin: 0,
                padding: 0
            }, 200, "easeInOutQuad", function () {
                $questionsOfPartitionByMd5.remove();
                if ($questionsOfPartitions.children().length === 0) {
                    let $empty = $(`
<div rounded style="cursor: auto;background: rgba(0,0,0,0);">
empty
</div>`);
                    // $empty.css("display", "none");
                    $empty.css("opacity", "0");
                    $empty.css("height", "0");
                    $empty.css("margin", "0");
                    $empty.css("height", "0");
                    $questionsOfPartitions.append($empty);
                    //padding 4 margin 6 height 20.667
                    $empty.animate({
                        opacity: 1,
                        height: 20.667,
                        margin: 6,
                        padding: 4,
                    }, 200, "easeInOutQuad");
                }

            });
        });
    } else {
        if ($questions.length >= 1) {
            $questions.find("div > div:nth-child(2)").text($questionContent.val());
        } else {
            let $firstChild = $questionsOfPartitions.children().eq(0);
            let $partitionDiv = $(`
<div rounded clickable class="question${questionMd5}" style="height: 21px;overflow: hidden;display: flex;flex-direction: row" onclick="switchToQuestion('${questionMd5}')">
<div class="pointer" style="width: 10px">•</div>
<div style="flex: 1">
${$questionContent.val()}
</div>
</div>
`);
            if ($firstChild.text().replaceAll(" ", "").replaceAll("\n", "") === "empty") {
                $firstChild.animate({
                    opacity: 0
                }, 200, "easeInOutQuad", function () {
                    $firstChild.animate({
                        height: 0,
                        margin: 0,
                        padding: 0
                    }, 200, "easeInOutQuad", function () {
                        $firstChild.remove();
                    });
                });
            }
            $partitionDiv.css("opacity", "0");
            $partitionDiv.css("height", "0");
            $partitionDiv.css("margin", "0");
            $partitionDiv.css("height", "0");
            $questionsOfPartitions.append($partitionDiv);
            //padding 4 margin 6 height 20.667
            $partitionDiv.animate({
                opacity: 1,
                height: 20.667,
                margin: 6,
                padding: 4,
            }, 200, "easeInOutQuad");//TODO 动画有点怪
        }
    }
    if (currentFormData !== undefined && currentFormData.get("md5") === $("#md5").val()) {
        checkChange();
    }

}
function checkChange(formData1 = currentFormData,changedFormData = new FormData(document.getElementById("questionEditForm"))) {
    //questionMD5ToOriginalFormDataMap.get($("#md5").val())
    for (const key of changedFormData.keys()) {
        let value = changedFormData.get(key);
        if (formData1.get(key) !== value) {
            formData1.delete(key);
            if (value !== null) {
                formData1.append(key, value);
            }
        }
    }
    let changedMap = new Map();
    for (const key of formData1.keys()) {
        let value = formData1.get(key);
        let changedValue = changedFormData.get(key);
        if (!key.startsWith("image") && changedValue !== value) {
            changedMap.set(key, changedValue);
        }
    }
    for (const key of changedMap.keys()) {
        formData1.delete(key);
        if (changedMap.get(key) !== null) {
            formData1.append(key, changedMap.get(key));
        }
    }
    let questionMD5 = formData1.get("md5");
    let oldFormData = questionMD5ToOriginalFormDataMap.get(questionMD5);
    if (oldFormData === undefined) return;
    let changed = false;
    for (const key of formData1.keys()) {
        if (formData1.get(key) !== oldFormData.get(key)) {
            changed = true;
            break;
        }
    }
    if (!changed) {
        for (const key of oldFormData.keys()) {
            if (formData1.get(key) !== oldFormData.get(key)) {
                changed = true;
                break;
            }
        }
    }
    if (changed) {
        $("#left > .subContentRoot > div > div > div.question" + questionMD5 + " > div.pointer").animate({
            opacity: 1
        }, 200, "easeInOutQuad");
        questionMD5ToChangedBooleanMap.set(questionMD5, true);
    } else {
        $("#left > .subContentRoot > div > div > div.question" + questionMD5 + " > div.pointer").animate({
            opacity: 0
        }, 200, "easeInOutQuad");
        questionMD5ToChangedBooleanMap.set(questionMD5, false);
    }
    if (questionMD5ToFormDataMap.has(questionMD5)) {
        questionMD5ToFormDataMap.delete(questionMD5);
    }
    questionMD5ToFormDataMap.set(questionMD5,formData1);

}

function contentChanged() {
    checkChange();
    let partitionId = $("#md5").val();
    let $questionContent = $("#questionContent");
    let questionMD5 = $('#md5').val();
    // let $partitionDetails = $("#left > .subContentRoot > div");
    // let $questions = $partitionDetails.find("." + md5);
    let $questions = $(`#left > .subContentRoot > div > div > div.question${questionMD5} > div:nth-child(2)`);
    if ($questions.length >= 1) {
        $questions.text($questionContent.val());
    }
    // checkChange();

}
