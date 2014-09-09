$(function() {
    setTestsCheckboxs();
});

function setTestsCheckboxs()
{
	var scheduleName  = $('#schedule').find(":selected").text();
	$("[type=checkbox").prop("checked", false);

        if (scheduleName !== null && scheduleName !== "")
        {
        it.doGetParametersFromFile(scheduleName, function(t) {
            console.log(t.responseObject());
           $(t.responseObject()).each(function(index) {
                console.log( index + ": " + this );
                $("[name=" + this).prop("checked", true);
            });
        }); 
    }
}

function validateForm() 
{
     var x  = $('#schedule').find(":selected").text();
    if (x==null || x=="") {
        alert("Please choose a schedule to delete ");
        return false;
    }
}


