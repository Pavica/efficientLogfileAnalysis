/**
 * author: Luka
 * version: 1.0
 * last changed: 21.07.2022
 */


/** variable used to set the filters for a search */
let filter;

/**
 * Function used to create a filterData object
 * @param startDate specified begin date for the search
 * @param endDate specified end date for the search
 * @param logLevel specified log levels for the search
 * @param module specified module for the search
 * @param className specified class for hte search
 * @param exception specified exception for the search
 * @returns {{exception: null, beginDate, endDate, module: null, logLevels: *[], className: null}} a filterData object
 */
function createFilterData(startDate, endDate, logLevel = [], module = null, className = null, exception = null)
{
    return {
        beginDate : startDate,
        endDate : endDate,
        logLevels : logLevel,
        module : module,
        className : className,
        exception : exception
    };
}

/**
 * Function used to fetch all files affected by the search
 * @param filterData specified filterData object, based on search input
 * @returns {Promise<any>} an array of log file elements
 */
async function searchForFiles(filterData)
{
    let response = await fetch("api/search/files", {
        method : "POST",
        body : JSON.stringify(filterData),
        headers : {
            "content-type" : "application/json"
        }
    });

    if(!response.ok)
    {
        alert("Not okay :(");
        return;
    }
    return await response.json();
}

/**
 * Function used to fetch all log entries in a specified file
 * @param filterData specified filterData object based on search input
 * @param filename specified filename of file
 * @returns {Promise<any>} an array of log entry elements based on filterData
 */
async function searchInFile(filterData, filename)
{
    let response = await fetch(`api/search/file/${filename}`, {
        method : "POST",
        body : JSON.stringify(filterData),
        headers : {
            "content-type" : "application/json"
        }
    });

    if(!response.ok)
    {
        alert("Not okay :(");
        return;
    }
    return await response.json();
}

async function searchInFileAmount(filterData, filename, lastSearchEntry, entryAmount)
{
    let request = {
        "lastSearchEntry" : lastSearchEntry,
        "filterData" : filter
    };

    let response = await fetch(`api/search/file/${filename}/${entryAmount}`, {
        method : "POST",
        body : JSON.stringify(request),
        headers : {
            "content-type" : "application/json"
        }
    });

    if(!response.ok)
    {
        alert("Not okay :(");
        return;
    }
    return await response.json();
}


/***
 * OLD function for searching for log entries
 * @param filterData specified filterData object based on search input
 * @returns {Promise<any>} an array of files with every entry element based on filterData
 */
async function search(filterData)
{
    let response = await fetch("api/search/filter", {
        method : "POST",
        body : JSON.stringify(filterData),
        headers : {
            "content-type" : "application/json"
        }
    });

    if(!response.ok)
    {
        alert("Not okay :(");
        return;
    }
    return await response.json();
}

/**
 * Functioon used to load one log Entry based on filename and logEntryID
 * @param filename specified log file name
 * @param logEntryID specified log entry id
 * @returns {Promise<any>} a log entry object
 */
async function loadLogEntry(filename, logEntryID)
{
    let response = await fetch(`api/logFiles/${filename}/${logEntryID}`);
    if(!response.ok){
        alert("Server Error: " + response.status);
        return;
    }
    return await response.json();
}

/**
 * OLD function used to get multiple log entries in a file
 * @param filename specified filename
 * @param logEntryIDs specified log entry ids
 * @returns {Promise<any>} an array of log entries of a file
 */
async function loadLogEntries(filename, logEntryIDs){
    let response = await fetch("api/logFiles/" + filename + "/specificEntries", {
        method: "POST",
        body: JSON.stringify(logEntryIDs),
        headers: {
            "content-type":"application/json"
        }
    });
    if(!response.ok){
        alert("Server Error: " + response.status);
        return;
    }
    return await response.json();
}

async function loadClassNames()
{
    let response = await fetch("api/logFiles/classNames");
    if(!response.ok){
        alert("Server Error: " + response.status);
        return;
    }
    return await response.json();
}

async function loadModules()
{
    let response = await fetch("api/logFiles/modules");
    if(!response.ok){
        alert("Server Error: " + response.status);
        return;
    }
    return await response.json();
}

/**
 * Function used to get all the data from the inputs and put the input in a filterData object.
 * Once the data has been set it begins the search.
 * @returns {Promise<void>}
 */
async function readyForSearch(){
    let startDate = trueStartDate.getTime();
    let endDate = trueEndDate.getTime();

    let logLevel = [];

    $('.logLevelCheckbox').each(function() {
       if($(this).prop("checked")){
           logLevel.push($(this).val());
       }
    });

    let moduleName =  $('#modulDataList').val();
    let className =  $('#classDataList').val();
    let exceptionName =  $('#exceptionDataList').val();


    filter = createFilterData(startDate, endDate, logLevel);
    await startSearch();
}

/**
 * function used to start the search and add the results into the gui
 *
 */
async function startSearch(){
    setSpinnerVisible(true);
    let data = await searchForFiles(filter);
    setSpinnerVisible(false);
    createLogFileElements(data);
}

/**
 * function used to set the search spinner if the search is in progress
 * @param isSpinnerVisible specified boolean used to set if the search is in progress or not
 */
function setSpinnerVisible(isSpinnerVisible){
    let button = $('#searchButton')[0];
    if(isSpinnerVisible){
        button.innerHTML =
            `Suchen
            <div class="spinner-border spinner-border-sm" role="status">
                  <span class="visually-hidden">Loading...</span>
            </div>`;
    }else{
        button.innerHTML = "Suchen";
    }
}

/**
 * Function used to format a date based on format: YYYY:MM:DD HH:mm:ss
 * @param date specified date object
 * @returns {string} a string of the date object in the specified format YYYY:MM:DD HH:mm:ss
 */
function formatDate2(date){
    return "" +
        date.getFullYear() + "." +
        ("0" + (date.getMonth()+1)).slice(-2) + "." +
        ("0" + date.getDate()).slice(-2) + " " +
        ("0" + date.getHours()).slice(-2) + ":" +
        ("0" + date.getMinutes()).slice(-2) + ":" +
        ("0" + date.getSeconds()).slice(-2);
}

/**
 * Function used to format a date based on format: YYYY:MM:DD HH:mm:ss
 * @param date specified date object
 * @returns {string} a string of the date object in the specified format DD:MM:YYYY HH:mm:ss
 */
function formatDate(date){
    return "" +
        ("0" + date.getDate()).slice(-2) + "." +
        ("0" + (date.getMonth()+1)).slice(-2) + "." +
        date.getFullYear() + " " +
        ("0" + date.getHours()).slice(-2) + ":" +
        ("0" + date.getMinutes()).slice(-2) + ":" +
        ("0" + date.getSeconds()).slice(-2);
}

/** variable used to tell which color belongs to which logLevel  */
let levelColor = {
    "INFO": "#36c590",
    "DEBUG": "#5188ca",
    "WARN": "#fbf571",
    "ERROR": "#ff7168",
    "TRACE": "#ffa566",
    "FATAL": "#c978b8"
}

/**
 * Function used to create log entry elements in the GUI
 * @param data specified file data
 */
function createLogFileElements(data){
    let container = document.getElementById("logFileElementHolder");
    let text ="";
    data.forEach(file =>{
        text +=`
    <a data-bs-toggle="modal" data-bs-target="#logModal" onclick="displayFileLogEntries('${file.filename}')">
        <div class="row mt-2">
            <div class="col-md-3 justify-content-center text-center log-date log-center border rounded-3">
                <p class="my-3">${formatDate(new Date(file.firstDate))} - ${formatDate(new Date(file.lastDate))}</p>
            </div>
            <div class="col-md-6 log-center border rounded-3">
                <p class="my-3"> ${file.filename}</p>
            </div>
            <div class="col-md-3 log-center border rounded-3">`;
                file.logLevels.sort().forEach(logLevel =>{
                    text +=`<span class="log-center h-100 badge badge-color" style="background-color: ${levelColor[logLevel]}">${logLevel.charAt(0)}</span>`
                });
        text +=`
            </div>
        </div>
    </a>`;
    })
    container.innerHTML = text;
}

/**
 * Functions used to slice up an array
 */
function sliceIntoChunks(arr, chunkSize) {
    const res = [];
    for (let i = 0; i < arr.length; i += chunkSize) {
        const chunk = arr.slice(i, i + chunkSize);
        res.push(chunk);
    }
    return res;
}

/**
 * Function used to display log entries of a specified file quickly by doing multiple fetches
 * @param filename specified filename
 */
async function displayFileLogEntries(filename){
    $('#logEntryTable').DataTable().clear();
    $('#logEntryTable').DataTable().destroy();

    document.getElementById("floatingTextarea").innerText = "";
    document.getElementById("logFileTitle").innerText = "Log file name: " + filename;

    let table = $('#logEntryTable').DataTable({
        scrollY: '250px',
        scrollCollapse: true,
        paging: true,
        pageLength: 5,
        lengthMenu: [5, 10, 20, 50, 100],
        select: {
            style: 'single'
        },
    });

    $('#logModal').on('shown.bs.modal', function (e) {
        $($.fn.dataTable.tables(true)).DataTable().columns.adjust();
    });

    let lastSearchEntry = null;
    let data = null;
    let num = 0;

    table.off('select.dt');
    table.on( 'select.dt', async function () {
       let id = $('.selected')[0].id;
        document.getElementById("floatingTextarea").innerText = (await loadLogEntry(filename, id)).message;
    });

    while((data = await searchInFileAmount(filter, filename, lastSearchEntry, 1000)) != null){
        lastSearchEntry = {"docNumber":data.lastSearchEntry.docNumber, "docScore":data.lastSearchEntry.docScore};
        let currentData = data.logEntries;

        for(let i=0; i<currentData.length; i++){
            table.row.add([formatDate2(new Date(currentData[i].time)),currentData[i].logLevel,currentData[i].module,currentData[i].className]).node().id = currentData[i].entryID;
            num++;
        }
        table.draw(false);
    }
}

async function displayFileLogEntriesFast(filename){
    $('#logEntryTable').DataTable().clear();
    $('#logEntryTable').DataTable().destroy();

    document.getElementById("floatingTextarea").innerText = "";
    document.getElementById("logFileTitle").innerText = "Log file name: " + filename;

    let table = $('#logEntryTable').DataTable({
        scrollY: '250px',
        scrollCollapse: true,
        paging: true,
        pageLength: 5,
        lengthMenu: [5, 10, 20, 50, 100],
    });

    $('#logModal'). on('shown.bs.modal', function () {
        $($.fn.dataTable.tables(true)).DataTable().columns.adjust();
    });

    let data = await searchInFile(filter, filename);
    let partedData = sliceIntoChunks(data, 1000);

    let num = 0;
    for(let j=0; j<partedData.length; j++){
        let currentData = partedData[j];
        for(let i=0; i<currentData.length; i++){
            table.row.add([formatDate2(new Date(currentData[i].time)),currentData[i].logLevel,currentData[i].module,currentData[i].className]).node().id = 'logEntry'+num;
           /* $('table tbody').off('click', '#logEntry' + num);
            $('table tbody').on('click', '#logEntry' + num, async function() {
                $(this).parent().children().removeClass("selected");
                $(this).addClass("selected");
                document.getElementById("floatingTextarea").innerText = (await loadLogEntry(filename, currentData[i].entryID)).message;
            });*/
            num++;
        }
        await new Promise(s => setTimeout(s, 1));
        table.draw(false);
    }
}

async function displayFileLogEntriesSlow(filename){
    $('#logEntryTable').DataTable().clear();
    $('#logEntryTable').DataTable().destroy();

    document.getElementById("floatingTextarea").innerText = "";
    document.getElementById("logFileTitle").innerText = "Log file name: " + filename;

    let table = $('#logEntryTable').DataTable({
        scrollY: '250px',
        scrollCollapse: true,
        paging: true,
        pageLength: 5,
        lengthMenu: [5, 10, 20, 50, 100],
    });
    let data = await searchInFile(filter, filename);
    let num = 0;
    data.forEach(logEntry =>{
        table.row.add([formatDate2(new Date(logEntry.time)),logEntry.logLevel,logEntry.module,logEntry.className]).node().id = 'logEntry'+num;
        /*$('table tbody').off('click', '#logEntry' + num);
        $('table tbody').on('click', '#logEntry' + num, async function() {
            $(this).parent().children().removeClass("selected");
            $(this).addClass("selected");
            document.getElementById("floatingTextarea").innerText = (await loadLogEntry(filename, logEntry.entryID)).message;
        });*/
        num++;
    });

    table.draw();

    $('#logModal'). on('shown.bs.modal', function (e) {
        $($.fn.dataTable.tables(true)).DataTable().columns.adjust();
    })
}
