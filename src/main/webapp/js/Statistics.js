function loadBarChart(){
    const barChartContext = document.getElementById("barChart").getContext('2d');
    const barChart = new Chart(barChartContext, {
        type: 'bar',
        data: {
            labels: ["Info", "Debug", "Warn", "Error",
                "Trace", "FATAL"],
            datasets: [{
                label: 'Statistik',
                backgroundColor: 'rgba(161, 198, 247, 1)',
                data: [1238, 178, 69, 5, 2, 1],
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

function loadPieChart(){
    const pieChartContext = document.getElementById("pieChart").getContext('2d');
    const pieChart = new Chart(pieChartContext, {
        plugins: [ChartDataLabels],
        type: 'pie',
        data: {
            labels: ["Info", "Debug", "Warn", "Error",
                "Trace", "FATAL"],
            datasets: [{
                label: 'food Items',
                backgroundColor: ["#0BDA51", "#0000FF", "#FFFF00",
                    "#f00000", "#FFA500", "#B10DC9"],
                data: [1238, 150, 278, 5, 2, 1],
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

function loadLineChart(){
    const lineChartContext = document.getElementById("lineChart").getContext('2d');
    const lineChart = new Chart(lineChartContext, {
        type: 'line',
        data: {
            labels: ["Info", "Debug", "Warn", "Error",
                "Trace", "FATAL"],
            datasets: [{
                label: 'Statistik',
                backgroundColor: 'rgba(161, 198, 247, 1)',
                data: [1238, 178, 69, 5, 2, 1],
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

let countStatistics;

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

function reloadStatistics(statistic){

    if(statistic == 'barChart'){
        loadBarChart();
    }else if(statistic == 'pieChart'){
        loadPieChart();
    }else if(statistic == 'lineChart'){
        loadLineChart();
    }
}
