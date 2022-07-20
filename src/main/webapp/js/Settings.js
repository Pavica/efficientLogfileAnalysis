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

async function loadPathIntoField(){
    let path = await getPath();
    $('#path').val(path);
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