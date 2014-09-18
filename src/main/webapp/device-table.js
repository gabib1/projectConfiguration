// The source code is taken from http://jqueryui.com/dialog/#modal-form
var orderedArrayId = [];

//$(function()... means this will be executed on document(DOM) ready state
$(function() {
        $("#dialog-form-div").dialog({
        autoOpen: false,
        //        height: 90%,
        width: 350,
        modal: true,
        buttons: {
            "save": function() {
                var selObj = document.getElementById("watchFiles");
                for (var i = 0; i < selObj.options.length; i++) {
                    selObj.options[i].selected = true;
                }
                $("#dialog-form").submit();

            },
            Cancel: function() {

                $(this).dialog("close");
            }
        },
        close: function() {
            $("#deviceID").val("-1");
        }
    });

    orderedArrayId = [];
    $("#dialog-form *[id]").each(function() {
        orderedArrayId.push(this.id);
    });

    $("#create-device").button().click(function() {
        $("#dialog-form-div").dialog("open");
    });

   
});

function addWatchFile() {
    $("#watchFiles").append("<option>" + $("#watchFileName").val() + "</option>");
    $("#watchFileName").val("");
}

function removeWatchFile() {
    var x = document.getElementById("watchFiles");
    x.remove(x.selectedIndex);
}

function removeDevice(index) {
    if (confirm("You are about to remove device #" + index + " , are you sure?") === true) {
        $("#removeDeviceID").val(index);
        $("#remove-device-form").submit()
    }
}

function editDevice(deviceIndex) {

    //    $("#editDeviceID").val(deviceIndex);
    $.each(orderedArrayId, function(i, value) {
        if (value === "watchFiles") {
            var values = $.map($("#watchFiles-" + deviceIndex + " option"), function(e) {
                return e.value;
            });
            $.each(values, function(i, value) {
                $("#watchFiles").append("<option>" + value + "</option>");
            });
			
        } else {		
			//console.log($(value + "-" + deviceIndex));
			if (value === "deploy" || value === "test") {
				document.getElementById(value).checked = document.getElementById(value + "-" + deviceIndex).checked
				}else{
            $("#" + value).val($("#" + value + "-" + deviceIndex).html());
			}
        }
    });

    $("#dialog-form-div").dialog("open");
}

function onClickCheckBox(checkBoxName) {
	console.log(checkBoxName);	
    if (confirm("You are about to change the device configuration, Do you wish to proceed?") === true) {
		var checkBoxValue = document.getElementById(checkBoxName).checked
		console.log(checkBoxValue);
        it.doOnClickCheckBoxDeviceManager(checkBoxName,checkBoxValue);
		location.reload();
    }else{
		location.reload();
	}
			
}

$(function() {
    $("input:checkbox").each(function() {
        var name = $(this).prop("name");
        //console.log(name);
        it.doIsDeviceOnList(name, function(k) {
            //console.log(k.responseObject());
            var response = k.responseObject();
			          //  console.log(response);

            if (response == true) {
                $("[name=" + name).prop("checked", true);
            } else {
                $("[name=" + name).prop("checked", false);
            }
        });
    });
});

