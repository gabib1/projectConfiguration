/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.projectConfiguration;

import org.jenkinsci.plugins.projectConfiguration.devices.DeviceManager;
import org.jenkinsci.plugins.projectConfiguration.exceptions.InvalidInputException;
import antlr.ANTLRException;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.security.Permission;
import hudson.triggers.TimerTrigger;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.xml.bind.JAXBException;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.projectConfiguration.devices.Unit;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.bind.JavaScriptMethod;

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
    
    public static String ProfilesDBPath;
    private final AbstractProject<?, ?> project;
    private MkverConf mkverConf;
    private CriteriaProperty criteriaProperty;
    private DeviceManager deviceManager;
    private int deviceID;       // This parameter is used when Edit slots button is pressed inorder to edit the chosen device slots
    private int chosenSlotID = 1;       // This slot ID is to remember the user's choice in the slots page
    
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
        try {
            deviceManager = new DeviceManager(confFileDir + "/Devices");
        } catch (JAXBException ex) {
            System.out.println("Failed to init Device Manager, validate XMLs are written correctly");
            Logger.getLogger(ProjectConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        }
        ProjectConfiguration.ProfilesDBPath = "/home/" + username + "/profilesDB";
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
    
    public ArrayList<Unit> getDevices()
    {
        if (deviceManager != null)
        {
            try {
                return this.deviceManager.getDeviceArr();
            } catch (JAXBException ex) {
                System.err.println("Failed to parse XMLs of " + this.project.getName());
                Logger.getLogger(ProjectConfiguration.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ProjectConfiguration.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
    
    public ArrayList<String> getAvailableProfiles()
    {
        ArrayList<String> profiles = new ArrayList<String>();
        File profileDBDir = new File(ProjectConfiguration.ProfilesDBPath);
        if (profileDBDir.isDirectory() == true)
        {
            File[] profileFiles = profileDBDir.listFiles(new ProfilesFileFilter());
            for (File file : profileFiles)
            {
                profiles.add(file.getName().replaceAll(".profile", ""));
            }
        }
        else
        {
            System.out.println("[ERROR] Profiles DB doesn't exist under " + profileDBDir.getAbsolutePath());
        }

        return profiles;
    }
    
    public ArrayList<String> getChosenProfiles()
    {
        ArrayList<String> profiles = new ArrayList<String>();
        File profilesFile = new File(this.confFileDir + "/tests_profiles.txt");
        if (profilesFile.exists() == true)
        {
            InputStream fis;
            BufferedReader br;
            String line;

            try 
            {
                fis = new FileInputStream(profilesFile);
                br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
                while ((line = br.readLine()) != null) 
                {
                    profiles.add(line.replace(".profile", ""));
                }
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(ProjectConfiguration.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return profiles;
    }
    
    public ArrayList<String> getDevicesFieldsNames()
    {
        if (deviceManager != null)
        {
            return DeviceManager.getFieldsNames();
        }
        return null;
    }

    @Override
    public String getIconFileName() {
        if (CheckBuildPermissions() == true){
            return "/plugin/projectConfiguration/configuration_icon.png";
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
        
        System.out.println("testsCriteria=" + testsCriteria);
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
            }
            else
            {
                criteriaProperty.setKWSevirity(CriteriaProperty.KWSeverityEnum.ERROR);
                criteriaProperty.setKWCriteria(false);
            }
            if (testsCriteria != null)
            {
                criteriaProperty.setTestsCriteria(true);
            }
            else
            {
                criteriaProperty.setTestsCriteria(false);
            }
            criteriaProperty.saveToFile();
        }
        catch(InvalidInputException ex){
            System.out.println(ex.getMessage());
        }
        
        rsp.sendRedirect2(req.getReferer());
    }
    
    // Add new device to current project's device folder (currently unit xml file that fits ATT format)
    public void doAddDevice(StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException 
    {
        Iterator keySetIt = req.getParameterMap().keySet().iterator();
        Object key;
        while (keySetIt.hasNext())
        {
            key = keySetIt.next();
            if (req.getParameterValues(key.toString()).length > 1){
                System.out.println("key: " + key.toString() + ", value: ");
                for (String parameterValue : req.getParameterValues(key.toString())) {
                    System.out.println(parameterValue);
                }
            }
            else{
                System.out.println("key: " + key.toString() + ", value: " + req.getParameter(key.toString()));
            }
        }
        try {
            System.out.println("Adding new device");
            deviceManager.addNewDevice(req.getParameterMap());
        } catch (JAXBException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(ProjectConfiguration.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Failed to add device to project " + this.getJobName());
        }
        
        rsp.sendRedirect2(req.getRootPath() + "/job/" + project.getName() + "/projectConfiguration/devices");
    }
    
    public void doRemoveDevice(StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException 
    {
        int deviceIndex = -1;
        try
        {
            deviceIndex = Integer.parseInt(req.getParameter("deviceID"));
            this.deviceManager.removeDevice(deviceIndex);
        }catch(NumberFormatException ex){
            System.out.println("Failed to parse deviceID, deviceID = " + req.getParameter("deviceID"));
            Logger.getLogger(ProjectConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        }catch(IndexOutOfBoundsException ex){
            System.out.println("Failed to remove device at index " + deviceIndex + 
                    " because index is out of bounds, number of devices is " + this.deviceManager.getNumberOfDevices());
            Logger.getLogger(ProjectConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        rsp.sendRedirect2(req.getRootPath() + "/job/" + project.getName() + "/projectConfiguration/devices");
    }
    
    public void doSlotsRedirect(StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException 
    {
        String slotFormSubmitButton = req.getParameter("slots-edit-button");
        String deviceIdStr = req.getParameter("device-id");
        System.out.println("device-id-string=" + deviceIdStr);
        System.out.println("slots-edit-button=" + slotFormSubmitButton);
        if (slotFormSubmitButton != null && slotFormSubmitButton.equals("") != true)
        {
            try{
                this.deviceID = Integer.parseInt(deviceIdStr);
                rsp.sendRedirect2(req.getRootPath() + "/job/" + project.getName() + "/projectConfiguration/slots");
            }catch(NumberFormatException ex){
                System.out.println("DeviceIdStr couldn't be parsed, value: " + deviceIdStr);
                Logger.getLogger(ProjectConfiguration.class.getName()).log(Level.SEVERE, null, ex);
                rsp.sendRedirect2(req.getRootPath() + "/job/" + project.getName() + "/projectConfiguration/devices");
            }
        }
        else
        {
            rsp.sendRedirect2(req.getRootPath() + "/job/" + project.getName() + "/projectConfiguration/devices");
        }
    }
    
    public void doSaveSlotInfo(StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException, JAXBException 
    {
        String saveSlotInfoButton = req.getParameter("save-slot-info");
        String removeSlotButton = req.getParameter("remove-slot");
        String slotIdStr = req.getParameter("slot-id");
        String deviceIDStr = req.getParameter("deviceID");
        String cardName = req.getParameter("card-name");
        String[] linkArr = new String[4];
        for (int i = 0; i < 4; i++)
        {
            String linkContent = req.getParameter("link-" + (i+1) + "-name");
            if (StringUtils.isBlank(linkContent) == false)
            {
                linkArr[i] = linkContent;
            }
        }
        
        System.out.println("slot-id-string=" + slotIdStr);
        System.out.println("deviceID-string=" + deviceIDStr);
        System.out.println("cardName=" + cardName);
        try{
            int slotID = Integer.parseInt(slotIdStr);
            this.chosenSlotID = slotID;
            int deviceID = Integer.parseInt(deviceIDStr);
            if (saveSlotInfoButton != null)
            {
                this.deviceManager.addSlot(deviceID, slotID, cardName, linkArr);
            }
            else if (removeSlotButton != null)
            {
                this.deviceManager.removeSlot(deviceID, slotID);
            }
        }catch(NumberFormatException ex){
            System.out.println("deviceIDStr or slotIdStr couldn't be parsed, slotIdStr=" + slotIdStr + ", deviceIDStr=" + deviceIDStr);
            Logger.getLogger(ProjectConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        }
        rsp.sendRedirect2(req.getRootPath() + "/job/" + project.getName() + "/projectConfiguration/slots");
    }
    
    @JavaScriptMethod
    public String doGetChosenSlotId()
    {
        String chosenSlotIDStr = String.valueOf(this.chosenSlotID);
//        this.chosenSlotID = 1;
        System.out.println("chosenSlotIDStr=" + chosenSlotIDStr);
        return chosenSlotIDStr;
    }
    
    @JavaScriptMethod
    public String doGetSlotCardName(String slotId)
    {
        try 
        {
            return this.deviceManager.getSlotCardName(deviceID, Integer.parseInt(slotId));
        }catch  (NumberFormatException ex){
            System.out.println("Failed to parse slodId: " + slotId);
            Logger.getLogger(ProjectConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
    
    @JavaScriptMethod
    public String doGetSlotLinkName(String slotId, String linkId)
    {
        try 
        {
            return this.deviceManager.getSlotLinkName(deviceID, Integer.parseInt(slotId), Integer.parseInt(linkId));
        }catch  (NumberFormatException ex){
            System.out.println("Failed to parse slodId(" + slotId + ") or linkId(" + linkId + ")");
            Logger.getLogger(ProjectConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
    
    public void doTestsProfiles(StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException 
    {
        System.out.println("In doTestsProfile");
        String[] profiles = req.getParameterValues("chosen-profiles-list");
        File profilesFile = new File(this.confFileDir + "/tests_profiles.txt");
        PrintWriter pw = new PrintWriter(profilesFile);
        
        if (profiles != null)
        {
            for (String profile : profiles) 
            {
                System.out.println("profile: " + profile);
                if (new File(ProjectConfiguration.ProfilesDBPath + "/" + profile + ".profile").exists() == true)
                {
                    pw.println(profile + ".profile");
                }
//                tests.addAll(Files.readAllLines(Paths.get(ProjectConfiguration.ProfilesDBPath + "/" + profile + ".profile"), Charset.defaultCharset()));
            }
            
            pw.close();
        }

        rsp.sendRedirect2(req.getReferer());
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
    
    public String getDeviceIP() throws JAXBException
    {
        try{
            return this.deviceManager.getDeviceArr().get(this.deviceID).getTelnetUnitIP();
        } catch (IOException ex) {
            Logger.getLogger(ProjectConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
    
    public int getDeviceID()
    {
        return this.deviceID;
    }
    
    public boolean isKWCriteriaSet()
    {
        this.criteriaProperty.initFromFile();
        if (this.criteriaProperty.getKWCriteria() == true)
        {
            System.out.println("KW criteria is set");
            return true;
        }
        System.out.println("KW criteria is not set");
        return false;
    }
    
    public boolean isTestsCriteriaSet()
    {
        this.criteriaProperty.initFromFile();
        if (this.criteriaProperty.getTestsCriteria() == true)
        {
            return true;
        }
        return false;
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

    private class ProfilesFileFilter implements FileFilter 
    {
        @Override
        public boolean accept(File pathname) {
            return pathname.getName().endsWith(".profile");
        }
    }
}
