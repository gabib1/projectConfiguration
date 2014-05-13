$(function() {
    $.each($("#chosen-profiles-list > option"), function(val, text) {
        $("#available-profiles-list option[name='" + $(this).text() + "']").remove();
//        $('#available-profiles-list').find($(this).text()).remove();
    });
});

function addToChosen(){
    $.each($("#available-profiles-list").find(":selected"), function(val, text) {
        $('#chosen-profiles-list').append(this);
    });
    $("#available-profiles-list").find(":selected").remove();
}

function removeToChosen(){
    $.each($("#chosen-profiles-list").find(":selected"), function(val, text) {
        $('#available-profiles-list').append(this);
    });
    $("#chosen-profiles-list").find(":selected").remove();
}

function save()
{
    var selObj = document.getElementById("chosen-profiles-list");
    for (var i=0; i<selObj.options.length; i++) {
        selObj.options[i].selected = true;
    }
    $("#tests-profiles-form").submit();
}