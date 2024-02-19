function rotateShrinkButton(div, shrinkFuncName, expandFuncName) {
    const $div = $(div);
    if ($div.attr('lock') === 'shrink') return;
    $div.attr('lock', 'shrink');
    if ($div.attr('shrink') === 'true') {
        $div.children(0).animate({
            rotate: '360deg',
        }, 300, 'easeInOutQuint', function () {
            $(this).css('rotate', '0deg');
            $div.removeAttr('lock');
        });
        $div.removeAttr('shrink');
        if (shrinkFuncName instanceof Function)
            shrinkFuncName(div);
        else
            window[shrinkFuncName](div);
    } else {
        $div.children(0).animate({
            rotate: '180deg',
        }, 300, 'easeInOutQuint', function () {
            $div.removeAttr('lock');
        });
        $div.attr('shrink', 'true');
        if (expandFuncName instanceof Function)
            expandFuncName(div);
        else
            window[expandFuncName](div);
    }
}

// noinspection JSUnusedGlobalSymbols
function expandDiv(shrinkButton) {
    const $content = $(shrinkButton).parent().parent().children().eq(1);
    $content.slideDown(200, "easeOutQuad", function () {
        $content.css("display", "block");
    });
}

// noinspection JSUnusedGlobalSymbols
function shrinkDiv(shrinkButton) {
    const $content = $(shrinkButton).parent().parent().children().eq(1);
    $content.slideUp(200, "easeOutQuad");
}

function toggleSwitch($toggleSwitch, moveLeft, toggleSwitchOn, toggleSwitchOff) {
    if ($toggleSwitch.attr('disabled') === 'disabled') return;
    const $input = $toggleSwitch.find('input');

    function switchOn() {
        $toggleSwitch.find('.toggleSwitchDot').animate({left: moveLeft}, 80, 'easeInOutCubic');
        $toggleSwitch.css('background', 'var(--highlight-component-background-color-hover)');
        $input.val('true');
    }

    function switchOff() {
        $toggleSwitch.find('.toggleSwitchDot').animate({left: 0}, 80, 'easeInOutCubic');
        $toggleSwitch.css('background', 'var(--context-menu-background-color)');
        $input.val('false');
    }

    if ($input.val() === 'false') {
        if (toggleSwitchOn instanceof Function) {
            const res = toggleSwitchOn();
            if (res !== false) {
                switchOn();
            }
        } else if (typeof toggleSwitchOn === "string") {
            let switchOnFunc = window[toggleSwitchOn];
            let res = true;
            if (switchOnFunc instanceof Function)
                res = switchOnFunc();
            else try {
                eval(toggleSwitchOn)
            } catch (ignored) {
            }
            if (res !== false) {
                switchOn();
            }
        } else {
            switchOn();
        }
    } else {
        if (toggleSwitchOff instanceof Function) {
            const res = toggleSwitchOff();
            if (res !== false) {
                switchOff();
            }
        } else if (typeof toggleSwitchOff === "string") {
            let switchOffFunc = window[toggleSwitchOff];
            let res = true;
            if (switchOffFunc instanceof Function)
                res = switchOffFunc();
            else try {
                eval(toggleSwitchOff)
            } catch (ignored) {
            }
            if (res !== false) {
                switchOff();
            }
        } else {
            switchOff();
        }
    }
    $input.trigger('input');
}

function switchOnIgnoreHandler($toggleSwitch) {
    const $input = $toggleSwitch.find('input');
    $toggleSwitch.find('.toggleSwitchDot').animate({left: $toggleSwitch.attr("move_distance")}, 80, 'easeInOutCubic');
    $toggleSwitch.css('background', 'var(--highlight-component-background-color-hover)');
    $input.val('true');
}

function switchOffIgnoreHandler($toggleSwitch) {
    const $input = $toggleSwitch.find('input');
    $toggleSwitch.find('.toggleSwitchDot').animate({left: 0}, 80, 'easeInOutCubic');
    $toggleSwitch.css('background', 'var(--context-menu-background-color)');
    $input.val('false');
}

function switchToIgnoreHandler($toggleSwitch, status) {
    if (status) {
        switchOnIgnoreHandler($toggleSwitch);
    } else {
        switchOffIgnoreHandler($toggleSwitch);
    }
}

function switchShrinkPane(shrinkPaneTitle) {
    $(shrinkPaneTitle).find("div[component_type='shrinkButton']").trigger("click");
}

let resizeObserver;
let lastCount = [];

function initWaterfall() {
    try {
        lastCount = [];
        resizeObserver = new ResizeObserver(debounce(onresize, 100));
        for (const ele of $(".waterfallBasic")) {
            resizeObserver.observe(ele);
        }
        onresize();
    } catch (e) {
        console.error(e)
    }

}

function destroyWaterfall() {
    try {
        resizeObserver.disconnect();
    } catch (e) {
        console.error(e)
    }

}

function onresize() {
    let index = 0;
    for (const waterfallBasicElement of $(".waterfallBasic")) {
        const $waterfallBasic = $(waterfallBasicElement);
        const width = $waterfallBasic.width();
        const elementWidth = 420;
        let columnCount = Math.floor(width / elementWidth);
        const $waterfallInfo = $waterfallBasic.parent().find(".waterfallInfo");
        if (columnCount === 0) columnCount = 1;
        if ($waterfallInfo.children().length === 0 && (columnCount === lastCount[index] || columnCount === 0)) {
            index++;
            continue;
        } else {
            lastCount[index] = columnCount;
        }
        $waterfallBasic.children().children().appendTo($waterfallInfo);
        $waterfallBasic.children().remove();
        for (let i = 0; i < columnCount; i++) {
            const $waterfallColumn = $(`<div class='waterfallColumn' style='min-width: ${elementWidth}'></div>`);
            $waterfallBasic.append($waterfallColumn);
        }
        let rowIndex = 0;
        for (const userDiv of $waterfallInfo.children()) {
            $waterfallBasic.children().eq(rowIndex).append($(userDiv));
            rowIndex++;
            if (rowIndex >= columnCount) {
                rowIndex = 0;
            }
        }
        index++;
        // console.log("div元素的大小已改变。");
    }
}

class Slider {
    $point;
    $lines;
    sliding;
    slidingPointIndex;
    pointCount;
    minValue;
    maxValue;
    round;

    constructor($slider) {
        this.$point = $slider.find("div[component_type='sliderPoint']");
        this.$lines = $slider.find("span[component_type='sliderLine']");
        this.sliding = false;
        this.slidingPointIndex = 0;
        this.pointCount = this.$point.length;
        this.maxValue = parseFloat($slider.attr("max-value"));
        this.minValue = parseFloat($slider.attr("min-value"));

        this.round = $slider.attr("round_calc") !== "false";

        const $minValueInput = $slider.find("input.sliderMinValue");
        const $maxValueInput = $slider.find("input.sliderMaxValue");
        const mousedown = function (e) {
            // this.$point.css("z-index", "2");
            this.sliding = true;
            $(this).attr("sliding", "true");
            let originalIndex = $(e.target).index();
            if (originalIndex === 0) {
                originalIndex = $(e.target).parent().index();
            }
            this.slidingPointIndex = (originalIndex + 1) / 2;
            $(window).on({
                mousemove: function (e) {
                    e.preventDefault();
                    const $sliderBase = $slider.find("div[component_type='sliderBase']");
                    const width = $sliderBase.width();
                    // const width = $slider.width();
                    const $line = this.$lines.eq(this.slidingPointIndex - 1);
                    let offset = e.originalEvent.x - $sliderBase.offset().left;
                    const i = this.slidingPointIndex - 2;
                    if (i === 0) {
                        offset -= this.$lines.eq(i).width();
                    }
                    if (offset < 0) offset = 0;
                    if (offset >= width) {
                        offset = width;
                    }
                    const value2 = this.$point.eq(1).find("input").val();
                    const value1 = parseFloat(this.$point.eq(0).find("input").val());
                    let value = offset / (width);
                    if (this.pointCount === 2 && this.slidingPointIndex === 1 && value >= value2) {
                        value = value2;
                    }
                    const $input = this.$point.eq(this.slidingPointIndex - 1).find("input");
                    // console.log(this.slidingPointIndex);
                    $line.css("width", `${value * 100}%`);
                    if (this.pointCount === 2) {
                        if (this.slidingPointIndex === 1) {
                            const $line2 = this.$lines.eq(1);
                            $line2.css("width", (value2 - this.$point.eq(0).find("input").val()) * 100 + "%");
                        } else {
                            value += value1;
                        }
                    }
                    if (value > 1) {
                        value = 1;
                        $line.css("width", `${(value - value1) * 100}%`);
                    }
                    $input.val(value);
                    if (this.pointCount === 2) {
                        $maxValueInput.val(this.calcFinalValue(this.$point.eq(1).find("input").val()));
                        $minValueInput.val(this.calcFinalValue(this.$point.eq(0).find("input").val()));
                    } else {
                        $maxValueInput.val(this.calcFinalValue(this.$point.eq(0).find("input").val()));
                    }
                }.bind(this),
                mouseup: function (e) {
                    this.sliding = false;
                    this.$point.removeAttr("sliding");
                    // this.$point.css("z-index", "1");
                    // this.$point.attr("component_type", "sliderPoint");
                    $(window).off("mousemove mouseup");
                }.bind(this)
            });
        }.bind(this)
        this.$point.on({
            mousedown: mousedown
        });
        this.$point.children().on({
            mousedown: mousedown
        });
        $minValueInput.on({
            input: function (e) {
                let value = ($(e.target).val() - this.minValue) / (this.maxValue - this.minValue);
                if (value < (this.minValue - 1) / (this.maxValue - this.minValue)) value = (this.minValue - 1) / (this.maxValue - this.minValue);
                else if (value > (this.maxValue - 1) / (this.maxValue - this.minValue)) value = (this.maxValue - 1) / (this.maxValue - this.minValue);
                this.$point.eq(0).find("input").val(value);
                this.$lines.eq(0).css("width", `${value * 100}%`);
                if (this.pointCount === 2) {
                    let value1 = this.$point.eq(1).find("input").val() - value;
                    if (value1 < 0) {
                        value1 = 0;
                        value = this.$point.eq(1).find("input").val();
                    }
                    this.$lines.eq(1).css("width", `${value1 * 100}%`);
                }
                $minValueInput.val(this.calcFinalValue(value));
            }.bind(this)
        });
        $maxValueInput.on({
            input: function (e) {
                let originalValue = $(e.target).val();
                let value = (originalValue - this.minValue) / (this.maxValue - this.minValue);
                if (value < (this.minValue - (this.pointCount - 1)) / (this.maxValue - this.minValue)) value = (this.minValue - (this.pointCount - 1)) / (this.maxValue - this.minValue);
                else if (value > (this.maxValue - (this.pointCount - 1)) / (this.maxValue - this.minValue)) value = (this.maxValue - (this.pointCount - 1)) / (this.maxValue - this.minValue);
                this.$point.eq(this.pointCount - 1).find("input").val(value);
                if (this.pointCount === 1) {
                    this.$lines.eq(0).css("width", `${value * 100}%`);
                    $maxValueInput.val(this.calcFinalValue(value));
                } else {
                    let value1 = value - this.$point.eq(0).find("input").val();
                    if (value1 < 0) {
                        value1 = 0;
                        value = this.$point.eq(0).find("input").val();
                    }
                    this.$lines.eq(1).css("width", `${value1 * 100}%`);
                    $maxValueInput.val(this.calcFinalValue(value));
                }
            }.bind(this)
        });
    }

    calcFinalValue(value) {
        let delta = this.maxValue - this.minValue;
        if (delta < 0) return this.minValue;
        value = this.minValue + value * delta;
        if (this.round) {
            value = Math.round(value);
        }
        return value;
    }
}

/**
 *  use as
 *  <div class="slider" name="xxx"></div>
 *  optional attributes:
 *  |round_calc （default false)
 *  |min-value (default 0)
 *  |max-value (default 100)
 *  |value (default {min-value})
 *  |value1 (default {min-value})
 *  |useDoubleSlider (default false)
 *  |name1 (if useDoubleSlider = true is required)
 * */
function initSlider($sliders) {
    for (const slider of $sliders) {
        let $slider = $(slider);
        if ($slider.attr("component_type")===undefined) {
            $slider.attr("component_type","slider")

            let minValue = $slider.attr("min-value");
            if (minValue===undefined) minValue = 0;
            else minValue = Number(minValue);

            let maxValue = $slider.attr("max-value");
            if (maxValue===undefined) maxValue = 100;
            else {
                maxValue = Number(maxValue);
                if (maxValue<minValue) maxValue = minValue;
            }

            let value = $slider.attr("value");
            if (value===undefined) value = minValue;
            else {
                value = Number(value);
                if (value < minValue) value = minValue;
                else if (value > maxValue) value = maxValue;
            }

            let value1 = $slider.attr("value1");
            if (value1===undefined) value1 = minValue;
            else {
                value1 = Number(value1);
                if (value1 < minValue) value1 = minValue;
                else if (value1 > maxValue) value1 = maxValue;
            }

            let name = $slider.attr("name");
            let name1 = $slider.attr("name1");

            //TODO
            let useDoubleSlider = $slider.attr("useDoubleSlider")==="true"||$slider.attr("useDoubleSlider")==="";
            if (useDoubleSlider){
                $slider.append($(`
<label>
    <input style="width: 60px" class="sliderMinValue" name="${name}" type="number" value="${value}" max_value="${maxValue}">
</label>`));
            }
            let $sliderBase = $("<div component_type=\"sliderBase\"></div>");
            $slider.append($sliderBase)
            if (useDoubleSlider) {
                $sliderBase.append($(`
<span component_type="sliderLine" style="width: ${100*(value-minValue)/(maxValue-minValue)}%"></span>
<div component_type="sliderPoint" value="${(value-minValue)/(maxValue-minValue)}">
    <div></div>
    <input type="hidden" value="${(value-minValue)/(maxValue-minValue)}"/>
</div>`));
            } else {
                // let v = value;
                // value = value1;
                // value1 = v;
                // value = value1;
                value1 = value;
                value = minValue;
                name1 = name;
            }
            $sliderBase.append($(`
<span component_type="sliderLine" style="width: ${100 * (value1 - value) / (maxValue - minValue)}%"></span>
<div component_type="sliderPoint" value="${(value1 - minValue) / (maxValue - minValue)}">
    <div></div>
    <input type="hidden" value="${(value1 - minValue) / (maxValue - minValue)}"/>
</div>`));
            $slider.append($(`
<label>
    <input style="width: 60px" class="sliderMaxValue" name="${name1}" type="number" value="${value1}" max_value="${maxValue}">
</label>
            `))
            new Slider($slider);
        }
    }
}

/*
<span border id="${switchID}" class="toggleSwitchBasic"
      style="width: ${width}px;height: ${height}px;align-self: center;flex: none;
      <c:if test="${state}">background: var(--highlight-component-background-color-hover)</c:if>;${empty style?null:style}" component_type="toggleSwitch" move_distance="${moveLeft}"
      <c:if test="${disabled}">disabled="disabled"</c:if>
      onclick="toggleSwitch($(this),${moveLeft},${onFuncName},${offFuncName})">
    <input type="hidden" class="toggleSwitchInput" name="${switchID}" id="input_${switchID}" value="${empty state?"true":state}">
    <span class="toggleSwitchDot" style="height: ${dotSize}px;width: ${dotSize}px;<c:if
            test="${state}">left: ${moveLeft}px</c:if>"></span>
</span>
* */

function initSwitch($spans) {
    for (const span of $spans) {
        const $span = $(span);
        if ($span.attr("component_type") !== undefined) continue;
        $span.addClass("toggleSwitchBasic")
        $span.attr("component_type", "toggleSwitch");
        let cssWidth = $span.css("width");
        if (cssWidth !== undefined) cssWidth = cssWidth.replace("px", "");
        else cssWidth = "0";
        if (cssWidth === "0") {
            $span.css("width", 45);
            cssWidth = 45;
        }

        let cssHeight = $span.css("height");
        if (cssHeight !== undefined) cssHeight = cssHeight.replace("px", "");
        else cssHeight = "0";
        if (cssHeight === "0") {
            $span.css("height", 24);
            cssHeight = 24
        }

        let dotMargin = $span.attr("dotMargin");
        if (dotMargin === null || dotMargin === undefined || dotMargin === "") {
            dotMargin = 4;
        }
        const dotSize = Number(cssHeight) - 2 * dotMargin;
        let moveLeft = $span.attr("moveLeft")
        if (moveLeft === null || moveLeft === undefined || moveLeft === "") {
            moveLeft = Number(cssWidth) - dotSize - dotMargin * 2;
        }
        const switchID = $span.attr("id");
        const state = $span.attr("state") !== undefined && $span.attr("state") === "true";
        let leftCssString = state ? `left: ${moveLeft}px` : ""
        $span.html(`
    <input type="hidden" class="toggleSwitchInput" name="${switchID}" id="input_${switchID}" value="${state}">
    <span class="toggleSwitchDot" style="height: ${dotSize + "px"};width: ${dotSize + "px"};${leftCssString}"></span>
    `)
        if (state) $span.css("background", "var(--highlight-component-background-color-hover)")
        $span.on("click", function () {
            toggleSwitch($span, moveLeft, $span.attr("onSwitchOn"), $span.attr("onSwitchOff"));
        });
        let disabledAttr = $span.attr("disabled");
        if (!(disabledAttr === "" || disabledAttr === "true" || disabledAttr === "disabled")) {
            $span.removeAttr("disabled");
        } else {
            $span.attr("disabled", "disabled");
        }
    }
}

/**
 *  use as
 *  <div rounded>
 *      <div title>{title}</div>
 *      <div content>{content}</div>
 *  <div>
 *  or
 *  <div rounded title="{title}">
 *      <div>{content}</div>
 *  </div>
 *  optional attributes:
 *  | titleMinHeight (default 36px)
 *  | titlePadding (default 4px)
 * */
function initShrinkPane($divs) {
    for (const div of $divs) {
        const $div = $(div);
        if ($div.attr("component_type") !== undefined) continue;
        let $title;
        let $content = $(`
<div style="overflow: hidden;display: none;">
    <div class="line"</div>
</div>`);
        let title = $div;

        $div.attr("component_type", "shrinkPane");

        let titleMinHeight = $div.attr("titleMinHeight");
        if (titleMinHeight === undefined) titleMinHeight = 36;
        let titlePadding = $div.attr("titleMinHeight");
        if (titlePadding === undefined) titlePadding = 4;

        let titleText = title.attr("title");
        if (titleText !== undefined) {
            $title = $(`
<div style="display: flex;flex-direction: row;align-items: center;padding: ${titlePadding}px;min-height: ${titleMinHeight + titlePadding * 2}px" ondblclick="switchShrinkPane(this)">
    <label style="margin-left: 8px;">${titleText}</label>
    <div class="blank"></div>
    <div style="width: ${titleMinHeight - titlePadding * 2}px;height: ${titleMinHeight - titlePadding * 2}px;display: flex;justify-content: center;align-items: center;flex: none;margin: ${titlePadding}px" rounded component_type="shrinkButton" clickable onclick="
           rotateShrinkButton(this,shrinkDiv,expandDiv)">
       <div style="rotate: 0deg;" component_type="shrinkButtonPointer">
           <div></div>
           <div></div>
       </div>
    </div>
</div>`);
            $content.append($div.children().eq(0));
        } else {
            $title = $(`
<div style="display: flex;flex-direction: row;align-items: center;padding: ${titlePadding}px;min-height: ${titleMinHeight + titlePadding * 2}px" ondblclick="switchShrinkPane(this)">
    <div style="width: ${titleMinHeight - titlePadding * 2 + "px"};height: ${titleMinHeight - titlePadding * 2 + "px"};display: flex;justify-content: center;align-items: center;flex: none;margin: ${titlePadding}px" rounded component_type="shrinkButton" clickable onclick="
                rotateShrinkButton(this,shrinkDiv,expandDiv)">
        <div style="rotate: 0deg;" component_type="shrinkButtonPointer">
            <div></div>
            <div></div>
        </div>
    </div>
</div>`);
            $title.prepend($div.children("*[title]"));
            $content.append($div.children("*[content]"));
        }
        $div.append($title);
        $div.append($content);
        $div.children("script").remove();
    }
}