function clearLocalStorage(){
    localStorage.clear();
}

function loadDocumentNearby(){
    //Cookies
    loadColorCookie();
    loadSearchNearbyCookie();

    //Show filename and entryID
    displayStorageValues();

    //DataTable
    resizeColumnOnResizeWindow();

    //Filter Button Text
    loadFirstTextIntoNearbyField();
}


function resizeColumnOnResizeWindow(){
    $(window).on('resize shown.bs.tab', function(){
        $($.fn.dataTable.tables(true)).DataTable().columns.adjust();
    });
}

async function loadNearbyEntries(entryID, filename, byteRange){
    let response = await fetch("api/logFiles/nearby?" + new URLSearchParams({
        filename: filename,
        entryID: entryID,
        byteRange: byteRange
    }));
    if(!response.ok){
        alert("Server Error: " + response.status);
        return;
    }
    return await response.json();
}

async function loadNearbyEntriesRaw(entryID, filename, byteRange){
    let response = await fetch("api/logFiles/nearbyRaw?" + new URLSearchParams({
        filename: filename,
        entryID: entryID,
        byteRange: byteRange
    }));
    if(!response.ok){
        alert("Server Error: " + response.status);
        return;
    }
    return await response.json();
}

function displayStorageValues(){
    $('#inputFilename')[0].value = localStorage.getItem('filename');
    $('#inputEntryID')[0].value = localStorage.getItem('entryID');
    $('#inputByteRange')[0].value = getCookie("searchNearby");
}

function startNearbySearch(){
    let filename = localStorage.getItem('filename');
    let entryID = localStorage.getItem('entryID');
    let byteRange = $('#inputByteRange')[0].value;

    displayNearbyLogEntries(entryID,filename,byteRange);
    displayNearbyLogEntriesRaw(entryID, filename, byteRange);
}

async function displayNearbyLogEntriesRaw(entryID, filename, byteRange){
    let data = await loadNearbyEntriesRaw(entryID, filename, byteRange);
    $('#floatingTextareaNearbyRaw')[0].innerHTML = "";
    for(let i=0; i<data.length; i++){
        if(data[i].entryID == entryID){
            $('#floatingTextareaNearbyRaw')[0].innerHTML += `<span class="fst-italic fw-bold mainEntry">${data[i].entry}</span><br>` ;
        }else{
            $('#floatingTextareaNearbyRaw')[0].innerHTML += data[i].entry + "<br>";
        }
    }
}

async function displayNearbyLogEntries(entryID, filename, byteRange){
        $('#logEntryTableNearby').DataTable().clear();
        $('#logEntryTableNearby').DataTable().destroy();

        document.getElementById("floatingTextareaNearby").innerText = "";

        let table = $('#logEntryTableNearby').DataTable({
            scrollY: true,
            scrollCollapse: true,
            stateSave: true,
            paging: true,
            pageLength: 5,
            lengthMenu: [5, 10, 20, 50, 100],
            select: {
                style: 'single'
            },
        });

        table.off('select.dt');
        table.on( 'select.dt', async function () {
            entryId = $('.selected')[0].id;
            document.getElementById("floatingTextareaNearby").innerText = (await loadLogEntry(filename, entryId)).message;
        });
        let data = await loadNearbyEntries(entryID, filename, byteRange);

        for(let i=0; i<data.length; i++){
            table.row.add([formatDate2(new Date(data[i].time)),data[i].logLevel,data[i].module,data[i].className]).node().id = data[i].entryID;
        }

        //highlight the original entry
        $('#logEntryTableNearby').on( 'draw.dt page.dt', function ( e, settings, len ) {
            $('#'+entryID).addClass("fst-italic fw-bold mainEntry");
        });
        table.draw(false);
}