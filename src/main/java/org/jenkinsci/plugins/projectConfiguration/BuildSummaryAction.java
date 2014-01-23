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
import org.jenkinsci.plugins.projectConfiguration.buildSteps.KlocworkInfo;
import org.jenkinsci.plugins.projectConfiguration.buildSteps.MkverBuildInfo;
import org.jenkinsci.plugins.projectConfiguration.buildSteps.ReportInfo;
import org.jenkinsci.plugins.projectConfiguration.exceptions.ScriptPluginInteractionException;

/**
 *
 * @author gabi
 */
public class BuildSummaryAction implements Action {
    
    public static final String statusPicsDir = "/jenkins/static/80397a94/images/32x32/";
    
    AbstractBuild<?, ?> build;
    
    BuildSummaryAction(AbstractBuild<?, ?> build) {
        this.build = build;
    }

    
    public String getMkverBuildStatus()
    {
        try
        {
            MkverBuildInfo buildInfo = new MkverBuildInfo(this.build);
            return buildInfo.getStatus();
        }
        catch (ScriptPluginInteractionException ex) 
        {
            Logger.getLogger(BuildSummaryProjectAction.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public String getKlocworkStatus()
    {
        try
        {
            KlocworkInfo klocworkInfo = new KlocworkInfo(this.build);
            return klocworkInfo.getStatus();
        }
        catch (ScriptPluginInteractionException ex) 
        {
            Logger.getLogger(BuildSummaryProjectAction.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
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
        try
        {
            MkverBuildInfo buildInfo = new MkverBuildInfo(this.build);
            return buildInfo.getStatus();
        }
        catch (ScriptPluginInteractionException ex) 
        {
            Logger.getLogger(BuildSummaryProjectAction.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public String getMkverBuildStatusImg()
    {
        try
        {
            MkverBuildInfo buildInfo = new MkverBuildInfo(this.build);
            return buildInfo.getImg();
        }
        catch (ScriptPluginInteractionException ex) 
        {
            Logger.getLogger(BuildSummaryProjectAction.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public String getKlocworkStatusImg()
    {
        try
        {
            KlocworkInfo klocworkInfo = new KlocworkInfo(this.build); 
            return klocworkInfo.getImg();
        }
        catch (ScriptPluginInteractionException ex) 
        {
            Logger.getLogger(BuildSummaryProjectAction.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
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
        try
        {
            ReportInfo reportInfo = new ReportInfo(this.build);
            return reportInfo.getImg();
        }
        catch (ScriptPluginInteractionException ex) 
        {
            Logger.getLogger(BuildSummaryProjectAction.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public String getBuildDetails()
    {
        try
        {
            MkverBuildInfo buildInfo = new MkverBuildInfo(this.build);
            return buildInfo.getDetails();
        }
        catch (ScriptPluginInteractionException ex) 
        {
            Logger.getLogger(BuildSummaryProjectAction.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public String getKlocworkDetails()
    {
        try
        {
            KlocworkInfo klocworkInfo = new KlocworkInfo(this.build);
            return klocworkInfo.getDetails();
        }
        catch (ScriptPluginInteractionException ex) 
        {
            Logger.getLogger(BuildSummaryProjectAction.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public String getReportDetails()
    {
        try
        {
            ReportInfo reportInfo = new ReportInfo(this.build);
            return reportInfo.getDetails();
        }
        catch (ScriptPluginInteractionException ex) 
        {
            Logger.getLogger(BuildSummaryProjectAction.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
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
