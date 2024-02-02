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
        } else {
            switchOn();
        }
    } else {
        if (toggleSwitchOff instanceof Function) {
            const res = toggleSwitchOff();
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
    for (const waterFallBasicElement of $(".waterFallBasic")) {
        const $waterfallBasic = $(waterFallBasicElement);
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
        this.maxValue = parseFloat($slider.attr("max_value"));
        this.minValue = parseFloat($slider.attr("min_value"));

        this.round = $slider.attr("round_calc") === "true";

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
                if (value < (this.minValue - (this.pointCount-1)) / (this.maxValue - this.minValue)) value = (this.minValue - (this.pointCount-1)) / (this.maxValue - this.minValue);
                else if (value > (this.maxValue - (this.pointCount-1)) / (this.maxValue - this.minValue)) value = (this.maxValue - (this.pointCount-1)) / (this.maxValue - this.minValue);
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
        value = this.minValue + value * (this.maxValue - this.minValue);
        if (this.round) {
            value = Math.round(value);
        }
        return value;
    }
}

function initSlider($slider) {
    for (const slider of $slider) {
        const sliderObj = new Slider($(slider));
    }
}