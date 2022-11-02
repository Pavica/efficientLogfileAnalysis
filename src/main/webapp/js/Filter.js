/**
 * author: Clark Jaindl
 * version: 1.0
 * last changed: 26.07.2022
 */

const openText = "ausklappen";
const closeText = "zuklappen";

/**
 * Function used to set the first values of the collapsable elements
 */
function loadFirstTextIntoFields(){
    $('#btnFilter')[0].innerHTML += `<span>${closeText}</span>`;
    $('#btnStatistics')[0].innerHTML += `<span>${openText}</span>`;
}

function loadFirstTextIntoNearbyField(){
    $('#btnNearby')[0].innerHTML += `<span>${closeText}</span>`;
}

/**
 * Function used to change the text of the button that expands and shrinks the area for Filters and Statistics
 * @param button button that has been clicked
 */
function onCollapseFilter(button)
{
    if(button.children("span")[0].innerText==openText){
        button.children("span").remove();
        button[0].innerHTML += `<span>${closeText}</span>`;
    }else{
        button.children("span").remove();
        button[0].innerHTML += `<span>${openText}</span>`;
    }
}

/**
 * Function used to open the filter menu, when the user has searched with invalid data
 */
function openFilterWhenInvalid(){
    let form = document.getElementById("searchForm");
    if($('#btnFilter').children("span")[0].innerText==openText && !form.checkValidity()){
        $('#collapseFilter').collapse("show");
        onCollapseFilter($('#btnFilter'));
    }
}

