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

    let jsonContent = await response.text();
    console.log(jsonContent);

    let data = JSON.parse(jsonContent);

    alert("It worked :D");
    return data;
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

    console.log(startDate, endDate, logLevel);
    let result = await search(startDate, endDate, logLevel);

    createLogFileElements(result);
}

let levelColor = {
    "INFO": "#36c590",
    "DEBUG": "#5188ca",
    "WARN": "#fbf571",
    "ERROR": "#ff7168",
    "TRACE": "#ffa566",
    "FATAL": "#c978b8"
};

function formatDate(date){
    return "" +
        ("0" + date.getUTCDate()).slice(-2) + "." +
        ("0" + (date.getUTCMonth()+1)).slice(-2) + "." +
        date.getUTCFullYear() + " " +
        ("0" + date.getUTCHours()).slice(-2) + ":" +
        ("0" + date.getUTCMinutes()).slice(-2) + ":" +
        ("0" + date.getUTCSeconds()).slice(-2);
}

function createLogFileElements(info){
    let container = document.getElementById("logFileElementHolder");
    let text ="";
    info.forEach(file =>{
        text +=`
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
        </div>`;
    })
    container.innerHTML = text;
}
