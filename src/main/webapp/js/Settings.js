/**
 * author: Clark
 * version: 1.0
 * last changed: 13.07.2022
 */

/** used for the active statistics that are to be displayed */
let activeStatistics = [];

/**
 * Function used to show the statistics which are clicked in the settings menu
 *
 * @param checkbox checkbox that has been clicked
 * @param chartName name of the chart which the checkbox is relating to
 * @param chartValue width that the chart may use to be displayed
 */
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