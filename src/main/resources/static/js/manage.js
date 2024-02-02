$.ajaxSetup({
    beforeSend: function (xhr) {
        xhr.setRequestHeader('Authorization', 'Bearer ' + $.cookie("token"));
    },
});

$(document).keyup(function (event) {
    let serverMenuNum = $("#serverMenu button").length;
    let userMenuNum = $("#userMenu button").length;
    const menuNum = serverMenuNum + userMenuNum;
    let numKey = event.keyCode - 48;
    if (numKey >= 0 && numKey <= menuNum && event.ctrlKey && event.altKey) {
        const inServerMenu = numKey <= serverMenuNum;
        const pageClass = inServerMenu ? "server" : "user";
        const index = inServerMenu ? numKey - 1 : numKey - 1 - serverMenuNum;
        switchToPageWithAnimate(pageClass, index, true);
    }
});

function deleteQuestion(questionMd5) {
    const messageJson = {
        "type": "deleteQuestion",
        "questionMD5": questionMd5
    };
    sendMessage(messageJson, function (event) {
        console.log(event);
    });
}

function initContextMenu() {
    const animation = {duration: 150, show: 'fadeIn', hide: 'fadeOut'};
    const partitionMenuItemsObj = {};
    if (Permission.permission_edit_partition_name)
        partitionMenuItemsObj.edit = {name: "修改"}
    if (Permission.permission_delete_partition)
        partitionMenuItemsObj.delete = {name: "删除"}
    if (Object.keys(partitionMenuItemsObj).length !== 0)
        $.contextMenu({
            selector: "button.partitionButton[editing='false']",
            animation: animation,
            callback: function (key) {
                switch (key) {
                    case "edit":
                        editPartitionName.call(this);
                        break;
                    case "delete":
                        const messageJson = {
                            "type": "deletePartition",
                            "partitionId": $(this).attr("id").substring(15)
                        };
                        sendMessage(messageJson, function (event) {
                        });
                        break;
                }
            },
            items: partitionMenuItemsObj
        });

    const partitionPaneMenuItemsObj = {};
    if (Permission.permission_delete_partition)
        partitionPaneMenuItemsObj.delete = {name: "删除"}
    if (Object.keys(partitionPaneMenuItemsObj).length !== 0)
        $.contextMenu({
            selector: ".partitionTop > div",
            animation: animation,
            callback: function (key) {
                switch (key) {
                    case "delete":
                        const messageJson = {
                            "type": "deletePartition",
                            "partitionId": $(this).parent().parent().attr("id").substring(9)
                        };
                        sendMessage(messageJson, function (event) {
                        });
                        break;
                }
            },
            items: partitionPaneMenuItemsObj
        });

    const questionMenuItemsObj = {};
    questionMenuItemsObj.edit = {name: "修改"}
    questionMenuItemsObj.delete = {name: "删除"}
    $.contextMenu({
        selector: "div.question",
        animation: animation,
        callback: function (key, options, e) {
            let $questionMD5Div = $(this).find("div.questionMD5");
            switch (key) {
                case "edit":
                    editQuestion($questionMD5Div.text());
                    break;
                case "delete":
                    deleteQuestion($questionMD5Div.text());
                    break;
            }
        },
        items: questionMenuItemsObj
    });

    $.contextMenu({
        selector: "div.partitionQuestionsList > div:not(.empty)",
        animation: animation,
        callback: function (key, options, e) {
            let $questionMD5Div = $(this);
            let questionMD5;

            function deleteQuestionDiv(questionMD5) {
                const messageJson = {
                    "type": "deleteQuestion",
                    "questionMD5": questionMD5
                };
                sendMessage(messageJson, function (message) {
                    if (JSON.parse(message.data).type === "success") {
                        // $("div.partitionQuestionsList > div.question" + messageJson.questionMD5).remove();
                        if ($("#md5").val() === questionMD5)
                            transitionPage($("#right"), "");
                        const $questionDivs = $questionMD5Div.parent();
                        if ($questionDivs.children().length === 1) {
                            const $empty = $("<div rounded style=\"cursor: auto;background: none;\" class=\"empty\">empty</div>");
                            $empty.css("padding", "0");
                            $empty.css("margin", "0");
                            $empty.css("height", "0");
                            $empty.css("opacity", "0");
                            $questionDivs.append($empty);
                            $empty.animate({
                                margin: 6,
                                padding: 4,
                                opacity: 1,
                                height: 20.667
                            }, 150, "easeOutCubic");
                        }
                        $questionMD5Div.hide(200, "easeOutCubic", function () {
                            $questionMD5Div.remove();
                        });
                    }
                });
            }

            switch (key) {
                case "delete":
                    questionMD5 = $questionMD5Div.attr("class").substring(8).replace(" context-menu-active", "");
                    deleteQuestionDiv(questionMD5);
                    break;
            }
        },
        items: {
            "delete": {name: "删除", /*icon: "delete"*/},
        }
    });
}

$(function () {//页面加载完成后执行
    if ($.cookie('page') == null || $.cookie('pageClass') == null) {
        $.cookie('page', 0);
        $.cookie('pageClass', 'server');
    }
    let over1 = false;
    let over2 = false;

    function showContent() {
        if (over1 && over2) {
            $("#content").animate({
                opacity: 1
            }, 300, "easeOutCubic");
        }
    }

    switchToPage($.cookie('pageClass'), $.cookie('page'), true, function () {
        over1 = true;
        showContent();
    },false);
    $("#top").animate({
        marginTop: 6
    }, 400, "easeOutCubic", function () {
        over2 = true;
        showContent();
    });
    initContextMenu();
})

function showMenu() {
    let menu = document.getElementById("menu");
    menu.onclick = function (e) {
        e.stopPropagation();
    };
    let $topMask = $("#topMask");
    let $menu = $("#menu");
    $topMask.css("display", "flex");
    $topMask.animate({
        opacity: 1,
    }, 150, "easeOutCubic", function () {
        $menu.animate({
            marginLeft: 8,
        }, 200, "easeOutCubic")
        let $root = $("#contentRoot");
        // $root.css("filter","blur(5px)");
    });
}

function closeMenu() {
    let $topMask = $("#topMask");
    let $menu = $("#menu");
    let $dialog = $("#dialog");
    $menu.animate({
        marginLeft: -$menu.width() - 20,
    }, 200, "easeInCubic", function () {
        $topMask.animate({
            opacity: 0,
        }, 150, "easeInCubic", function () {
            newUserLock = false;
            dialogLock = false;
            $topMask.css("display", "none");
        });
    });
    $dialog.animate({
        opacity: 0,
    }, 200, "easeInCubic", function () {
        $dialog.remove();
    });
}

function selectButtonOf(pageClass, index) {
    const $menuButtons = $("#menuButtons button");
    $menuButtons.attr("class", "button")
    $menuButtons.removeAttr("selected");
    const $selectedButton = $("#" + pageClass + "Menu button").eq(index);
    $selectedButton.attr("class", "selectedMenuButton");
    $selectedButton.attr("selected", "selected");
}

function changePath(path) {
    let pathBlocks = path.split("/");
    for (let pathBlock of pathBlocks) {
        pathBlock = "> " + pathBlock;
        clearPath();
        let pathHtml = `
<div class="path" preText rounded clickable onclick="doBackFunc('${pathBlock}')">${pathBlock}</div>
`
        let $path = $(
            pathHtml
        );
        $path.appendTo($("#pagePath"));
    }
}

function escapeHTML(string) {
    string = "" + string;
    return string.replace(/&/g, "&").replace(/</g, "<").replace(/>/g, ">").replace(/"/g, "").replace(/'/g, "'");
}

function clearPath() {
    let $path1 = $(".path");
    $path1.not($("[undeleted]")).remove();
}

// let pagePathMap = new Map();
// let breakBool = true;
let nameToFuncMap = new Map();

function addPath(pathName, backFunc) {
    // breakBool = true;
    nameToFuncMap.set(pathName, backFunc);
    pathName = escapeHTML(pathName);
    let pathHtml = `
<div class="path" preText rounded clickable onclick="doBackFunc('${pathName}')"></div>
`
    let $path = $(
        pathHtml
    );
    $path.text("> " + pathName);
    $path.css("opacity", 0);
    $path.css("marginLeft", -20);
    $path.css("marginRight", 20);
    $path.appendTo($("#pagePath"));
    $path.animate({
        opacity: 1,
        marginLeft: 3,
        marginRight: 3
    }, 200, "easeInCubic");
}

function doBackFunc(pathName) {
    nameToFuncMap.get(pathName)();//执行回调函数
    removePathAfter(pathName);
}

function containsPath(pathName) {
    let $pagePath = $("#pagePath");
    let $paths = $pagePath.children(".path");
    for (const $path of $paths) {
        if ($path.innerText === "> " + pathName) {
            return true;
        }
    }
    return false;
}

let pathLock;

function removePath(pathName) {
    if (pathLock) return "pathLock";
    pathLock = true;
    let $pagePath = $("#pagePath");
    let $paths = $pagePath.children(".path");
    for (const $path of $paths) {
        if ($path.innerText === "> " + pathName) {
            $path.animate({
                opacity: 0,
                marginLeft: -20,
                marginRight: 20,
            }, 200, "easeInCubic", function () {
                setTimeout(function () {
                    $path.remove();
                }, 100);
            });
            break;
        }
    }
}

function removePathAfter(pathName, contains = false, doCallBackFunc = false) {
    if (pathLock) return "pathLock";
    pathLock = true;
    let $pagePath = $("#pagePath");
    let $paths = $pagePath.children(".path");
    let indexOfPathName = -1;
    for (const $path of $paths) {
        if ($path.innerText === "> " + pathName) {
            indexOfPathName = $paths.index($path);
            break;
        }
    }
    if (indexOfPathName !== -1) {
        for (let i = indexOfPathName + (contains ? 0 : 1); i < $paths.length; i++) {
            $paths.eq(i).animate({
                opacity: 0,
                marginLeft: -20,
                marginRight: 20,
            }, 200, "easeInOutCubic", function () {
                setTimeout(function () {
                    $paths.eq(i).remove();
                }, 100);
            });
        }
    }
    if (doCallBackFunc) {
        $paths[indexOfPathName - (contains ? 1 : 0)].onclick();
    }
    pathLock = false;
    return "success";
}

function switchToPageWithAnimate(pageClass, index, clearPathBool = true, callback) {
    $("#content").fadeTo(200, 0, "easeInCubic", function () {
        switchToPage(pageClass, index, clearPathBool, function () {
            if (callback instanceof Function) {
                callback();
            }
            setTimeout(function () {
                $("#content").fadeTo(200, 1, "easeInCubic");
            }, 200);
        });
    });
}

function switchToPage(pageClass, index, clearPathBool = true, callback, isCloseMenu = true) {
    try {
        document.getElementById("managePage").onclose(undefined);
    } catch (ignored) {
    }
    const $content = $('#content');
    // $content.html("");
    $.ajax({
        url: location.href,
        type: 'get',
        data: "page=" + index + "&pageClass=" + pageClass,
        xhrFields: {
            withCredentials: true
        },
        success: function (res) {
            $content.html(res);
            try {
                document.getElementById("managePage").onload(undefined);
            } catch (ignored) {
            }
            if ($.cookie('page') !== index) {
                $.cookie('page', index);
                $.cookie('pageClass', pageClass);
            }
            const pathName = $("#" + pageClass + "Menu button").eq(index).attr("blockString");
            if (clearPathBool) {
                clearPath();
                addPath(pathName, function () {
                    switchToPageWithAnimate(pageClass, index, false);
                });
            }
            selectButtonOf(pageClass, index);
            if (isCloseMenu)
                closeMenu();
            if (callback instanceof Function)
                callback();
        },
        error: function (res) {
            showTip('error', '加载页面时发生错误:' + res.status);
        }
    });
}


function showTip(type, content, autoClose = true) {
    var title = '';
    switch (type) {
        case 'error':
            title = '错误';
            break;
        case 'warning':
            title = '警告';
            break;
        case 'info':
            title = '提示';
            break;
        default:
            title = '提示';
    }
    //-------------------
    let tip = new Tip(title, content);
    tip.autoClose = autoClose;
    var $tip = tip.getJQueryObject();
    $tip.appendTo($("#tipsMask"));
    //-------------------

}

class Tip {
    tipHtml;
    mouseEnteredTimes = 0;
    autoClose = true;

    constructor(title, content) {
        this.tipHtml = `
<div component_type="tip" rounded>
<div class="tipTop">
<label class="tipTitle">
${title}
</label>
<div class="blank"></div>
<button class='tipCloseButton' onclick="closeTipOfButton(this)">×</button>
</div>
<hr style="opacity: 0.5"/>
<div class='tipContent' style="font-size: 12px">
${content}
</div>
</div>`;
    }

    getHtml() {
        return this.tipHtml;
    }

    getJQueryObject() {
        let $tip = $(this.tipHtml);
        $tip.css("marginBottom", -96);
        $tip.animate({
            marginBottom: 8,
        }, 200, "easeInOutCubic", function () {
            $tip.on("mouseenter", function () {
                this.mouseEnteredTimes++;
            }.bind(this));
            $tip.on("mouseleave", function () {
                setTimeout(function () {
                    this.closeTipWhileMouseNotHover($tip, this)
                }.bind(this), 1000);
            }.bind(this));
            if (this.autoClose) {
                const closeTipWhileMouseNotHover = this.closeTipWhileMouseNotHover;
                setTimeout(function () {
                        closeTipWhileMouseNotHover($tip, this)
                    }.bind(this)
                    , 2000);
            }
        }.bind(this));
        return $tip;
    }

    closeTipWhileMouseNotHover($tip, thisObj) {
        if (thisObj.mouseEnteredTimes > 0) {
            thisObj.mouseEnteredTimes--;
        } else {
            $tip.animate({
                marginLeft: $tip.width() + 40,
                marginRight: -$tip.width() - 40,
            }, 200, "easeInOutCubic", function () {
                $tip.animate({
                    maxHeight: 0,
                    minHeight: 0,
                    height: 0,
                    marginTop: 0,
                    marginBottom: 0,
                    paddingTop: 0,
                    paddingBottom: 0,
                }, 250, "easeInOutCubic", function () {
                    $tip.remove();
                });
            }.bind(this));
        }
    }
}

function closeTipOfButton(tipCloseButton) {
    const $tip = $(tipCloseButton).parent().parent();
    $tip.animate({
        marginLeft: $tip.width() + 40,
        marginRight: -$tip.width() - 40,
    }, 200, "easeInOutCubic", function () {
        $tip.slideUp(200, "easeInOutCubic", function () {
            $tip.remove();
        });
    });
}

let dialogLock = false;

function popDialog(html, callBack) {
    if (dialogLock) return;
    dialogLock = true;
    const $topMask = $("#topMask");
    // $topMask.stopAnimation(true);
    $topMask.find("#dialog").remove();
    $topMask.css("display", "flex");
    let $dialog = $("<div rounded id='dialog' style='overflow: auto'></div>");
    $dialog.html(html);
    $dialog.css("opacity", 0);
    $topMask.append($dialog);
    $topMask.animate({opacity: 1}, 200, "easeOutCubic", function () {
        if (callBack instanceof Function)
            callBack();
        $dialog.animate({
            opacity: 1,
        }, 200, "easeOutCubic");
    });
    $dialog.on("click", function (e) {
        e.stopPropagation();
    });
    // const previousOnClick = $topMask.on("click");
    $topMask.on("click", function () {
        $topMask.stop(true, false);
        $topMask.animate({
            opacity: 0,
        }, 200, "easeInCubic", function () {
            $topMask.css("display", "none");
            setTimeout(function () {
                dialogLock = false;
            }, 100);
        });
    });
}

function logout() {
    closeMenu();
    closeWebSocket();
    $.removeCookie("token", {path: "/checkIn/manage"});
    $.removeCookie("qq", {path: "/checkIn"});

    $("#content").animate({
        opacity: 0
    }, 200, "easeInCubic");
    setTimeout(function () {
        $("#top").animate({
            opacity: 0
        }, 200, "easeInCubic", function () {
            // $.removeCookie("password", {path: "checkIn"});
            window.location.href = "./../login/"
        });
    }, 100);
}

let trafficPageIndex = 0;
let counts = [];
let trafficChart;

function initChart() {
    $.ajax({
        url: 'data',
        type: 'get',
        data: "type=traffic",
        xhrFields: {
            withCredentials: true
        },
        success: function (res) {
            trafficChart = echarts.init(document.getElementById('chart'), "walden");
            const dates = [];
            const trafficObj = JSON.parse(res);
            for (let i = 0; i <= 6; i++) {
                const trafficObjKey = trafficObj[i];
                dates[i] = trafficObjKey.date;
                counts[i] = Number(trafficObjKey.count);
            }
            // 指定图表的配置项和数据
            const option = {
                tooltip: {},
                legend: {
                    data: ['访问次数'],
                    textStyle: {
                        color: '#d3d3d3'
                    }
                },
                xAxis: {
                    data: dates,
                    axisLabel: {
                        textStyle: {
                            color: '#d3d3d3',
                            fontSize: 10
                        }
                    }
                },
                yAxis: {
                    axisLabel: {
                        textStyle: {
                            color: '#d3d3d3'
                        }
                    }
                },
                series: [{
                    name: '访问次数',
                    type: 'line',
                    data: counts,
                }]
            };

            // 使用刚指定的配置项和数据显示图表。
            trafficChart.setOption(option);
        },
        error: function (res) {
            showTip("error", res.data);
            console.error(res);
        }
    });

    $.ajax({
        url: './data',
        type: 'get',
        data: 'type=trafficDetail&pageIndex=' + trafficPageIndex,
        xhrFields: {
            withCredentials: true
        },
        success: function (res) {
            const trafficObj = JSON.parse(res);
            let $todayTrafficsDiv = $("#todayTraffics");
            let $userTrafficDiv;
            for (const trafficObjElement of trafficObj) {
                let ip = trafficObjElement.ip;
                let qq = trafficObjElement.qq;
                let time = trafficObjElement.time;
                let date = trafficObjElement.date;
                let id = trafficObjElement.id;
                const userTrafficDivHtml = `
<div rounded component_type="userTraffic" clickable class="userTraffic${id}">
<label>${date} ${time}</label>
<label>IP：${ip}</label>
<label>QQ：${qq}</label>
</div>
`;
                $userTrafficDiv = $(userTrafficDivHtml);
                $userTrafficDiv.on("click", function () {
                    onClickTraffic(id, date);
                })
                $userTrafficDiv.appendTo($todayTrafficsDiv);
            }
        },
        error: function (res) {
            showTip("error", res.data);
            console.error(res)
        }
    })
}

pathLock = false;

const offset = 100;

function transitionPage($root, html, pathName = undefined, pathBackFunc = undefined, callBack = undefined, outDirection = "Top", inDirection = "Bottom", offset = 200) {
    if (pathName instanceof String || pathBackFunc instanceof Function) {
        if (pathLock) return "pathLock";
        pathLock = true;
    }
    let $subContentRoot = $root.children(".subContentRoot");
    const outTo = "margin" + outDirection;
    const inTo = "margin" + inDirection;
    let outFrom;
    let inFrom;
    const margin = 8;
    switch (outDirection) {
        case "Top":
            outFrom = "marginBottom";
            break;
        case "Bottom":
            outFrom = "marginTop";
            break;
        case "Left":
            outFrom = "marginRight";
            break;
        case "Right":
            outFrom = "marginLeft";
            break;
    }
    switch (inDirection) {
        case "Top":
            inFrom = "marginBottom";
            break;
        case "Bottom":
            inFrom = "marginTop";
            break;
        case "Left":
            inFrom = "marginRight";
            break;
        case "Right":
            inFrom = "marginLeft";
            break;
    }
    const outProp = {"opacity": 0};
    outProp[outTo] = -offset - margin;
    outProp[outFrom] = offset - margin;
    $subContentRoot.animate(outProp, 250, "easeInQuart", function () {
        // changePath("Questions/" + pathName);
        if (pathName instanceof String || pathBackFunc instanceof Function) {
            addPath(pathName, pathBackFunc);
            pathLock = false;
        }
        try {
            $subContentRoot.children()[0].onclose(undefined)
        } catch (ignored) {
        }
        setTimeout(function () {
            $subContentRoot.html(html);
            $subContentRoot.css(inFrom, offset + margin);
            $subContentRoot.css(inTo, -offset + margin);
            // $subContentRoot.css("margin", 8);
            $subContentRoot.animate({
                margin: margin,
                opacity: 1
            }, 200, "easeOutQuart");
            setTimeout(function () {
                try {
                    $subContentRoot.children()[0].onload(undefined)
                } catch (ignored) {
                }
                if (callBack instanceof Function) {
                    callBack();
                }
            }, 150);
        }, 200);
    });
    if (pathName instanceof String || pathBackFunc instanceof Function) {
        setTimeout(function () {
            if (pathLock) pathLock = false;//防止出错锁死
        }, 500);
    }
    return "success";
}

const onlineUserCountMap = new Map;

function userOnline(userQQ) {
    const $userPane = $("#userDiv" + userQQ);
    const $userOnlineState = $userPane.find(".userOnLine");
    let count = onlineUserCountMap.get(userQQ);
    if (count === undefined) {
        if ($userOnlineState.css("opacity") !== "1")
            onlineUserCountMap.set(userQQ, 1);
        else return;
    } else {
        onlineUserCountMap.set(userQQ, count + 1);
        return;
    }
    $userOnlineState.fadeTo(200, 1, "easeInOutCubic");
    $userPane.find("button").attr("disabled", "disabled");
    $userPane.find("span[component_type=\"toggleSwitch\"]").attr("disabled", "disabled");
    if (Permission.permission_force_offline) {
        const $forceOffLineButton = $("<button highlight=\"\" class=\"optionButton\" onclick=\"offLine(" + userQQ + ")\">强制下线</button>");
        $forceOffLineButton.css("opacity", "0");
        $forceOffLineButton.css("marginLeft", -50);
        $forceOffLineButton.prependTo($userPane.find(".userOperation").children().eq(0));
        $forceOffLineButton.animate({
            opacity: 1,
            marginLeft: 2
        }, 200, "easeOutCubic");
    }
}

function userOffline(userQQ) {
    const $userPane = $("#userDiv" + userQQ);
    const $userOnlineState = $userPane.find(".userOnLine");
    let count = onlineUserCountMap.get(userQQ);
    if (count === undefined) {
        if ($userOnlineState.css("opacity") !== "1")
            return;
    } else {
        if (count === 1) {
            onlineUserCountMap.delete(userQQ);
        } else {
            onlineUserCountMap.set(userQQ, count - 1);
            return;
        }
    }
    $userOnlineState.fadeTo(200, 0, "easeInOutCubic");
    // noinspection JSUnresolvedReference
    for (const operationButton of $userPane.find(".operationButton")) {
        if (Permission["permission_" + $(operationButton).attr("operation_type")])
            $(operationButton).removeAttr("disabled");
    }
    // $userPane.find("button").removeAttr("disabled");
    // noinspection JSUnresolvedReference
    if (Permission.permission_change_user_state)
        $userPane.find("span[component_type=\"toggleSwitch\"]").removeAttr("disabled");
    // noinspection JSUnresolvedReference
    if (Permission.permission_force_offline) {
        const $forceOffLineButton = $userPane.find(".userOperation").children().eq(0).children().eq(0);
        const width = $forceOffLineButton.width();
        $forceOffLineButton.animate({
            opacity: 0,
            marginLeft: -width
        }, 200, "easeOutCubic", function () {
            setTimeout(function () {
                $forceOffLineButton.remove();
            }, 50);
        });
    }
}

function updateTrafficLog(trafficObj) {
    let pageIndex = $("#managePage").attr("index");
    let ip = trafficObj.ip;
    let qq = trafficObj.qqnumber;
    let time = trafficObj.localTime;
    let date = trafficObj.localDate;
    let id = trafficObj.id;
    if (pageIndex == 0) {
        let $todayTrafficsDiv = $("#todayTraffics");
        let $userTrafficDiv;
        const userTrafficDivHtml = `
<div rounded component_type="userTraffic" clickable class="userTraffic${id}" style="overflow: hidden">
<label>${date} ${time}</label>
<label>IP：${ip}</label>
<label>QQ：${qq}</label>
</div>
`;
        counts[counts.length - 1] = ++counts[counts.length - 1];
        trafficChart.setOption({series: [{data: counts}]});
        $userTrafficDiv = $(userTrafficDivHtml);
        $userTrafficDiv.on("click", function () {
            onClickTraffic(id, date);
        });
        $userTrafficDiv.prependTo($todayTrafficsDiv);
        $userTrafficDiv.slideUp(0);
        $userTrafficDiv.slideDown(200, "easeOutQuad");
        let children = $todayTrafficsDiv.children();
        if (children.length >= 20) {
            let $lastChildren = children.last();
            $lastChildren.slideUp(200, "easeOutQuad", function () {
                $lastChildren.remove();
            })
        }
    } else if (pageIndex == 1) {
        let $dateTrafficDetail = $(".dateTrafficDetail");
        if ($dateTrafficDetail.attr("id") === date) {
            const userTrafficDivHtml = `<div component_type="userTraffic" clickable rounded onclick="selectTrafficDetail(${id})">${time} ${ip} ${qq}</div>`;
            let $userTrafficDiv = $(userTrafficDivHtml);
            $userTrafficDiv.prependTo($dateTrafficDetail.children().eq(1));
            $userTrafficDiv.slideUp(0);
            $userTrafficDiv.slideDown(200, "easeOutQuad");
        }
    }
}

let onClickTraffic = function (id, date) {
    $.ajax({
        url: "./page/trafficDetail",
        data: {
            id: id
        },
        type: "get",
        success: function (res) {
            switchToPageWithAnimate("server", 1, true, function () {
                $(`#${date}`).trigger("click");
                transitionPage($("#right"), res);
            });
        }
    })
};

