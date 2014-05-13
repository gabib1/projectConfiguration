$(function() {
    getSelectedSlotId();
});

function getSelectedSlotId(){
    var id = 1;
    it.doGetChosenSlotId(function(t) {
        $("select option").filter(function() {
            //may want to use $.trim in here

            return $(this).text() == t.responseObject(); 
        }).prop('selected', true);
        id = t.responseObject();
        getSlotInfo(id)
    });
    return id;
}

function getSlotInfo(id)
{
    var slotID;
    if (id == null || id == ""){
        slotID = $('#slot-id').find(":selected").text();
    }
    else{
        slotID = id;
    }
    console.log(slotID);
    if (slotID !== null && slotID !== "")
    {
        it.doGetSlotCardName(slotID, function(t) {
            $("#card-name").val(t.responseObject());
        });
        it.doGetSlotLinkName(slotID, 1, function(t) {
            $("#link-1-name").val(t.responseObject());
        });
        it.doGetSlotLinkName(slotID, 2, function(t) {
            $("#link-2-name").val(t.responseObject());
        });
        it.doGetSlotLinkName(slotID, 3, function(t) {
            $("#link-3-name").val(t.responseObject());
        });
        it.doGetSlotLinkName(slotID, 4, function(t) {
            $("#link-4-name").val(t.responseObject());
        });
    }
}

