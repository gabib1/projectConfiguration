
	// validte that the option that was chosen is not empty 
function validateForm() {
    var formValue = $(document.activeElement).val()
    console.log("form Value  : " + formValue);
    if (formValue == "save") {
        var x = $('input[name="projects"]:checked').val();
        if (x == null || x == "") {
            alert("Please choose a project first");
            return false;
        }
    } else {
        it.doGetNameDepenedency( function(k) {
            var returnedValue = k.responseObject();
            console.log("returnedValue  : " + returnedValue);
            if ("empty" == returnedValue) {
                alert("No dependency to remove");
                return false;
            } else {
                return false;
            }
        });
    }
}

function checkedCheck(projectName) {
    console.log("projectName:  " + projectName);
    it.doGetNameDepenedency( function(k) {
        var returnedValue = k.responseObject();
        console.log("returnedValue  : " + returnedValue);
        if (projectName == returnedValue) {
            return true;
        } else {
            return false;
        }
    });
}
			






	
