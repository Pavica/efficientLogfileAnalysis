/**
 * author: Luka
 * version: 1.0
 * last changed: 21.07.2022
 */

/**
 * Function used to load all the data, that needs to be added when the page is loaded
 */
function loadDocument(){
    //Settings
    loadCookies();
    loadPathIntoField();
    setInputColors(document.documentElement.style.getPropertyValue('--main-color'),
        document.documentElement.style.getPropertyValue('--main-text-color'));

    initIndexToast()

    //Inputs
    initializeDatePickers();
    fillDataLists();

    //Modal
    abortFetchOnModalExit();
    resizeColumnsOnModalEnter();


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
    let exceptions = await loadExceptions();
    $.each(exceptions, function(i, item) {
        $("#datalistOptionsException").append($("<option>").attr('value', item));
    });
}

function initIndexToast(){

    document.getElementById("toastIndex").innerHTML =
        `
        <div class="position-fixed bottom-0 end-0 p-3">
            <div class="toast text-white bg-danger fade show">
                 <div class="toast-header text-white" style="background-color: #c32232">
                    <strong class="me-auto">Indexing!</strong>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="toast"></button>
                 </div>
                <div class="toast-body">
                    <p>Index is currently being updated.</p>
                    <p>Please wait 10 seconds!</p>
                </div>
            </div>
        </div>
        `

    var toastElementList = 0;
    var toastList = 0;

    toastElementList  = [].slice.call(document.querySelectorAll(".toast"));
    toastList = toastElementList.map(function (element) {
        return new bootstrap.Toast(element, {
            autohide: false
        });
    });
}