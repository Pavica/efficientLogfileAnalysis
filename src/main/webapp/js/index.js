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
    fillDataLists();
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

async function fillDataLists(){
    let modules = await loadModules();
    $.each(modules, function(i, item) {
        $("#datalistOptionsModul").append($("<option>").attr('value', item));
    });
    let classes = await loadClassNames();
    $.each(classes, function(i, item) {
        $("#datalistOptionsClass").append($("<option>").attr('value', item));
    });
}
