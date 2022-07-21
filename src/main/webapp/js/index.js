function loadDocument(){
    loadCookies();
    loadPathIntoField();
    setInputColors(document.documentElement.style.getPropertyValue('--main-color'),
        document.documentElement.style.getPropertyValue('--main-text-color'));
    initializeDatePickers();
}

function initializeDatePickers(){
        $( "#startDate" ).datepicker({
            dateFormat: "dd.mm.yy"
        });
        setMinMaxDate("startDate");

        $( "#endDate" ).datepicker({
            dateFormat: "dd.mm.yy"
        });
        setMinMaxDate("endDate");
}


