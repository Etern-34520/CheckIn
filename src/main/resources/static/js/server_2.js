let editing = false;
let selectedOption = "content";
function selectSortOption(/**HTMLElement*/optionDiv) {
    if (transferring) {
        return;
    }
    const $selections = $(optionDiv.parentElement);
    let haveSelected = false;
    const $optionDiv = $(optionDiv);
    if ($optionDiv.attr("selected") === "selected") haveSelected = true;
    $selections.children().removeAttr("selected");
    $optionDiv.attr("selected", "selected");
    if (!haveSelected)
        selectedOption = $optionDiv.attr("type");
        sortQuestion();
}

let sortType = "asc";

function sortQuestion() {
    const $selectedPartitionButton = $("#partitionButtons .partitionButton[selected='selected']");
    if (transferring || $selectedPartitionButton.length === 0) {
        return;
    }
    const $right = $("#right");
    const partitionID = $selectedPartitionButton.attr("id").substring(15);
    $.ajax({
        url: "./page/partitionQuestion",
        data: {
            id: partitionID,
            sortType: sortType,
            sortBy: selectedOption
        },
        success: function (res) {
            transferring = true;
            transitionPage($right, res, undefined, undefined, function () {
                transferring = false;
            });
        }
    });
}

// noinspection JSUnusedGlobalSymbols
function sortQuestionAsc() {
    sortType = "asc";
    sortQuestion();
}

// noinspection JSUnusedGlobalSymbols
function sortQuestionDesc() {
    sortType = "desc";
    sortQuestion();
}

function editPartitionName() {
    editing = true;
    const $partitionButton = $(this);
    $partitionButton.attr("editing", "true");
    const $editInput = $("<input type=\"text\" value=\"New Partition\">");
    const originalName = $partitionButton.text();
    $editInput.val(originalName);
    $partitionButton.text("");
    $partitionButton.append($editInput);
    // $editInput.focus();
    $editInput.focus();
    $partitionButton.off("keydown");
    $partitionButton.on("keydown", function (event) {
        if (event.key === "Enter") {
            saveEditing($editInput, $partitionButton, originalName);
        } else if (event.key === "Escape") {
            $editInput.val(originalName);
            $editInput.blur();
            editing = false;
            $partitionButton.attr("editing", "false");
        }
    });
    $editInput.on("blur", function () {
        if ($editInput.val() === originalName || $editInput.val() === "") {
            $partitionButton.text(originalName);
            editing = false;
            $partitionButton.attr("editing", "false");
        } else {
            saveEditing($editInput, $partitionButton, originalName);
        }
    });
}

function saveEditing($editingInput, $partitionButton, previousName) {
    let existedName = false;
    for (const button of $(".partitionButton")) {
        if (button.innerText === $editingInput.val()) {
            existedName = true;
            break;
        }
    }
    if (existedName) {
        showTip("error", "Partition name already exists");
        $editingInput.focus();
        return;
    }
    for (const path of $(".path")) {
        const $path = $(path);
        if ($path.text() === "> " + previousName) {
            $path.text("> " + $editingInput.val());
            $("#partitionLabel" + $partitionButton.attr("id").substring(15)).text($editingInput.val());
            break;
        }
    }
    $partitionButton.text($editingInput.val());
    $editingInput.remove();
    initContextMenu();
    editing = false;
    $partitionButton.attr("editing", "false");
    sendMessage({
        "type": "editPartition",
        "partitionName": $partitionButton.text(),
        "partitionId": $partitionButton.attr("id").substring(15)
    }, function (e) {
        showTip("info", "Partition name changed");
    });
}

function initQuestionViewImages() {
    for (const questionDiv of $(".questions").children()) {
        const $questionDiv = $(questionDiv);
        const questionMD5 = $questionDiv.attr("id");
        initQuestionViewImage(questionMD5);
    }
}

function initQuestionViewImage(questionMD5) {
    $.ajax({
        url: "./../question/image/" + questionMD5 + "/", method: "get",
        xhrFields: {
            withCredentials: true
        },
        success: function (res) {
            let originalImageCount = res.count;
            if (originalImageCount >= 1) {
                for (let i = 0; i < originalImageCount; i++) {
                    let imageName = res.names[i];
                    let imageData = dataURLtoFile(res.imagesBase64[imageName], imageName);
                    addImageData(imageData, questionMD5);
                }
            }
        }.bind(this),
        error: function (res) {
            showTip("error", res.status, false);
        }.bind(this)
    });
}

/**void*/ function addImageData(/**Blob*/image, /**string*/ questionMD5) {
    let imageNameAndSize = image.name + image.size;
    const reader = new FileReader();
    reader.readAsDataURL(image);
    reader.onload = function () {
        const img = new Image();
        img.src = reader.result;
        img.onload = function () {
            const $questionDiv = $("#" + questionMD5 + " .questionOverviewImages");
            const width = (img.width / (img.height / (100)));
            let $imageDiv = $(`
<div id="${imageNameAndSize}" class="imageDiv" style="border-radius:8px;background-image: url('${reader.result}');width: ${width + 'px'}"></div>`);
            /*
                        $imageDiv.css({
                            marginLeft: -5, opacity: 0
                        });
            */
            $questionDiv.attr("rounded", "");
            $questionDiv.append($imageDiv);
            /*
                        $imageDiv.animate({
                            marginLeft: 0, opacity: 1
                        }, 300, 'easeInOutCubic');
            */
        }.bind(this);
    }.bind(this);
}

function addNewPartitionButton($nameInput, $newPartitionButton) {
    for (const $button of $(".partitionButton")) {
        if ($button.innerText === $nameInput.val()) {
            $nameInput.val("");
            $nameInput.blur();
            showTip("error", "Partition name already exists");
            return;
        }
    }
    let partitionName = $nameInput.val();
    $nameInput.remove();
    $newPartitionButton.text(partitionName);
    $newPartitionButton.attr("id", partitionName);
    $newPartitionButton.attr("onclick", "switchToPartition(this)");
    $newPartitionButton.attr("editing", "false");
    $newPartitionButton.attr("disabled", "disabled");
    const messageJson = {
        "type": "addPartition",
        "partitionName": partitionName
    };
    sendMessage(messageJson, function (event) {
        const message = JSON.parse(event.data);
        if (message.type === "addPartitionCallBack") {
            $newPartitionButton.attr("id", "partitionButton" + message.id);
            $newPartitionButton.removeAttr("disabled");
        }
    });
}

function newPartition() {
    let newPartitionButtonHtml =
        '<button class="partitionButton" id="newPartitionButton" preText editing="true" style="border: none;padding: 0"><input type="text" value="New Partition"></button>';
    let $newPartitionButton = $(newPartitionButtonHtml);
    $newPartitionButton.css({
        height: 0,
        marginTop: 0,
        marginBottom: 0,
        paddingTop: 0,
        paddingBottom: 0
    });
    $("#addPartitionButton").before($newPartitionButton);
    setTimeout(function () {
        $newPartitionButton.animate({
            height: 40,
            marginTop: 4,
            marginBottom: 4,
            paddingTop: 1,
            paddingBottom: 1
        }, 100, "easeOutQuad", function () {
            let $nameInput = $newPartitionButton.children("input").eq(0);
            $nameInput.focus();
            $newPartitionButton.on("keydown", function (event) {
                if (event.key === "Enter") {
                    addNewPartitionButton($nameInput, $newPartitionButton);
                } else if (event.key === "Escape") {
                    $nameInput.val("");
                    $nameInput.blur();
                }
            });
            $nameInput.on("blur", function () {
                if ($nameInput.val() === "New Partition" || $nameInput.val() === "") {
                    $newPartitionButton.slideUp(200, "easeInOutCubic", function () {
                        setTimeout(function () {
                                $newPartitionButton.remove();
                            }
                            , 100);
                    });
                } else {
                    addNewPartitionButton($nameInput, $newPartitionButton);
                }
            });
        })
    }, 100);

    // $newPartitionButton.stop();
}

let transferring = false;

function switchToPartition(button) {
    let $button = $(button);
    if ($button.attr("editing") === "true") return;
    if (transferring) return;
    transferring = true;
    $(".partitionButton").removeAttr("selected");
    $button.attr("selected", "true");
    let partitionName = $button.text();
    let partitionID = $button.attr("id").substring(15);
    let $right = $("#right");
    removePathAfter("题库", false, false);
    $.ajax({
        url: "./page/partitionQuestion",
        method: "post",
        xhrFields: {
            withCredentials: true
        },
        data: {
            "id": partitionID
        },
        success: function (res) {
            const leftHtml = $("#left .subContentRoot").html();
            transitionPage($right, res, partitionName, function () {
                // transitionPage($right, res);
                transitionPage($("#left"), leftHtml);
            }, function () {
                transferring = false;
            });
        },
        error: function (res) {
            showTip("error", res.status);
            transferring = false;
        }
    });
}

function updatePartition(partitionIdNameMap) {
    const differenceMap = compareExistedPartitions(partitionIdNameMap);
    const inEditingPage = differenceMap.get("editingQuestion");
    for (const partitionId of differenceMap.get("removed").keys()) {
        if (inEditingPage) {

            const $partitionDiv = $("#partition" + partitionId);
            $partitionDiv.hide(200, "easeInCubic", function () {
                $partitionDiv.remove();
            });

            const $partitionSelect = $("#partition_select_" + partitionId);
            $partitionSelect.hide(200, "easeInCubic", function () {
                if ($partitionSelect.attr("selected") === "selected") {
                    showTip("info", "a partition is deleted, the question is moved to undefined", false);
                    const $undefinedPartition = $("#partitionSelectionsDiv > div").eq(0);
                    if ($undefinedPartition.attr("selected") !== "selected") {
                        $undefinedPartition.trigger("click");
                    }
                }
                $partitionSelect.remove();
            });

        } else {
            const $partitionButton = $("#partitionButton" + partitionId);
            $partitionButton.hide(200, "easeInCubic", function () {
                $partitionButton.remove();
            })

            const partitionName = differenceMap.get("removed").get(partitionId);
            if (containsPath(partitionName)) {
                removePathAfter(partitionName, true, true);
                showTip("info", "the partition is deleted", false);
            }
        }
    }
    for (const partitionId of differenceMap.get("added").keys()) {
        const partitionName = differenceMap.get("added").get(partitionId);
        if (inEditingPage) {
            let addButtonHtml = "";
            if (Permission.permission_create_and_edit_owns_question) {
                addButtonHtml = `<button style="min-width: 20px;margin: 0 0 0 2px;font-size: 18px" onclick="newQuestionOf('${partitionId}')">+</button>`;
            }
            const partitionDivHtml = `
<div style="background:var(--input-background-color);margin: 0 0 8px;" rounded="" clickable="" id="partition${partitionId}" class="partitionDiv">
<div style="display: flex;flex-direction: row" class="partitionTop">
<div style="margin: 0;flex: 1" rounded="" clickable="" onclick="togglePartition(this)">${partitionName}</div>
${addButtonHtml}
</div>
<div style="display: none" class="partitionQuestionsList">
<div rounded="" style="cursor: auto;background: none;" class="empty">empty</div>
</div>
</div>
`;
            /*
            * <div style="background:var(--input-background-color);margin: 0 0 8px;" rounded clickable id="partition${partitionId}" class="partitionDiv">
<div style="margin: 0" rounded clickable onclick="togglePartition(this)">${partitionName}</div>
<div style="display: none">
<div rounded="" style="cursor: auto;background: none;" class="empty">empty</div>
</div>
</div>
* */
            const $partition = $(partitionDivHtml);
            $partition.slideUp(0);
            $("#partitionDivs").append($partition);
            $partition.slideDown(200, "easeOutCubic");

            const partitionSelectHtml = `
<div rounded clickable onclick="selectQuestionPartition(this)" id="partition_select_${partitionId}">
<input type="hidden" id="question_partition_${partitionId}" name="question_partition_${partitionId}" value="false">
<label>${partitionName}</label>
</div>
`;
            const $partitionSelect = $(partitionSelectHtml);
            $partitionSelect.slideUp(0);
            $("#partitionSelectionsDiv").append($partitionSelect);
            $partitionSelect.slideDown(200, "easeOutCubic");
        } else {
            const partitionButtonHtml = `
<button id="partitionButton${partitionId}" class="partitionButton" onclick="switchToPartition(this)" preText editing="false">${partitionName}</button>
`;
            const $partitionButton = $(partitionButtonHtml);
            $partitionButton.slideUp(0);
            $("#addPartitionButton").before($partitionButton);
            $partitionButton.slideDown(200, "easeOutCubic");

        }
    }
    for (const partitionId of differenceMap.get("changed").keys()) {
        const newPartitionName = differenceMap.get("changed").get(partitionId);
        if (inEditingPage) {
            const $partitionDiv = $("#partition" + partitionId);
            $partitionDiv.children().eq(0).children().eq(0).text(newPartitionName);
            //TODO form selections name should be changed
            $("#partition_select_" + partitionId + "> label").text(newPartitionName);
        } else {
            const $partitionButton = $("#partitionButton" + partitionId);
            const oldName = $partitionButton.text();
            if (containsPath(oldName)) {
                const $pathBlocks = $(".path");
                for (const pathBlock of $pathBlocks) {
                    if ($(pathBlock).text() === "> " + oldName) {//FIXME
                        $(pathBlock).text("> " + newPartitionName);
                    }
                }
                $("#partitionLabel" + partitionId).text(newPartitionName);
            }
            $partitionButton.text(newPartitionName);
        }
    }
}

/**Map*/function compareExistedPartitions(partitionIdNameMap) {//FIXME
    /*
    testMap = new Map();
    testMap.set("-1388849766","undefined");
    testMap.set("-1706023334","New Partition");
    testMap.set("114514","114514");
    updatePartition(testMap);
    */
    const differenceMap = new Map();
    const added = new Map();
    const removed = new Map();
    const changed = new Map();
    differenceMap.set("editingQuestion", false);
    let $partitionButtons = $("#partitionButtons > .partitionButton");
    if ($partitionButtons.length !== 0) {//if it is not editing question
        for (const partitionButton of $partitionButtons) {
            const $partitionButton = $(partitionButton);
            const partitionId = $partitionButton.attr("id").substring(15);
            const partitionName = $partitionButton.text();
            if (!partitionIdNameMap.has(partitionId)) {
                removed.set(partitionId, partitionName);
            }
        }
        for (const partitionId of partitionIdNameMap.keys()) {
            if ($partitionButtons.parent().find("#partitionButton" + partitionId).length === 0) {
                added.set(partitionId, partitionIdNameMap.get(partitionId));
            } else if ($partitionButtons.parent().find("#partitionButton" + partitionId).text() !== partitionIdNameMap.get(partitionId)) {
                changed.set(partitionId, partitionIdNameMap.get(partitionId));
            }
        }
    } else {
        differenceMap.set("editingQuestion", true);
        const $partitionDivs = $(".partitionDiv");
        for (const partitionDiv of $partitionDivs) {
            const $partitionDiv = $(partitionDiv);
            if (!partitionIdNameMap.has($partitionDiv.attr("id").substring(9))) {
                removed.set($partitionDiv.attr("id").substring(9), $partitionDiv.children().eq(0).text());
            }
        }
        for (const partitionId of partitionIdNameMap.keys()) {
            if ($partitionDivs.parent().find("#partition" + partitionId).length === 0) {
                added.set(partitionId, partitionIdNameMap.get(partitionId));
            } else if ($partitionDivs.parent().find("#partition" + partitionId).children().eq(0).text() !== partitionIdNameMap.get(partitionId)) {
                changed.set(partitionId, partitionIdNameMap.get(partitionId));
            }
        }
    }
    differenceMap.set("added", added);
    differenceMap.set("removed", removed);
    differenceMap.set("changed", changed);
    return differenceMap;
}

function removeQuestionDiv(questionMD5) {
    let $questionMD5Divs = $("div.questions > div.question div.questionMD5");
    for (const questionMD5Div of $questionMD5Divs) {
        let $questionMD5Div = $(questionMD5Div);
        if ($questionMD5Div.text() === questionMD5) {
            let $questionDiv = $("#" + questionMD5);
            $questionDiv.animate({
                height: 0,
                opacity: 0,
                marginTop: 0,
                marginBottom: 0,
                paddingTop: 0,
                paddingBottom: 0,
            }, 200, "easeOutQuad", function () {
                setTimeout(function () {
                    $questionDiv.remove();
                }, 100);
            });
            break;
        }
    }

    let $question = $(".question" + questionMD5);
    if ($question.length > 0) {
        if ($("#md5").val() !== questionMD5 && $question.find(".pointer").css("opacity") !== 0) {
            $question.hide(200, "easeInOutCubic");
            $question.remove();
        } else {
            $question.fadeTo(200, 0.5);
            showTip("info", "The question you have edited has been deleted, you can edit it but it will be a new question", false);
        }
    }
}

function generateQuestionHtml(questionObject) {
    let choicesHTML = '';
    for (const choice of questionObject.choices) {
        let correctTextBlock = '';
        if (choice.correct === "true") {
            correctTextBlock = 'correct';
        }
        choicesHTML = choicesHTML + `<div class="questionChoice" ${correctTextBlock} rounded >${choice.content}</div>`;
    }
    return `
<div class="question" id="${questionObject.md5}" rounded clickable
 onclick="md5ToQuestionFormDataMap = new Map();editQuestion('${questionObject.md5}')">
<div class="l1">
    <div class="t1">
        <div class="questionContent" rounded>${questionObject.content}</div>
    </div>
    <div class="t2">
        <div class="questionMD5" rounded>${questionObject.md5}</div>
        <div class="questionType" rounded>${questionObject.type}</div>
    </div>
    <div class="t3">
        <div class="questionChoices">
            ${choicesHTML}
        </div>
    </div>
</div>
</div>
    `;
}

function updateQuestionDiv(questionJsonData) {
    let questionObject = JSON.parse(questionJsonData);

    const $questionDivs = $("#" + questionObject.md5);
    const $questionsDiv = $("div.questions");
    if ($questionDivs.length === 0 && $questionsDiv.length > 0) {
        let containedInCurrentPartition = false;
        for (const partitionName of questionObject.partitions) {
            if (containsPath(partitionName)) {
                containedInCurrentPartition = true;
                break;
            }
        }
        if (!containedInCurrentPartition) {
            return;
        }
        $.ajax({
            url: "./page/questionOverview",
            data: {
                md5: questionObject.md5
            },
            success: function (res) {
                const $questionDiv = $(res);
                $questionDiv.css({
                    maxHeight: 0,
                    opacity: 0
                });
                $questionsDiv.append($questionDiv);
                $questionDiv.animate({
                    maxHeight: 1000,
                    opacity: 1
                }, 200, "easeOutQuad");
            }
        });
    } else {
        let deletedFromCurrentPartition = true;
        const $questionDiv = $questionDivs.eq(0);
        for (const partitionName of questionObject.partitions) {
            if (containsPath(partitionName) && $questionDiv.length > 0) {//if the question is deleted from the current partition
                deletedFromCurrentPartition = false;
            }
        }
        if (deletedFromCurrentPartition) {
            $questionDiv.animate({
                height: 0,
                opacity: 0
            }, 200, "easeOutQuad", function () {
                setTimeout(function () {
                    $questionDiv.remove();
                }, 100);
            });
        }
        const $questionContentDiv = $questionDiv.find(".questionContent");
        const $questionTypeDiv = $questionDiv.find(".questionType");
        const $questionChoicesDiv = $questionDiv.find(".questionChoices");
        $questionDiv.find(".imageDiv").remove();
        initQuestionViewImage(questionObject.md5);

        if ($questionsDiv.length > 0) {
            showTip("info", "Question updated:" + questionObject.md5);
        }
        $questionContentDiv.text(questionObject.content);
        $questionTypeDiv.text(questionObject.type);
        $questionChoicesDiv.empty();
        for (const choice of questionObject.choices) {
            let correctTextBlock = '';
            if (choice.correct === "true") {
                correctTextBlock = 'correct';
            }
            $questionChoicesDiv.append(`<div class="questionChoice" ${correctTextBlock} rounded >${choice.content}</div>`);
        }
    }

    for (let partitionId of questionObject.partitionIds) {
        const $question = $("div#partition" + partitionId + " .question" + questionObject.md5);
        if ($question.length === 0) {
            const $partitionDiv = $(`
<div rounded clickable class="question${questionObject.md5}" style="height: 21px;overflow: hidden;display: flex;flex-direction: row" onclick="switchToQuestion('${questionObject.md5}',this)">
<div class="pointer" style="width: 10px">•</div>
<div style="flex: 1;line-height: 21px">
${questionObject.content}
</div>
</div>`);
            $partitionDiv.hide(0);
            const $questions = $("#partition" + partitionId).children().eq(1);
            $questions.append($partitionDiv);
            if ($questions.children().eq(0).attr("class") === "empty") {
                $questions.children().eq(0).animate({
                    opacity: 0,
                    height: 0
                }, 200, "easeInOutCubic", function () {
                    $questions.children().eq(0).remove();
                });
            }
            $partitionDiv.show(200, "easeInOutCubic");
        } else {
            $question.children().eq(1).text(questionObject.content);//TODO animate
        }
    }
}

function newQuestionOf(partitionId) {
    editQuestion("", partitionId, false);
    /*let $button = $(button);
    let $partitionDiv = $button.parent().parent();
    let $questionsDiv = $partitionDiv.children().eq(1);
    let questionMd5 = md5(new Date().getTime().toString()+Math.random().toString());
    let $newQuestionDiv = $(`
<div rounded clickable initPartition="${$partitionDiv.attr('id').substring(9)}" class="question${questionMd5}" style="height: 21px;overflow: hidden;display: flex;flex-direction: row" onclick="switchToQuestion('${questionMd5}',this)">
<div class="pointer" style="width: 10px">•</div>
<div style="flex: 1;line-height: 21px"></div>
</div>`);
    if ($questionsDiv.children().eq(0).attr("class") === "empty") {
        $questionsDiv.children().eq(0).animate({
            opacity: 0,
            height: 0
        }, 200, "easeInOutCubic", function () {
            $questionsDiv.children().eq(0).remove();
        });
    }
    $newQuestionDiv.css("maxHeight",0);
    $questionsDiv.append($newQuestionDiv);
    $newQuestionDiv.animate({
        maxHeight:100
    },100,"easeOutCubic");*/
}