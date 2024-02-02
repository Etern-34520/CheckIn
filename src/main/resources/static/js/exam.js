function boxMullarRandom(mean, std) {
    let u = 0, v = 0, w = 0;
    do {
        //获得两个（-1,1）的独立随机变量
        u = Math.random() * 2 - 1;
        v = Math.random() * 2 - 1;
        w = u * u + v * v;
    } while (w === 0 || w >= 1)
    //Box-Muller转换
    let c = Math.sqrt((-2 * Math.log(w)) / w);
    return Math.round(mean + (u * c) * std);
}

function slideDownWithFadeIn($element) {
    $element.slideDown(800, "easeOutQuad");
    $element.animate({
        opacity: 1
    }, {
        duration: 800,
        easing: "easeOutQuad",
        queue: false
    });
}

function slideUpWithFadeOut($element) {
    $element.slideUp(800, "easeOutQuad");
    $element.animate({
        opacity: 0
    }, {
        duration: 800,
        easing: "easeOutQuad",
        queue: false
    });
}

let questionsArray = [];
let idToQuestionImage = {};
let uploadData;

let item = localStorage.getItem("examProgress");
if (item !== null && item !== undefined) {
    uploadData = JSON.parse(item);
} else {
    uploadData = {};
}

let currentIndex = -1;

let qqNumber1 = 0;
let selectLock = false;
let started = false;


function showHomePage($title, windowHeight, $background) {
    $title.css("opacity", 0);
    setTimeout(function () {
        $title.animate({
            marginTop: windowHeight / 8,
        }, {
            duration: 800,
            easing: "easeInOutQuad",
            queue: false
        });
        const $detail = $("#detail");
        setTimeout(function () {
            $detail.fadeTo(2000, 1, "easeOutQuad");
        }, 800);
        const $down = $detail.find("div.down");
        const downFloat = function () {
            $down.animate({
                marginTop: 0,
            }, 1000, "easeInOutQuad", function () {
                $down.animate({
                    marginTop: 10,
                }, 1000, "easeInOutQuad", downFloat);
            });
        }
        downFloat();
    }, 1000);
    $title.animate({
        opacity: 1,
    }, {
        duration: 2000,
        easing: "easeInOutCubic",
        queue: false
    });
    const $content = $("#content");
    let pageIndex = 0;
    $content.on("scroll", function () {
        if ($content[0].scrollTop - $content.children().eq(0).height() / 3 > 0) {
            if (pageIndex === 1) return;
            pageIndex = 1;
            let i = 0;
            for (const child of $background.children()) {
                $(child).attr("state", i % 4 + 1);
                i++;
            }
            const $selectPartitionTitle = $("#selectPartitionTitle");
            $selectPartitionTitle.animate({
                marginTop: windowHeight / 6,
                opacity: 1
            }, 800, "easeOutSine", function () {
                $("#step2").fadeTo(1000, 1, "easeOutQuad");
            });
            // $selectPartitionTitle.fadeTo(1000, 1, "easeOutQuad");
        } else if (pageIndex === 1) {
            pageIndex = 0;
            $background.children().attr("state", "0");
            const $selectPartitionTitle = $("#selectPartitionTitle");
            $("#step2").fadeTo(1000, 0, "easeOutQuad");
            $selectPartitionTitle.animate({
                marginTop: 0,
                opacity: 0
            }, 800, "easeOutSine");
        }
    });
    const $qqNumberInputDiv = $("#qqNumberInputDiv");
    const $startButton = $qqNumberInputDiv.find("button#start");
    let qqNumber = 0;
    $("#partitions > div[clickable]").on("click", function (e) {
        const $target = $(e.target);
        if ($target.attr("selected") === "selected") $target.removeAttr("selected");
        else $target.attr("selected", "selected");
        const selectedCount = $("#partitions > div[selected]").length;
        if (selectedCount <= partitionMaxSelectCount && selectedCount >= partitionMinSelectCount) {
            slideDownWithFadeIn($qqNumberInputDiv);
            const $qqNumberInput = $qqNumberInputDiv.find("input#qqNumberInput");
            $qqNumberInput.on("input", function () {
                qqNumber = $qqNumberInput.val();
                if (qqNumber.length >= 5) {
                    slideDownWithFadeIn($startButton);
                } else {
                    slideUpWithFadeOut($startButton);
                }
            });
        } else {
            slideUpWithFadeOut($qqNumberInputDiv);
        }
    });
    $startButton.on("click", function () {
        if (started) return;
        started = true;
        $.cookie("qqNumber", qqNumber);
        qqNumber1 = qqNumber;
        $startButton.text("loading...");
        $startButton.attr("disabled","disabled");
        $.ajax({
            url: "./../data/reload/",
            method: "post",
            data: {
                qq: qqNumber,
                partitionIds: $("#partitions > div[selected]").map(function () {
                    return $(this).attr("id");
                }).get().join(",")
            },
            success: function (res) {
                startExam(res, $content, $background);
            },
            error: function (res) {
                started = false;
                $startButton.removeAttr("disabled");
                $startButton.text("开始答题");
            }
        })
    });
}

let questionImageLoadingCount = 0;

function startExam(res, $content, $background) {
    questionsArray = JSON.parse(res);
    let index = 0;
    for (const question of questionsArray) {
        if (question.type.endsWith("WithImages")) {
            questionImageLoadingCount++;
            $.ajax({
                url: "./../question/image/" + question.id + "/",
                method: "get",
                success: function (res) {
                    idToQuestionImage[question.id] = res;
                    questionImageLoadingCount--;
                }
            });
        }
        let hasProgress = false;
        if (uploadData !== {}) {
            hasProgress = true;
        }
        let uploadDatum = uploadData[question.id];
        if (uploadDatum === undefined) {
            if (question.type.includes("Single")) {
                uploadData[question.id] = {
                    id: question.id,
                    index: index,
                    completed: false,
                    answer: null,
                }
            } else if (question.type.includes("Multiple")) {
                uploadData[question.id] = {
                    id: question.id,
                    index: index,
                    completed: false,
                    answer: [],
                }
            }
        } /*else {
            if (uploadDatum.completed) {

            }
        }*/
        index++;
    }
    $content.animate({
        scale: 0.8,
        opacity: 0
    }, 1000, "easeInQuad", function () {
        let waiting = false;
        function waitImage(func){
            if (questionImageLoadingCount!==0) setTimeout(function () {
                if (waiting===false) {
                    $content.html("<div class='page' style='text-align: center'>Loading...</div>");
                    $content.animate({
                        opacity: 1
                    }, 600, "easeInOutCubic")
                    waiting = true;
                }
                waitImage(func)
            },500);
            else {
                if (waiting) {
                    $content.stop();
                    $content.animate({
                        opacity: 0
                    }, 600, "easeInOutCubic",function () {
                        func();
                    })
                    waiting = false;
                } else {
                    $content.html("");
                    func();
                }
            }
        }
        waitImage(function (){
            $content.html("");
            pageIndex = 2;
            let index = 0;
            for (const backgroundChild of $background.children()) {
                $(backgroundChild).attr("state", 10 + index % 4);
                index++;
            }
            let selectorContentHtml = "";
            index = 0;
            for (const question of questionsArray) {
                index++;
                const selectorUnitHtml = `
<div class="selector" clickable>${index}</div>`;
                selectorContentHtml += selectorUnitHtml;
            }
            // noinspection CssInvalidPropertyValue
            const $questionPage = $(`
<div class="page" id="examPage" style="display: flex;flex-direction: column;align-items: center;height: 100%">
    <div id="top" style="display: flex;flex-direction: row;margin-top: 5%;width: 90%" ">
        <label id="questionCountLabel" style="margin-right: 8px">Q1</label>
        <label id="questionTypeLabel"></label>
    </div>
    <div component_type="progressBar" style="
        display:flex;
        flex-direction: row;
        height: 8px;
        width: 90%;
        border-radius: 5px;
        flex: none;
        align-self: center;
        margin: 8px 0;
        border: solid rgba(255,255,255,0.1) 1px
    "><div style="background:rgba(146, 206, 255, 0.4);border-radius: 4px"></div></div>
    <div id="questionDiv"></div>
    <div id="selectors">
        <div style="rotate: 90deg;align-self: center;margin: 20px;opacity: 0;transition: 0.4s;transition-timing-function: ease-out" class="down" id="pointerLeft">
            <div></div>
            <div></div>
        </div>
        <div class="noScrollBar" id="questionIndexPointers">
    ${selectorContentHtml}
        </div>
        <div style="rotate: -90deg;align-self: center;margin: 20px;opacity: 0;transition: 0.4s;transition-timing-function: ease-out" class="down" id="pointerRight">
            <div></div>
            <div></div>
        </div>
        <button id="submitButton" highlight style="
    font-size: 16px;
    padding: 0;
    margin: 4px 0;
    display: none;
    text-wrap: nowrap;
    opacity: 0;
    width: 0;
    border-width: 0;">提交</button>
    </div>
</div>
`);
            $questionPage.appendTo($content);
            const $questionDiv = $questionPage.find("#questionDiv");
            const $selectors = $questionPage.find("#selectors");
            const $pointerLeft = $selectors.find("#pointerLeft");
            const $pointerRight = $selectors.find("#pointerRight");
            const $questionIndexPointer = $selectors.find("#questionIndexPointers");
            const $questionCountLabel = $questionPage.find("#questionCountLabel");
            const $questionTypeLabel = $questionPage.find("#questionTypeLabel");

            let completedCount = 0;
            let allCompleted = true;
            for (let questionId in uploadData) {
                let uploadDatum1 = uploadData[questionId];
                if (uploadDatum1.completed) {
                    $("#questionIndexPointers > div").eq(uploadDatum1.index).css("color", "rgb(146, 206, 255)");
                    completedCount++;
                } else {
                    allCompleted = false;
                }
            }
            if (allCompleted) {
                const $submitButton = $("#submitButton");
                $submitButton.css("display", "block");
                $submitButton.animate({
                    opacity: 1,
                    width: 72,
                    marginLeft: 4,
                    marginRight: 4,
                    paddingLeft: 16,
                    paddingRight: 16,
                    borderWidth: 4,
                }, 400, "easeOutQuad");
                $submitButton.on("click", submit);
            }
            const $progressBar = $("div[component_type=progressBar] > div");
            $progressBar.animate({
                width: `${completedCount / questionsArray.length * 100}%`
            }, 400, "easeOutQuad");

            function selectQuestion(index) {
                if (selectLock) return;
                selectLock = true;
                const question = questionsArray[index];
                if (currentIndex < index) {
                    $questionDiv.animate({
                        marginLeft: -100,
                        marginRight: 100,
                        opacity: 0
                    }, 400, "easeInQuad", function () {
                        $questionDiv.css({
                            marginLeft: 100,
                            marginRight: -100
                        });
                        $questionDiv.html(generateQuestionHtml(question));
                        initAction(question);
                    });
                } else if (currentIndex > index) {
                    $questionDiv.animate({
                        marginLeft: 100,
                        marginRight: -100,
                        opacity: 0
                    }, 400, "easeInQuad", function () {
                        $questionDiv.css({
                            marginLeft: -100,
                            marginRight: 100,
                        });
                        $questionDiv.html(generateQuestionHtml(question));
                        initAction(question);
                    });
                } else {
                    selectLock = false;
                    return;
                }
                $questionCountLabel.text(`Q${index + 1}`);
                $questionTypeLabel.text(question.type);
                currentIndex = index;
                $questionDiv.animate({
                    opacity: 1,
                    marginRight: 0,
                    marginLeft: 0
                }, 400, "easeOutQuad", function () {
                    selectLock = false;
                });
                const $selector = $(".selector");
                $selector.removeAttr("selected");
                $selector.eq(index).attr("selected", "selected");
            }

            function initAction(question) {
                const uploadQuestionData = uploadData[question.id];
                if (uploadQuestionData.completed) {
                    if (question.type.includes("Single")) {
                        $(`#${uploadQuestionData.answer}`).attr("selected", "selected");
                    } else if (question.type.includes("Multiple")) {
                        for (const answer of uploadQuestionData.answer) {
                            $(`#${answer}`).attr("selected", "selected");
                        }
                    }
                }
                const $choices = $(".choice");
                for (const choice of $choices) {
                    const $choice = $(choice);
                    $choice.on("click", function (e) {
                        if (question.type.includes("Single")) {
                            if ($choice.attr("selected") === "selected") {
                                $choice.removeAttr("selected");
                            } else {
                                $choices.removeAttr("selected");
                                $choice.attr("selected", "selected");
                            }
                            updateQuestionTempData(question);
                        } else if (question.type.includes("Multiple")) {
                            if ($choice.attr("selected") === "selected") {
                                $choice.removeAttr("selected");
                            } else {
                                $choice.attr("selected", "selected");
                            }
                            updateQuestionTempData(question);
                        }
                    });
                }
            }

            setTimeout(function () {
                const onPointerMousewheel = function (e) {
                    const deltaY = e.originalEvent.deltaY;
                    if (deltaY !== 0 && deltaY !== undefined) $questionIndexPointer[0].scrollLeft += deltaY;
                    if ($questionIndexPointer[0].scrollLeft <= 10) {
                        $pointerLeft.css("opacity", 0);
                    } else {
                        $pointerLeft.css("opacity", 1);
                    }
                    if ($questionIndexPointer[0].clientWidth + $questionIndexPointer[0].scrollLeft >= $questionIndexPointer[0].scrollWidth - 10) {
                        $pointerRight.css("opacity", 0);
                    } else {
                        $pointerRight.css("opacity", 1);
                    }
                };
                onPointerMousewheel({originalEvent: {deltaY: 0}});
                const sizeChangeListener = new ResizeObserver(function () {
                    onPointerMousewheel({originalEvent: {deltaY: 0}});
                });
                sizeChangeListener.observe($questionIndexPointer[0]);
                $questionIndexPointer.on("mousewheel", onPointerMousewheel);
                $questionIndexPointer.on("scroll", onPointerMousewheel);
                $questionIndexPointer.children().on("click", function (e) {
                    const $selector = $(e.originalEvent.target);
                    selectQuestion($selector.index())
                });
                let offset = 0;
                $questionDiv.on("mousewheel", function (e) {
                    if ($(e.target).parent().attr("id") === "imagesDiv") return;
                    offset += e.originalEvent.deltaY;

                    function a(e) {
                        const currentOffset = offset;
                        setTimeout(function () {
                            let index = currentIndex + Math.round(currentOffset / 300);
                            if (index >= questionsArray.length) index = questionsArray.length - 1;
                            else if (index < 0) index = 0;
                            if (currentOffset === offset) {
                                if (offset >= 300) {
                                    offset = 0;
                                    selectQuestion(index);
                                } else if (offset <= -300) {
                                    offset = 0;
                                    selectQuestion(index);
                                }
                            } else {
                                if ((currentOffset >= 300 || currentOffset <= -300) && !selectLock) {
                                    $questionCountLabel.text(`Q${index + 1}`);
                                    $questionTypeLabel.text(questionsArray[index].type);
                                }
                                a(e);
                            }
                        }, 400);
                    }

                    a(e);
                });
                $(window).on("keydown", function (e) {
                    // console.log(e);
                    switch (e.key) {
                        case "ArrowUp":
                        case "ArrowLeft":
                            if (currentIndex > 0) selectQuestion(currentIndex - 1);
                            break;
                        case "ArrowDown":
                        case "ArrowRight":
                            if (currentIndex < questionsArray.length - 1) selectQuestion(currentIndex + 1);
                            break;
                        default:
                            if (isNumber(e.key)) {
                                $(".choice").eq(Number(e.key) - 1).trigger("click");
                            }
                            break;
                    }
                });
                selectQuestion(0);
            }, 500);
            $content.css("scale", 1);
            $content.animate({
                opacity: 1
            }, 1000, "easeOutQuad");
        });
    });
}

$(function () {
    const $background = $("#background");
    $background.css("opacity", 0);
    const windowWidth = window.innerWidth;
    const windowHeight = window.innerHeight;
    const $title = $("#title");
    $title.css("marginTop", windowHeight / 2 - $title.height() / 2);
    for (const i of new Array(Math.round(windowWidth / 120))) {
        // const widthAndHeight = Math.round(400 + windowHeight / 8 - Math.random() * windowHeight / 4);
        const widthAndHeight = boxMullarRandom(400, windowHeight * windowWidth / 40000);
        // const translateX = Math.round(-(50 + windowWidth / 8 - Math.random() * windowWidth / 4));
        // const translateY = Math.round(-(50 + windowHeight / 8 - Math.round(Math.random() * windowHeight / 4)));
        const translateX = boxMullarRandom(-50, windowWidth / 12);
        const translateY = boxMullarRandom(-50, windowHeight / 12);
        const originalR = 146;
        const originalG = 206;
        const originalB = 255;
        const bound = 25;
        // let r = Math.round(originalR + Math.random() * bound);
        // let g = Math.round(originalG + Math.random() * bound);
        // let b = Math.round(originalB + Math.random() * bound);
        let r = boxMullarRandom(originalR, bound);
        let g = boxMullarRandom(originalG, bound);
        let b = boxMullarRandom(originalB, bound);
        if (r > 255) r = 255;
        if (g > 255) g = 255;
        if (b > 255) b = 255;
        if (r < 0) r = 0;
        if (g < 0) g = 0;
        if (b < 0) b = 0;
        const color = `rgb(${r},${g},${b})`;
        // const color = `rgb(${Math.round(Math.random() * 255)},${Math.round(Math.random() * 255)},${Math.round(Math.random() * 255)})`;
        // const color = "rgba(146, 206, 255, 1)";
        // background: radial-gradient(rgba(146, 206, 255, 1) 0px, rgba(0, 0, 0, 0) 60%);
        /*rgba(146, 206, 255, 1)*/
        $(`
<div style="
width: ${widthAndHeight}px;
height: ${widthAndHeight}px;
transform: translate(${translateX}%, ${translateY}%);
background: radial-gradient(${color} 0px, rgba(0, 0, 0, 0) 60%);
" state="0"></div>`).appendTo($background)
    }
    $background.fadeTo(2000, 1, "easeOutQuad");
    let qq = $.cookie("qqNumber");
    const $content = $("#content");
    if (qq !== undefined) {
        started = true;
        $.ajax({
            url: "./../data/",
            method: "post",
            data: {
                qq: qq
            },
            success: function (res) {
                qqNumber1 = qq;
                startExam(res, $content, $background);
            },
            error: function (res) {
                showHomePage($title, windowHeight, $background);
                started = false;
            }
        })
    } else {
        showHomePage($title, windowHeight, $background);
    }
});

function isNumber(str) {
    const n = Number(str);
    return !isNaN(n);
}

function updateQuestionTempData(question) {
    const $selectedChoices = $(".choice[selected]");
    const completed = $selectedChoices.length > 0;
    uploadData[question.id].completed = completed;
    if (completed) {
        $("#questionIndexPointers > div").eq(currentIndex).css("color", "rgb(146, 206, 255)");
        let allCompleted = true;
        for (const questionDataId in uploadData) {
            if (!uploadData[questionDataId].completed) {
                allCompleted = false;
                break;
            }
        }
        if (allCompleted) {
            const $submitButton = $("#submitButton");
            $submitButton.css("display", "block");
            $submitButton.animate({
                opacity: 1,
                width: 72,
                marginLeft: 4,
                marginRight: 4,
                paddingLeft: 16,
                paddingRight: 16,
                borderWidth: 4,
            }, 400, "easeOutQuad");
            $submitButton.on("click", submit);
        }
    } else {
        $("#questionIndexPointers > div").eq(currentIndex).css("color", "white");
        const $submitButton = $("#submitButton");
        if ($submitButton.css("display") !== "none") {
            $submitButton.off("click");
            $submitButton.animate({
                opacity: 0,
                width: 0,
                marginLeft: 0,
                marginRight: 0,
                padding: 0,
                borderWidth: 0,
            }, 400, "easeOutQuad", function () {
                $submitButton.css("display", "none");
            });
        }
    }
    if (question.type.includes("Single")) {
        uploadData[question.id].answer = $selectedChoices.attr("id");
    } else if (question.type.includes("Multiple")) {
        uploadData[question.id].answer = $selectedChoices.map(function () {
            return $(this).attr("id");
        }).get();
    }
    let completedCount = 0;
    for (const questionDataId in uploadData) {
        if (uploadData[questionDataId].completed) completedCount++;
    }
    const $progressBar = $("div[component_type=progressBar] > div");
    $progressBar.animate({
        width: `${completedCount / questionsArray.length * 100}%`
    }, 400, "easeOutQuad");
    localStorage.setItem("examProgress", JSON.stringify(uploadData));
}

function submit() {
    const $submitButton = $("#submitButton");
    $submitButton.off("click");
    $.ajax({
        url: "./../exam/submit/",
        method: "post",
        contentType: "application/json",
        dataType: "json",
        data: JSON.stringify({
            qq: qqNumber1,
            data: uploadData
        }),
        success: function (res) {
            localStorage.removeItem("examProgress");
            $.removeCookie("qqNumber");
            const $examPage = $("#examPage");
            $examPage.css("scale", 1);
            $examPage.animate({
                scale: 0.8,
                opacity: 0
            }, 1000, "easeInQuad", function () {
                $examPage.remove();
                const $resultPage = $(`
<div class="page" id="resultPage">
<div style="
margin-top:10%;
width: 300px;
aspect-ratio: 1;
max-width: 90%;
border-radius: 50%;
background-size: 100% 100%;
background-image: url('https://q1.qlogo.cn/g?b=qq&nk=${qqNumber1}&s=640');"></div>
<div style="margin-top: 4px">作答情况</div>
<div style="margin-top: 4px">QQ: ${res.qq}</div>
<div style="margin-top: 4px">成绩: ${res.score}</div>
<div style="margin-top: 4px">正确率: ${res.correctCount}/${res.questionCount}</div>
<div>${res.message}</div>
</div>
                `);
                const $content = $("#content");
                $resultPage.appendTo($content);
                $resultPage.css({
                    scale: 1.2,
                    opacity: 0
                })
                $resultPage.animate({
                    scale: 1,
                    opacity: 1
                }, 1000, "easeOutQuad");
            });
        },
        error: function (data) {
            $submitButton.on("click", submit);
            console.log(data);
        }
    })
}

function generateQuestionHtml(question) {
    let choicesHtml = "";
    const choices = question.choices;
    let count = 1;
    for (const choiceId in choices) {
        choicesHtml += `
<div class="choice" id="${choiceId}" clickable rounded>
    <div></div>
    <label>( ${count} )</label>
    <div>${choices[choiceId]}</div>
</div>`;
        count++;
    }
    let imagesDivHtml = "";
    if (question.type.endsWith("WithImages")) {
        // const imagesCount = question.imagesCount;

        for (const imageName in idToQuestionImage[question.id].imagesBase64) {

            // noinspection CssUnknownTarget
            imagesDivHtml += `
    <img
  src="${idToQuestionImage[question.id].imagesBase64[imageName]}"
  alt="question image ${imageName}"/>`;
        }
    }
    return `
<div id="questionMain">
    <div style="display: flex;flex-direction: column;flex: 1">
        <div style="display: flex;flex-direction: column">
            <div style="font-size: 20px">${question.content}</div>
        </div>
        <div class="choices">
            ${choicesHtml}
        </div>
    </div>
    <div id="imagesDiv">
        ${imagesDivHtml}
    </div>
</div>
`;
}
