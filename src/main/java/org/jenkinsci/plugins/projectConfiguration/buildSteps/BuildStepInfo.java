/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.projectConfiguration.buildSteps;

import hudson.model.AbstractBuild;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jenkinsci.plugins.projectConfiguration.BuildSummaryAction;
import org.jenkinsci.plugins.projectConfiguration.exceptions.ScriptPluginInteractionException;

/**
 *
 * @author gabi
 */
public abstract class BuildStepInfo {
    
    protected String status;
    protected String img;
    protected String details;
    
    public String getStatus()
    {
        return this.status;
    }
    
    public String getImg()
    {
        return this.img;
    }
    
    public String getDetails()
    {
        return this.details;
    }
    
    public abstract void initInfo(AbstractBuild<?, ?> build) throws ScriptPluginInteractionException;
    
    
    // Used for jenkins email report file info retreive regarding build steps details
    public static String getDetails(String stepName) 
    {
//        String jenkinsReportFilePath="/home/builder/BuildSystem/cc-views/builder_" +this.build.getParent().getName()
//                + "_int/vobs/linux/CI_Build_Scripts/src/report/templates/jenkins_email.txt.temp";
        String jenkinsReportFilePath="/home/gabi/jenkins_email.txt";
        String details = "";
        InputStream fis;
        BufferedReader br;
        
        try 
        {    
            fis = new FileInputStream(jenkinsReportFilePath);
            br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
            
            String line;
            while ((line = br.readLine()) != null) 
            {
                if (line.startsWith(stepName) == true)
                {
                    details = line.split("\\s+")[2];
                }
            }
            br.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BuildSummaryAction.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BuildSummaryAction.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return details;
    }
    
}
