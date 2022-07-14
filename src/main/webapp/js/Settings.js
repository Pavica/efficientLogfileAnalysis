/**
 * author: Clark
 * version: 1.0
 * last changed: 13.07.2022
 */

/** used for the active statistics that are to be displayed */
let activeStatistics = [];


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
        console.log(activeStatistics);
    }
     showActiveStatistics(activeStatistics, chartValue);

     /**
     let checkboxesDisabled = [];

     if(activeStatistics.length == 3){
         for(let i = 0 ; i < allStatistics.length ; i++){
             alert("show" + allStatistics[i])
             if(document.getElementById("show" + allStatistics[i]).checked == false){
                 alert("in");
                 let checkbox = document.getElementById(allStatistics[i]);
                 checkbox.disabled = true;
                 checkboxesDisabled.push(checkbox);
             }
         }
     }
      else
     {
         for(let i = 0 ; i < checkboxesDisabled.length ; i++){
             checkboxesDisabled[i].disabled = false;
             let index = checkboxesDisabled.indexOf(checkboxesDisabled[i].chartName);
             checkboxesDisabled.splice(index, 1)
         }
     }
      */
}