/**
 * author: Luka
 * version: 1.0
 * last changed: 22.07.2022
 */

/**
 * Function used to set a cookie
 * @param cname name of  cookie
 * @param cvalue value of cookie
 * @param exdays days until the cookie expires
 */
function setCookie(cname, cvalue, exdays) {
    const d = new Date();
    d.setTime(d.getTime() + (exdays * 24 * 60 * 60 * 1000));
    let expires = "expires="+d.toUTCString();
    document.cookie = cname + "=" + cvalue + ";" + expires + ";path=/";
}

/**
 * Function used to get the value of an existing cookie
 * @param cname name of a specified cookie
 * @returns {string} returns the value of an existing cookie
 */
function getCookie(cname) {
    let name = cname + "=";
    let ca = document.cookie.split(';');
    for(let i = 0; i < ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}

/**
 * Function used to see if a cookie already exists
 * @param name name of a specified cookie
 * @returns {boolean} returns if the specified cookie exists or not
 */
function checkCookie(name) {
    let cookie = getCookie(name);
    if (cookie != "") {
       return true;
    } else {
        return false;
    }
}

/**
 * Function used to set a statistics cookie based on delimiter |
 */
function setStatisticsCookie(){
    let statisticsCookie = getCookie("statistics").split("|");
    let statisticsCheckboxes = $(".statisticsCheckbox");

    for(let i = 0; i< statisticsCookie.length; i++){
        statisticsCheckboxes[i].checked = (statisticsCookie[i] == 'true');
    }
}

/**
 * Function used to reload (fill it with new data and reset it) a statistics cookie
 */
function reloadStatisticsCookie(){
    let statisticsValues ="";
    $(".statisticsCheckbox").each(function (){
        statisticsValues += $(this).prop("checked") + "|";
    });
    statisticsValues = statisticsValues.substring(0,statisticsValues.length-1);
    setCookie("statistics", statisticsValues, 3652);
}

/**
 * Function used to set a color cookie based on delimiter |
 */
function setColorCookie(){
    let colorCookie = getCookie("color").split("|");
    saveColor(colorCookie[0]);
    saveTextColor(colorCookie[1]);
}

/**
 * Function used to reload (fill it with new data and reset it) a color cookie
 */
function reloadColorCookie(){
    let style =document.documentElement.style;
    let cookieValues = style.getPropertyValue('--main-color') + "|" + style.getPropertyValue('--main-text-color');
    setCookie("color", cookieValues, 3652);
}

/**
 * Function used to set a fetch nearby cookie
 */
function setSearchNearbyCookie(){
    let searchNearbyCookie = getCookie("searchNearby");
    $('#searchNearby').val(searchNearbyCookie);
}

/**
 * Function used to reload (fill it with new data and reset it) a fetch nearby cookie
 */
function reloadSearchNearbyCookie(){
    setCookie("searchNearby", $('#searchNearby').val(), 3652);
    console.log("updated to " + getCookie("searchNearby"));
}

/**
 * Function used to load in all existing cookies
 */
function loadCookies(){
    loadStatisticsCookie();
    loadColorCookie();
    loadSearchNearbyCookie();
}

function loadColorCookie(){
    if(checkCookie("color")){
        setColorCookie();
    }else{
        setCookie("color", "#fff200|#ffffff", 3652);
    }
}

function loadSearchNearbyCookie(){
    if(checkCookie("searchNearby")){
        setSearchNearbyCookie();
    }else{
        setCookie("searchNearby", "0", 3652);
    }
}

function loadStatisticsCookie(){
    if(checkCookie("statistics")){
        setStatisticsCookie();
        reloadAllActiveStatistics();
    }else{
        setCookie("statistics", "false|false|false|false|false", 3652);
    }
}
