$(function() {
    $.each($("#chosen-profiles-list > option"), function() {
        $("#available-profiles-list option[name='" + $(this).text() + "']").remove();
//        $('#available-profiles-list').find($(this).text()).remove();
    });
});

function addToChosen() {
    $.each($("#available-profiles-list").find(":selected"), function() {
        $('#chosen-profiles-list').append(this);
    });
    $("#available-profiles-list").find(":selected").remove();
}

function removeToChosen() {
    $.each($("#chosen-profiles-list").find(":selected"), function() {
        $('#available-profiles-list').append(this);
    });
    $("#chosen-profiles-list").find(":selected").remove();
}

function save()
{
    var selObj = document.getElementById("chosen-profiles-list");
    for (var i = 0; i < selObj.options.length; i++) {
        selObj.options[i].selected = true;
    }
    $("#tests-profiles-form").submit();
}

function cleanSelectBoxes()
{
    cleanSelectBoxe("chosen-profiles-list");
    cleanSelectBoxe("available-profiles-list");
}
function cleanSelectBoxe(boxId)
{
    var x = document.getElementById(boxId);
    while (x.length > 0)
    {
        x.remove(x.length - 1);
    }
}


function fillSelectBox(boxName, infoArray)
{
    var arrayLength = infoArray.length;
    for (var i = 0; i < arrayLength; i++) {
        console.log(infoArray[i]);
        $('#' + boxName).append('<option name=' + infoArray[i] + ' value=' + infoArray[i] + ' >' + infoArray[i] + '</option>');
    }
}
function duplicateFixer() {
    ($("#chosen-profiles-list > option"), function() {
        $("#available-profiles-list option[name='" + $(this).text() + "']").remove();
    });
    $("#chosen-profiles-list > option").each(function() {
        $("#available-profiles-list option[name='" + $(this).text() + "']").remove();
    });


}


function setProfiles() {
    cleanSelectBoxes();
    button.innerText = "";
    document.getElementById("button").style.background = '#FFFFFF';


    //get the value inside the ption bar named schedule
    var testType = $('#type').find(":selected").text();
    //chnage all the checkbox to false

    // making sure the given name is not empty
    if (testType !== null && testType !== "") {
        //goes to doFileExist in the server side, and the returned value will be the element k
        document.getElementById("button").style.background = '#CCCCCC';
        button.innerText = testType;

        it.doGetAvailableProfiles(testType, function(k) {
            fillSelectBox("available-profiles-list", k.responseObject());

            it.doGetChosenProfiles(testType, function(k) {
                console.log(k.responseObject());
                fillSelectBox("chosen-profiles-list", k.responseObject());
                $.each($("#chosen-profiles-list > option"), function() {
                    $("#available-profiles-list option[name='" + $(this).text() + "']").remove();
                });
            });
        });
    }
}

// validte that the option that was chosen is not empty 
function validateForm() {
    var x = $('#type').find(":selected").text();
    if (x == null || x == "") {
        alert("Please choose a profile first");
        return false;
    }
}


