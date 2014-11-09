$(function() {

   it.doGetReleaseNotesText("new_limitations", function(k) {
        console.log(k.responseObject());
        var new_limitations = k.responseObject();
    $('#new_limitations').val(new_limitations);
    });
    
       it.doGetReleaseNotesText("solved_limitations", function(k) {
        console.log(k.responseObject());
        var solved_limitations = k.responseObject();
    $('#solved_limitations').val(solved_limitations);
    });
});
