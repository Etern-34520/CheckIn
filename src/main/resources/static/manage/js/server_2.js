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
    const messageJson = {
        "type": "addPartition",
        "partitionName": partitionName
    };
    sendMessage(JSON.stringify(messageJson), function (event) {
        console.log(event);
    });
}

function newPartition() {

    /*let $oldNewPartitionButton = $("#newPartitionButton");
    $oldNewPartitionButton.remove();*/
    let newPartitionButtonHtml =
        '<button class="partitionButton" id="newPartitionButton" preText><input type="text" value="New Partition"></button>';
    let $newPartitionButton = $(newPartitionButtonHtml);
    // $newPartitionButton.hide();//TODO
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
    $.ajax({
        url: "page/partitionQuestion",
        method: "post",
        data: "name=" + partitionName + "",
        success: function (res) {
            removePathContainsAfter("Questions")
            easePage($right, res, partitionName);
            // $right.html(res);
        },
        error: function (res) {
            showTip("error", res.status);
        }
    })
    // getPage("page_partition", function (event) {
    //     $right.text(event);
    //     $("#partitionName").text(partitionName);
    //     changePath(partitionName);
    //     removePathContainsAfter(partitionName);
    // });
}

function updatePartition(partitionNames) {
    let $partitionButtons = $("#partitionButtons > button");
    const existedPartitionNames = [];
    for (const partitionButton of $partitionButtons) {
        existedPartitionNames.push(partitionButton.innerText);
    }
    for (const partitionName of partitionNames) {
        if (!existedPartitionNames.includes(partitionName)) {
            let partitionButtonHtml = `<button class="partitionButton" onclick="switchToPartition(this)" preText>${partitionName}</button>`;
            let $partitionButton = $(partitionButtonHtml);
            $("#addPartitionButton").before($partitionButton);
        }
    }
}

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
    $.ajax({
        url: url,
        method: "post",
        data: data,
        success: function (res) {
            let $right = $("#right");
            // removePathContainsAfter("Edit Question")
            easePage($right, res, "Edit Question");
            // $right.html(res);
        },
        error: function (res) {
            showTip("error", res.status);
        }
    })
}


function addQuestion(){
/*    openAjax('add' , function (respData) {
        console.log(respData);
        if (respData === "success") {
            showTip('success', '添加成功');
            switchToPage(1);
        } else if (respData === "failed:have no access") {
            showTip('error', '权限不足');
        } else if (respData === "failed:illegal data") {
            showTip('error', '添加失败：数据错误');
        } else if (respData.toString().startsWith("failed:")) {
            showTip('error', '添加失败：' + respData.toString().substring(7));
        }
    });
    enableScroll();*/
}


function addOption() {
    var optionsDiv = document.getElementById('optionsDiv');
    var div = document.createElement('div');
    div.className="optionDiv"
    var index = optionsDiv.children.length;
    $(div).slideUp(0);
    div.innerHTML = `
<input type="checkbox" name="correct`+(index+1)+`" id="correct`+(index+1)+`" value="true">
<input type="text" style="margin-left: 8px" name="`+(index+1)+`"  id="`+(index+1)+`" value="">
<button class="deleteOptionButton" type="button" style="height: 32px;width: 32px;margin: 4px;font-size: 24px" onclick="deleteOption(this)">-</button>
`
    optionsDiv.append(div);
    $(div).slideDown(150);
    let $deleteOptionButton = $(".deleteOptionButton");
    if (document.getElementById("optionsDiv").children.length <=2){
        $deleteOptionButton.fadeOut(200,"easeInOutCubic");
    } else {
        $deleteOptionButton.fadeIn(200,"easeInOutCubic");
        $deleteOptionButton.removeAttr("disabled");
    }
}

function deleteOption(e){
    let parentNode = e.parentNode;
    const $parentNode = $(parentNode);
    $parentNode.slideUp(150,function (){
        const delFunc = function () {
            parentNode.parentNode.removeChild(parentNode);
        };
        const children = parentNode.parentNode.children;
        delFunc();
        if (children.length <=2){
            let $deleteOptionButton = $(".deleteOptionButton");
            $deleteOptionButton.attr("disabled","disabled");
            $deleteOptionButton.fadeOut(200,"easeInOutCubic");
        }
    });
}