function editQuestion(md5) {
    if (md5 === undefined) {
        md5 = "";
    }
    let data = "md5=" + md5 + "";
    let url = "page/editQuestion";
    if (md5 === "") {//new question
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
            if (md5 === "") {
                pathBlock = "New Question";
                removePathAfter("Questions", false, false);
            }
            transitionPage($right, res, pathBlock, function () {
                // switchToPage("server",2);
            });
            // $right.html(res);
        }, error: function (res) {
            showTip("error", res.status);
        }
    });
}

function updateQuestion(md5) {
    const jsonData = JSON.stringify({
        type: "updateQuestion", md5: md5,

    });
    sendMessage(jsonData, function (data) {

    });
}

function deleteQuestion(md5) {

}

const imageNameAndSizeArray = [];

function addImages(uploadInput) {
    const fileList = uploadInput.files; // 获取FileList
    for (let i = 0; i < fileList.length; i++) {
        let image = fileList[i];
        if (!imageNameAndSizeArray.includes(image.name + image.size)) {
            addImage(image);
            imageNameAndSizeArray.push(image.name + image.size);
        } else {
            showTip("info", "图片已存在");
        }
    }
    $('#imageInput')[0].value = "";
    onChangeFunc();
}

function showDeleteDiv(div) {
    let $childDiv = $(div).children().eq(0);
    $childDiv.animate({
        opacity: 1
    }, 200, 'easeInOutCubic');
}

function removeImage(div) {
    let $parent = $(div).parent();
    let parentId = $parent.attr("id");
    $parent.animate({
        opacity: 0
    }, 200, 'easeInOutCubic', function () {
        $parent.animate({
            width: 0, margin: 0,
        }, 200, 'easeInOutCubic', function () {
            setTimeout(function () {
                $parent.remove();
            }, 100);
        })
    });
    imageNameAndSizeArray.splice(imageNameAndSizeArray.indexOf(parentId), 1);
    onChangeFunc();
}

function hideDeleteDiv(childDiv) {
    let $childDiv = $(childDiv);
    $childDiv.animate({
        opacity: 0
    }, 200, 'easeInOutCubic');
}

function addImage(image) {
    const reader = new FileReader();
    reader.readAsDataURL(image);
    reader.onload = function (e) {
        const img = new Image();
        img.src = reader.result;
        img.onload = function () {
            const width = (img.width / (img.height / 100));
            let $imageDiv = $(`
<div id="${image.name + image.size}" class="imageDiv" style="background-image: url('${reader.result}');width: ${width + 'px'}" rounded onmouseenter="showDeleteDiv(this);">
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
        }
    }
}

function addOption() {
    var optionsDiv = document.getElementById('optionsDiv');
    var div = document.createElement('div');
    div.className = "optionDiv"
    var index = optionsDiv.children.length;
    $(div).slideUp(0);
    div.innerHTML = `
<input type="checkbox" name="correct` + (index + 1) + `" id="correct` + (index + 1) + `" value="true">
<input type="text" style="margin-left: 8px" name="` + (index + 1) + `"  id="` + (index + 1) + `" value="">
<button class="deleteOptionButton" type="button" style="height: 32px;width: 32px;margin: 4px;font-size: 24px" onclick="deleteOption(this)">-</button>
`
    optionsDiv.append(div);
    $(div).slideDown(150);
    let $deleteOptionButton = $(".deleteOptionButton");
    if (document.getElementById("optionsDiv").children.length <= 2) {
        $deleteOptionButton.fadeOut(200, "easeInOutCubic");
    } else {
        $deleteOptionButton.fadeIn(200, "easeInOutCubic");
        $deleteOptionButton.removeAttr("disabled");
    }
    onChangeFunc();
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
        onChangeFunc();
    });
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
    let md5 = $('#md5').val();
    let $questionContent = $("#questionContent");
    let $partitionDetail = $("#partition" + partitionId);
    let $questions = $partitionDetail.find(".question" + md5);
    let $questionsOfPartitions = $partitionDetail.children().eq(1);
    if (selected) {
        $questionsOfPartitions.find(".question" + md5).remove();//TODO animation
        if ($questionsOfPartitions.children().length === 0) {
            let $empty = $(`
<div rounded style="cursor: auto;background: rgba(0,0,0,0);">
empty
</div>`);
            $questionsOfPartitions.append($empty);//TODO animation
        }
    } else {
        if ($questions.length >= 1) {
            $questions.find("div > div:nth-child(2)").text($questionContent.val());
        } else {
            let $firstChild = $questionsOfPartitions.children().eq(0);
            let $partitionDiv = $(`
<div rounded clickable class="question${md5}" style="height: 21px;overflow: hidden;display: flex;flex-direction: row">
<div class="pointer" style="width: 10px">•</div>
<div style="flex: 1">
${$questionContent.val()}
</div>
</div>
`);
            if ($firstChild.text().replaceAll(" ", "").replaceAll("\n", "") === "empty") {
                $firstChild.remove();//TODO animation
            }
            $questionsOfPartitions.append($partitionDiv);//TODO animation
        }
    }
}

function onChangeFunc(){
    let md5 = $('#md5').val();
    let formData = new FormData(document.getElementById("questionEditForm"));
    let oldFormData = questionMD5ToFormDataMap.get(md5);
    let changed = false;
    for (const key of formData.keys()) {
        if (formData.get(key) !== oldFormData.get(key)) {
            changed = true;
            break;
        }
    }
    if (changed){
        $("#left > .subContentRoot > div > div > div.question" + md5 + " > div.pointer").animate({
            opacity: 1
        },200,"easeInOutQuad");
    } else {
        $("#left > .subContentRoot > div > div > div.question" + md5 + " > div.pointer").animate({
            opacity: 0
        },200,"easeInOutQuad");
    }
}

function contentChanged() {
    onChangeFunc();
    let partitionId = $("#md5").val();
    let $questionContent = $("#questionContent");
    let md5 = $('#md5').val();
    // let $partitionDetails = $("#left > .subContentRoot > div");
    // let $questions = $partitionDetails.find("." + md5);
    let $questions = $(`#left > .subContentRoot > div > div > div.question${md5} > div:nth-child(2)`);
    if ($questions.length >= 1) {
        $questions.text($questionContent.val());
    }
}

const questionMD5ToFormDataMap = new Map();

function initQuestionForm() {
    let $partitionSelections = $("#partitionSelectionsDiv");
    for (const partitionSelection of $partitionSelections.children()) {
        let $partitionSelection = $(partitionSelection);
        if ($partitionSelection.attr("selected") === "selected") {
            $partitionSelection.removeAttr("selected");
            $partitionSelection.trigger("click");
        }
    }
    let formData = new FormData(document.getElementById("questionEditForm"));
    let md5 = $('#md5').val();
    questionMD5ToFormDataMap.set(md5, formData);
    let $questionEditForm = $("#questionEditForm");
    $questionEditForm.find("input").on("input",onChangeFunc);
    $("#questionContent").on("input",contentChanged);
    $questionEditForm.find("textarea").on("input",onChangeFunc);
    $questionEditForm.find("select").on("input",onChangeFunc);
}