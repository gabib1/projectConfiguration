/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.projectConfiguration;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BallColor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
public class BuildSummaryProjectAction implements Action {
    
    private AbstractProject<?, ?> project;
    
    BuildSummaryAction buildAction;
    
    public BuildSummaryProjectAction(AbstractProject<?, ?> project)
    {
        this.project = project;
    }
    
    public String getBuildDetails()
    {
        return getBuildStepInfo(this.project.getLastBuild(), StepNameEnum.BUILD).getDetails();
    }
    
    public String getKlocworkDetails()
    {
        return getBuildStepInfo(this.project.getLastBuild(), StepNameEnum.KW).getDetails();
    }
    
    public String getMkverBuildStatus()
    {
        return getBuildStepInfo(this.project.getLastBuild(), StepNameEnum.BUILD).getStatus();
    }
    
    public String getKlocworkStatus()
    {
        return getBuildStepInfo(this.project.getLastBuild(), StepNameEnum.BUILD).getStatus();
    }
    
    public String getDeploymentStatus()
    {
        return getBuildStepInfo(this.project.getLastBuild(), StepNameEnum.DEPLOYMENT).getDetails();
    }
    
    public String getTestsStatus()
    {
        return getBuildStepInfo(this.project.getLastBuild(), StepNameEnum.TESTS).getDetails();
    }
    
    public String getReportStatus()
    {
        if (this.project.getLastBuild().isBuilding() == true)
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
        return getBuildStepInfo(this.project.getLastBuild(), StepNameEnum.BUILD).getImg();
    }
    
    public String getKlocworkStatusImg()
    {
        return getBuildStepInfo(this.project.getLastBuild(), StepNameEnum.KW).getImg();
    }
    
    public String getDeploymentStatusImg()
    {
        return getBuildStepInfo(this.project.getLastBuild(), StepNameEnum.DEPLOYMENT).getImg();
    }
    
    public String getTestsStatusImg()
    {
        return getBuildStepInfo(this.project.getLastBuild(), StepNameEnum.TESTS).getImg();
    }
    
    public String getReportStatusImg()
    {
        if (this.project.getLastBuild().isBuilding() == true)
        {
            return BuildSummaryAction.statusPicsDir + BallColor.GREY.getImage();
        }
        else
        {
            return BuildSummaryAction.statusPicsDir + BallColor.BLUE.getImage();
        }
    }
    
    public int getBuildNumber()
    {
        AbstractBuild<?, ?> lastbuild = project.getLastBuild();
        if (lastbuild != null)
        {
            return lastbuild.getNumber();
        }
        return -1;
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
