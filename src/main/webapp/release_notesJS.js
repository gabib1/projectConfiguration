$(function() {

    it.doGetReleaseNotesText("new_limitations", function(k) {
        //console.log(k.responseObject());
        var new_limitations = k.responseObject();
        console.log(new_limitations)
        $('#new_limitations').val(new_limitations);
    });

    it.doGetReleaseNotesText("solved_limitations", function(k) {
        //console.log(k.responseObject());
        var solved_limitations = k.responseObject();
        console.log(solved_limitations)
        $('#solved_limitations').val(solved_limitations);
    });

    it.doGetReleaseNotesText("additional_information", function(k) {
        //console.log(k.responseObject());
        var additional_information = k.responseObject();
        console.log(additional_information)
        $('#additional_information').val(additional_information);
    });

    $(document).tooltip();

});
