/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.projectConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gabi
 * TODO Clearcase checkin/out the conf file before and after updating the parameters
 */
public class MkverConf 
{
    // Information used by Jenkins and exists in the conf file
    private String confFilePath;
    private String cleartool;
    private String jenkinsCCActivity;
    
    // Information used by mkver and edited via Jenkins
    private String projectName;
    private String streamName;
    private String mailingList;
    private String mailingListTesting;
    private String logsPath;
    private String rpmPath;
    private String imagePath;
    private String ccActivity;
    private String kwProjectName;
    private String kwDirPath;
    private String idcVersionFilePath;
    private String productName;
    private String buildDirPath;
    
    public MkverConf(String confFilePath)
    {
        this.confFilePath = confFilePath;
    }
    
    public void createObjectFromFile()
    {
        InputStream fis;
        BufferedReader br;
        String line;

        try 
        {
            fis = new FileInputStream(confFilePath);
            br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
            while ((line = br.readLine()) != null) 
            {
                if(line.startsWith("PRJ_NAME=") == true)
                {
                    int indexOfEquels = line.indexOf('=') + 1;
                    this.projectName = line.substring(indexOfEquels);
                }
                else if(line.startsWith("PRODUCT_NAME=") == true)
                {
                    int indexOfEquels = line.indexOf('=') + 1;
                    this.productName = line.substring(indexOfEquels);
                }
                else if (line.startsWith("STREAM_NAME=") == true)
                {
                    int indexOfEquels = line.indexOf('=') + 1;
                    this.streamName = line.substring(indexOfEquels);
                }
                else if (line.startsWith("APP_CLEARQUEST_ACTIVITY=") == true)
                {
                    int indexOfEquels = line.indexOf('=') + 1;
                    this.ccActivity = line.substring(indexOfEquels);
                }
                else if (line.startsWith("JENKINS_CLEARQUEST_ACTIVITY=") == true)
                {
                    int indexOfEquels = line.indexOf('=') + 1;
                    this.jenkinsCCActivity = line.substring(indexOfEquels);
                }
                else if (line.startsWith("RESULTS_PATH_BASE=") == true)
                {
                    int indexOfEquels = line.indexOf('=') + 1;
                    this.logsPath = line.substring(indexOfEquels);
                }
                else if (line.startsWith("G2U_RPM_DOWNLOAD_PATH_BASE=") == true)
                {
                    int indexOfEquels = line.indexOf('=') + 1;
                    this.rpmPath = line.substring(indexOfEquels);
                }
                else if (line.startsWith("IMAGE_RELEASE_DIR_BASE=") == true)
                {
                    int indexOfEquels = line.indexOf('=') + 1;
                    this.imagePath = line.substring(indexOfEquels);
                }
                else if (line.startsWith("MAILING_LIST=") == true)
                {
                    int indexOfOpenDoubleQuote = line.indexOf('"') + 1;
                    int indexOfCloseDoubleQuote = line.lastIndexOf('"');
                    this.mailingList = line.substring(indexOfOpenDoubleQuote, indexOfCloseDoubleQuote);
                }
                else if (line.startsWith("MAILING_LIST_TESTING=") == true)
                {
                    int indexOfOpenDoubleQuote = line.indexOf('"') + 1;
                    int indexOfCloseDoubleQuote = line.lastIndexOf('"');
                    this.mailingListTesting = line.substring(indexOfOpenDoubleQuote, indexOfCloseDoubleQuote);
                }
                else if (line.startsWith("KLOCWORK_PROJECT_NAME=") == true)
                {
                    int indexOfEquels = line.indexOf('=') + 1;
                    this.kwProjectName = line.substring(indexOfEquels);
                }
                else if (line.startsWith("KLOCWORK_PROJECT_DIR=") == true)
                {
                    int indexOfEquels = line.indexOf('=') + 1;
                    this.kwDirPath = line.substring(indexOfEquels);
                }
                else if (line.startsWith("IDC_VERSION_FILE=") == true)
                {
                    int indexOfEquels = line.indexOf('=') + 1;
                    this.idcVersionFilePath = line.substring(indexOfEquels);
                }
                else if (line.startsWith("PREBUILT_ROOT=") == true)
                {
                    int indexOfEquels = line.indexOf('=') + 1;
                    this.buildDirPath = line.substring(indexOfEquels);
                }
                else if (line.startsWith("CLEARTOOL=") == true)
                {
                    int indexOfEquels = line.indexOf('=') + 1;
                    this.cleartool = line.substring(indexOfEquels);
                }
            }

            br.close();
            
        } catch (FileNotFoundException | ArrayIndexOutOfBoundsException | NullPointerException ex) {
            System.out.println("File not found");
//            Logger.getLogger(ProjectConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ProjectConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void saveObjectToFile()
    {
        String line;
        List<String> newFileContent = new ArrayList<String>();
        
        ProcessBuilder pb = new ProcessBuilder().inheritIO();
        File log = new File("/var/log/jenkins/MkverConf.log");
        pb.redirectErrorStream(true);
        pb.redirectOutput(Redirect.appendTo(log));
        
        String[] clearcaseSetActivityCMD = {this.cleartool, "setact", "-view", 
            "builder_" + getProjectNameWithoutBashVariables() + "_int", this.jenkinsCCActivity};
        String[] clearcaseCheckoutCMD = {this.cleartool, "checkout", "-nc", this.confFilePath};
        String[] clearcaseCheckinCMD = {this.cleartool, "checkin", "-nc", "-identical", this.confFilePath};
        String[] clearcaseUnsetActivityCMD = {this.cleartool, "setact", "-none"};
        
        try 
        {
            pb.command(clearcaseSetActivityCMD).start().waitFor();
            pb.command(clearcaseCheckoutCMD).start().waitFor();
            
            InputStream fis = new FileInputStream(confFilePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
            while ((line = br.readLine()) != null) 
            {
                if(line.startsWith("PRJ_NAME=") == true)
                {
                    line = "PRJ_NAME=" + this.projectName;
                }
                else if(line.startsWith("PRODUCT_NAME=") == true)
                {
                    line = "PRODUCT_NAME=" + this.productName;
                }
                else if(line.startsWith("STREAM_NAME=") == true)
                {
                    line = "STREAM_NAME=" + this.streamName;
                }
                else if(line.startsWith("APP_CLEARQUEST_ACTIVITY=") == true)
                {
                    line = "APP_CLEARQUEST_ACTIVITY=" + this.ccActivity;
                }
                else if(line.startsWith("RESULTS_PATH=") == true)
                {
                    line = "RESULTS_PATH=" + this.logsPath;
                }
                else if(line.startsWith("G2U_RPM_DOWNLOAD_PATH_BASE=") == true)
                {
                    line = "G2U_RPM_DOWNLOAD_PATH_BASE=" + this.rpmPath;
                }
                else if(line.startsWith("IMAGE_RELEASE_DIR_BASE=") == true)
                {
                    line = "IMAGE_RELEASE_DIR_BASE=" + this.imagePath;
                }
                else if (line.startsWith("MAILING_LIST=") == true)
                {
                    line = "MAILING_LIST=\"" + this.mailingList + "\"";
                }
                else if (line.startsWith("MAILING_LIST_TESTING=") == true)
                {
                    line = "MAILING_LIST_TESTING=\"" + this.mailingListTesting + "\"";
                }
                else if(line.startsWith("KLOCWORK_PROJECT_NAME=") == true)
                {
                    line = "KLOCWORK_PROJECT_NAME=" + this.kwProjectName;
                }
                else if(line.startsWith("KLOCWORK_PROJECT_DIR=") == true)
                {
                    line = "KLOCWORK_PROJECT_DIR=" + this.kwDirPath;
                }
                else if(line.startsWith("IDC_VERSION_FILE=") == true)
                {
                    line = "IDC_VERSION_FILE=" + this.idcVersionFilePath;
                }
                else if(line.startsWith("PREBUILT_ROOT=") == true)
                {
                    line = "PREBUILT_ROOT=" + this.buildDirPath;
                }
                
                newFileContent.add(line);
            }

            br.close();
            
            PrintWriter pw = new PrintWriter(confFilePath);
            
            for (String currLine : newFileContent)
            {
                pw.println(currLine);
            }
            
            pw.close();
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MkverConf.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(MkverConf.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        int indexOfLastSlash = this.confFilePath.lastIndexOf('/');
        String confFileDir = this.confFilePath.substring(0, indexOfLastSlash);
        
        try {
            pb.command(clearcaseCheckinCMD).start().waitFor();
            pb.directory(new File(confFileDir)); // needed to unset the activity
            pb.command(clearcaseUnsetActivityCMD).start().waitFor();
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(MkverConf.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean checkIfFileIsUptodateAndUpdate(String projectName, String streamName,
                                                String mailingList, String mailingListTesting,
                                                String logsPath, String rpmPath,
                                                String imagePath, String ccActivity,
                                                String kwProjectName, String kwDirPath,
                                                String idcVersionFilePath, String productName,
                                                String buildDirPath)
    {
        boolean isNewDataFound = false;
        
        if (isBlank(new String[]{this.projectName}) == false && this.projectName.equals(projectName) == false)
        {
            isNewDataFound = true;
            this.projectName = projectName;
        }
        if (isBlank(new String[]{this.productName}) == false && this.productName.equals(productName) == false)
        {
            isNewDataFound = true;
            this.productName = productName;
        }
        if (isBlank(new String[]{this.streamName}) == false && this.streamName.equals(streamName) == false)
        {
            isNewDataFound = true;
            this.streamName = streamName;
        }
        if (isBlank(new String[]{this.ccActivity}) == false & this.ccActivity.equals(ccActivity) == false)
        {
            isNewDataFound = true;
            this.ccActivity = ccActivity;
        }
        if(isBlank(new String[]{this.logsPath}) == false && this.logsPath.equals(logsPath) == false)
        {
            isNewDataFound = true;
            this.logsPath = logsPath;
        }
        if (isBlank(new String[]{this.rpmPath}) == false && this.rpmPath.equals(rpmPath) == false)
        {
            isNewDataFound = true;
            this.rpmPath = rpmPath;
        }
        if (isBlank(new String[]{this.imagePath}) == false && this.imagePath.equals(imagePath) == false)
        {
            isNewDataFound = true;
            this.imagePath = imagePath;
        }
        if(isBlank(new String[]{this.mailingList}) == false && this.mailingList.equals(mailingList) == false)
        {
            isNewDataFound = true;
            this.mailingList = mailingList;
        }
        if(isBlank(new String[]{this.mailingListTesting}) == false && this.mailingListTesting.equals(mailingListTesting) == false)
        {
            isNewDataFound = true;
            this.mailingListTesting = mailingListTesting;
        }
        if (isBlank(new String[]{this.kwProjectName}) == false && this.kwProjectName.equals(kwProjectName) == false)
        {
            isNewDataFound = true;
            this.kwProjectName = kwProjectName;
        }
        if (isBlank(new String[]{this.kwDirPath}) == false && this.kwDirPath.equals(kwDirPath) == false)
        {
            isNewDataFound = true;
            this.kwDirPath = kwDirPath;
        }
        if (isBlank(new String[]{this.idcVersionFilePath}) == false && this.idcVersionFilePath.equals(idcVersionFilePath) == false)
        {
            isNewDataFound = true;
            this.idcVersionFilePath = idcVersionFilePath;
        }
        if (isBlank(new String[]{this.buildDirPath}) == false && this.buildDirPath.equals(buildDirPath) == false)
        {
            isNewDataFound = true;
            this.buildDirPath = buildDirPath;
        }
        
        if (isNewDataFound)
        {
            saveObjectToFile();
        }
        
        return isNewDataFound;
    }
    
    private String getProjectNameWithoutBashVariables()
    {
        // Not good fails on horizon case consider using the Jenkins project name
        return this.projectName.replaceAll("\\$\\{.*\\}", "");
    }
    
    public String getProjectName()
    {
        return this.projectName;
    }
    
    public String getProductName()
    {
        return this.productName;
    }
    
    public String getMailingList()
    {
        return this.mailingList;
    }
    
    public String getMailingListTesting()
    {
        return this.mailingListTesting;
    }
    
    public String getCCActivity()
    {
        return this.ccActivity;
    }
    
    public String getLogsPath()
    {
        return this.logsPath;
    }
    
    public String getRPMPath()
    {
        return this.rpmPath;
    }
    
    public String getImagePath()
    {
        return this.imagePath;
    }
    
    public String getStreamName()
    {
        return this.streamName;
    }
    
    public String getKWProjectName()
    {
        return this.kwProjectName;
    }
    
    public String getKWDirPath()
    {
        return this.kwDirPath;
    }
    
    public String getIDCVersionFilePath()
    {
        return this.idcVersionFilePath;
    }
    
    public String getBuildDirPath()
    {
        return this.buildDirPath;
    }
    
    private boolean isBlank(String[] variables)
    {
        for (String variable : variables) 
        {
            if (variable == null || variable.isEmpty() == true) {
                return true;
            }
        }
        return false;
    }
}
