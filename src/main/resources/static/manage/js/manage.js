function initContextMenu() {
    $.contextMenu({
        selector: "button.partitionButton[editing='false']",
        callback: function (key, options, e) {
            switch (key) {
                case "edit":
                    editPartitionName.call(this);
                    break;
                case "delete":
                    const messageJson = {
                        "type": "deletePartition",
                        "partitionName": $(this).text()
                    };
                    sendMessage(JSON.stringify(messageJson), function (event) {
                        // console.log(event);
                    });
                    break;
            }
        },
        items: {
            "edit": {name: "Edit", icon: "edit"},
            "delete": {name: "Delete", icon: "delete"},
        }
    });
    $.contextMenu({
        selector: "div.question",
        callback: function (key, options, e) {
            let $questionMD5Div = $(this).find("div.questionMD5");
            switch (key) {
                case "edit":
                    editQuestion($questionMD5Div.text());
                    break;
                case "delete":
                    const messageJson = {
                        "type": "deleteQuestion",
                        "questionMD5": $questionMD5Div.text()
                    };
                    sendMessage(JSON.stringify(messageJson), function (event) {
                        console.log(event);
                    });
                    break;
            }
        },
        items: {
            "edit": {name: "Edit", icon: "edit"},
            "delete": {name: "Delete", icon: "delete"},
        }
    });
    $.contextMenu({
        selector: "div.partitionQuestionsList > div:not(.empty)",
        callback: function (key, options, e) {
            let $questionMD5Div = $(this);

            function deleteQuestionDiv(questionMD5) {
                const messageJson = {
                    "type": "deleteQuestion",
                    "questionMD5": questionMD5
                };
                sendMessage(JSON.stringify(messageJson), function (event) {
                    console.log(event);//FIXME
                    $("div.partitionQuestionsList > div.question" + messageJson.questionMD5).remove();
                    $questionMD5Div.remove();
                }, function (event) {
                    $("div.partitionQuestionsList > div." + messageJson.questionMD5).remove();
                    $questionMD5Div.remove();
                });
            }

            switch (key) {
                case "delete":
                    const questionMD5 = $questionMD5Div.attr("class").substring(8).replace(" context-menu-active", "");
                    if ($("#md5").val() === questionMD5)
                        transitionPage($("#right"), "", undefined, undefined, function () {
                            deleteQuestionDiv(questionMD5);
                        });
                    else
                        deleteQuestionDiv(questionMD5);
                    break;
            }
        },
        items: {
            "delete": {name: "Delete", icon: "delete"},
        }
    });
}

$(function () {//页面加载完成后执行
    if ($.cookie('page') == null || $.cookie('pageClass') == null) {
        $.cookie('page', 0);
        $.cookie('pageClass', 'server');
    }
    switchToPage($.cookie('pageClass'), $.cookie('page'));
    $("#top").animate({
        opacity: 1
    }, 200, "easeInCubic");
    setTimeout(function () {
        $("#content").animate({
            opacity: 1
        }, 200, "easeInCubic");
    }, 100);
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
    }, 100, "easeInOutCubic", function () {
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
    $menu.animate({
        marginLeft: -$menu.width() - 20,
    }, 200, "easeInOutCubic", function () {
        $topMask.animate({
            opacity: 0,
        }, 150, "easeInOutCubic", function () {
            $topMask.css("display", "none");
        });
    })
}

function selectButtonOf(pageClass, index) {
    $("#menuButtons button").attr("class", "button")
    $("#" + pageClass + "Menu button").eq(index).attr("class", "selectedMenuButton");
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

function switchToPage(pageClass, index, clearPathBool = true, callback) {
    $.ajax({
        url: location.href,
        type: 'get',
        data: "page=" + index + "&pageClass=" + pageClass,
        success: function (res) {
            const $content = $('#content');
            $content.html(res);
            try {
                document.getElementById("managePage").onload(undefined);
            } catch (e) {
                //ignored
            }
            if ($.cookie('page') !== index) {
                $.cookie('page', index);
                $.cookie('pageClass', pageClass);
            }
            const pathName = $("#" + pageClass + "Menu button").eq(index).text();
            if (clearPathBool) {
                clearPath();
                addPath(pathName, function () {
                    $("#content").fadeTo(200, 0, "easeInCubic", function () {
                        switchToPage(pageClass, index, false, function () {
                            setTimeout(function () {
                                $("#content").fadeTo(200, 1, "easeInCubic");
                            }, 200);
                        });
                    });
                });
            }
            selectButtonOf(pageClass, index);
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
<div class='tipTitle'>
${title}
</div>
<div class="blank"></div>
<button class='tipCloseButton' onclick="closeTipOfButton(this)" style="width: 20px;height: 20px;margin: 0;font-size: 14px">×</button>
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
                marginLeft: $tip.width() + 20,
                marginRight: -$tip.width() - 20,
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
        marginLeft: $tip.width() + 20,
        marginRight: -$tip.width() - 20,
    }, 200, "easeInOutCubic", function () {
        $tip.slideUp(200, "easeInOutCubic", function () {
            $tip.remove();
        });
    });
}


function logout() {
    closeMenu();
    $("#content").animate({
        opacity: 0
    }, 200, "easeInCubic");
    setTimeout(function () {
        $("#top").animate({
            opacity: 0
        }, 200, "easeInCubic", function () {
            $.removeCookie("qq", {path: "/checkIn"});
            $.removeCookie("password", {path: "checkIn"});
            window.location.href = "./../login/"
        });
    }, 100);
}

var trafficPageIndex = 0;

function initChart() {
    $.ajax({
        url: 'data',
        type: 'get',
        data: "type=traffic",
        success: function (res) {
            var myChart = echarts.init(document.getElementById('chart'), "walden");
            var dates = [];
            var counts = [];
            const trafficObj = JSON.parse(res);
            for (var i = 0; i <= 6; i++) {
                var trafficObjKey = trafficObj[i];
                dates[i] = trafficObjKey.date;
                counts[i] = trafficObjKey.count;
            }
            // 指定图表的配置项和数据
            var option = {
                tooltip: {},
                legend: {
                    data: ['流量'],
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
            myChart.setOption(option);
        },
        error: function (res) {
            showTip("error", res.data);
            console.error(res);
        }
    });
    $.ajax({
        url: 'data',
        type: 'get',
        data: 'type=trafficDetail&pageIndex=' + trafficPageIndex,
        success: function (res) {
            const trafficObj = JSON.parse(res);
            $todayTrafficsDiv = $("#todayTraffics");
            for (const trafficObjElement of trafficObj) {
                let ip = trafficObjElement.ip;
                let qq = trafficObjElement.qq;
                let time = trafficObjElement.time;
                let date = trafficObjElement.date;
                var userTrafficDivHtml = `
<div rounded component_type="userTraffic" clickable>
<label>${date} ${time}</label>
<label>IP：${ip}</label>
<label>QQ：${qq}</label>
</div>
`;
                $userTrafficDiv = $(userTrafficDivHtml);
                $userTrafficDiv.appendTo($todayTrafficsDiv);
            }
        },
        error: function (res) {
            showTip("error", res.data);
            console.error(res)
        }
    })
}

var pathLock = false;

function transitionPage($root, html, pathName, backFunc, doneCallBack) {
    if (pathName instanceof String || backFunc instanceof Function) {
        if (pathLock) return "pathLock";
        pathLock = true;
    }
    let $subContentRoot = $root.children(".subContentRoot");
    $subContentRoot.animate({
        marginTop: -100,
        marginBottom: 100,
        opacity: 0,
    }, 300, "easeInCubic", function () {
        // changePath("Questions/" + pathName);
        if (pathName instanceof String || backFunc instanceof Function) {
            addPath(pathName, backFunc);
            pathLock = false;
        }
        setTimeout(function () {
            $subContentRoot.html(html);
            $subContentRoot.css("marginTop", 8);
            $subContentRoot.css("marginBottom", 8);
            if (doneCallBack instanceof Function) {
                doneCallBack();
            }
            $subContentRoot.animate({
                opacity: 1,
            }, 300, "easeOutCubic");
            try {
                $subContentRoot.children()[0].onload(undefined)
            } catch (ignored) {
            }
        }, 200)
    });
    if (pathName instanceof String || backFunc instanceof Function) {
        setTimeout(function () {
            if (pathLock) pathLock = false;//防止出错锁死
        }, 500);
    }
    return "success";
}