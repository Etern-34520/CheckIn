let transiting = false;

function initTraffic() {
    let $children = $("#dateSelections").children();
    for (const child of $children) {
        let $child = $(child);
        $child.on("click", function () {
            transiting = true;
            $children.removeAttr("selected");
            $child.attr("selected", "selected");
            let date = $child.find("label").text();
            sendMessage({
                type: "getDateTrafficDetail",
                date: date
            }, function (event) {
                transitionPage($("#userTraffics"), generateDateTrafficHtml(date, JSON.parse(event.data)), undefined, undefined, function () {
                    setTimeout(function () {
                        transiting = false;
                    }, 500);
                    searchTraffic();
                }, "Left", "Right");
            })
        });
    }
}

function generateDateTrafficHtml(date, trafficData) {
    let traffics = "";
    for (const trafficElement of trafficData.traffics) {
        traffics += `<div component_type="userTraffic" class="dateTrafficItem" clickable rounded onclick="selectTrafficDetail(${trafficElement.id})">${trafficElement.localTime} ${trafficElement.ip} ${trafficElement.qqNumber}</div>`;
    }
    return `
<div id="${date}" class="dateTrafficDetail">
    <label>${date}</label>
    <div id="trafficList" style="display: flex;flex-direction: column;max-height: 100%">${traffics}</div>
</div>`;
}

function selectTrafficDetail(id) {
    $.ajax({
        url: "./page/trafficDetail",
        data: {
            id: id
        },
        type: "get",
        success: function (res) {
            transitionPage($("#right"), res);
        },
        error: function (res) {
            showTip('error', '加载页面时发生错误:' + res.status);
        }
    })
}

function searchTraffic() {
    let $dateTrafficDetails = $(".dateTrafficItem");
    let searchString = $("#searchInput").val().toLowerCase();
    if (searchString === "") {
        for (const dateTrafficDetail of $dateTrafficDetails) {
            $(dateTrafficDetail).stop();
            $(dateTrafficDetail).slideDown(200,"easeOutQuint");
        }
        return;
    }
    let words = searchString.split(" ");
    for (const dateTrafficDetail of $dateTrafficDetails) {
        const $dateTrafficDetail = $(dateTrafficDetail);
        let show = false;
        for (const word of words) {
            if (word === "" || word === " ") continue;
            if ($dateTrafficDetail.text().toLowerCase().includes(word)) {
                show = true;
                break;
            }
        }
        if (show) {
            $dateTrafficDetail.css("display", "block");
        } else {
            $dateTrafficDetail.css("display", "none");
        }
    }
}

