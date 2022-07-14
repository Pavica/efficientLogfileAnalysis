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

function setMinMaxDate(id){
    let date = $("#"+id);
    date.datepicker('option', 'minDate', new Date(1970,1-1,1));
    date.datepicker('option', 'maxDate', new Date());
}

function addValidity(element){
    element.setCustomValidity("");
}

function removeValidity(element){
    element.setCustomValidity("invalid");
}

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

function stringToDate(date){
    let help = date.val().split(".");
    return new Date(help[2], help[1] - 1, help[0]);
}

function nvl(value, turnsInto){
    return value == null ? turnsInto : value;
}

function stringToTime(time){
    let help = time.val().split(":");
    return  new Date(0,0,0,help[0], nvl(help[1],0), nvl(help[2],0));
}

function createTrueDateTime(date, time){
    return new Date(date.getFullYear(), date.getMonth(), date.getDate(), time.getHours(), time.getMinutes(), time.getSeconds());
}

let trueStartDate;
let trueEndDate;
let isSameDay;

function addTime(){
    if(isSameDay){
        compareBothTimeInputs();
    }
    trueStartDate = createTrueDateTime(stringToDate($('#startDate')), stringToTime($('#startTime')));
    console.log(trueStartDate);
    trueEndDate = createTrueDateTime(stringToDate($('#endDate')), stringToTime($('#endTime')));
    console.log(trueEndDate);
}

function compareBothTimeInputs(){
    let isProperRange = stringToTime($('#startTime')).getTime() <= stringToTime($('#endTime')).getTime();
    if(isProperRange){
        addValidity($('#startTime')[0]);
        addValidity($('#endTime')[0]);
    }else{
        removeValidity($('#startTime')[0]);
        removeValidity($('#endTime')[0]);
    }
}

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

function checkIfListInputExists(id, listID){
    let element = $("#"+ id);

    let obj = $("#"+listID).find("option[value='" + element.val() + "']");

    if(obj != null && obj.length > 0 || element.val() == "") {
        addValidity(element[0]);
    }else{
        removeValidity(element[0]);
    }
}

//update time validation when the date is changed while the time was already enterd
/*
$('#startDate #endDate').on('input change', function(event) {
    console.log(this.id);
    checkInputValidity(this.id);

});*/
