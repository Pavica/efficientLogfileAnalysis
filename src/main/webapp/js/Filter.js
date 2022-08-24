/**
 * author: Clark Jaindl
 * version: 1.0
 * last changed: 26.07.2022
 */

/**
 * Function used to change the text of the button that expands and shrinks the area for Filters and Statistics
 * @param button button that has been clicked
 * @param doCollapse determines if the text should be "shrink" or "expand"
 * @param name name of the button that has been clicked
 */
function onCollapseFilter(button, doCollapse, name)
{
    if(doCollapse){
        button.innerText = name + " zuklappen";
    }else{
        button.innerText = name + " ausklappen";
    }
    doCollapse == !doCollapse;
}

