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

    let data = await response.json();

    alert("It worked :D");
    return data;
}