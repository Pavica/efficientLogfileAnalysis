/**
 * author: Andreas
 * version: 1.0
 * last changed: 21.07.2022
 */

/**
 * Fucntion used to get the currently set log file path by fetching it
 * @returns {Promise<string>} the specified path
 */
async function getPath()
{
    let response = await fetch("api/settings/path");

    if(!response.ok)
    {
        alert("Path kann nicht geladen werden :(");
        return "";
    }

    let path = await response.text();
    return path;
}

/**
 * Function used to set the log file path
 * @param path specified path
 * @returns {Promise<boolean>} if the setting of the path was successful or not
 */
async function setPath(path)
{
    let response = await fetch("api/settings/path", {
        method : "PUT",
        body : path,
    });

    if(!response.ok)
    {
        alert("Path konnte nicht gesetzt werden!");
        return false;
    }

    return true;
}

/**
 * Function used ot laod the current path into the path field in settings
 */
async function loadPathIntoField(){
    let path = await getPath();
    $('#path').val(path);
}

/** const used to describe the base color of the gui */
const baseColor ="#fff200";
/** const used to describe the base text color of the gui elements*/
const baseTextColor ="#ffffff";

/**
 * Function used to save the color to the specified style variable
 * @param color specified color
 */
function saveColor(color){
    document.documentElement.style.setProperty('--main-color', color);
    reloadColorCookie();
}

/**
 * Function used to save the text color to the specified style variable
 * @param color specified text color
 */
function saveTextColor(color){
    document.documentElement.style.setProperty('--main-text-color', color);
    reloadColorCookie();
}

/**
 * Function used to reset the element and text colors back to their base
 */
function resetColor(){
    saveColor(baseColor);
    saveTextColor(baseTextColor);

    setInputColors(baseColor,baseTextColor);
}

/**
 * Function used to set the element and text color to specified colors
 * @param color specified element color
 * @param textColor specified text color
 */
function setInputColors(color, textColor){
    $('#color').val(color);
    $('#textColor').val(textColor);
}
