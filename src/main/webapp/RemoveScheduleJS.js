$(function () {
    setTestsCheckboxs();
});

function setTestsCheckboxs() {
    var scheduleName = $('#schedule').find(":selected").text();
    $("[type=checkbox").prop("checked", false);

    if (scheduleName !== null && scheduleName !== "") {
        it.doFileExist(scheduleName, function (k) {
            console.log(k.responseObject());
            var fileexist = k.responseObject();
            if (fileexist) {
                it.doGetParametersFromFile(scheduleName, function (t) {
                    console.log(t.responseObject());
                    title.innerText = "This schedule has a uniqe parameters :"
                    $(t.responseObject()).each(function (index) {
                        console.log(index + ": " + this);
                        $("[name=" + this).prop("checked", true);
                    });
                });
            } else {
                it.doGetDefaultParametersInList(scheduleName, function (t) {
                    console.log(t.responseObject());
                    title.innerText = "This schedule running on the default parameters :"
                    $(t.responseObject()).each(function (index) {
                        console.log(index + ": " + this);
                        $("[name=" + this).prop("checked", true);
                    });
                });
            }
        });
    }
}
//fileExist(String schduleName)
//doGetParametersFromFile<F5>
//doGetDefaultParametersInList

function validateForm() {
    var x = $('#schedule').find(":selected").text();
    if (x == null || x == "") {
        alert("Please choose a schedule to delete ");
        return false;
    }
}
