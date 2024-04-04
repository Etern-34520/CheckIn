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
    const $selectedPartitionButton = $(".partitionButtons .partitionButton[selected='selected']");
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

function initQuestionViewImages(partitionId) {
    // for (const questionDiv of $(".questions").children()) {
    //     const $questionDiv = $(questionDiv);
    //     const questionID = $questionDiv.attr("id");
    //     initQuestionViewImage(questionID);
    // }
    $.ajax({
        url: "./../question/withImages/ofPartition/" + partitionId,
        xhrFields: {
            withCredentials: true
        },
        success: function (res) {
            if (res instanceof Array && res.length > 0) {
                for (const questionId of res) {
                    initQuestionViewImage(questionId);
                }
            }
        }
    })
}

function initQuestionViewImage(questionID) {
    $.ajax({
        url: "./../question/image/" + questionID + "/", method: "get",
        xhrFields: {
            withCredentials: true
        },
        success: function (res) {
            let originalImageCount = res.count;
            if (originalImageCount >= 1) {
                for (let i = 0; i < originalImageCount; i++) {
                    let imageName = res.names[i];
                    let imageData = dataURLtoFile(res.imagesBase64[imageName], imageName);
                    addImageData(imageData, questionID);
                }
            }
        }.bind(this),
        error: function (res) {
            showTip("error", res.status, false);
        }.bind(this)
    });
}

/**void*/ function addImageData(/**Blob*/image, /**string*/ questionID) {
    let imageNameAndSize = image.name + image.size;
    const reader = new FileReader();
    reader.readAsDataURL(image);
    reader.onload = function () {
        const img = new Image();
        img.src = reader.result;
        img.onload = function () {
            const $questionDiv = $("#" + questionID + " .questionOverviewImages");
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
    let $partitionButtons = $(".partitionButtons > .partitionButton");
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

function removeQuestionDiv(questionID) {
    let $questionIDDivs = $("div.questions > div.question div.questionID");
    for (const questionIDDiv of $questionIDDivs) {
        let $questionIDDiv = $(questionIDDiv);
        if ($questionIDDiv.text() === questionID) {
            let $questionDiv = $("#" + questionID);
            $questionDiv.animate({
                marginLeft: "150%",
                marginRight: "-150%"
            }, 200, "easeInQuad", function () {
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
            });
            break;
        }
    }

    let $question = $(".question" + questionID);
    if ($question.length > 0) {
        if ($("#id").val() !== questionID && $question.find(".pointer").css("opacity") !== 0) {
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
<div class="question" id="${questionObject.id}" rounded clickable
 onclick="idToQuestionFormDataMap = new Map();editQuestion('${questionObject.id}')">
<div class="l1">
    <div class="t1">
        <div class="questionContent" rounded>${questionObject.content}</div>
    </div>
    <div class="t2">
        <div class="questionID" rounded>${questionObject.id}</div>
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

function loadQuestion(id, $questionsDiv) {
    $.ajax({
        url: "./page/questionOverview",
        data: {
            id: id
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
}

function updateQuestionDiv(questionJsonData, animation = true) {
    let questionObject = JSON.parse(questionJsonData);

    const $questionDivs = $("#" + questionObject.id);
    const $questionsDiv = $("div.questions");
    if ($questionDivs.length === 0 && $questionsDiv.length > 0) {
        let containedInCurrentPartition = false;
        for (const partitionId of questionObject.partitionIds) {
            if (Number($("#partitionId").text()) === partitionId) {
                containedInCurrentPartition = true;
                break;
            }
        }
        if (!containedInCurrentPartition) {
            return;
        }
        loadQuestion(questionObject.id, $questionsDiv);
    } else {
        let deletedFromCurrentPartition = true;
        const $questionDiv = $questionDivs.eq(0);
        for (const partitionId of questionObject.partitionIds) {
            if (Number($("#partitionId").text()) === partitionId && $questionDiv.length > 0) {//if the question is deleted from the current partition
                deletedFromCurrentPartition = false;
            }
        }
        if (deletedFromCurrentPartition) {
            if (animation)
                $questionDiv.animate({
                    height: 0,
                    opacity: 0
                }, 200, "easeOutQuad", function () {
                    setTimeout(function () {
                        $questionDiv.remove();
                    }, 100);
                });
            else $questionDiv.remove();
        }
        const $questionContentDiv = $questionDiv.find(".questionContent");
        const $questionTypeDiv = $questionDiv.find(".questionType");
        const $questionChoicesDiv = $questionDiv.find(".questionChoices");
        const $questionEnabledSwitch = $questionDiv.find(".questionEnabled");
        const $questionAuthorDiv = $questionDiv.find(".questionAuthor");
        $questionDiv.find(".imageDiv").remove();
        initQuestionViewImage(questionObject.id);

        /*
                if ($questionsDiv.length > 0) {
                    showTip("info", "Question updated:" + questionObject.id);
                }
        */
        $questionContentDiv.text(questionObject.content);
        $questionTypeDiv.text(questionObject.type);
        // $questionEnabledDiv.text(questionObject.enabled ? "已启用" : "已禁用");
        if (questionObject.enabled) {
            switchOnIgnoreHandler($questionEnabledSwitch);
        } else {
            switchOffIgnoreHandler($questionEnabledSwitch);
        }
        $questionAuthorDiv.text(questionObject.author.name + "(" + questionObject.author.qq + ")");
        $questionChoicesDiv.empty();
        for (const choice of questionObject.choices) {
            $questionChoicesDiv.append(`<div class="questionChoice" correct="${choice.correct}" rounded >${choice.content}</div>`);
        }
    }

    for (let partitionId of questionObject.partitionIds) {
        const $question = $("div#partition" + partitionId + " div[questionId='" + questionObject.id + "']");
        if ($question.length === 0) {
            const $partitionDiv = $(`
<div rounded clickable questionId="${questionObject.id}" style="height: 21px;overflow: hidden;display: flex;flex-direction: row" onclick="switchToQuestion('${questionObject.id}',this)">
<div class="pointer" style="width: 10px">•</div>
<div style="flex: 1;line-height: 21px">
${questionObject.content}
</div>
</div>`);
            $partitionDiv.hide(0);
            const $questions = $("#partition" + partitionId).children().eq(1);
            $questions.append($partitionDiv);
            if ($questions.children().eq(0).attr("class") === "empty") {
                if (animation)
                    $questions.children().eq(0).animate({
                        opacity: 0,
                        height: 0
                    }, 200, "easeInOutCubic", function () {
                        $questions.children().eq(0).remove();
                    });
                else
                    $questions.children().eq(0).remove();
            }
            $partitionDiv.show(200, "easeInOutCubic");
        } else {
            $question.children().eq(1).text(questionObject.content);//TODO animate
        }
    }
}

function newQuestionOf(partitionId) {
    editQuestion("", partitionId, false);
}

function searchQuestion() {
    const $searchInput = $("#searchQuestionInput");
    const searchText = $searchInput.val().toLowerCase();
    let $questions = $(".question");
    if (searchText === "") {
        $questions.css("display", "flex");
        return;
    }
    const searchWords = searchText.split(" ");
    for (const question of $questions) {
        const $question = $(question);
        let content = $question.find(".questionContent").text().toLowerCase();
        let type = $question.find(".questionType").text().toLowerCase();
        let authorNameAndQQ = $question.find(".questionAuthor").text().toLowerCase();
        let id = $question.find(".questionID").text().toLowerCase();
        let editTime = $question.find(".questionEditTime").text().toLowerCase();
        let choices = "";
        for (const choice of $question.find(".questionChoice")) {
            choices = choices + $(choice).text();
        }
        choices = choices.toLowerCase();
        let match = false;
        for (const searchWord of searchWords) {
            if (searchWord === "" || searchWord === " ") continue;
            if (content.includes(searchWord) ||
                type.includes(searchWord) ||
                choices.includes(searchWord) ||
                authorNameAndQQ.includes(searchWord) ||
                id.includes(searchWord) ||
                editTime.includes(searchWord)) {
                match = true;
                break;
            }
        }
        if (match) {
            $question.css("display", "flex");
            // $question.hide(200, "easeInOutCubic");
        } else {
            $question.css("display", "none");
            // $question.show(200, "easeInOutCubic");
        }
    }
}

function toggleBatchAction() {
    const $batchActionButton = $("#batchActionButton");
    let $questionOverviewButton = $(".questionOverviewButton");
    let $questionDivs = $(".question");
    if ($batchActionButton.attr("confirm") !== "confirm") {
        $batchActionButton.attr("confirm", "confirm");
        $batchActionButton.text("取消");
        let $actions = $(`
<div id="batchActions" style="display: flex;flex-direction: row;overflow: hidden">
</div>
        `);
        $actions.append($(`<button id="selectAll" onclick="selectOrCancelSelectAll(this)" style="margin-left: 0" rounded highlight>全选</button>`));
        $actions.append($(`<button onclick="reverseSelect()" style="margin-left: 0" rounded highlight>反选</button>`));
        if (Permission["permission_delete_others_question"]) {
            $actions.append($(`<button onclick="confirmAction(function() {deleteSelectedQuestions()},$(this))" class="batch" disabled rounded highlight>删除</button>`));
        }
        if (Permission["permission_batch_move_or_copy_question"]) {
            $actions.append($(`<button onclick="moveSelectedQuestions()" class="batch" style="margin-left: 0" disabled rounded highlight>移动或复制到...</button>`));
        }
        if (Permission["permission_batch_enable_or_disable_question"]) {
            $actions.append($(`<button onclick="enableSelectedQuestions()" class="batch" style="margin-left: 0" disabled rounded highlight>启用</button>`));
            $actions.append($(`<button onclick="disableSelectedQuestions()" class="batch" style="margin-left: 0" disabled rounded highlight>禁用</button>`));
        }
        $actions.css("display", "none");
        $batchActionButton.parent().append($actions);
        $actions.show(200, "easeOutCubic");
        $questionOverviewButton.fadeOut(200, "easeOutCubic", function () {
            $questionOverviewButton.css("opacity", 0);
        });
        $questionDivs.attr("clickable", "clickable");
        $questionDivs.on("click", function (e) {
            if (e.originalEvent === undefined || $(e.originalEvent.target).attr("component_type") === "shrinkButton") return;
            let $questionDiv = $(this);
            if ($questionDiv.attr("selected") === "selected") {
                $questionDiv.removeAttr("selected");
            } else {
                $questionDiv.attr("selected", "selected");
            }
            checkBatchSelect();
        });
    } else {
        $batchActionButton.removeAttr("confirm");
        $batchActionButton.text("批量操作");
        let $batchActions = $("#batchActions");
        $batchActions.hide(200, "easeInCubic", function () {
            $batchActions.remove();
        });
        $questionOverviewButton.css("display", "");
        $questionOverviewButton.fadeTo(1, 200, "easeInCubic");
        $questionDivs.removeAttr("clickable");
        $questionDivs.removeAttr("selected");
        $questionDivs.off("click");
    }
}

function selectOrCancelSelectAll(button) {
    const $button = $(button);
    let $questions = $(".question");
    if ($questions.length === 0) return;
    if ($button.text() === "全选") {
        $questions.attr("selected", "selected");
        $button.text("取消全选");
        $("#batchActions").find("button.batch").removeAttr("disabled");
    } else {
        $questions.removeAttr("selected");
        $button.text("全选");
        $("#batchActions").find("button.batch").attr("disabled", "disabled");
    }
}

function reverseSelect() {
    let $questions = $(".question");
    if ($questions.length === 0) return;
    for (let question of $questions) {
        let $question = $(question);
        if ($question.attr("selected") === "selected") {
            $question.removeAttr("selected");
        } else {
            $question.attr("selected", "selected");
        }
    }
    checkBatchSelect();
}

function getSelectedQuestionsIds() {
    let ids = [];
    $(".question[selected='selected']").each(function () {
        let $question = $(this);
        let id = $question.find(".questionID").text();
        ids.push(id);
    });
    return ids;
}

function deleteSelectedQuestions() {
    let ids = getSelectedQuestionsIds();
    if (ids.length === 0) {
        showTip("error", "没有选择题目");
        return;
    }
    sendMessage({
        "type": "batchDeleteQuestions",
        "ids": ids
    }, function (e) {
        let message = JSON.parse(e.data);
        if (message.type === "success") {
            for (let id of ids) {
                removeQuestionDiv(id);
            }
            showTip("info", "题目已删除");
            $("button.batch").attr("disabled", "disabled");
        }
    });
}

function toggleBatchActionPartition(partitionDivButton) {
    let $partitionDivButton = $(partitionDivButton);
    if ($partitionDivButton.attr("selected") === "selected") {
        $partitionDivButton.removeAttr("selected");
        $partitionDivButton.find("input").val("false");
    } else {
        $partitionDivButton.attr("selected", "selected");
        $partitionDivButton.find("input").val("true");
    }
}

function moveSelectedQuestions() {
    let partitionIdNameMap = new Map();
    let partitionDivString = "";
    for (let partition of $(".partitionButton")) {
        partitionIdNameMap.set(partition.id.substring(15), partition.innerText);
        partitionDivString = partitionDivString + `
<div class="partitionSelectItem" style="display: flex;flex-direction: row;align-items: center" onclick="toggleBatchActionPartition(this)" 
id="partition_select_${partition.id.substring(15)}" clickable rounded>
${partition.innerText}
</div>`;
    }
    popDialog(`
<div style="padding: 8px">
    <label title>移动或复制到</label>
    <div id="partitionSelections" rounded style="background: var(--shrinkPane-background-color)">
        ${partitionDivString}
    </div>
    <div style="display: flex;flex-direction: row;padding: 4px;align-items: center">
        <div style="display: flex;flex-direction: column">
            <div style="display: flex;flex-direction: row;align-items: center">
                <input id="move" type="radio" name="actionType" value="move" checked>
                <label for="move">移动 (不保留原所在分区)</label>
            </div>
            <div style="display: flex;flex-direction: row;align-items: center">
                <input id="copy" type="radio" name="actionType" value="copy">
                <label for="copy">复制 (保留原所在分区)</label>
            </div>
        </div>
        <div class="blank"></div>
        <button id="moveOrCopyButton" highlight rounded onclick="sendMoveOrCopy()">确定</button>
    </div>
</div>
    `);
}

function sendMoveOrCopy() {
    let ids = getSelectedQuestionsIds();
    if (ids.length === 0) {
        showTip("error", "没有选择题目");
        return;
    }
    let $moveOrCopyButton = $("#moveOrCopyButton");
    $moveOrCopyButton.attr("disabled", "disabled");
    let partitionIds = [];
    for (let partition of $(".partitionSelectItem")) {
        let $partition = $(partition);
        if ($partition.attr("selected") === "selected") {
            partitionIds.push($partition.attr("id").substring(17));
        }
    }
    let actionType = $("input[name='actionType']:checked").val();
    sendMessage({
        type: "batchMoveOrCopyQuestions",
        ids: ids,
        partitionIds: partitionIds,
        actionType: actionType,
        sourcePartitionId: $(".partitionButton[selected]").attr("id").substring(15)
    }, function (e) {
        let message = JSON.parse(e.data);
        if (message.type === "success") {
            showTip("info", "题目已" + (actionType === "move" ? "移动" : "复制"));
            closeMenu();
            setTimeout(function () {
                checkBatchSelect();
            },400);
        }
    }, function () {
        $moveOrCopyButton.removeAttr("disabled");
    });
}

function disableSelectedQuestions() {
    let ids = getSelectedQuestionsIds();
    if (ids.length === 0) {
        showTip("error", "没有选择题目");
        return;
    }
    sendMessage({
        "type": "batchDisableQuestions",
        "ids": ids
    }, function (e) {
        let message = JSON.parse(e.data);
        if (message.type === "success") {
            showTip("info", "题目已禁用");
        }
    });
}

function enableSelectedQuestions() {
    let ids = getSelectedQuestionsIds();
    if (ids.length === 0) {
        showTip("error", "没有选择题目");
        return;
    }
    sendMessage({
        "type": "batchEnableQuestions",
        "ids": ids
    }, function (e) {
        let message = JSON.parse(e.data);
        if (message.type === "success") {
            showTip("info", "题目已启用");
        }
    });
}

function checkBatchSelect() {
    let $batchButtons = $("#batchActions");
    let $actionButtons = $batchButtons.find("button.batch");
    let $questions = $(".question");
    let $selectedQuestion = $questions.filter("[selected='selected']");
    if ($selectedQuestion.length === 0) {
        $actionButtons.attr("disabled", "disabled");
    } else {
        $actionButtons.removeAttr("disabled");
    }
    if ($selectedQuestion.length === $questions.length) {
        $batchButtons.find("button#selectAll").text("取消全选");
    } else {
        $batchButtons.find("button#selectAll").text("全选");
    }
}

function updateBatchCopy(questionIds, partitionIds) {
    let $partitionButton = $(".partitionButton[selected]");
    if ($partitionButton.length !== 0) {
        if (partitionIds.includes(Number($partitionButton.attr("id").substring(15)))) {
            for (const questionId of questionIds) {
                if ($("#" + questionId).length === 0)
                    loadQuestion(questionId, $(".questions"));
            }
        }
    } else if ($("#partitionDivs").length === 1) {
        for (const questionId of questionIds) {
            copyQuestionTo(partitionIds, questionId);
        }
    }
}

function updateBatchMove(questionIds, partitionIds, sourcePartitionId) {
    let $partitionButton = $(".partitionButton[selected]");
    if ($partitionButton.length !== 0) {
        let currentPartitionId = Number($partitionButton.attr("id").substring(15));
        if (!partitionIds.includes(sourcePartitionId) && sourcePartitionId === currentPartitionId) {
            let elements = [];
            for (const questionId of questionIds) {
                elements.push(document.getElementById(questionId));
            }
            $(elements).fadeOut(200, "easeInCubic", function () {
                $(this).slideUp(200, "easeInOutCubic", function () {
                    $(this).remove();
                });
            });
        } else if (partitionIds.includes(currentPartitionId)) {
            for (const questionId of questionIds) {
                if ($("#" + questionId).length === 0)
                    loadQuestion(questionId, $(".questions"));
            }
        }
    } else if ($("#partitionDivs").length === 1) {
        for (const questionId of questionIds) {
            copyQuestionTo(partitionIds, questionId);
            // let shouldRemove = false;
            // for (let partitionId of partitionIds) {
            //     if (partitionId === sourcePartitionId){
            //         shouldRemove = true;
            //     }
            // }
            // if (shouldRemove) {
            for (let partitionId of partitionIds) {
                let $questionContentItem = $(`#partitionDivs > div:not(#partition${partitionId}) .questionContentItem[questionId='${questionId}']`);
                $questionContentItem.slideUp(200, "easeInCubic", function () {
                    for (const questionContentItemSingle of $questionContentItem) {
                        let $questionContentItemSingle = $(questionContentItemSingle);
                        if ($questionContentItemSingle.parent().children().length === 1) {
                            $(`<div rounded style="cursor: auto;background: none;" class="empty">empty</div>`).appendTo($questionContentItemSingle.parent());
                        }
                    }
                    $questionContentItem.remove();
                });
            }
        }
    }
}

function copyQuestionTo(partitionIds, questionId) {
    const $questionContentItem = $(".questionContentItem[questionId='" + questionId + "']");
    let moved = false;
    for (const partitionId of partitionIds) {
        if ($(`#partition${partitionId} .questionContentItem[questionId='${questionId}']`).length === 0) {
            let $cloneItem = $questionContentItem.clone();
            // $cloneItem.slideUp(0, "linear", function () {
                $cloneItem.appendTo($("#partition" + partitionId).children(".partitionQuestionsList"));
                // $cloneItem.slideDown(200, "easeOutCubic");
            // });
            let $empty = $("#partition" + partitionId).find(".empty");
            $empty.slideUp(200, "easeInOutCubic", function () {
                $empty.remove();
            });
            moved = true;
        }
    }
    return moved;
}
