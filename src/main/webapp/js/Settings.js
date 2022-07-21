/**
 * author: Andreas
 * version: 1.0
 * last changed: 18.07.2022
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

async function loadPathIntoField(){
    let path = await getPath();
    $('#path').val(path);
}

let baseColor ="#fff200";
let baseTextColor ="#ffffff";

function saveColor(color){
    document.documentElement.style.setProperty('--main-color', color);
    reloadColorCookie();
}

function saveTextColor(color){
    document.documentElement.style.setProperty('--main-text-color', color);
    reloadColorCookie();
}

function resetColor(){
    saveColor(baseColor);
    saveTextColor(baseTextColor);

    setInputColors(baseColor,baseTextColor);
}

function setInputColors(color, textColor){
    $('#color').val(color);
    $('#textColor').val(textColor);
}
