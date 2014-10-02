
function formAlerts() {
    var formValue = $(document.activeElement).val();
    console.log("form Value  : " + formValue);
    if (formValue === "update view") {
        alert("Please be patient the update will take a minute");
		return true;
        }
	else if (formValue === "save") {
		return true;
        }
}



			






	
