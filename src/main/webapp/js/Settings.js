/**
 * author: Clark
 * version: 1.0
 * last changed: 13.07.2022
 */

/** used for the active statistics that are to be displayed */
let activeStatistics = [];

/** contains all currently available statistics */
let allStatistics = ['barChart', 'pieChart', 'lineChart', 'polarAreaChart'];

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
    }
     else
    {
        let index = activeStatistics.map(o => o.chartName).indexOf(chartName);
        if(index > -1)
            activeStatistics.splice(index, 1);
    }
     showActiveStatistics(activeStatistics, chartValue);

     let checkboxesDisabled = [];

     if(activeStatistics.length == 3){
         for(let i = 0 ; i < allStatistics.length ; i++){
             let checkbox = document.getElementById("show" + allStatistics[i]);
             if(!checkbox.checked){
                 checkbox.disabled = true;
                 checkboxesDisabled.push(checkbox);
             }
         }
     }
      else if(activeStatistics.length < 3){

         for(let i = 0 ; i < allStatistics.length ; i++){
             let checkbox = document.getElementById("show" + allStatistics[i]);
             if(checkbox.disabled){
                 checkbox.disabled = false;
                 checkboxesDisabled.splice(i,1);
             }
         }
     }
}