function validateForm() {
    
    var x = document.forms["form"]["scheduleName"].value;
    console.log(x);
    if (x == null || x == "") {
        alert("Name must be filled out");
        return false;
    }

    var hourElement = document.getElementById("ScheduleTime-hour");
    var hour = hourElement.options[hourElement.selectedIndex].text;

    var minuteElement = document.getElementById("ScheduleTime-minute");
    var minute = minuteElement.options[minuteElement.selectedIndex].text;

    var hourMeridiem = document.getElementById("ScheduleTime-meridiem");
    var meridiem = hourMeridiem.options[hourMeridiem.selectedIndex].text;

    if (meridiem === "PM")
    {
        hour = parseInt(hour) + 12;
    }

    var time = hour + "" + minute;

    it.doSchedulerTimeDuplicate(time, function(k) {
        console.log(k.responseObject());
        var fileexist = k.responseObject();

        // file exist = true if there is a parameters file with the same hour of the scheduler
        if (fileexist) {
            alert("Please change the Scheduler time, Scheduler with this time already exist!");
            return false;
        }
    });
}


