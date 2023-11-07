function togglePartition(titleDiv) {
    let $brothers = $(titleDiv).parent().children();
    $brothers.eq(1).toggle(200,function (){
        if ($brothers.eq(0).attr('selected') === 'selected'){
            $brothers.eq(0).removeAttr('selected');
        } else {
            $brothers.eq(0).attr('selected',true);
        }
    })
}
