$(function() {
    setTestsCheckboxs();
});

//set the textbox in with the correct parameters for this scheduler.
//it can be defailt or uniqe saved parameters.
function setTestsCheckboxs() {
    //get the value inside the ption bar named schedule
    var scheduleName = $('#schedule').find(":selected").text();
    //chnage all the checkbox to false
    $("[type=checkbox").prop("checked", false);

    // making sure the given name is not empty
    if (scheduleName !== null && scheduleName !== "") {
        //goes to doFileExist in the server side, and the returned value will be the element k
        it.doFileExist(scheduleName, function(k) {
            console.log(k.responseObject());
            var fileexist = k.responseObject();

            // file exist = true if there is a parameters file with the same hour of the scheduler
            if (fileexist) {

                //doGetParametersFromFile reads the parameters from the found file and than changed the check boxes accordinly, other wise will change to the default paramters
                it.doGetParametersFromFile(scheduleName, function(t) {
                    console.log(t.responseObject());
                    title.innerText = "This schedule has a uniqe parameters :"
                    $(t.responseObject()).each(function(index) {
                        console.log(index + ": " + this);
                        $("[name=" + this).prop("checked", true);
                        // remove the onclick attrubute from the checkboxes
                   //     $("[type=checkbox").attr('onclick', '').unbind('click');
                    });
                });
            } else {
                it.doGetDefaultParametersInList(scheduleName, function(t) {
                    console.log(t.responseObject());
                    title.innerText = "This schedule running on the default parameters :"
                    $(t.responseObject()).each(function(index) {
                        console.log(index + ": " + this);
                        $("[name=" + this).prop("checked", true);
                        // add the attr onclock='return false'
                      //  $("[type=checkbox").attr('onclick', 'return false')
                    });
                });
            }
        });

        // check if the scheduler active or not and change the message to match the status
        // remark - relate to to button 
        it.doSchdulePaused(scheduleName, function(k) {
            console.log(k.responseObject());
            var schdulerPaused = k.responseObject();
            if (schdulerPaused) {
                button.innerText = "scheduler is not active";
                document.getElementById("button").style.background = '#FF0000';
                $('input:radio[name=active]')[1].checked = true;
            } else {
                button.innerText = "the scheduler is active";
                document.getElementById("button").style.background = '#00CC00';
                $('input:radio[name=active]')[0].checked = true;
            }
        });
    } else {
        console.log(button.innerText);
        if (button.innerText != "SCHEDULER STATUS")
        {
            location.reload();
        }
    }
}

// validte that the option that was chosen is not empty 
function validateForm() {
    var x = $('#schedule').find(":selected").text();
    if (x == null || x == "") {
        alert("Please choose a schedule first");
        return false;
    }
}


function test() {
    
    var scheduleName = $('#schedule').find(":selected").text();
    if (scheduleName !== null && scheduleName !== "") {
        //goes to doFileExist in the server side, and the returned value will be the element k
        it.doFileExist(scheduleName, function(k) {
            console.log(k.responseObject());
            var fileexist = k.responseObject();
            return 	fileexist;
        });
    }
}




