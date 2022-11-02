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
    setInputColors(document.documentElement.style.getPropertyValue('--main-color'),
        document.documentElement.style.getPropertyValue('--main-text-color'));

    //fetch Path and Limit on modal open
    $(document).on('show.bs.modal', '#settingsModal', function () {
        loadPathIntoField();

    });

    loadFirstTextIntoFields();
    initIndexToast();

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

async function initIndexToast() {

    let state = "NOT_READY";

    while (state != "READY") {

        console.log("test")

        await delay(500);

        try{
            let response = await fetch("api/state");
            state = await response.text();
        }catch(e){
            setToast("Connection lost!", "Lost connection to server", "Please check the connection.", "#c32232", "bg-danger", true);
        }

        switch (state) {
            case "INDEXING":
                setToast("Indexing!", "Index is currently being updated!", "Please wait about 10 seconds.", "#ffc240", "bg-warning", false);
                break;

            case "ERROR":
                setToast("Error!", "Index could not be updated!", "Please try again.", "#c32232", "bg-danger", true);
                break;

            case "INTERRUPTED":
                setToast("Interrupted!", "Indexing was interrupted!", "Please restart.", "#c32232", "bg-danger", true);
                break;
            case "READY":

                setToast("Ready!", "Index is ready!", "You may continue working.", "forestgreen", "bg-success", true);
                break;
        }
        if(state == "INTERRUPTED" || state == "ERROR")
            break;

        let toastElementList = 0;
        let toastList = 0;

        toastElementList = [].slice.call(document.querySelectorAll(".toast"));
        toastList = toastElementList.map(function (element) {
            return new bootstrap.Toast(element, {
                autohide: true
            });
        });
    }
}

function setToast(header, text1, text2, n_background, h_background, x){
    if(x){
        document.getElementById("toastIndex").innerHTML =
            `
        <div class="position-fixed bottom-0 end-0 p-3">
            <div class="toast text-white fade show" style="background-color: ${n_background}">
                 <div class="toast-header text-white ${h_background}">
                    <strong class="me-auto">${header}</strong>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="toast"></button>
                 </div>
                <div class="toast-body">
                    <p>${text1}</p>
                    <p>${text2}</p>
                </div>
            </div>
        </div>
        `;
    }else{
        document.getElementById("toastIndex").innerHTML =
            `
        <div class="position-fixed bottom-0 end-0 p-3">
            <div class="toast text-white fade show" style="background-color: ${n_background}">
                 <div class="toast-header text-white ${h_background}">
                    <strong class="me-auto">${header}</strong>
                 </div>
                <div class="toast-body">
                    <p>${text1}</p>
                    <p>${text2}</p>
                </div>
            </div>
        </div>
        `;
    }

}

function delay(milliseconds){
    return new Promise(resolve => {
        setTimeout(resolve, milliseconds);
    });
}