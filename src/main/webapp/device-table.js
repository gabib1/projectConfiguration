// The source code is taken from http://jqueryui.com/dialog/#modal-form

var orderedArrayId = [];

//$(function()... means this will be executed on document(DOM) ready state
$(function() {
//    var unitType = $("#unitType");
//    var watchFiles = [];
//    $("#watchFiles").each(function()
//    {
//        watchFiles.push($(this).val());
//    });
//    var telnetUnitIP = $("#telnetUnitIP");
//    var comOverTelnetIP = $("#comOverTelnetIP");
//    var comOverTelnetPort = $("#comOverTelnetPort");
//    var serialPort = $("#serialPort");
//    var rootPassword = $("#rootPassword");
//    var clishUser = $("#clishUser");
//    var swVersion = $("#swVersion");
//    var chassis = $("#chassis");
//    var developmentVersion = $("#developmentVersion");
//    var unitConfigFile = $("#unitConfigFile");
//    var allFields = $([]).add(unitType).add(watchFiles).add(password);
//    var tips = $(".validateTips");
//
//    function updateTips(t) {
//        tips.text(t).addClass("ui-state-highlight");
//        setTimeout(function() {
//            tips.removeClass("ui-state-highlight", 1500);
//        }, 500);
//    }
//
//    function checkLength(o, n, min, max) {
//        if (o.val().length > max || o.val().length < min) {
//            o.addClass("ui-state-error");
//            updateTips("Length of " + n + " must be between " + min + " and " + max + ".");
//            return false;
//        } else {
//            return true;
//        }
//    }
//
//    function checkRegexp(o, regexp, n) {
//        if (!(regexp.test(o.val()))) {
//            o.addClass("ui-state-error");
//            updateTips(n);
//            return false;
//        } else {
//            return true;
//        }
//    }

    $("#dialog-form-div").dialog({
        autoOpen: false,
//        height: 90%,
        width: 350,
        modal: true,
        buttons: {
            "Add new device": function() {
                var selObj = document.getElementById("watchFiles");
                for (var i=0; i<selObj.options.length; i++) {
                    selObj.options[i].selected = true;
                }
                $("#dialog-form").submit();
//                allFields.removeClass("ui-state-error");
//                
//                if (validateInput()) {
//                    $("#users tbody").append("<tr>" +
//                            "<td>" + name.val() + "</td>" +
//                            "<td>" + email.val() + "</td>" +
//                            "<td>" + password.val() + "</td>" +
//                            "</tr>");
//                    $(this).dialog("close");
//                }
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
    $("#dialog-form *[id]").each(function(){
        orderedArrayId.push(this.id);
    });

    $("#create-device").button().click(function() {
        $("#dialog-form-div").dialog("open");
    });
    
//    function validateInput(){
//        return checkLength(name, "username", 3, 16) && checkLength(email, "email", 6, 80) && checkLength(password, "password", 5, 16)
//               && checkRegexp(name, /^[a-z]([0-9a-z_])+$/i, "Username may consist of a-z, 0-9, underscores, begin with a letter.")
//               // From jquery.validate.js (by joern), contributed by Scott Gonzalez: http://projects.scottsplayground.com/email_address_validation/
//               && checkRegexp(email, /^((([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+(\.([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+)*)|((\x22)((((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(([\x01-\x08\x0b\x0c\x0e-\x1f\x7f]|\x21|[\x23-\x5b]|[\x5d-\x7e]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(\\([\x01-\x09\x0b\x0c\x0d-\x7f]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]))))*(((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(\x22)))@((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?$/i, "eg. ui@jquery.com")
//               && checkRegexp(password, /^([0-9a-zA-Z])+$/, "Password field only allow : a-z 0-9");
//
//    }
});

function addWatchFile(){
    $("#watchFiles").append("<option>" + $("#watchFileName").val() + "</option>");
    $("#watchFileName").val("");
}

function removeWatchFile(){
    var x = document.getElementById("watchFiles");
    x.remove(x.selectedIndex);
}

function removeDevice(index){
    if (confirm("You are about to remove device #" + index + " , are you sure?") === true)
    {
        $("#removeDeviceID").val(index);
        $("#remove-device-form").submit()
    }
}

function editDevice(deviceIndex){
//    $("#editDeviceID").val(deviceIndex);
    $.each(orderedArrayId, function (i, value){
        if (value === "watchFiles"){
            var values = $.map($("#watchFiles-" + deviceIndex +" option"), function(e) { return e.value; });
            $.each(values, function (i, value){
                $("#watchFiles").append("<option>" + value + "</option>");
            });
//            var x = document.getElementById("watchFile-");
//            var txt = "All options: ";
//            var i;
//            for (i = 0; i < x.length; i++) {
//                txt = txt + "\n" + x.options[i].value;
//            }
//            alert(txt);
        }
        else{
            $("#" + value).val($("#" + value + "-" + deviceIndex).html());
        }
    });
    
    $("#dialog-form-div").dialog("open");
}