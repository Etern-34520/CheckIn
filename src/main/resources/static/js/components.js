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

function expandDiv(shrinkButton) {
    const $content = $(shrinkButton).parent().parent().children().eq(1);
    $content.show(200, "easeOutQuad", function (){
        $content.css("display","block");
    });
}

function shrinkDiv(shrinkButton) {
    $(shrinkButton).parent().parent().children().eq(1).hide(200, "easeOutQuad");
}

function toggleSwitch($toggleSwitch, moveLeft, toggleSwitchOn, toggleSwitchOff) {
    if ($toggleSwitch.attr('disabled') === 'disabled') return;
    const $input = $toggleSwitch.find('input');
    if ($input.val() === 'false') {
        $toggleSwitch.find('.toggleSwitchDot').animate({left: moveLeft}, 80, 'easeInOutCubic');
        $toggleSwitch.css('background', 'var(--highlight-component-background-color-hover)');
        $input.val('true');
        if (toggleSwitchOn instanceof Function)
            toggleSwitchOn();
    } else {
        $toggleSwitch.find('.toggleSwitchDot').animate({left: 0}, 80, 'easeInOutCubic');
        $toggleSwitch.css('background', 'var(--context-menu-background-color)');
        $input.val('false');
        if (toggleSwitchOff instanceof Function)
            toggleSwitchOff();
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

function switchToIgnoreHandler($toggleSwitch,status) {
    if (status) {
        switchOnIgnoreHandler($toggleSwitch);
    } else {
        switchOffIgnoreHandler($toggleSwitch);
    }
}