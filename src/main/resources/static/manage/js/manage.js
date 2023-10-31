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
        opacity: 150,
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
    pathBlocks = path.split("/");
    for (let pathBlock of pathBlocks) {
        pathBlock = "> " + pathBlock;
        let $path1 = $(".path");
        $path1.not($("[undeleted]")).remove();
        let pathHtml = `
<div class="path" preText rounded clickable onclick="backToPageOfPath(${pathBlock})">${pathBlock}</div>
`
        let $path = $(
            pathHtml
        );
        $path.appendTo($("#pagePath"));
    }
}

function addPath(pathName) {
    pathName = "> " + pathName;
    let pathHtml = `
<div class="path" preText rounded clickable onclick="backToPageOfPath(${pathName})">${pathName}</div>
`
    let $path = $(
        pathHtml
    );
    $path.css("opacity", 0);
    $path.css("marginLeft", -20);
    $path.css("marginRight", 20);
    $path.appendTo($("#pagePath"));
    $path.animate({
        opacity: 1,
        marginLeft: 3,
        marginRight: 3
    }, 200, "easeInOutCubic");
}

function backToPageOfPath(pathName) {

}

function removePathContainsAfter(pathName) {
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
        for (let i = indexOfPathName + 1; i < $paths.length; i++) {
            $paths.eq(i).animate({
                opacity: 0,
                marginLeft: -20,
                marginRight: 20,
            }, 200, "easeInOutCubic", function () {
                $paths.eq(i).remove();
            });
        }
    }
    pathLock = false;
    return "success";
}

function switchToPage(pageClass, index) {
    $.ajax({
        url: location.href,
        type: 'get',
        data: "page=" + index + "&pageClass=" + pageClass,
        success: function (res) {
            var $content = $('#content');
            $content.html(res);
            try {
                document.getElementById("managePage").onload();
            } catch (e) {
                //ignored
            }
            if ($.cookie('page') !== index) {
                $.cookie('page', index);
                $.cookie('pageClass', pageClass);
                // $content.animate({opacity: 1}, 100, "easeOutCubic");
            }
            const pathName = $("#" + pageClass + "Menu button").eq(index).text();
            changePath(pathName);
            selectButtonOf(pageClass, index);
            closeMenu();
        },
        error: function (res) {
            showTip('error', '加载页面时发生错误:' + res.status);
        }
    });
}


function showTip(type, content) {
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
    var $tip = tip.getJQueryObject();
    $tip.appendTo($("#tipsMask"));
    //-------------------

}

class Tip {
    tipHtml;

    constructor(title, content) {
        this.mouseEntered = false;
        this.tipHtml = `
<div class='roundedDiv' component_type="tip">
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
                this.mouseEntered = true;
                let onmouseleave = function () {
                    this.mouseEntered = false;
                }.bind(this);
                $tip.on("mouseleave", function () {
                    onmouseleave();
                }).bind();
            }.bind(this));
            const closeTipWhileMouseNotHover = this.closeTipWhileMouseNotHover;
            setTimeout(function () {
                    closeTipWhileMouseNotHover($tip, this)
                }.bind(this)
                /*
                $tip.animate({
                    opacity: 0,
                },200,function (){
                    $tip.remove();
                });*/
                , 1500);
        }.bind(this));
        return $tip;
    }

    mouseEntered;

    closeTipWhileMouseNotHover($tip, thisObj) {
        if (thisObj.mouseEntered === true)
            setTimeout(function () {
                thisObj.closeTipWhileMouseNotHover($tip, thisObj)
            }.bind(this), 1000);
        else {
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
                    // $tip.remove();
                });
            }.bind(this));
        }
    }
}

function closeTipOfButton(tipCloseButton) {
    var $tip = $(tipCloseButton).parent().parent();
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
            var myChart = echarts.init(document.getElementById('chart'));
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
        }
    })
}

var pathLock = false;
function easePage($root, html, pathName) {
    if (pathLock) return "pathLock";
    pathLock = true;
    let $subContentRoot = $root.children(".subContentRoot");
    $subContentRoot.animate({
        marginTop: -100,
        marginBottom: 100,
        opacity: 0,
    }, 300, "easeInCubic", function () {
        // changePath("Questions/" + pathName);
        addPath(pathName);
        pathLock = false;
        setTimeout(function () {
            $subContentRoot.html(html);
            $subContentRoot.css("marginTop", 8);
            $subContentRoot.css("marginBottom", 8);
            $subContentRoot.animate({
                opacity: 1,
            }, 300, "easeOutCubic");
        }, 200)
    });
    setTimeout(function () {
        if (pathLock) pathLock = false;
    },500);
    return "success";
}