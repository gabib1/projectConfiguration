/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.projectConfiguration;

import org.jenkinsci.plugins.projectConfiguration.exceptions.InvalidInputException;
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
 * @author gabi
 * TODO add side-panel.jelly to the side
 * Consider: The classes(Scheduled, WeeklySch...) were created to try and make the work with
 * the cron spec easier but it seems that working with the cron is pretty simple, so the object
 * oriented approach might have been unnecessary, check if maybe we should declare an
 * ArrayList<Schedule> and each time we add or remove from it we would overwrite the existing spec.
 */
public class ProjectConfiguration implements Action{//, Describable<ProjectConfiguration>{
    
    private String confFileDir;
    
    private final AbstractProject<?, ?> project;
    private MkverConf mkverConf;
    private CriteriaProperty criteriaProperty;
    
    public ProjectConfiguration() 
    {
        project = null;
    }
    
    public ProjectConfiguration(AbstractProject<?, ?> project) 
    {
    	this.project = project;
        String username = System.getProperty("user.name");
        confFileDir = "/home/" + username + "/BuildSystem/cc-views/" + username + "_" + project.getName() + "_int/vobs/linux/CI_Conf";
        // the _int is hard coded although it may be anything, but because we currently don't
        // have our own project we can't store stuff in the freestyle one, this will should be changed
        // as soon as we create our own project type
        mkverConf = new MkverConf(confFileDir + "/mkver.conf");
        criteriaProperty = new CriteriaProperty(confFileDir + "/criteria.conf");
    }
    
    public String getJobName()
    {
        return project.getName();
    }
    
    public ArrayList<String> getActiveSchedules()
    {
        ArrayList<Scheduled> schedules;
        ArrayList<String> schedulesDescription = new ArrayList<String>();
        TimerTrigger timerTrigger = project.getTrigger(TimerTrigger.class);
        if (timerTrigger != null)
        {
            try
            {
                schedules = parseSpec(timerTrigger.getSpec());
                Iterator<Scheduled> it = schedules.iterator();
                while (it.hasNext())
                {
                    String currentScheduleDescription = it.next().toString();
                    schedulesDescription.add(currentScheduleDescription);
                }
            }catch(InvalidInputException ex){
                    Logger.getLogger(ProjectConfiguration.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("Error while parsing spec file");
            }
        }
        
        return schedulesDescription;
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
    public void doSubmitConfFile(StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException 
    {
        mkverConf.checkIfFileIsUptodateAndUpdate(req.getParameter("projectName"), 
                req.getParameter("streamName"), req.getParameter("mailingList"),
                req.getParameter("mailingListTesting"), req.getParameter("logsPath"),
                req.getParameter("rpmPath"), req.getParameter("imagePath"),
                req.getParameter("ccActivity"), req.getParameter("kwProjectName"),
                req.getParameter("kwDirPath"), req.getParameter("idcVersionFilePath"),
                req.getParameter("productName"), req.getParameter("buildDirPath"));
        
        //Find a better way to redirect the response so it won't be hard coded.
        rsp.sendRedirect2(req.getRootPath() + "/job/" + project.getName() + "/projectConfiguration");
    }
    
    // Set a new Corntab entry using TimerTrigger class
    public void doSubmitSchedule(StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException 
    {
        try
        {
            Scheduled schedule = generateScheduleClassFromRequest(req);

            addEntryToSpec(schedule.getSpec());

        } catch(InvalidInputException ex){
            Logger.getLogger(ProjectConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //Find a better way to redirect the response so it won't be hard coded.
        rsp.sendRedirect2(req.getRootPath() + "/job/" + project.getName() + "/projectConfiguration");
    }
    
    public void doRemoveSchedule(StaplerRequest req, StaplerResponse rsp) throws IOException    
    {
        // the schedules as they appear to the user are in toString() format which is more
        // informative but not the same as it appears in the cron spec, we will extract the
        // schedule description (which is the content that appears until the ":") and search for
        // objects with the same description
        String userChoice = req.getParameter("existingSchedules");
        int indexOfColon = userChoice.indexOf(':');
        String scheduleName = userChoice.substring(0, indexOfColon);
        
        try 
        {
            removeEntryFromSpec(scheduleName);
        } catch (InvalidInputException ex) {
            Logger.getLogger(ProjectConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //Find a better way to redirect the response so it won't be hard coded.
        rsp.sendRedirect2(req.getRootPath() + "/job/" + project.getName() + "/projectConfiguration");
    }
    
    /**
     *
     * @param req
     * @param rsp
     * @throws IOException
     */
    public void doFailCriteria(StaplerRequest req, StaplerResponse rsp) throws IOException    
    {
        String kwCriteria = req.getParameter("Klocwork");
        String testsCriteria = req.getParameter("Tests");
        String kwCriticalOption = req.getParameter("kw-critical");
        String kwErrorOption = req.getParameter("kw-error");
        String kwAnyOption = req.getParameter("kw-any");
        
        System.out.println("kwCriteria=" + kwCriteria);
        System.out.println("kwCriticalOption=" + kwCriticalOption);
        System.out.println("kwErrorOption=" + kwErrorOption);
        System.out.println("kwAnyOption=" + kwAnyOption);
        
        try
        {
            if (kwCriteria != null)
            {
                if (kwAnyOption != null)
                {
                    criteriaProperty.setKWSevirity(CriteriaProperty.KWSeverityEnum.ANY);
                }
                else if (kwErrorOption != null)
                {
                    criteriaProperty.setKWSevirity(CriteriaProperty.KWSeverityEnum.ERROR);
                }
                else if (kwCriticalOption != null)
                {
                    criteriaProperty.setKWSevirity(CriteriaProperty.KWSeverityEnum.CRITICAL);
                }
                else
                {
                    throw new InvalidInputException("Non of the kwCriteria has been checked");
                }
                
                criteriaProperty.setKWCriteria(true);
                this.project.removeProperty(CriteriaProperty.class);
                this.project.addProperty(criteriaProperty);
                criteriaProperty.saveToFile();
            }
            else
            {
                this.project.removeProperty(CriteriaProperty.class);
                criteriaProperty.setKWSevirity(CriteriaProperty.KWSeverityEnum.ERROR);
                criteriaProperty.setKWCriteria(false);
                criteriaProperty.saveToFile();
            }
            if (testsCriteria != null)
            {
                System.out.println("Option not supported yet");
            }
            
        }
        catch(InvalidInputException ex){
            System.out.println(ex.getMessage());
        }
        
        //Find a better way to redirect the response so it won't be hard coded.
        rsp.sendRedirect2(req.getRootPath() + "/job/" + project.getName() + "/projectConfiguration");
    }
    
    /**
     *
     * @param newEntry should be in the following format: 
     * #Name\n* * * * *
     * "*" might be replaced by the desired value
     */
    private void addEntryToSpec(String newEntry)
    {
        String prevSpec, newSpec;
        
        TimerTrigger timerTrigger = project.getTrigger(TimerTrigger.class);

        if (timerTrigger != null && timerTrigger.getSpec().equals("") == false)
        {
            prevSpec = timerTrigger.getSpec();
            newSpec = prevSpec + "\n" + newEntry;
        }
        else
        {
            System.out.println("No previous TimerTrigger");
            newSpec = newEntry;
        }

        updateSpec(timerTrigger, newSpec);
    }
    
    private void removeEntryFromSpec(String name) throws InvalidInputException
    {
        String spec = "";
        String[] specArr;
        TimerTrigger timerTrigger = project.getTrigger(TimerTrigger.class);

        if (timerTrigger != null)
        {
            specArr = timerTrigger.getSpec().split("\n");
            
            for (int i = 0; i < specArr.length; i+=2)
            {
                // we extract the specArray back to spec except the description and the following
                // cron entry which coresponds with the input name
                if (specArr[i].startsWith("#" + name) == false)
                {
                    spec += specArr[i] + "\n" + specArr[i+1] + "\n";
                }
            }
        }
        else
        {
            throw new InvalidInputException("timerTrigger is empty can't remove " + name);
        }
        
        updateSpec(timerTrigger, spec);
    }
    
    private void updateSpec(TimerTrigger timerTrigger, String newSpec)
    {
        try 
        {
            if (timerTrigger != null)
            {
                timerTrigger.stop();
                project.removeTrigger(timerTrigger.getDescriptor());
            }

            timerTrigger = new TimerTrigger(newSpec);
            timerTrigger.start(project, true);
            project.addTrigger(timerTrigger);
        } catch (ANTLRException | IOException ex) {
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
             
                if(cronEntry[4].equals("*") == false)
                {
                    String[] daysArr = cronEntry[4].split(",");                 //days init                    

                    schedules.add(new WeeklyScheduled(minute, hour.toString(), meridiem.toString(), daysArr, description));
                }
                else if(cronEntry[2].equals("*") == false)
                {
                    String dayOfMonth = cronEntry[2];
                    
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
    
    
    
    
    
    public boolean isKWCriteriaSet()
    {
        this.criteriaProperty.initFromFile();
        return this.project.getProperty(CriteriaProperty.class) != null;
    }
    
    public boolean isKWCriticalSet()
    {
        return this.criteriaProperty.isKWCriticalSet();
    }
    
    public boolean isKWErrorSet()
    {
        return this.criteriaProperty.isKWErrorSet();
    }
    
    public boolean isKWAnySet()
    {
        return this.criteriaProperty.isKWAnySet();
    }
}
