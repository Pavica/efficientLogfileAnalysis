/**
 * author: Clark
 * version: 1.1
 * last changed: 27.07.2022
 */


/** used for the active statistics that are to be displayed */
let activeStatistics = [];

/** contains all currently available statistics */
let allStatistics = ['barChart', 'pieChart', 'lineChart', 'polarAreaChart'];

/** contains all existing statistics with their checkbox id */
let statisticsMap = new Map([
    ['barChart', 'showbarChart'],
    ['pieChart', 'showpieChart'],
    ['lineChart', 'showlineChart'],
    ['polarAreaChart', 'showpolarAreaChart'],
]);

/** contains all LogLevels and the amount of each */
statisticsDataMap = new Map([
    ['INFO', 0],
    ['DEBUG', 0],
    ['WARN', 0],
    ['ERROR', 0],
    ['TRACE', 0],
    ['FATAL', 0],
]);

/**
 * Function used to add statistics to active statistics that have been clicked via checkbox.
 *
 * @param checkbox checkbox that has been clicked
 * @param chartName name of the chart which the checkbox is relating to
 * @param chartValue width that the chart may use to be displayed
 */
function onShowStatistics(checkbox, chartName, chartValue)
{
    //adds the current selection to cookies
    reloadStatisticsCookie();

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

    showActiveStatistics(activeStatistics);

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

/**
 * Function used to create and load the bar chart into the corresponding canvas.
 */
function loadBarChart(){
    const barChartContext = document.getElementById("barChart").getContext('2d');
    const barChart = new Chart(barChartContext, {
        type: 'bar',
        data: {
            labels: ["Info", "Debug", "Warn", "Error",
                "Trace", "FATAL"],
            datasets: [{
                label: 'Statistik',
                backgroundColor: '#c978b8',
                data: [statisticsDataMap.get('INFO'), statisticsDataMap.get('DEBUG'), statisticsDataMap.get('WARN'), statisticsDataMap.get('ERROR'), statisticsDataMap.get('TRACE'), statisticsDataMap.get('FATAL')],
            }]
        },
        options: {
            maintainAspectRatio: false,
            scales: {
                yAxes: [{
                    ticks: {
                        beginAtZero: true,
                    }
                }]
            }
        },
    });
}

/**
 * Function used to create and load the pie chart into the corresponding canvas.
 */
function loadPieChart(){
    const pieChartContext = document.getElementById("pieChart").getContext('2d');
    const pieChart = new Chart(pieChartContext, {
        plugins: [ChartDataLabels],
        type: 'pie',
        data: {
            labels: ["INFO", "DEBUG", "WARN", "ERROR",
                "TRACE", "FATAL"],
            datasets: [{
                label: 'food Items',
                backgroundColor: ["#36c590", "#5188ca", "#fbf571",
                    "#ff7168", "#ffa566", "#c978b8"],
                data: [statisticsDataMap.get('INFO'), statisticsDataMap.get('DEBUG'), statisticsDataMap.get('WARN'), statisticsDataMap.get('ERROR'), statisticsDataMap.get('TRACE'), statisticsDataMap.get('FATAL')],
            }]
        },options: {
            legend: {
                position: 'right',
                labels: {
                    padding: 20,
                    boxWidth: 10
                },
            },
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                datalabels: {
                    formatter: (value, ctx) => {
                        let sum = 0;
                        let dataArr = ctx.chart.data.datasets[0].data;
                        dataArr.map(data => {
                            sum += data;
                        });
                        let percentage = Math.floor(Number.EPSILON + (value * 100 / sum).toFixed(2) * 10) / 10;
                        if(percentage < 1){
                            percentage = '< 1%';
                            return percentage
                        }

                        return percentage + "%";
                    },
                    color: 'black',
                    labels: {
                        title: {
                            font: {
                                size: '12'
                            }
                        }
                    }
                }
            }
        }
    });
}


/**
 * Function used to create and load the line chart into the corresponding canvas.
 */
function loadLineChart(){
    const lineChartContext = document.getElementById("lineChart").getContext('2d');
    const lineChart = new Chart(lineChartContext, {
        type: 'line',
        data: {
            labels: ["Info", "Debug", "Warn", "Error",
                "Trace", "FATAL"],
            datasets: [{
                label: 'Statistik',
                backgroundColor: '#36c590',
                data: [statisticsDataMap.get('INFO'), statisticsDataMap.get('DEBUG'), statisticsDataMap.get('WARN'), statisticsDataMap.get('ERROR'), statisticsDataMap.get('TRACE'), statisticsDataMap.get('FATAL')],
            }]
        },
        options: {
            maintainAspectRatio: false,
            scales: {
                yAxes: [{
                    ticks: {
                        beginAtZero: true,
                    }
                }]
            }
        },
    });
}

/**
 * Function used to create and load the polarArea chart into the corresponding canvas.
 */
function loadPolarAreaChart(){
    const polarAreaChartContext = document.getElementById("polarAreaChart").getContext('2d');
    const polarAreaChart = new Chart(polarAreaChartContext, {
        type: 'polarArea',
        data: {
            labels: ["Warn", "Error", "Trace", "FATAL"],
            datasets: [{
                label: 'Statistik',
                backgroundColor: ["#fbf571", "#ff7168", "#ffa566", "#c978b8"],
                data: [statisticsDataMap.get('INFO'), statisticsDataMap.get('DEBUG'), statisticsDataMap.get('WARN'), statisticsDataMap.get('ERROR'), statisticsDataMap.get('TRACE'), statisticsDataMap.get('FATAL')],
            }]
        },
        options: {
            maintainAspectRatio: false,
            responsive: true,
            plugins: {
                legend: {
                    position: 'top',
                },
                title: {
                    display: true,
                    text: 'Warnings and above'
                }
            }
        },
    });
}

/** used to identify how many statistics are to be displayed */
let countStatistics;

/**
 * Function used to determine how each statistic is displayed.
 *
 * @param activeStatistics statistics that are to be displayed.
 */
function showActiveStatistics(activeStatistics)
{
    countStatistics = activeStatistics.length;

    switch(countStatistics){
        case 0:
            document.getElementById('statisticsHolder').innerHTML = ""
            break;

        case 1:

            createChartContainer(activeStatistics, [12]);

            break;

        case 2:
            if((activeStatistics[0].chartValue == 2 && activeStatistics[1].chartValue == 2) || (activeStatistics[0].chartValue == 1 && activeStatistics[1].chartValue == 1))
            {
                createChartContainer(activeStatistics, [6,6]);
             }
              else if(activeStatistics[0].chartValue == 2)
             {
                 createChartContainer(activeStatistics, [8, 4])
            }
            else if(activeStatistics[1].chartValue == 2)
            {
                createChartContainer(activeStatistics, [4, 8])
            }

             break;

        case 3:

            for(let i = 0 ; i < countStatistics ; i++){
                createChartContainer(activeStatistics, [4,4,4]);
            }

            break;
    }
    for(let i = 0 ; i < countStatistics ; i++)
        reloadStatistics(activeStatistics[i].chartName)
}

/**
 * Creates the canvas for each statistic, depending on their width.
 *
 * @param charts charts which are to be displayed
 * @param chartWidths width of the charts in an array
 */
function createChartContainer(charts, chartWidths){

    document.getElementById('statisticsHolder').innerHTML = "";

    for(let i = 0 ; i < countStatistics ; i++)
        document.getElementById('statisticsHolder').innerHTML +=
            `
            <div class="d-flex flex-column border col-md-${chartWidths[i]} text-center">
                <div class="mt-auto">
                    <div class="chart-container chartHeight">
                        <canvas id="${charts[i].chartName}"></canvas>
                    </div>
                </div>
            </div>
            `
}

/**
 * Function used to reload the statistics, especially when the canvas of the statistic was deleted and recreated.
 *
 * @param statistic
 */
function reloadStatistics(statistic){
    if(statistic == 'barChart'){
        loadBarChart();
    }else if(statistic == 'pieChart'){
        loadPieChart();
    }else if(statistic == 'lineChart'){
        loadLineChart();
    } else if(statistic == 'polarAreaChart'){
        loadPolarAreaChart();
    }
}

/**
 * Function used to check if a specific checkbox is checked
 *
 * @param elementId id of the checkbox
 */
function checkIfChecked(elementId){
   return document.getElementById(elementId).checked;
}

/** Function used to reload all currently active statistics */
function reloadAllActiveStatistics(){

    statisticsMap.forEach((value, key, map) => {
        if(checkIfChecked(value)){
            onShowStatistics(document.getElementById(value), key, 2)
        }
    })
    console.log("I'm in")
}

/** Function used to determine the data for some statistics */
function getStatisticData(){

    statisticsDataMap = new Map([
        ['INFO', 0],
        ['DEBUG', 0],
        ['WARN', 0],
        ['ERROR', 0],
        ['TRACE', 0],
        ['FATAL', 0],
    ]);

    let searchFullData = getStatisticsData();

    for(let i = 0 ; i < searchFullData.length ; i++){
        for(let j = 0 ; j < searchFullData[i].length ; j++){
            addLogLevelData(searchFullData[i][j].logLevel);
        }
    }
}

/** Function used to set the amount of each loglevel */
function addLogLevelData(logLevel){

    let value = statisticsDataMap.get(logLevel);
    value++;
    statisticsDataMap.set(logLevel, value);
}