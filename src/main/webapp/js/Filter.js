//TODO: pls change pls
function onCollapseFilter(button, doCollapse)
{
    if(doCollapse){
        if(button.id == 'btnFilter'){
            button.innerText = "Filter zuklappen";
            doCollapse == !doCollapse;
        }else if(button.id == 'btnStatistics'){
            button.innerText = "Statistiken zuklappen";
            doCollapse == !doCollapse;
        }

    }else{
        if(button.id == 'btnFilter'){
            button.innerText = "Filter ausklappen";
            doCollapse == !doCollapse;
        }else if(button.id == 'btnStatistics'){
            button.innerText = "Statistiken ausklappen";
            doCollapse == !doCollapse;
        }
    }
}

