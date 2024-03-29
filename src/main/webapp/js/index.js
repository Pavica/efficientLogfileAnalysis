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
    let classes = await loadClassNames();
    let exceptions = await loadExceptions();

    $("#datalistOptionsModul").empty();
    $("#datalistOptionsClass").empty();
    $("#datalistOptionsException").empty();

    $.each(modules, function(i, item) {
        $("#datalistOptionsModul").append($("<option>").attr('value', item));
    });
    $.each(classes, function(i, item) {
        $("#datalistOptionsClass").append($("<option>").attr('value', item));
    });
    $.each(exceptions, function(i, item) {
        $("#datalistOptionsException").append($("<option>").attr('value', item));
    });
}

//TODO declare state in initIndexToast
let state;
async function initIndexToast() {

    state = "NOT READY";

    while (true) {
        try
        {
            let response = await fetch("api/state/updates", {
                method : "POST",
                headers : {
                    "content-type" : "application/json"
                },
                body : state
            });

            console.log(response.status);

            //HTTP 204 = No content; Means that the request timeout was reached
            if(response.status == 204){
                continue;
            }

            if(!response.ok){
                throw "Server is unreachable";
            }

            state = await response.text();
            displayState(state);
        }
        catch(e){
            state = "SERVER UNREACHABLE";
            setToast("Connection lost!", "Lost connection to server", "Please check the connection.", "#c32232", "bg-danger", true);
            await delay(1000);
        }
    }
}

function displayState(state)
{
    switch (state) {
        case "INDEXING":
            setToast("Indexing!", "Index is currently being updated!", "Please wait about 10 seconds.", "#ffc240", "bg-warning", false, false);
            break;

        case "ERROR":
            setToast("Error!", "Index could not be updated!", "Please try again.", "#c32232", "bg-danger", true, false);
            break;

        case "INTERRUPTED":
            setToast("Interrupted!", "Indexing was interrupted!", "Please restart.", "#c32232", "bg-danger", true, false);
            break;

        case "READY":
            setToast("Ready!", "Index is ready!", "You may continue working.", "forestgreen", "bg-success", true, true);
            fillDataLists();
            break;
    }

    let toastElementList = 0;
    let toastList = 0;

    toastElementList = [].slice.call(document.querySelectorAll(".toast"));
    toastList = toastElementList.map(function (element) {
        return new bootstrap.Toast(element, {
            autohide: true
        });
    });
}

function setToast(header, text1, text2, n_background, h_background, hasCloseButton, autohide = false){
    document.getElementById("toastIndex").innerHTML = `
    <div class="position-fixed bottom-0 end-0 p-3">
        <div class="toast text-white fade show" style="background-color: ${n_background}" data-bs-autohide="${autohide}"> 
             <div class="toast-header text-white ${h_background}">
                <strong class="me-auto">${header}</strong>
                ${hasCloseButton ? '<button type="button" class="btn-close btn-close-white" data-bs-dismiss="toast"></button>' : ''}
             </div>
            <div class="toast-body">
                <p>${text1}</p>
                <p>${text2}</p>
            </div>
        </div>
    </div>
    `;
}

function delay(milliseconds){
    return new Promise(resolve => {
        setTimeout(resolve, milliseconds);
    });
}