/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.projectConfiguration;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BallColor;
import jenkins.model.Jenkins;

/**
 *
 * @author gabi
 */
public class BuildSummaryProjectAction implements Action {
    
    private static final String statusPicsDir = "/static/80397a94/images/32x32/";
        
    private AbstractProject<?, ?> project;
    
    BuildSummaryAction buildAction;
    
    public BuildSummaryProjectAction(AbstractProject<?, ?> project)
    {
        this.project = project;
    }
    
    public String getBuildDetails()
    {
        if (this.project.getLastBuild().isBuilding() == true)
        {
            return "";
        }
        return this.buildAction.getBuildDetails();
    }
    
    public String getReportDetails()
    {     
        if (this.project.getLastBuild().isBuilding() == true)
        {
            return "";
        }   
        return this.buildAction.getReportDetails();
    }
    
    public String getMkverBuildStatus()
    {
        if (this.buildAction == null)
        {
            return null;
        }
        else if (this.project.getLastBuild().isBuilding() == true)
        {
            return "In progress";
        }
        return this.buildAction.getMkverBuildStatus();
    }
    
    public String getKlocworkStatus()
    {
        if (this.buildAction == null)
        {
            return null;
        }
        return this.buildAction.getKlocworkStatus();
    }
    
    public String getDeploymentStatus()
    {
        if (this.buildAction == null)
        {
            return null;
        }
        return this.buildAction.getDeploymentStatus();
    }
    
    public String getTestsStatus()
    {
        if (this.buildAction == null)
        {
            return null;
        }
        return this.buildAction.getTestsStatus();
    }
    
    public String getReportStatus()
    {
        if (this.buildAction == null)
        {
            return null;
        }
        else if (this.project.getLastBuild().isBuilding() == true)
        {
            return "N/A";
        }
        return this.buildAction.getReportStatus();
    }
    
    public String getMkverBuildStatusImg()
    {
        AbstractBuild<?, ?> lastBuild = project.getLastBuild();
        if (lastBuild == null)
        {
            return statusPicsDir + BallColor.GREY.getImage();
        }
        else if (lastBuild.isBuilding() == true)
        {
            return statusPicsDir + BallColor.GREY_ANIME.getImage();
        }
        this.buildAction = lastBuild.getAction(BuildSummaryAction.class);
        return this.buildAction.getMkverBuildStatusImg();
    }
    
    public String getKlocworkStatusImg()
    {
        if (this.buildAction == null)
        {
            return statusPicsDir + BallColor.GREY.getImage();
        }
        return this.buildAction.getKlocworkStatusImg();
    }
    
    public String getDeploymentStatusImg()
    {
        if (this.buildAction == null)
        {
            return statusPicsDir + BallColor.GREY.getImage();
        }
        return this.buildAction.getDeploymentStatusImg();
    }
    
    public String getTestsStatusImg()
    {
        if (this.buildAction == null)
        {
            return statusPicsDir + BallColor.GREY.getImage();
        }
        return this.buildAction.getTestsStatusImg();
    }
    
    public String getReportStatusImg()
    {
        if (this.buildAction == null || this.project.getLastBuild().isBuilding() == true)
        {
            return statusPicsDir + BallColor.GREY.getImage();
        }
        return this.buildAction.getReportStatusImg();
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
    
}
