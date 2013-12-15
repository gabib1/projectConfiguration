/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.projectConfiguration;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;

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
    
    public String getMkverBuildStatus()
    {
        AbstractBuild<?, ?> lastBuild = project.getLastBuild();
        if (lastBuild != null && lastBuild.isBuilding() != true)
        {
            this.buildAction = lastBuild.getAction(BuildSummaryAction.class);
        }
        return this.buildAction.getMkverBuildStatus();
    }
    
    public String getKlocworkStatus()
    {
        return this.buildAction.klocworkStatus;
    }
    
    public String getDeploymentStatus()
    {
        return this.buildAction.deploymentStatus;
    }
    
    public String getTestsStatus()
    {
        return this.buildAction.testsStatus;
    }
    
    public String getReportStatus()
    {
        return this.buildAction.reportStatus;
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
