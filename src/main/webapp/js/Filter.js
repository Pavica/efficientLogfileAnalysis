let startDate = document.getElementById('startDate')
let endDate = document.getElementById('endDate')

startDate.addEventListener('change',(e)=>{
    let startDateVal = e.target.value
    document.getElementById('startDateSelected').innerText = startDateVal
})

endDate.addEventListener('change',(e)=>{
    let endDateVal = e.target.value
    document.getElementById('endDateSelected').innerText = endDateVal
})

function onCollapseFilter(buttonFilter, doCollapse)
{
    if(doCollapse){
        buttonFilter.innerText = "Collapse Filter";
        doCollapse == !doCollapse;
    }else{
        buttonFilter.innerText = "Expand Filter";
        doCollapse == !doCollapse;
    }
}

