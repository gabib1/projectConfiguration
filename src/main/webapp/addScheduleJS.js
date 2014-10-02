function validateForm() {


    var x = document.forms["form"]["scheduleName"].value;
	console.log(x);
    if (x == null || x == "") {
        alert("Name must be filled out");
        return false;
    }
}
