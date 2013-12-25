/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.projectConfiguration;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.BallColor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;

/**
 *
 * @author gabi
 */
public class BuildSummaryAction implements Action {
    
    private static final String statusPicsDir = "/static/80397a94/images/32x32/";
    
    AbstractBuild<?, ?> build;
    
    BuildSummaryAction(AbstractBuild<?, ?> build) {
        this.build = build;
    }

    
    public String getMkverBuildStatus()
    {
        return this.build.getResult().color.getDescription();
    }
    
    public String getKlocworkStatus()
    {
        return "N/A";
    }
    
    public String getDeploymentStatus()
    {
        return "N/A";
    }
    
    public String getTestsStatus()
    {
        return "N/A";
    }
    
    public String getReportStatus()
    {
        return "Success";
    }
    
    public String getMkverBuildStatusImg()
    {
        return statusPicsDir + this.build.getResult().color.getImage();
    }
    
    public String getKlocworkStatusImg()
    {
        return statusPicsDir + BallColor.GREY.getImage();
    }
    
    public String getDeploymentStatusImg()
    {
        return statusPicsDir + BallColor.GREY.getImage();
    }
    
    public String getTestsStatusImg()
    {
        return statusPicsDir + BallColor.GREY.getImage();
    }
    
    public String getReportStatusImg()
    {
        return statusPicsDir + BallColor.BLUE.getImage();
    }
    
    public String getBuildDetails()
    {
        return getDetails("Build");
    }
    
    public String getReportDetails()
    {        
        return getDetails("Report");
    }

    private String getDetails(String stepName) 
    {
        String jenkinsReportFilePath="/home/builder/BuildSystem/cc-views/builder_" +this.build.getParent().getName()
                + "_int/vobs/linux/CI_Build_Scripts/src/report/templates/jenkins_email.txt.temp";
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
                    details = line.split("\t\t")[2];
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
    
    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return null;
    }
}
