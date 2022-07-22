/**
 * author: Luka
 * version: 1.0
 * last changed: 21.07.2022
 */

/**
 * Function used to load all the data, that needs to be added when the page is loaded
 */
function loadDocument(){
    loadCookies();
    loadPathIntoField();
    setInputColors(document.documentElement.style.getPropertyValue('--main-color'),
        document.documentElement.style.getPropertyValue('--main-text-color'));
    initializeDatePickers();
}

/**
 * Function used to initialize and thereby activate the datepicker for startDate and endDate
 */
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


