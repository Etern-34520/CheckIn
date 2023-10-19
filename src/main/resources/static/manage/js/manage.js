$(function (){//页面加载完成后执行
    if ($.cookie('page') == null || $.cookie('pageClass') == null) {
        $.cookie('page', 0);
        $.cookie('pageClass', 'server');
    }
    switchToPage($.cookie('pageClass'),$.cookie('page'));
})
function showMenu(){
    let menu = document.getElementById("menu");
    menu.onclick = function (e) {
        e.stopPropagation();
    };
    let $topMask = $("#topMask");
    let $menu = $("#menu");
    $topMask.css("display","flex");
    $topMask.animate({
        opacity: 150,
    },100,"easeInOutCubic",function (){
        $menu.animate({
            marginLeft: 8,
        },200,"easeOutCubic")
        let $root = $("#contentRoot");
        // $root.css("filter","blur(5px)");
    });
}
function closeMenu(){
    let $topMask = $("#topMask");
    let $menu = $("#menu");
    $menu.animate({
        marginLeft: -$menu.width()-20,
    },200,"easeInOutCubic",function (){
        $topMask.animate({
            opacity: 0,
        },150,"easeInOutCubic",function (){
            $topMask.css("display","none");
        });
    })
}

function selectButtonOf(pageClass,index) {
    $("#menuButtons button").attr("class", "button")
    $("#"+pageClass+"Menu button").eq(index).attr("class", "selectedMenuButton");
}

function switchToPage(pageClass,index){
    $.ajax({
        url: location.href,
        type: 'get',
        data: "page=" + index + "&pageClass=" + pageClass,
        success: function (res) {
            var $content = $('#content');
            if ($.cookie('page') !== index) {
                /*$content.animate({opacity: 0}, 100, "easeInCubic", function () {
                    $content.html(res);
                    try {
                        document.getElementById("content").children[0].onload();
                    } catch (e) {
                        //ignored
                    }
                });*/
                $content.html(res);
                try {
                    document.getElementById("content").children[0].onload();
                } catch (e) {
                    //ignored
                }
                $.cookie('page', index);
                $.cookie('pageClass', pageClass);
                // $content.animate({opacity: 1}, 100, "easeOutCubic");
            } else {
                $content.html(res);
                try {
                    document.getElementById("content").children[0].onload();
                } catch (e) {
                    //ignored
                }
            }
            let $path1 = $(".path");
            $path1.not($("[undeleted]")).remove();
            const pathName = "> "+$("#"+pageClass+"Menu button").eq(index).text();
            let pathHtml = `
            <div class="path">${pathName}</div>
`
            let $path = $(
                pathHtml
            );
            $path.appendTo($("#pagePath"));
            selectButtonOf(pageClass,index);
            closeMenu();
        },
        error: function (res) {
            showTip('error', '加载页面时发生错误:' + res.status);
        }
    });
}


function showTip(type,content){
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
    let tip = new Tip(title,content);
    var $tip = tip.getJQueryObject();
    $tip.appendTo($("#tipsMask"));
    //-------------------

}

class Tip {
    tipHtml;
    constructor(title,content) {
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
    getHtml(){
        return this.tipHtml;
    }
    getJQueryObject(){
        let $tip = $(this.tipHtml);
        $tip.css("marginBottom",-96);
        $tip.animate({
            marginBottom: 8,
        },200,"easeInOutCubic",function (){
            $tip.on("mouseenter",function (){
                this.mouseEntered = true;
                let onmouseleave = function (){
                    this.mouseEntered = false;
                }.bind(this);
                $tip.on("mouseleave",function (){
                    onmouseleave();
                }).bind();
            }.bind(this));
            const closeTipWhileMouseNotHover = this.closeTipWhileMouseNotHover;
            setTimeout(function (){
                closeTipWhileMouseNotHover($tip,this)
            }.bind(this)
                /*
                $tip.animate({
                    opacity: 0,
                },200,function (){
                    $tip.remove();
                });*/
            ,1500);
        }.bind(this));
        return $tip;
    }
    mouseEntered;
    closeTipWhileMouseNotHover($tip,thisObj){
        if (thisObj.mouseEntered === true)
            setTimeout(function (){
                thisObj.closeTipWhileMouseNotHover($tip,thisObj)
            }.bind(this),1000);
        else {
            $tip.animate({
                marginLeft: $tip.width()+20,
                marginRight: -$tip.width()-20,
            },200,"easeInOutCubic",function (){
                $tip.animate({
                    maxHeight: 0,
                    minHeight: 0,
                    height: 0,
                    marginTop: 0,
                    marginBottom: 0,
                    paddingTop: 0,
                    paddingBottom: 0,
                },250,"easeInOutCubic",function (){
                    // $tip.remove();
                });
            }.bind(this));
        }
    }
}

function closeTipOfButton(tipCloseButton){
    var $tip = $(tipCloseButton).parent().parent();
    $tip.animate({
        marginLeft: $tip.width()+20,
        marginRight: -$tip.width()-20,
    },200,"easeInOutCubic",function (){
        $tip.slideUp(200,"easeInOutCubic",function (){
            $tip.remove();
        });
    });
}


//TODO
function logout(){

}