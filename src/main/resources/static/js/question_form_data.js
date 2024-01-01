let md5ToQuestionFormDataMap = new Map();

function addQuestion(partitionId) {
    editQuestion("", partitionId);
}

function editQuestion(questionMD5, partitionId, transitionLeft = true) {
    let data = "md5=" + questionMD5;
    if (questionMD5 === undefined) {
        questionMD5 = "";
    }
    let url = "page/editQuestion";
    if (questionMD5 === "") {//new question
        data = data + "new=true";
        url = "page/newQuestion";
        if (partitionId !== undefined) {
            data = data + "&partitionId=" + partitionId;
        } else {
            data = data + "&ignorePartitionSelection=false"
        }
    }
    let $left = $("#left");
    let ajax1Over = !transitionLeft;
    let ajax2Over = false;
    if (transitionLeft)
        $.ajax({
            xhrFields: {
                withCredentials: true
            },
            url: "page/partitionQuestionLeft", method: "post", data: "", success: function (res) {
                ajax1Over = true;
                transitionPage($left, res);
            },
        });
    $.ajax({
        xhrFields: {
            withCredentials: true
        },
        url: url, method: "post", data: data, success: function (res) {
            let $right = $("#right");
            let pathBlock = "编辑";
            ajax2Over = true;
            if (transitionLeft) {
                // removePathContainsAfter("Edit Question")
                removePathAfter("题库", false, false);
                // $right.html(res);
                transitionPage($right, res, pathBlock, function () {
                });
            } else {
                transitionPage($right, res);
            }
        }, error: function (res) {
            showTip("error", res.status);
            console.error(res)
        }
    });
}

function selectQuestionPartition(partitionDiv) {
    md5ToQuestionFormDataMap.get($("#md5").val()).selectQuestionPartition(partitionDiv);
}

function switchToQuestion(questionMD5, element) {
    const questionFormData = md5ToQuestionFormDataMap.get(questionMD5);
    if (questionFormData === null || questionFormData === undefined || $(element).find(".pointer").css("opacity") !== "1") {
        editQuestion(questionMD5, undefined, false);
        return;
    }
    let formData = questionFormData.toUploadFormData();
    $.ajax({
        method: "post",
        xhrFields: {
            withCredentials: true
        },
        url: "./page/questionFormOfFormData",
        data: formData,
        processData: false,
        contentType: false,
        success: function (res) {
            transitionPage($("#right"), res)
        },
        error: function (res) {
            showTip("error", res.status);
            console.error(res)
        }
    })


    // let data = "new=true";
    // let url = "page/newQuestion";
    /*let currentQuestionFormData = md5ToQuestionFormDataMap.get(questionMD5);*/
    // const partitionId = $(element).attr("initPartition");
    /*$(element).removeAttr("initPartition");*/
    // if (currentQuestionFormData === undefined) {
    //     url = "page/editQuestion";
    //     data = "md5=" + questionMD5;
    //     if (partitionId !== undefined) {
    //         data = data + "&partitionId=" + partitionId + "&ignorePartitionSelection=true";
    //     }
    // }

    /*$.ajax({
        xhrFields: {
            withCredentials: true
        },
        url: "./page/editQuestion", method: "post", data: {md5:questionMD5}, success: function (res) {
            let $right = $("#right");
            // removePathContainsAfter("Edit Question")
            transitionPage($right, res, undefined, undefined, function () {
                let fromServer = false;
                if (currentQuestionFormData === undefined) {
                    fromServer = true;
                    currentQuestionFormData = new QuestionFormData(false);
                } else {
                    currentQuestionFormData = md5ToQuestionFormDataMap.get(questionMD5);
                }
                let $questionEditForm = $("#questionEditForm");
                let formDataMap = new Map();
                for (let key of currentQuestionFormData.keys()) {
                    formDataMap.set(key, currentQuestionFormData.get(key));
                }
                for (const key of formDataMap.keys()) {
                    let $formElement = $questionEditForm.find("*[name=" + key + "]");
                    if ((!isNaN(Number(key)) || key.startsWith("correct")) && $questionEditForm.find("*[name=" + key + "]").length === 0) {
                        addNewChoice(false);
                        $formElement = $questionEditForm.find("*[name=" + key + "]");
                    } else if (key.startsWith("question_partition_")) {
                        if (formDataMap.get(key) === "true") {
                            $formElement.parent().attr("selected", "selected");
                        }
                    }
                    if (key.startsWith("correct")) {
                        $formElement.attr("checked", "checked");
                    }
                    $formElement.val(formDataMap.get(key));
                    initQuestionForm()
                }
                if (fromServer) {
                }
                // currentQuestionFormData.loadImagesFromServer();
                else
                    currentQuestionFormData.loadCurrentImages();
            });
        }, error: function (res) {
            showTip("error", res.status);
            console.error(res);
        }
    });*/

}


function initQuestionForm() {
    try {
        let newQ = false;
        let questionFormData = md5ToQuestionFormDataMap.get($("#md5").val());
        if (questionFormData === undefined) {
            newQ = true;
            questionFormData = new QuestionFormData();
            md5ToQuestionFormDataMap.set(questionFormData.get("md5"), questionFormData);
        } else {
            questionFormData.loadImagesFromServer();
        }
        let $partitionSelections = $("#partitionSelectionsDiv");
        for (const partitionSelection of $partitionSelections.children()) {
            let $partitionSelection = $(partitionSelection);
            if ($partitionSelection.attr("selected") === "selected") {
                $partitionSelection.removeAttr("selected");
                $partitionSelection.trigger("click");
            }
            if ($partitionSelection.attr("disabled") === "disabled") {
                $partitionSelection.removeAttr("onclick");
            }
        }
        if (newQ) {
            questionFormData.originalFormData = new FormData(/*document.getElementById("questionEditForm")*/);
            // const authorQQ = $("#author").val();
            // questionFormData.originalFormData.append("author", authorQQ);
            md5ToQuestionFormDataMap.set(questionFormData.get("md5"), questionFormData);
            questionFormData.updateDataAndCheckChange();
        }
        if ($("#optionsDiv").children().length <= 2) {
            let $deleteOptionButton = $(".deleteOptionButton");
            $deleteOptionButton.attr("disabled", "disabled");
            $deleteOptionButton.fadeOut(200, "easeInOutCubic");
        }
        let $questionEditForm = $("#questionEditForm");
        $questionEditForm.find("input").on("input", function () {
            questionFormData.updateDataAndCheckChange();
        });
        $questionEditForm.find("select").on("input", function () {
            questionFormData.updateDataAndCheckChange();
        });
        $questionEditForm.find("textarea").on("input", function () {
            questionFormData.updateDataAndCheckChange();
        });
        // questionFormData.loadImagesFromServer();
    } catch (e) {
        console.error(e);
    }
}

function dataURLtoFile(dataUrl, filename) {
    let arr = dataUrl.split(','),
        mime = arr[0].match(/:(.*?);/)[1],
        bstr = atob(arr[1]),
        n = bstr.length,
        u8arr = new Uint8Array(n);
    while (n--) {
        u8arr[n] = bstr.charCodeAt(n);
    }
    return new File([u8arr], filename, {type: mime});
}

class QuestionFormData {
    /**Map<String,Blob>*/ imageNamesAndSizesToDataMap = new Map();
    /**Map<String,Blob>*/ originalImageNamesAndSizesToDataMap = new Map();

    /**FormData*/ formData;
    /**FormData*/ originalFormData;

    /**boolean*/ changed = false;

    constructor(/**boolean*/loadImagesFromServer = true) {
        const formElement = document.getElementById("questionEditForm")
        this.formData = new FormData(formElement);
        const authorQQ = $("#author").val();
        this.formData.append("author", authorQQ);
        this.originalFormData = new FormData(formElement);
        this.originalFormData.append("author", authorQQ);
        if (loadImagesFromServer) {
            setTimeout(function () {
                this.loadImagesFromServer()
            }.bind(this), 100)
        }
    }

    loadImagesFromServer() {
        let originalImageCount = 0;
        let questionMD5 = this.formData.get("md5");
        $.ajax({
            xhrFields: {
                withCredentials: true
            },
            url: "./../question/image/" + questionMD5 + "/", method: "get",
            success: function (res) {
                originalImageCount = res.count;
                if (originalImageCount >= 1) {
                    for (let i = 0; i < originalImageCount; i++) {
                        let imageName = res.names[i];
                        let imageSize = res.sizes[i];
                        let imageData = dataURLtoFile(res.imagesBase64[imageName], imageName);
                        this.addImageData(imageData, false);
                    }
                }
            }.bind(this),
            error: function (res) {
                showTip("error", res.status, false);
                console.error(res);
            }.bind(this)
        })
    }

    loadCurrentImages() {
        for (const value of this.imageNamesAndSizesToDataMap.values()) {
            this.addImageData(value);
        }
    }

    /**FormData*/ toUploadFormData() {
        this.updateDataAndCheckChange(this.formData);
        const uploadFormData = new FormData();
        for (const key of this.formData.keys()) {
            uploadFormData.append(key, this.formData.get(key));
        }
        let index = 0;
        for (const key of this.imageNamesAndSizesToDataMap.keys()) {
            uploadFormData.append("image_" + index, this.imageNamesAndSizesToDataMap.get(key));
            index++;
        }

        this.originalImageNamesAndSizesToDataMap = new Map();
        for (const key of this.imageNamesAndSizesToDataMap.keys()) {
            this.originalImageNamesAndSizesToDataMap.set(key, this.imageNamesAndSizesToDataMap.get(key));
        }

        return uploadFormData;
    }

    /**void*/ updateOriginalFormData() {
        const originalFormData = new FormData();
        for (const key of this.formData.keys()) {
            originalFormData.append(key, this.formData.get(key));
        }
        this.originalFormData = originalFormData;
        this.updateDataAndCheckChange(this.formData);
    }

    /**void*/ contentChanged() {
        // this.updateDataAndCheckChange();
        let $questionContent = $("#questionContent");
        let questionMD5 = $('#md5').val();
        let $questions = $("#partitionDivs div.question" + questionMD5 + "> div:nth-child(2)");
        if ($questions.length >= 1) {
            $questions.text($questionContent.val());
        }
    }

    /**void*/ updateDataAndCheckChange(/**FormData*/ changedFormData = new FormData(document.getElementById("questionEditForm"))) {
        const authorQQ = $("#author").val();
        changedFormData.append("author", authorQQ)
        for (const key of changedFormData.keys()) {
            let value = changedFormData.get(key);
            if (this.formData.get(key) !== value) {
                this.formData.delete(key);
                if (value !== null) {
                    this.formData.append(key, value);
                }
            }
        }
        let changedMap = new Map();
        for (const key of this.formData.keys()) {
            let value = this.formData.get(key);
            let changedValue = changedFormData.get(key);
            if (!key.startsWith("image") && changedValue !== value) {
                changedMap.set(key, changedValue);
            }
        }
        for (const key of changedMap.keys()) {
            this.formData.delete(key);
            if (changedMap.get(key) !== null) {
                this.formData.append(key, changedMap.get(key));
            }
        }
        let questionMD5 = this.formData.get("md5");

        this.changed = this.checkChange();

        this.contentChanged();
        if (this.changed) {
            $("#partitionDivs div.question" + questionMD5 + " > div.pointer").animate({
                opacity: 1
            }, 200, "easeInOutQuad");
            // questionMD5ToChangedBooleanMap.set(questionMD5, true);
        } else {
            $("#partitionDivs div.question" + questionMD5 + " > div.pointer").animate({
                opacity: 0
            }, 200, "easeInOutQuad");
            // questionMD5ToChangedBooleanMap.set(questionMD5, false);
        }

    }


    /**boolean*/ checkChange() {
        let changed = false;
        for (const key of this.formData.keys()) {
            if (key.startsWith("question_partition_") && this.formData.get(key) === "false") {
                continue;
            }
            if (this.formData.get(key) !== this.originalFormData.get(key)) {
                changed = true;
                break;
            }
        }
        if (!changed) {//已经改变了就无需再检查
            for (const key of this.originalFormData.keys()) {
                if (key.startsWith("question_partition_") && this.formData.get(key) === "false") {
                    if (this.originalFormData.get(key) === "true") {
                        changed = true;
                        break;
                    } else
                        continue;
                }
                if (this.formData.get(key) !== this.originalFormData.get(key)) {
                    changed = true;
                    break;
                }
            }
        }
        if (!changed) {
            for (const key of this.imageNamesAndSizesToDataMap.keys()) {
                if (!this.originalImageNamesAndSizesToDataMap.has(key)) {
                    changed = true;
                    break;
                }
            }
        }
        if (!changed) {
            for (const key of this.originalImageNamesAndSizesToDataMap.keys()) {
                if (!this.imageNamesAndSizesToDataMap.has(key)) {
                    changed = true;
                    break;
                }
            }
        }
        return changed;
    }

    /**void*/ addNewChoice(/**boolean*/animate = true) {
        const $optionsDiv = $("#optionsDiv");
        let index = $optionsDiv.children().length;
        const divHtml = `
<div class="optionDiv" style="display: none">
<input type="checkbox" name="correct` + (index + 1) + `" id="correct` + (index + 1) + `" value="true">
<input type="text" style="margin-left: 8px" name="` + (index + 1) + `"  id="` + (index + 1) + `" value="">
<button class="deleteOptionButton" type="button" style="height: 32px;width: 32px;margin: 4px;font-size: 24px" onclick="deleteChoice(this)">-</button>
</div>
`;
        let $optionDiv = $(divHtml);
        $optionDiv.slideUp(0);
        $optionsDiv.append($optionDiv);
        if (animate) {
            $optionDiv.slideDown(150);
        } else {
            $optionDiv.slideDown(0);
        }
        $optionDiv.find("input").on("input", function () {
            this.updateDataAndCheckChange();
        }.bind(this));
        let $deleteOptionButton = $(".deleteOptionButton");
        if ($optionsDiv.children().length <= 2) {
            $deleteOptionButton.fadeOut(200, "easeInOutCubic");
        } else {
            $deleteOptionButton.fadeIn(200, "easeInOutCubic");
            $deleteOptionButton.removeAttr("disabled");
        }
        if (animate) {
            this.updateDataAndCheckChange();
        }
    }

    /**void*/ deleteChoice(/**HTMLElement*/deleteButton) {
        let parentNode = deleteButton.parentNode;
        const $parentNode = $(parentNode);
        $parentNode.slideUp(150, function () {
            const children = parentNode.parentNode.children;
            parentNode.parentNode.removeChild(parentNode);
            if (children.length <= 2) {
                let $deleteOptionButton = $(".deleteOptionButton");
                $deleteOptionButton.attr("disabled", "disabled");
                $deleteOptionButton.fadeOut(200, "easeInOutCubic");
            }
            this.updateDataAndCheckChange();
        }.bind(this));
    }

    /**void*/ addImageData(/**Blob*/image,/**boolean*/ newImage = true) {
        let imageNameAndSize = image.name + image.size;
        const reader = new FileReader();
        reader.readAsDataURL(image);
        if (!newImage)
            this.originalImageNamesAndSizesToDataMap.set(imageNameAndSize, image);
        this.imageNamesAndSizesToDataMap.set(imageNameAndSize, image);
        reader.onload = function () {
            const img = new Image();
            img.src = reader.result;
            img.onload = function () {
                const width = (img.width / (img.height / 100));
                let $imageDiv
                if ($("#imageInput").length !== 0)
                    $imageDiv = $(`
<div id="${imageNameAndSize}" class="imageDiv" style="background-image: url('${reader.result}');width: ${width + 'px'}" rounded onmouseenter="showDeleteDiv(this);" imageNameAndSize="${imageNameAndSize}">
<div rounded text onclick="removeImage(this);"
onmouseout="hideDeleteDiv(this);">-
</div>
</div>`);
                else
                    $imageDiv = $(`<div id="${imageNameAndSize}" class="imageDiv" style="background-image: url('${reader.result}');width: ${width + 'px'}" rounded imageNameAndSize="${imageNameAndSize}"></div>`)
                $imageDiv.css({
                    marginLeft: -5, opacity: 0
                });
                $("#imagesDiv").append($imageDiv);
                $imageDiv.animate({
                    marginLeft: 0, opacity: 1
                }, 300, 'easeInOutCubic');
                this.updateDataAndCheckChange();
            }.bind(this);
        }.bind(this);
    }

    removeImageData(imageNameAndSize) {
        this.imageNamesAndSizesToDataMap.delete(imageNameAndSize);
    }

    selectQuestionPartition(partitionDiv) {
        let $partitionSelection = $(partitionDiv);
        let hiddenPartitionInput = $partitionSelection.find("input[type='hidden']");

        let partitionId = partitionDiv.id.replace("partition_select_", "");
        let selected = $partitionSelection.attr("selected") === "selected";
        if (selected) {
            if ($partitionSelection.parent().find("div[selected='selected']").length <= 1) {
                showTip("info", "至少选择一个分区");
                return;
            }
            $partitionSelection.removeAttr("selected");
            hiddenPartitionInput.attr("value", "false");
        } else {
            hiddenPartitionInput.attr("value", "true");
            $partitionSelection.attr("selected", "");
        }
        let questionMd5 = this.formData.get("md5");
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
                        let $empty = $(`<div rounded style="cursor: auto;background: rgba(0,0,0,0);" class="empty">empty</div>`);
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
<div rounded clickable class="question${questionMd5}" style="height: 21px;overflow: hidden;display: flex;flex-direction: row" onclick="switchToQuestion('${questionMd5}',this)">
<div class="pointer" style="width: 10px">•</div>
<div style="flex: 1">
${$questionContent.val()}
</div>
</div>
`);
                if ($firstChild.text().replaceAll(" ", "").replaceAll("\n", "") === "empty") {
                    $firstChild.animate({
                        opacity: 0,
                        height: 0,
                        margin: 0,
                        padding: 0
                    }, 150, "easeInQuad", function () {
                        $firstChild.remove();
                    });
                }
                $partitionDiv.css("opacity", "0");
                $partitionDiv.css("height", "0");
                $partitionDiv.css("margin", "0");
                $partitionDiv.css("padding", "0");
                $partitionDiv.css("height", "0");
                $questionsOfPartitions.append($partitionDiv);
                //padding 4 margin 6 height 20.667
                $partitionDiv.animate({
                    opacity: 1,
                    height: 20.667,
                    margin: 6,
                    padding: 4,
                }, 150, "easeOutCubic");//TODO 动画有点怪
            }
        }
        this.updateDataAndCheckChange();
    }

    get(/**String*/key) {
        return this.formData.get(key);
    }

    set(/**String*/key, /**String*/value) {
        this.formData.set(key, value);
    }

    delete(/**String*/key) {
        this.formData.delete(key);
    }

    has(/**String*/key) {
        return this.formData.has(key);
    }

    keys() {
        return this.formData.keys();
    }

    values() {
        return this.formData.values();
    }

    entries() {
        return this.formData.entries();
    }

    forEach(/**Function*/callback) {
        this.formData.forEach(callback);
    }

    [Symbol.iterator]() {
        return this.formData[Symbol.iterator]();
    }
}


//following functions for current questionFormData
function togglePartition(titleDiv) {
    const $titleDiv = $(titleDiv);
    $titleDiv.parent().parent().children().eq(1).toggle(200, function () {
        if ($titleDiv.attr('selected') === 'selected') {
            $titleDiv.removeAttr('selected');
        } else {
            $titleDiv.attr('selected', true);
        }
    });
}

function addNewChoice(animate = true) {
    md5ToQuestionFormDataMap.get($("#md5").val()).addNewChoice(animate);
}

function deleteChoice(/**HTMLElement*/deleteButton) {
    md5ToQuestionFormDataMap.get($("#md5").val()).deleteChoice(deleteButton);
}

function addImages(uploadInput) {
    const fileList = uploadInput.files;
    let currentQuestionFormData = md5ToQuestionFormDataMap.get($("#md5").val());
    for (let i = 0; i < fileList.length; i++) {
        let image = fileList[i];
        if (!currentQuestionFormData.imageNamesAndSizesToDataMap.has(image.name + image.size)) {
            currentQuestionFormData.addImageData(image);
        } else {
            showTip("info", "图片已存在");
        }
    }
    $('#imageInput')[0].value = "";
    currentQuestionFormData.updateDataAndCheckChange();
}

function showDeleteDiv(div) {
    let $childDiv = $(div).children().eq(0);
    $childDiv.animate({
        opacity: 1
    }, 200, 'easeInOutCubic');
}

function removeImage(div) {
    let currentQuestionFormData = md5ToQuestionFormDataMap.get($("#md5").val());
    let $parent = $(div).parent();
    let imageNameAndSize = $parent.attr("imageNameAndSize");//image.name + image.size;
    currentQuestionFormData.removeImageData(imageNameAndSize);
    // let parentId = $parent.attr("id");
    // let index = imageNameAndSizeArray.indexOf(parentId);
    $parent.animate({
        opacity: 0
    }, 200, 'easeInOutCubic', function () {
        $parent.animate({
            width: 0, margin: 0,
        }, 200, 'easeInOutCubic', function () {
            setTimeout(function () {
                $parent.remove();
                currentQuestionFormData.updateDataAndCheckChange();
            }, 100);
        })
    });
}

function hideDeleteDiv(childDiv) {
    let $childDiv = $(childDiv);
    $childDiv.animate({
        opacity: 0
    }, 200, 'easeInOutCubic');
}

function updateAllQuestions() {
    for (let formData of md5ToQuestionFormDataMap.values()) {
        if (formData.changed) {
            let questionMD5 = formData.get("md5");
            updateQuestionBy(questionMD5, formData);
        }
    }
}

function updateQuestionBy(questionMD5, questionFormData) {
    if (questionFormData === undefined)
        questionFormData = md5ToQuestionFormDataMap.get(questionMD5);
    $.ajax({
        url: "./data/updateQuestion/",
        method: "post",
        data: questionFormData.toUploadFormData(),
        processData: false,
        contentType: false,
        xhrFields: {
            withCredentials: true
        },
        success: function (res) {
            if (res === "success") {
                showTip("info", "upload success: " + questionMD5);
                const $questionSimpleDiv = $(`.question${questionMD5}`);
                if ($questionSimpleDiv.css("opacity") !== 1) {
                    $questionSimpleDiv.fadeTo(200, 1, "easeInOutQuad");
                }
                questionFormData.updateOriginalFormData();
            } else {
                showTip("error", "upload failed: " + questionMD5 + "; " + res, true);
            }
        },
        error: function (res) {
            showTip("error", "upload failed: " + questionMD5 + ";" + res.responseJSON.message, true);
            console.error(res);
        }
    });
}