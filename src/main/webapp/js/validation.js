/**
 * author: Luka
 * version: 1.0
 * last changed: 14.07.2022
 */


// Example starter JavaScript for disabling form submissions if there are invalid fields
(function () {
    'use strict'

    // Fetch all the forms we want to apply custom Bootstrap validation styles to
    let forms = document.querySelectorAll('.needs-validation')

    // Loop over them and prevent submission
    Array.prototype.slice.call(forms)
        .forEach(function (form) {
            form.addEventListener('submit', function (event) {
                if (!form.checkValidity()) {
                    event.preventDefault()
                    event.stopPropagation()
                }

                form.classList.add('was-validated')
            }, false)
        })
})()

/**
 * Sets the Minimum and Maximum date of a specified date jquery element
 * @param id used to identify a jquery html element
 */
function setMinMaxDate(id){
    let date = $("#"+id);
    date.datepicker('option', 'minDate', new Date(1970,1-1,1));
    date.datepicker('option', 'maxDate', new Date());
}

/**
 * Adds Validation (which decides the style and validity) to a specified html element
 * @param element specified html element
 */
function addValidity(element){
    element.setCustomValidity("");
}

/**
 * Removes Validation (which decides the style and validity) to a specified html element
 * @param element specified html element
 */
function removeValidity(element){
    element.setCustomValidity("invalid");
}

/**
 * Compares both the startTime and endTime to see if they are in proper Order. Based on the result it
 * sets the validation properly.
 */
function compareBothTimeInputs(){
    let isProperRange = stringToTime($('#startTime')).getTime() < stringToTime($('#endTime')).getTime();
    if(isProperRange){
        addValidity($('#startTime')[0]);
        addValidity($('#endTime')[0]);
    }else{
        removeValidity($('#startTime')[0]);
        removeValidity($('#endTime')[0]);
    }
}

/**
 * Compares both the startDate and endDate to see if they are in proper Order. Based on the result it
 * sets the validation properly. Here is also where it is checked, werther the dates set are the same.
 */
function compareBothDateInputs(){
    let startDate = $('#startDate');
    let endDate = $('#endDate');
    let isProperRange = stringToDate(startDate).getTime() <= stringToDate(endDate).getTime();

    isSameDay = stringToDate(startDate).getTime() === stringToDate(endDate).getTime();

    if(isProperRange){
        addValidity(startDate[0]);
        addValidity(endDate[0]);

        $("#startTime").prop('disabled', false);
        $("#endTime").prop('disabled', false);
    }else{
        removeValidity($('#startDate')[0]);
        removeValidity($('#endDate')[0]);

        $("#startTime").prop('disabled', true);
        $("#endTime").prop('disabled', true);

        trueStartDate = null;
        trueEndDate = null;
    }
}

/**
 * Creates a Date object from a specified date jquery element. Specifically only day, month and year.
 * @param date specified date jquery element
 * @returns {Date} a Javascript Date object
 */
function stringToDate(date){
    let help = date.val().split(".");
    return new Date(help[2], help[1] - 1, help[0]);
}

/**
 * Creates a Date object from a specified date jquery element. Specifically only hour, minute and second.
 * @param time specified date jquery element
 * @returns {Date} a Javascript Date object
 */
function stringToTime(time){
    let help = time.val().split(":");
    return  new Date(0,0,0,help[0], nvl(help[1],0), nvl(help[2],0));
}

/**
 * Creates a Javascript Date object based on both a specified date and time.
 * @param date specified Date object with date elements
 * @param time specified Date object with time elements
 * @returns {Date} a combined Date object
 */
function createTrueDateTime(date, time){
    return new Date(date.getFullYear(), date.getMonth(), date.getDate(), time.getHours(), time.getMinutes(), time.getSeconds());
}

/**
 * Method used to replace null with a specified value
 * @param value current value that is being checked
 * @param turnsInto the element the value turns into if null
 * @returns {value} if the value is not null
 * @returns {turnsInto} if the value is null
 */
function nvl(value, turnsInto){
    return value == null ? turnsInto : value;
}

let trueStartDate;
let trueEndDate;
let isSameDay;

/**
 * checks if the set startDate and endDate are the same. if that is the case, it compares the time.
 * Also sets the trueStartDate and trueEndDate.
 */
function addTime(){
    if(isSameDay){
        compareBothTimeInputs();
    }else{
        addValidity($('#startTime')[0]);
        addValidity($('#endTime')[0]);
    }
    trueStartDate = createTrueDateTime(stringToDate($('#startDate')), stringToTime($('#startTime')));
    console.log(trueStartDate.getTime());
    trueEndDate = createTrueDateTime(stringToDate($('#endDate')), stringToTime($('#endTime')));
    console.log(trueEndDate.getTime());
}

/**
 * Checks if the user input is convertable into a Date object. It also sets
 * the validity based on said result.
 * Furthermore, it calls addTime() to make sure every entry is up to date on its validation.
 * @param id
 */
function checkInputValidity(id){
    let date = $('#'+id);
    let isDateInRange = false;
    let dateValue = "";

    let regexDate = /^(?:31(.)(?:0?[13578]|1[02])\1|(?:29|30)(.)(?:0?[13-9]|1[0-2])\2)(?:1[6-9]|[2-9]\d)?\d{2}$|^29(.)0?2\3(?:(?:1[6-9]|[2-9]\d)?(?:0[48]|[2468][048]|[13579][26])|(?:16|[2468][048]|[3579][26])00)$|^(?:0?[1-9]|1\d|2[0-8])(.)(?:0?[1-9]|1[0-2])\4(?:1[6-9]|[2-9]\d)?\d{2}$/gi;

    if(regexDate.test(date.val())){
        dateValue = stringToDate(date);
        isDateInRange = dateValue >= date.datepicker('option', 'minDate')
            && dateValue <= date.datepicker('option', 'maxDate')
    }

    if(isDateInRange){
        addValidity(date[0]);
        compareBothDateInputs();
    }else{
        removeValidity(date[0]);
    }
    addTime();
}

/**
 *  Used to see if an input for a droplist is actually a part of the droplists options
 * @param id specified id of an input field
 * @param listID specified listID of a droplist
 */
function checkIfListInputExists(id, listID){
    let element = $("#"+ id);

    let obj = $("#"+listID).find("option[value='" + element.val() + "']");

    if(obj != null && obj.length > 0 || element.val() == "") {
        addValidity(element[0]);
    }else{
        removeValidity(element[0]);
    }
}
