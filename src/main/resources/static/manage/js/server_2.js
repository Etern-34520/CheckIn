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
    const messageJson = {
        "type": "addPartition",
        "partitionName": partitionName
    };
    sendMessage(JSON.stringify(messageJson), function (event) {
        console.log(event);
    });
}

function newPartition() {
    let newPartitionButtonHtml =
        '<button class="partitionButton" id="newPartitionButton" preText editing="true"><input type="text" value="New Partition"></button>';
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

function switchToPartition(button) {
    $(".partitionButton").removeAttr("selected");
    let $button = $(button);
    $button.attr("selected", "true");
    let partitionName = $button.text();
    let $right = $("#right");
    removePathAfter("Questions", false, false);
    $.ajax({
        url: "page/partitionQuestion",
        method: "post",
        data: "name=" + partitionName + "",
        success: function (res) {
            const leftHtml = $("#left .subContentRoot").html();
            transitionPage($right, res, partitionName, function () {
                transitionPage($right, res);
                transitionPage($("#left"), leftHtml);
            });
        },
        error: function (res) {
            showTip("error", res.status);
        }
    });
}

function updatePartition(partitionNames) {
    let $partitionButtons = $("#partitionButtons > button");
    const existedPartitionNames = [];
    if ($partitionButtons.length === 0) {
        let removedPartitionName = "";
        for (const partitionName of partitionNames) {
            if (!containsPath(partitionName)) {
                removedPartitionName = partitionName;
                break;
            }
        }
        if (removedPartitionName !== "") {
            removePathAfter("Questions", false, true);
            showTip("info", "Partition " + removedPartitionName + " has been deleted", true);
        }
    } else {
        for (const partitionButton of $partitionButtons) {
            let partitionButtonText = partitionButton.innerText;
            if (partitionNames.includes(partitionButtonText)) {
                existedPartitionNames.push(partitionButtonText);
            } else if (partitionButtonText !== "+") {
                $(partitionButton).remove();
                if (containsPath(partitionButtonText)) {
                    removePathAfter(partitionButtonText, true, true);
                    showTip("info", "Partition " + partitionButtonText + " has been deleted", true);
                }
            }
        }
        for (const partitionName of partitionNames) {
            if (!existedPartitionNames.includes(partitionName)) {
                let partitionButtonHtml = `<button class="partitionButton" onclick="switchToPartition(this)" preText editing="false">${partitionName}</button>`;
                let $partitionButton = $(partitionButtonHtml);
                let $addPartitionButton = $("#addPartitionButton");
                $addPartitionButton.css({
                    height: 0,
                    marginTop: 0,
                    marginBottom: 0,
                    paddingTop: 0,
                    paddingBottom: 0
                });
                $addPartitionButton.before($partitionButton);
                $addPartitionButton.animate({
                    height: 40,
                    marginTop: 4,
                    marginBottom: 4,
                    paddingTop: 1,
                    paddingBottom: 1
                }, 100, "easeOutQuad");
            }
        }
    }
}

function removeQuestionDiv(questionMD5) {
    let $questionMD5Divs = $("div.questions > div.question div.questionMD5");
    for (const questionMD5Div of $questionMD5Divs) {
        let $questionMD5Div = $(questionMD5Div);
        if ($questionMD5Div.text() === questionMD5) {
            let $questionDiv = $questionMD5Div.parent().parent();
            $questionDiv.animate({
                height: 0,
                opacity: 0
            },200,"easeOutQuad",function (){
                setTimeout(function (){
                    $questionDiv.remove();
                },100);
            });
            break;
        }
    }
}

function updateQuestionDiv(questionJsonData) {
    let questionObject = JSON.parse(questionJsonData);

}