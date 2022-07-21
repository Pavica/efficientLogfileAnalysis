function setCookie(cname, cvalue, exdays) {
    const d = new Date();
    d.setTime(d.getTime() + (exdays * 24 * 60 * 60 * 1000));
    let expires = "expires="+d.toUTCString();
    document.cookie = cname + "=" + cvalue + ";" + expires + ";path=/";
}

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

function checkCookie(name) {
    let cookie = getCookie(name);
    if (cookie != "") {
       return true;
    } else {
        return false;
    }
}

function setStatisticsCookie(){
    let statisticsCookie = getCookie("statistics").split("|");
    let statisticsCheckboxes = $(".statisticsCheckbox");

    for(let i = 0; i< statisticsCookie.length; i++){
        statisticsCheckboxes[i].checked = (statisticsCookie[i] == 'true');
    }
}

function reloadStatisticsCookie(){
    let statisticsValues ="";
    $(".statisticsCheckbox").each(function (){
        statisticsValues += $(this).prop("checked") + "|";
    });
    statisticsValues = statisticsValues.substring(0,statisticsValues.length-1);
    //currently set to 3 days
    setCookie("statistics", statisticsValues, 3);
}

function setColorCookie(){
    let colorCookie = getCookie("color").split("|");
    saveColor(colorCookie[0]);
    saveTextColor(colorCookie[1]);
}

function reloadColorCookie(){
    let style =document.documentElement.style;
    let cookieValues = style.getPropertyValue('--main-color') + "|" + style.getPropertyValue('--main-text-color');
    setCookie("color", cookieValues, 3);
}


function loadCookies(){
    if(checkCookie("statistics")){
        setStatisticsCookie();
        reloadAllActiveStatistics();
    }else{
        setCookie("statistics", "false|false|false|false", 3);
    }

    if(checkCookie("color")){
        setColorCookie();
    }else{
        setCookie("color", "#fff200|#ffffff", 3);
    }
}
