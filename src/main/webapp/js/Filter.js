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

