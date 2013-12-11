/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.projectConfiguration;

import antlr.ANTLRException;
import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.security.Permission;
import hudson.triggers.TimerTrigger;
import hudson.util.ListBoxModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 *
 * @author gabi
 * TODO add side-panel.jelly to the side
 */
public class ProjectConfiguration implements Action, Describable<ProjectConfiguration>{
    
    private final AbstractProject<?, ?> project;
    private MkverConf mkverConf;
    
    public ProjectConfiguration() 
    {
        project = null;
    }
    
    public ProjectConfiguration(AbstractProject<?, ?> project) 
    {
    	this.project = project;
        mkverConf = new MkverConf("/home/builder/BuildSystem/cc-views/builder_" + project.getName() + "_int/vobs/linux/CI_Conf/mkver.conf");
    }
    
    public String getJobName()
    {
        return project.getName();
    }
    
    @Override
    public Descriptor<ProjectConfiguration> getDescriptor() 
    {
        return Jenkins.getInstance().getDescriptorOrDie(getClass());
    }
    
    public ArrayList<String> getActiveSchedules()
    {
        ArrayList<Scheduled> schedules;
        ArrayList<String> schedulesDescription = new ArrayList<String>();
        TimerTrigger timerTrigger = project.getTrigger(TimerTrigger.class);
        try
        {
            schedules = parseSpec(timerTrigger.getSpec());
            Iterator<Scheduled> it = schedules.iterator();
            while (it.hasNext())
            {
                schedulesDescription.add(it.next().toString());
            }
        }catch(InvalidInputException ex){
                Logger.getLogger(ProjectConfiguration.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Error while parsing spec file");
        }
        
        
        return schedulesDescription;
    }

    public DescriptorExtensionList<Scheduled,Descriptor<Scheduled>> getScheduleDescriptors() {
        return Jenkins.getInstance().<Scheduled,Descriptor<Scheduled>>getDescriptorList(Scheduled.class);
    }
   
    public static ExtensionList<ProjectConfiguration> all()
    {
        return Jenkins.getInstance().getExtensionList(ProjectConfiguration.class);
    }
    
    @Extension
    public static final class DescriptorImpl extends Descriptor<ProjectConfiguration> 
    {
    
        public ListBoxModel doFillSchedulesItems() {
            System.out.println("in doFillSchedulesItems");
            ListBoxModel m = new ListBoxModel();
            m.add("Yellow Submarine","1");
            m.add("Abbey Road","2");
            m.add("Let It Be","3");
            return m;
        }

        @Override
        public String getDisplayName() {
            return clazz.getSimpleName();
        }
    }

    @Override
    public String getIconFileName() {
        if (CheckBuildPermissions() == true){
            return "/plugin/projectConfiguration/configuration_icon.jpg";
        }
        else{
            return null;
        }
    }

    @Override
    public String getDisplayName() {
        if (CheckBuildPermissions() == true){
            return "Configuration";
        }
        else{
            return null;
        }
    }

    @Override
    public String getUrlName() {
        if (CheckBuildPermissions() == true){
            return "projectConfiguration";
        }
        else{
            return null;
        }
    }
    
    // Set a new Corntab entry using TimerTrigger class
    public void doSubmit(StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException 
    {
        System.out.println(req.getParameter("checkboxConfFile"));
        System.out.println(req.getParameter("checkboxSave"));
        System.out.println(req.getParameter("removeSchdule"));
        if (req.getParameter("checkboxConfFile") != null)
        {
            mkverConf.checkIfFileIsUptodateAndUpdate(req.getParameter("projectName"), 
                    req.getParameter("streamName"), req.getParameter("mailingList"),
                    req.getParameter("mailingListTesting"), req.getParameter("logsPath"),
                    req.getParameter("rpmPath"), req.getParameter("imagePath"),
                    req.getParameter("ccActivity"), req.getParameter("kwProjectName"),
                    req.getParameter("kwDirPath"), req.getParameter("idcVersionFilePath"),
                    req.getParameter("productName"), req.getParameter("buildDirPath"));
        }
        if (req.getParameter("checkboxSave") != null)
        {
            try 
            {
                String prevSpec;
                String newSpec;
                Scheduled schedule = generateScheduleClassFromRequest(req);

                //Read previous Cron and add current to it end

                TimerTrigger timerTrigger = project.getTrigger(TimerTrigger.class);

                if (timerTrigger != null)
                {
                    System.out.println(timerTrigger.getSpec());
                    prevSpec = timerTrigger.getSpec();
                    newSpec = prevSpec + "\n" + schedule.getSpec();
                    timerTrigger.stop();
                    project.removeTrigger(timerTrigger.getDescriptor());
                }
                else
                {
                    System.out.println("No TimerTrigger available, creating one");
                    newSpec = schedule.getSpec();
                }

                System.out.println(newSpec);
                timerTrigger = new TimerTrigger(newSpec);
                timerTrigger.start(project, true);
                project.addTrigger(timerTrigger);

            } catch (InvalidInputException | ANTLRException ex) {
                Logger.getLogger(ProjectConfiguration.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (req.getParameter("checkboxRemove") != null)
        {
            System.out.println("in doSubmit");
        }
        //Find a better way to redirect the response so it won't be hard coded.
        rsp.sendRedirect2(req.getRootPath() + "/job/" + project.getName() + "/projectConfiguration");
    }
    
    public void doRemoveSchedule(StaplerRequest req, StaplerResponse rsp)    
    {
        String scheduleToRemove;
        try {
            scheduleToRemove = req.bindJSON(String.class, req.getSubmittedForm().getJSONObject("existingSchedules"));
            System.out.println(scheduleToRemove);
        } catch (ServletException ex) {
            Logger.getLogger(ProjectConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private boolean CheckBuildPermissions(){
        for ( Permission permission : Permission.getAll())
        {
            if (permission.name.equals("Build") == true)
            {
                if (Jenkins.getInstance().hasPermission(permission) == true)
                {
                    return true;
                }
            }
        }
        return false;
    }
    
    private Scheduled generateScheduleClassFromRequest(StaplerRequest req) throws InvalidInputException 
    {
        String name = req.getParameter("scheduleName");
        String hour = req.getParameter("ScheduleTime-hour");
        String minute = req.getParameter("ScheduleTime-minute");
        String meridiem = req.getParameter("ScheduleTime-meridiem");
        String type = req.getParameter("type");
        ArrayList<String> daysOfWeek = new ArrayList<>();
        String dateOfMonth;
        
        System.out.println("after getParams:");
        System.out.println("hour: " + hour);
        System.out.println("minute: " + minute);
        System.out.println("meridiem: " + meridiem);
        System.out.println("type: " + type);
        
        Scheduled schedule;
        
        switch (type) {
            case "Daily":
                schedule = new Scheduled(minute, hour, meridiem, name);
                break;
            case "Weekly":
                if (req.getParameter("Sunday") != null)
                    daysOfWeek.add("Sunday");
                if (req.getParameter("Monday") != null)
                    daysOfWeek.add("Monday");
                if (req.getParameter("Tuesday") != null)
                    daysOfWeek.add("Tuesday");
                if (req.getParameter("Wednesday") != null)
                    daysOfWeek.add("Wednesday");
                if (req.getParameter("Thursday") != null)
                    daysOfWeek.add("Thursday");
                if (req.getParameter("Friday") != null)
                    daysOfWeek.add("Friday");
                if (req.getParameter("Saturday") != null)
                    daysOfWeek.add("Saturday");
                schedule = new WeeklyScheduled(minute, hour, meridiem, daysOfWeek, name);
                break;
            case "Monthly":
                dateOfMonth = req.getParameter("DateOfMonth");
                System.out.println("dateOfMonth: " + dateOfMonth);
                schedule = new MonthlyScheduled(minute, hour, meridiem, dateOfMonth, name);
                break;
            default: 
                throw new InvalidInputException(type + " is an unrecognized schedule type, must be Daily, Weekly or Monthly");
        }
        
        return schedule;
    }
    
    public String getSchdules()
    {
        TimerTrigger timerTrigger = project.getTrigger(TimerTrigger.class);
        if(timerTrigger == null)
        {
            return "";
        }
        String spec = timerTrigger.getSpec();
        
        return spec;
    }
    
    private ArrayList<Scheduled> parseSpec(String spec) throws InvalidInputException
    {
        System.out.println(spec);
        String[] specArr = spec.split("\n");
        ArrayList<Scheduled> schedules = new ArrayList<Scheduled>();
        
        for (int i = 0; i < specArr.length; i+=2)
        {
            if (specArr[i].startsWith("#"))
            {
                String description = specArr[i];                                //description init
                String[] cronEntry = specArr[i+1].split(" ");
                
                // Initializing Schduled parameters
                String minute = cronEntry[0];
                StringBuilder hour = new StringBuilder();
                StringBuilder  meridiem = new StringBuilder();                  //minute init
                Scheduled.getHourAndMerdidiem(cronEntry[1], hour, meridiem);    //hour & meridiem init
                
                
                System.out.println("minute: " + minute);
                System.out.println("hour: " + hour.toString());
                System.out.println("meridiem: " + meridiem.toString());
                System.out.println("description: " + description);
                
                if(cronEntry[4].equals("*") == false)
                {
                    String[] daysArr = cronEntry[4].split(",");                 //days init                    

                    schedules.add(new WeeklyScheduled(minute, hour.toString(), meridiem.toString(), daysArr, description));
                }
                else if(cronEntry[2].equals("*") == false)
                {
                    String dayOfMonth = cronEntry[2];
                    
                    System.out.println("day: " + dayOfMonth);
                    
                    schedules.add(new MonthlyScheduled(minute, hour.toString(), meridiem.toString(), dayOfMonth, description));
                }
                else
                {
                    schedules.add(new Scheduled(minute, hour.toString(), meridiem.toString(), description));
                }
            }
        }
        return schedules;
    }
    
    // get functions for index.jelly to display
    public String getProjectName()
    {        
        mkverConf.createObjectFromFile();
        return mkverConf.getProjectName();
    }
    
    public String getProductName()
    {        
        return mkverConf.getProductName();
    }
    
    public String getStreamName()
    {
        return mkverConf.getStreamName();
    }
    
    public String getCCActivity()
    {
        return mkverConf.getCCActivity();
    }
    
    public String getLogsPath()
    {
        return mkverConf.getLogsPath();
    }
    
    public String getRPMPath()
    {
        return mkverConf.getRPMPath();
    }
    
    public String getImagePath()
    {
        return mkverConf.getImagePath();
    }
    
    public String getMailingList()
    {        
        return mkverConf.getMailingList();
    }
    
    public String getMailingListTesting()
    {        
        return mkverConf.getMailingListTesting();
    }
    
    public String getKWProjectName()
    {
        return mkverConf.getKWProjectName();
    }
    
    public String getKWDirPath()
    {
        return mkverConf.getKWDirPath();
    }
    
    public String getIDCVersionFilePath()
    {
        return mkverConf.getIDCVersionFilePath();
    }
    
    public String getBuildDirPath()
    {
        return mkverConf.getBuildDirPath();
    }
}
