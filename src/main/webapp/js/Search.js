async function search(startDate, endDate, logLevel = [], module = null, className = null, exception = null)
{
    let filterData = {
        beginDate : startDate,
        endDate : endDate,
        logLevels : logLevel,
        module : module,
        className : className,
        exception : exception
    };

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

async function readyForSearch(){
    let startDate = trueStartDate.getTime();
    let endDate = trueEndDate.getTime();

    let logLevel = [];

    $('.logLevelCheckbox').each(function() {
       if($(this).prop("checked")){
           console.log($(this).val());
           logLevel.push($(this).val());
       }
    });

    let moduleName =  $('#modulDataList').val();
    let className =  $('#classDataList').val();
    let exceptionName =  $('#exceptionDataList').val();

    setSpinnerVisible(true);
    info = await search(startDate, endDate, logLevel);
    setSpinnerVisible(false);
    createLogFileElements();
}

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

let levelColor = {
    "INFO": "#36c590",
    "DEBUG": "#5188ca",
    "WARN": "#fbf571",
    "ERROR": "#ff7168",
    "TRACE": "#ffa566",
    "FATAL": "#c978b8"
};

function formatDate2(date){
    return "" +
        date.getUTCFullYear() + "." +
        ("0" + (date.getUTCMonth()+1)).slice(-2) + "." +
        ("0" + date.getUTCDate()).slice(-2) + " " +
        ("0" + date.getUTCHours()).slice(-2) + ":" +
        ("0" + date.getUTCMinutes()).slice(-2) + ":" +
        ("0" + date.getUTCSeconds()).slice(-2);
}


function formatDate(date){
    return "" +
        ("0" + date.getUTCDate()).slice(-2) + "." +
        ("0" + (date.getUTCMonth()+1)).slice(-2) + "." +
        date.getUTCFullYear() + " " +
        ("0" + date.getUTCHours()).slice(-2) + ":" +
        ("0" + date.getUTCMinutes()).slice(-2) + ":" +
        ("0" + date.getUTCSeconds()).slice(-2);
}

let info;
function createLogFileElements(){
    let container = document.getElementById("logFileElementHolder");
    let text ="";
    info.forEach(file =>{
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

async function displayFileLogEntries(filename){
    $('#logEntryTable').DataTable().destroy()
    ;
    let file = findFile(filename);
    let data = await loadLogEntries(file.filename, file.logEntryIDs);
    console.log(data);

    document.getElementById("logFileTitle").innerText = "Log file name: " + file.filename;

    let container = document.getElementById("logEntryHolder");
    let text = "";

    let num = 0;
    data.forEach( logEntry => {
        text +=
        `<tr id="logEntry${num}">
            <td>${formatDate2(new Date(logEntry.time))}</td>
            <td>${logEntry.logLevel}</td>
            <td>${logEntry.module}</td>
            <td>${logEntry.className}</td>
        </tr>`;
        num++;
    });
   container.innerHTML = text;

   num = 0;
    data.forEach( logEntry => {
        let id = "logEntry"+num;
        $('#'+id).click(function(){
            document.getElementById("floatingTextarea").innerText = logEntry.message;
        });
        num++;
    });
    $('#logEntryTable').DataTable();

    //TODO: Add red if its a NullPointerException
    /* document.querySelectorAll(".table > tbody > tr").forEach();*/

    //TODO: Add yellow if they are searched for (only if fetch nearby is active)
    /* document.querySelectorAll(".table > tbody > tr").forEach();*/
}

function findFile(filename){
    let data;
    info.forEach( file =>{
        if(file.filename.toString() === filename.toString()){
            data = file;
        }
    })
    return data;
}

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