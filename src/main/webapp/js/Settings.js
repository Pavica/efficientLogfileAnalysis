let activeStatistics = [];


function onShowStatistics(checkbox, chartName, chartValue)
{
    if (checkbox.checked)
    {
        activeStatistics.push({
           "chartName" : chartName,
           "chartValue" : chartValue
        });
        console.log(activeStatistics);
    }
     else
    {
        let index = activeStatistics.map(o => o.chartName).indexOf(chartName);
        console.log(index)
        if(index > -1)
            activeStatistics.splice(index, 1);
        console.log(activeStatistics);
    }
     showActiveStatistics(activeStatistics, chartValue);
}