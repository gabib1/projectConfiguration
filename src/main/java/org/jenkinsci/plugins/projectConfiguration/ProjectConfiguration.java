/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.projectConfiguration;

import antlr.ANTLRException;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BooleanParameterDefinition;
import hudson.model.View;
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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.xml.bind.JAXBException;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.projectConfiguration.devices.DeviceManager;
import org.jenkinsci.plugins.projectConfiguration.devices.Unit;
import org.jenkinsci.plugins.projectConfiguration.exceptions.InvalidInputException;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.bind.JavaScriptMethod;

/**
 * @author gabi TODO add side-panel.jelly to the side Consider: The
 * classes(Scheduled, WeeklySch...) were created to try and make the work with
 * the cron spec easier but it seems that working with the cron is pretty
 * simple, so the object oriented approach might have been unnecessary, check if
 * maybe we should declare an ArrayList<Schedule> and each time we add or remove
 * from it we would overwrite the existing spec.
 */
public class ProjectConfiguration implements Action
{//, Describable<ProjectConfiguration>{

    private String confFileDir;

    public static String ProfilesDBPath;
    public final String PROJECTNAME;
    private final AbstractProject<?, ?> project;
    private MkverConf mkverConf;
    private CriteriaProperty criteriaProperty;
    private DeviceManager deviceManager;
    private int deviceID;       // This parameter is used when Edit slots button is pressed inorder to edit the chosen device slots
    private int chosenSlotID = 1;       // This slot ID is to remember the user's choice in the slots page
    private String username = System.getProperty("user.name");
    private String parameterFileDirPath;

    public ProjectConfiguration()
    {
        project = null;
        PROJECTNAME = null;
    }

    public ProjectConfiguration(AbstractProject<?, ?> project)
    {
        this.project = project;
        PROJECTNAME = project.getName();
        String username = System.getProperty("user.name");
        confFileDir = "/home/" + username + "/BuildSystem/cc-views/" + username + "_" + project.getName() + "_int/vobs/linux/CI_Conf";
        // the _int is hard coded although it may be anything, but because we currently don't
        // have our own project we can't store stuff in the freestyle one, this will should be changed
        // as soon as we create our own project type
        mkverConf = new MkverConf(confFileDir + "/mkver.conf");
        criteriaProperty = new CriteriaProperty(confFileDir + "/criteria.conf");
        parameterFileDirPath = "/home/" + username + "/BuildSystem/cc-views/"
                + username + "_" + project.getName()
                + "_int/vobs/linux/CI_Conf/schduledRunsParameters/";
        try
        {
            deviceManager = new DeviceManager(confFileDir + "/Devices");
        } catch (JAXBException ex)
        {
            System.out.println("Failed to init Device Manager, validate XMLs are written correctly");
            Logger.getLogger(ProjectConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        }
        ProjectConfiguration.ProfilesDBPath = "/home/" + username + "/profilesDB";
    }

    public String getJobName()
    {
        return project.getName();
    }

//  
    /**
     * checks if the given file in the list, file name will be "deploy-[number]"
     * or test - [number]" by parsing the number will know the file name, and by
     * the name will know if to check in the deploy or test folder
     *
     * @param checkboxName - the name as it defines in the html
     *
     * @return true if the file exist, false otherwise
     *
     * Author Oren
     */
    @JavaScriptMethod
    public boolean doIsDeviceOnList(String checkboxName)
    {
        String[] list = checkboxName.split("-");
        if (list.length == 0)
        {
            return false;
        }

        String deviceNumber = list[1];
        String fileName = "unit_" + deviceNumber + ".xml";

        //we need to check if it deplot or tet list
        boolean returnValue = false;
        try
        {

            if (checkboxName.startsWith("test-"))
            {
                returnValue = new DeviceManager(confFileDir + "/Devices").isDeviceOnTestList(fileName);
            }
            if (checkboxName.startsWith("deploy-"))
            {
                returnValue = new DeviceManager(confFileDir + "/Devices").isDeviceOnDeployList(fileName);
            }
        } catch (JAXBException ex)
        {
            Logger.getLogger(ProjectConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        }

        return returnValue;

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
        } else
        {
            System.out.println("[ERROR] Profiles DB doesn't exist under " + profileDBDir.getAbsolutePath());
        }

        return profiles;
    }

    public ArrayList<String> getActiveSchedules()
    {
        System.out.println("");
        System.out.println("");
        System.out.println("In getActiveSchedules");

        ArrayList<Scheduled> schedules;
        ArrayList<String> schedulesDescription = new ArrayList<String>();
        TimerTrigger timerTrigger = project.getTrigger(TimerTrigger.class);
        if (timerTrigger != null)
        {
            try
            {
                schedules = parseSpec(timerTrigger.getSpec());
                System.out.println("schedules:\n" + schedules);
                Iterator<Scheduled> it = schedules.iterator();
                while (it.hasNext())
                {
                    String currentScheduleDescription = it.next().toString();
                    System.out.println("currentScheduleDescription:\n" + currentScheduleDescription);
                    if (!currentScheduleDescription.isEmpty())
                    {
                        schedulesDescription.add(currentScheduleDescription);
                    }
                }
            } catch (InvalidInputException ex)
            {
                Logger.getLogger(ProjectConfiguration.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Error while parsing spec file");
            }
        }

        return schedulesDescription;
    }

    /**
     * return the active schedulers
     *
     * @return ArrayList - with all the active schedulers
     */
    public ArrayList<String> getActiveSchedulesParameters()
    {
        System.out.println("In getActiveSchedulessParameters");
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
                    System.out.println("currentScheduleDescriptio)" + currentScheduleDescription);
                    schedulesDescription.add(currentScheduleDescription);
                }
            } catch (InvalidInputException ex)
            {
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
            try
            {
                return this.deviceManager.getDeviceArr();
            } catch (JAXBException ex)
            {
                System.err.println("Failed to parse XMLs of " + this.project.getName());
                Logger.getLogger(ProjectConfiguration.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex)
            {
                Logger.getLogger(ProjectConfiguration.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public ArrayList<String> getParametersForSchudle()
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
        } else
        {
            System.out.println("[ERROR] Profiles DB doesn't exist under " + profileDBDir.getAbsolutePath());
        }

        return profiles;
    }

    /**
     *
     *
     * returns an array with the saved parameters in the file
     *
     * @param path
     * @return
     *
     * Author Oren
     */
    @JavaScriptMethod
    public ArrayList<String> doGetParametersFromFile(String schduleName)
    {
        // schdule name is for exmaple "daily : Daily at 00:00" 
        // will split it for getting the hour
        String fileName = UtilsClass.getTimeOfSchduleFromItsName(schduleName);
        System.out.println("fileName: " + fileName);

        ArrayList<String> temp = UtilsClass.getParametersFromFile(parameterFileDirPath + fileName);
        for (String parameter : temp)
        {
            System.out.println(parameter);
        }
        return temp;
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
    public String getIconFileName()
    {
        if (CheckBuildPermissions() == true)
        {
            return "/plugin/projectConfiguration/configuration_icon.png";
        } else
        {
            return null;
        }
    }

    @Override
    public String getDisplayName()
    {
        if (CheckBuildPermissions() == true)
        {
            return "Configuration";
        } else
        {
            return null;
        }
    }

    @Override
    public String getUrlName()
    {
        if (CheckBuildPermissions() == true)
        {
            return "projectConfiguration";
        } else
        {
            return null;
        }
    }

    // Set a new Corntab entry using TimerTrigger class
    public void doSubmitConfFile(StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException
    {
        System.out.println("IN doSubmitConfFile");
        String formOption = req.getParameter("formValue");
        System.out.println("formOption:   " + formOption);

        switch (formOption)
        {
            case "update view":
                System.out.println("UPDATE VIEW");
                mkverConf.updateView();
                System.out.println("UPDATE VIEW ENDED");
                break;

            case "save":
                System.out.println("SAVING CONFFILE");
                mkverConf.checkIfFileIsUptodateAndUpdate(req.getParameter("projectName"),
                        req.getParameter("streamName"), req.getParameter("mailingList"),
                        req.getParameter("mailingListTesting"), req.getParameter("logsPath"),
                        req.getParameter("rpmPath"), req.getParameter("imagePath"),
                        req.getParameter("ccActivity"), req.getParameter("kwProjectName"),
                        req.getParameter("kwDirPath"), req.getParameter("idcVersionFilePath"),
                        req.getParameter("productName"), req.getParameter("buildDirPath"));

                System.out.println("SAVING CONFFILE ENDED");

            //Find a better way to redirect the response so it won't be hard coded.
            }
        rsp.sendRedirect2(req.getRootPath() + "/job/" + project.getName() + "/projectConfiguration");

        // Set a new defualts param base on the request from the server
    }

    public void doSubmitParam(StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException
    {
        DefaultParameter defaultParameter = new DefaultParameter(this.project);
        defaultParameter.editDefaultParamsValues(req);
        rsp.sendRedirect2(req.getRootPath() + "/job/" + project.getName() + "/projectConfiguration/defaultParam");
    }

    /**
     * TODO - Remarkes
     *
     * @param req
     * @param rsp
     * @throws ServletException
     * @throws IOException
     */
    // Set a new Corntab entry using TimerTrigger class
    public void doSubmitSchedule(StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException
    {
        try
        {

            // we need to check if to create parameters or not
            String isParametrized = req.getParameter("parameters");
            System.out.println("isParametrized : " + isParametrized);

            Scheduled schedule = generateScheduleClassFromRequest(req);
            addEntryToSpec(schedule.getSpec());
            // validate that the scheduler time is uniqe

            if (isParametrized.equals("true"))
            {
                UtilsClass.CreateParamterFile(this.project, req);
            }
        } catch (InvalidInputException ex)
        {
            Logger.getLogger(ProjectConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Find a better way to redirect the response so it won't be hard coded.
        rsp.sendRedirect2(req.getRootPath() + "/job/" + project.getName() + "/projectConfiguration");
    }

    /**
     * oren
     *
     * @param req
     * @param rsp
     * @throws IOException
     */
    public void doRemoveSchedule(StaplerRequest req, StaplerResponse rsp) throws IOException
    {
        // the schedules as they appear to the user are in toString() 
        //format which is more informative but not the same as it appears in 
        //the cron spec, we will extract the schedule description 
        // (which is the content that appears until the ":") and search for
        // objects with the same description.

        System.out.println("-------------------------------------------------");
        System.out.println("-------------------------------------------------");
        System.out.println("-------------------------------------------------");
        System.out.println("-------------------------------------------------");
        System.out.println("-------------------------------------------------");
        System.out.println("----------------doRemoveSchedule---------------");
        System.out.println("-------------------------------------------------");
        System.out.println("-------------------------------------------------");
        System.out.println("-------------------------------------------------");
        System.out.println("-------------------------------------------------");
        System.out.println("-------------------------------------------------");
        System.out.println("-------------------------------------------------");

        String formOption = req.getParameter("formValue");
        String userChoice = req.getParameter("schedule");
        System.out.println("remove value : " + formOption);
        System.out.println("userChoice value : " + userChoice);
        String schduleTime = UtilsClass.getTimeOfSchduleFromItsName(userChoice);
        String pathForParamerterFile = this.parameterFileDirPath + schduleTime;
        System.out.println("pathForParamerterFile:  " + pathForParamerterFile);
        int indexOfColon = userChoice.indexOf(':');
        String scheduleName = userChoice.substring(0, indexOfColon);

        //Oren - the form option can be remove or save 
        try
        {
            switch (formOption)
            {
                case "remove":
                    removeEntryFromSpec(scheduleName);
                    UtilsClass.removeSchduleParametersFile(this.parameterFileDirPath + schduleTime);
                    break;

                case "save":
                    String action = req.getParameter("active");
                    System.out.println("Action value is : " + action);

                    if (!action.isEmpty())
                    {
                        //will check that is realy paused before were making it active
                        if (action.equals("activate") && isSchdulePaused(scheduleName))
                        {
                            System.out.println(" activate!!!!!!!!!!!!!!!!");
                            changeScheduleStatus(scheduleName, "activate");
                        } else if (action.equals("stop") && !isSchdulePaused(scheduleName))
                        {
                            System.out.println("stop!!!!!!!!!!!!!");
                            changeScheduleStatus(scheduleName, "stop");
                        }
                    }
            }

        } catch (InvalidInputException ex)
        {
            Logger.getLogger(ProjectConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("-------------------------------------------------");
        System.out.println("-------------------------------------------------");
        System.out.println("-------------------------------------------------");
        System.out.println("-------------------------------------------------");
        System.out.println("-------------------------------------------------");
        //Find a better way to redirect the response so it won't be hard coded.
        rsp.sendRedirect2(req.getRootPath() + "/job/" + project.getName() + "/projectConfiguration/edit_schedule");

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
        String deploymentCriteria = req.getParameter("Deployment");
        String kwCriticalOption = req.getParameter("kw-critical");
        String kwErrorOption = req.getParameter("kw-error");
        String kwAnyOption = req.getParameter("kw-any");

        System.out.println("deploymentCriteria=" + deploymentCriteria);
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
                } else if (kwErrorOption != null)
                {
                    criteriaProperty.setKWSevirity(CriteriaProperty.KWSeverityEnum.ERROR);
                } else if (kwCriticalOption != null)
                {
                    criteriaProperty.setKWSevirity(CriteriaProperty.KWSeverityEnum.CRITICAL);
                } else
                {
                    throw new InvalidInputException("Non of the kwCriteria has been checked");
                }
                criteriaProperty.setKWCriteria(true);
            } else
            {
                criteriaProperty.setKWSevirity(CriteriaProperty.KWSeverityEnum.ERROR);
                criteriaProperty.setKWCriteria(false);
            }

            //oren
            if (deploymentCriteria != null)
            {
                criteriaProperty.setDeploymentCriteria(true);
            } else
            {
                criteriaProperty.setDeploymentCriteria(false);
            }

            if (testsCriteria != null)
            {
                criteriaProperty.setTestsCriteria(true);
            } else
            {
                criteriaProperty.setTestsCriteria(false);
            }
            criteriaProperty.saveToFile();
        } catch (InvalidInputException ex)
        {
            System.out.println(ex.getMessage());
        }

        rsp.sendRedirect2(req.getReferer());
    }

    // Add new device to current project's device folder (currently unit xml file that fits ATT format)
    public void doAddDevice(StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException
    {
        System.out.println("In doAddDevice");
        Iterator keySetIt = req.getParameterMap().keySet().iterator();
        Object key;
        while (keySetIt.hasNext())
        {
            key = keySetIt.next();
            if (req.getParameterValues(key.toString()).length > 1)
            {
                // System.out.println("key: " + key.toString() + ", value: ");
                for (String parameterValue : req.getParameterValues(key.toString()))
                {
                    //   System.out.println(parameterValue);
                }
            } else
            {
                //  System.out.println("key: " + key.toString() + ", value: " + req.getParameter(key.toString()));
            }
        }
        try
        {
            System.out.println("Adding new device");
            deviceManager.addNewDevice(req.getParameterMap());
        } catch (JAXBException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
        {
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
        } catch (NumberFormatException ex)
        {
            System.out.println("Failed to parse deviceID, deviceID = " + req.getParameter("deviceID"));
            Logger.getLogger(ProjectConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IndexOutOfBoundsException ex)
        {
            System.out.println("Failed to remove device at index " + deviceIndex
                    + " because index is out of bounds, number of devices is " + this.deviceManager.getNumberOfDevices());
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
            try
            {
                this.deviceID = Integer.parseInt(deviceIdStr);
                rsp.sendRedirect2(req.getRootPath() + "/job/" + project.getName() + "/projectConfiguration/slots");
            } catch (NumberFormatException ex)
            {
                System.out.println("DeviceIdStr couldn't be parsed, value: " + deviceIdStr);
                Logger.getLogger(ProjectConfiguration.class.getName()).log(Level.SEVERE, null, ex);
                rsp.sendRedirect2(req.getRootPath() + "/job/" + project.getName() + "/projectConfiguration/devices");
            }
        } else
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
            String linkContent = req.getParameter("link-" + (i + 1) + "-name");
            if (StringUtils.isBlank(linkContent) == false)
            {
                linkArr[i] = linkContent;
            }
        }

        System.out.println("slot-id-string=" + slotIdStr);
        System.out.println("deviceID-string=" + deviceIDStr);
        System.out.println("cardName=" + cardName);
        try
        {
            int slotID = Integer.parseInt(slotIdStr);
            this.chosenSlotID = slotID;
            int deviceID = Integer.parseInt(deviceIDStr);
            if (saveSlotInfoButton != null)
            {
                this.deviceManager.addSlot(deviceID, slotID, cardName, linkArr);
            } else if (removeSlotButton != null)
            {
                this.deviceManager.removeSlot(deviceID, slotID);
            }
        } catch (NumberFormatException ex)
        {
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
        } catch (NumberFormatException ex)
        {
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
        } catch (NumberFormatException ex)
        {
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
            } catch (IOException ex)
            {
                Logger.getLogger(ProjectConfiguration.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return profiles;
    }

    /**
     *
     * @param newEntry should be in the following format: #Name\n* * * * * "*"
     * might be replaced by the desired value
     */
    private void addEntryToSpec(String newEntry)
    {
        String prevSpec, newSpec;

        TimerTrigger timerTrigger = project.getTrigger(TimerTrigger.class);

        if (timerTrigger != null && timerTrigger.getSpec().equals("") == false)
        {
            prevSpec = timerTrigger.getSpec();
            //Oren FIX
            newSpec = prevSpec.trim() + "\n" + newEntry.trim();
            System.out.println("The new spec is : \n " + newSpec);
        } else
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

            for (int i = 0; i < specArr.length; i += 2)
            {
                // we extract the specArray back to spec except the description and the following
                // cron entry which coresponds with the input name
                if (specArr[i].startsWith("#" + name) == false)
                {
                    spec += specArr[i] + "\n" + specArr[i + 1] + "\n";
                }
            }
        } else
        {
            throw new InvalidInputException("timerTrigger is empty can't remove " + name);
        }

        updateSpec(timerTrigger, spec);
    }

    //Oren
    /**
     * Gets the name of the schedule to pause  or activate and  do so
     * by rebuilding of the crone lists
     *with add or remove of # infront of the needed line
     *
     * @param name - the name of the schedule to pause
     *
     * @throws InvalidInputException author Oren
     */
    private void changeScheduleStatus(String name, String newStatus) throws InvalidInputException
    {

        System.out.println("In ---- changeScheduleStatus");
        System.out.println("name = " + name + ",     newstatus = " + newStatus);

        StringBuilder spec = new StringBuilder();
        String[] specArr;
        TimerTrigger timerTrigger = project.getTrigger(TimerTrigger.class);

        if (timerTrigger != null)
        {

            specArr = timerTrigger.getSpec().split("\n");
            for (int i = 0; i < specArr.length; i += 2)
            {
                // we extract the specArray back to spec except the description and the following
                // cron entry which coresponds with the input name,
                //which will make the sechedule also as a comment by adding "#"
                spec.append(specArr[i]);
                spec.append("\n");
                String cronToAdd = specArr[i + 1];
                if (specArr[i].startsWith("#" + name))
                {
                    switch (newStatus)
                    {

                        case "stop":
                            //will make sure not to add # twice
                            if (!cronToAdd.startsWith("#"))
                            {
                                cronToAdd = "#" + cronToAdd;
                            }
                            break;

                        case "activate":
                            // activate only paused schedules
                            if (cronToAdd.startsWith("#"))
                            {
                                cronToAdd = cronToAdd.substring(1);
                            }
                            break;
                    }
                }

                spec.append(cronToAdd);
                spec.append("\n");
            }
            updateSpec(timerTrigger, spec.toString());
        }
    }

    /**
     *
     * parse the given user choice and than calls to isSchdulePaused
     *
     * @param userChoice
     * @return boolean
     *
     * Author Oren
     */
    @JavaScriptMethod
    public boolean doSchdulePaused(String userChoice)
    {
        System.out.println("userChoice:" + userChoice);
        int indexOfColon = userChoice.indexOf(':');
        String name = userChoice.substring(0, indexOfColon);
        return isSchdulePaused(name);
    }

    /**
     * return true if the schedule is paused, returned false otherwise
     *
     * @param name
     * @return boolean
     *
     * Author - Oren
     */
    public boolean isSchdulePaused(String name)
    {
        System.out.println("In isSchdulePaused  ");
        TimerTrigger timerTrigger = project.getTrigger(TimerTrigger.class);
        boolean isSchdulerPaused = false;
        String[] specArray;

        if (timerTrigger != null)
        {
            specArray = timerTrigger.getSpec().split("\n");
            for (int i = 0; i < specArray.length; i += 2)
            {
                if (specArray[i].startsWith("#" + name))
                {
                    //will check if it paused
                    if (specArray[i + 1].startsWith("#"))
                    {
                        isSchdulerPaused = true;
                        break;
                    }
                }
            }
        }
        System.out.println("Return Value is :" + isSchdulerPaused);
        return isSchdulerPaused;
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
        } catch (ANTLRException | IOException ex)
        {
            Logger.getLogger(ProjectConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean CheckBuildPermissions()
    {
        for (Permission permission : Permission.getAll())
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
        String name = req.getParameter("scheduleName").trim();
        String hour = req.getParameter("ScheduleTime-hour");
        String minute = req.getParameter("ScheduleTime-minute");
        String meridiem = req.getParameter("ScheduleTime-meridiem");
        String type = req.getParameter("type");
        ArrayList<String> daysOfWeek = new ArrayList<>();
        String dateOfMonth;

        Scheduled schedule;

        switch (type)
        {
            case "Daily":
                schedule = new Scheduled(minute, hour, meridiem, name);
                break;
            case "Weekly":
                if (req.getParameter("Sunday") != null)
                {
                    daysOfWeek.add("Sunday");
                }
                if (req.getParameter("Monday") != null)
                {
                    daysOfWeek.add("Monday");
                }
                if (req.getParameter("Tuesday") != null)
                {
                    daysOfWeek.add("Tuesday");
                }
                if (req.getParameter("Wednesday") != null)
                {
                    daysOfWeek.add("Wednesday");
                }
                if (req.getParameter("Thursday") != null)
                {
                    daysOfWeek.add("Thursday");
                }
                if (req.getParameter("Friday") != null)
                {
                    daysOfWeek.add("Friday");
                }
                if (req.getParameter("Saturday") != null)
                {
                    daysOfWeek.add("Saturday");
                }
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
        if (timerTrigger == null)
        {
            return "";
        }
        String spec = timerTrigger.getSpec();

        return spec;
    }
//here

    private ArrayList<Scheduled> parseSpec(String spec) throws InvalidInputException
    {
        System.out.println("In parse spec ,SPEC:  " + spec);

        System.out.println("********************************");

        String[] specArr = spec.split("\n");
        ArrayList<Scheduled> schedules = new ArrayList<Scheduled>();

        for (int i = 0; i < specArr.length; i += 2)
        {
            System.out.println("specArr[i]: " + specArr[i]);

            if (specArr[i].startsWith("#"))
            {
                String description;
                description = specArr[i];
                System.out.println("description:" + description);
                //description init
                String[] cronEntry;

                //we need to check if it on pause, will if it starts with "#"
                String currnetSchdule = specArr[i + 1].trim();
                System.out.println("currnetSchdule == " + currnetSchdule);

                if (currnetSchdule.startsWith("#"))
                {
                    //will elimanate the first #, and then split
                    currnetSchdule = currnetSchdule.substring(1);
                    System.out.println("Start with #");
                }

                cronEntry = currnetSchdule.split(" ");
                // Initializing Schduled parameters

                String minute = cronEntry[0];
                StringBuilder hour = new StringBuilder();
                StringBuilder meridiem = new StringBuilder();                  //minute init
                Scheduled.getHourAndMerdidiem(cronEntry[1], hour, meridiem);    //hour & meridiem init

                if (cronEntry[4].equals("*") == false)
                {
                    String[] daysArr = cronEntry[4].split(",");                 //days init                    

                    schedules.add(new WeeklyScheduled(minute, hour.toString(), meridiem.toString(), daysArr, description));
                } else if (cronEntry[2].equals("*") == false)
                {
                    String dayOfMonth = cronEntry[2];

                    schedules.add(new MonthlyScheduled(minute, hour.toString(), meridiem.toString(), dayOfMonth, description));
                } else
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
        try
        {
            return this.deviceManager.getDeviceArr().get(this.deviceID).getTelnetUnitIP();
        } catch (IOException ex)
        {
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

    /**
     * Oren
     *
     * @true if test set as criteria, false otherwise
     */
    public boolean isDeploymentCriteriaSet()
    {
        this.criteriaProperty.initFromFile();
        if (this.criteriaProperty.getDeploymentCriteria() == true)
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

    /**
     * Oren returns the default parameters of this project
     *
     *
     *
     * @return List<BooleanParameterDefinition>-defaultParameter
     */
    public List<BooleanParameterDefinition> getParameters()
    {
        DefaultParameter defaultParameter = new DefaultParameter(this.project);
        try
        {
            return defaultParameter.getParameters();
        } catch (IOException ex)
        {
            Logger.getLogger(ProjectConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Oren
     *
     * return the names of parameters that their default value is true
     *
     * @return
     */
    @JavaScriptMethod
    public ArrayList<String> doGetDefaultParametersInList(String name) throws IOException
    {
        System.out.println("In doGetDefaultParametersInList");
        DefaultParameter defaultParameter = new DefaultParameter(this.project);
        return defaultParameter.getParametersInList();

    }

    /**
     * Oren
     *
     * return true if this schedule have a file with parameters saved
     *
     * @return
     */
    @JavaScriptMethod
    public boolean doFileExist(String schduleName)
    {
        System.out.println("In doFileExist");
        String fileName = UtilsClass.getTimeOfSchduleFromItsName(schduleName);
        System.out.println("fileName: " + fileName);
        boolean result = UtilsClass.fileExists(parameterFileDirPath + fileName);
        System.out.println("the return value is " + result);
        return result;

    }

    private class ProfilesFileFilter implements FileFilter
    {

        @Override
        public boolean accept(File pathname)
        {
            return pathname.getName().endsWith(".profile");
        }
    }

    /**
     * called from the java script when an event(click) on check box in the
     * device page
     *
     * @param checkBoxName
     * @param CheckBoxValue
     */
    @JavaScriptMethod
    public void doOnClickCheckBoxDeviceManager(String checkBoxName, String CheckBoxValue)
    {
        System.out.println("In onClickCheckBoxDeviceManager ");
        try
        {
            deviceManager = new DeviceManager(confFileDir + "/Devices");
            deviceManager.onClickCheckBox(CheckBoxValue, checkBoxName);
        } catch (JAXBException ex)
        {
            Logger.getLogger(ProjectConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * return all the projects in Jenkins
     *
     * @return - list of all the projects
     */
    public Collection<String> getProjectlist()
    {
        Collection<String> projectNames = Jenkins.getInstance().getJobNames();
        projectNames.remove(this.PROJECTNAME);
        projectNames.remove(getNameDepenedency());
        return projectNames;
    }

    /**
     *
     * calls by the client side, can be called with 2 option s
     *
     * remove - will remove the current dependency save - will change / add the
     * current dependency the the chosen one
     *
     * @param req
     * @param rsp
     * @throws IOException
     */
    public void doChoseDepenedencyOption(StaplerRequest req, StaplerResponse rsp) throws IOException
    {
        System.out.println("^^^^^^^^^^^^^");
        System.out.println("in doChoseDepenedencyOption");

        String formOption = req.getParameter("formValue");

        System.out.println("formOption :  " + formOption);
        if (formOption != null)
        {
            switch (formOption)
            {
                case "save":
                    String chosenProjectName = req.getParameter("projects");
                    System.out.println("chosenProjectName :  " + chosenProjectName);
                    if (chosenProjectName != null)
                    {
                        UtilsClass.writeToTestDependencyFile(this.project.getName(), chosenProjectName);

                    }
                    break;

                case "remove":
                    UtilsClass.removeDependencyFile(PROJECTNAME);
                    break;
            }
        }

        rsp.sendRedirect2(req.getRootPath() + "/job/" + project.getName() + "/projectConfiguration/testDependency");
    }

    /**
     *
     * return the name saved in the dependency file, or "empty" if the file does
     * not exist
     *
     */
    @JavaScriptMethod
    public String getNameDepenedency()
    {
        String dependencyName = null;
        System.out.println("^^^^^^^^^^^^^");
        System.out.println("in doGetNameDepenedency");
        try
        {

            dependencyName = UtilsClass.readFromTestDependencyFile(this.project.getName());
        } catch (IOException ex)
        {
            System.out.println("*********************************************");
            System.err.println("getNameDepenedency -- Could not read the dependency");
            System.out.println("*********************************************");

            Logger.getLogger(ProjectConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dependencyName;
    }
}
