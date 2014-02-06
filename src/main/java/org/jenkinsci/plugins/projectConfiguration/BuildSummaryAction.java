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
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.projectConfiguration.buildSteps.BuildStepInfo;
import org.jenkinsci.plugins.projectConfiguration.buildSteps.StepNameEnum;
import org.jenkinsci.plugins.projectConfiguration.exceptions.ScriptPluginInteractionException;

/**
 *
 * @author gabi
 */
public class BuildSummaryAction implements Action {
    
    public static final String statusPicsDir = "/static/80397a94/images/32x32/";
    
    AbstractBuild<?, ?> build;
    
    BuildSummaryAction(AbstractBuild<?, ?> build) {
        this.build = build;
    }
    
    public String getMkverBuildStatus()
    {
        return getBuildStepInfo(this.build, StepNameEnum.BUILD).getDetails();
    }
    
    public String getKlocworkStatus()
    {
        return getBuildStepInfo(this.build, StepNameEnum.KW).getStatus();
    }
    
    public String getDeploymentStatus()
    {
        return getBuildStepInfo(this.build, StepNameEnum.DEPLOYMENT).getStatus();
    }
    
    public String getTestsStatus()
    {
        return getBuildStepInfo(this.build, StepNameEnum.TESTS).getStatus();
    }
    
    public String getReportStatus()
    {
        if (this.build.isBuilding() == true)
        {
            return "N/A";
        }
        else
        {
            return "SUCCESS";
        }
    }
    
    public String getMkverBuildStatusImg()
    {
        return getBuildStepInfo(this.build, StepNameEnum.BUILD).getImg();
    }
    
    public String getKlocworkStatusImg()
    {
        return getBuildStepInfo(this.build, StepNameEnum.KW).getImg();
    }
    
    public String getDeploymentStatusImg()
    {
        return getBuildStepInfo(this.build, StepNameEnum.DEPLOYMENT).getImg();
    }
    
    public String getTestsStatusImg()
    {
        return getBuildStepInfo(this.build, StepNameEnum.TESTS).getImg();
    }
    
    public String getReportStatusImg()
    {
        if (this.build.isBuilding() == true)
        {
            return BuildSummaryAction.statusPicsDir + BallColor.GREY.getImage();
        }
        else
        {
            return BuildSummaryAction.statusPicsDir + BallColor.BLUE.getImage();
        }
    }
    
    public String getBuildDetails()
    {
        return getBuildStepInfo(this.build, StepNameEnum.BUILD).getDetails();
    }
    
    public String getKlocworkDetails()
    {
        return getBuildStepInfo(this.build, StepNameEnum.KW).getDetails();
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

    private BuildStepInfo getBuildStepInfo(AbstractBuild<?, ?> build, StepNameEnum stepNameEnum) 
    {
        try
        {
            return new BuildStepInfo(build, stepNameEnum);
        }
        catch (ScriptPluginInteractionException ex) 
        {
            Logger.getLogger(BuildSummaryProjectAction.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
