let current_page = 1;
let records_per_page = 5;

let elements;

function fillElementArray(elementArray){
    elements = [];
    for (let i = 0; i<elementArray.length; i++){
        elements[i] = {"element":elementArray[i]};
    }
}

function prevPage()
{
    if (current_page > 1) {
        current_page--;
        changePage(current_page);
    }
}

function nextPage()
{
    if (current_page < numPages()) {
        current_page++;
        changePage(current_page);
    }
}

function changePage(page)
{
    document.getElementById("paginator").classList.remove("visually-hidden");
    let btnNext = document.getElementById("btnNext");
    let btnPrev = document.getElementById("btnPrev");
    let table = document.getElementById("logFileElementHolder");
    let currentPage = document.getElementById("page");

    if (page < 1) page = 1;
    if (page > numPages()) page = numPages();

    table.innerHTML = "";

    for (let i = (page-1) * records_per_page; i < (page * records_per_page); i++) {
        if(i == elements.length){
            break;
        }
        table.innerHTML += elements[i].element;
    }
    currentPage.innerHTML = page;
    current_page = page;

    if (page == 1) {
        btnPrev.parentElement.classList.add("disabled");
    } else {
        btnPrev.parentElement.classList.remove("disabled");
    }

    if (page == numPages()) {
        btnNext.parentElement.classList.add("disabled");
    } else {
        btnNext.parentElement.classList.remove("disabled");
    }
}

function numPages()
{
    return Math.ceil(elements.length / records_per_page);
}