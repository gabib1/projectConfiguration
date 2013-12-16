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
import jenkins.model.Jenkins;

/**
 *
 * @author gabi
 */
public class BuildSummaryAction implements Action {
    
    private static final String statusPicsDir = Jenkins.getInstance().getRootUrl() +
            "/static/80397a94/images/32x32/";
    
    String mkverBuildStatus;
    String klocworkStatus;
    String deploymentStatus;
    String testsStatus;
    String reportStatus;
    
    public String getMkverBuildStatus()
    {
        return this.mkverBuildStatus;
    }
    
    public String getKlocworkStatus()
    {
        return this.klocworkStatus;
    }
    
    public String getDeploymentStatus()
    {
        return this.deploymentStatus;
    }
    
    public String getTestsStatus()
    {
        return this.testsStatus;
    }
    
    public String getReportStatus()
    {
        return this.reportStatus;
    }
    
    BuildSummaryAction(AbstractBuild<?, ?> build) {
        this.mkverBuildStatus = statusPicsDir + build.getResult().color.getImage();
        this.klocworkStatus = statusPicsDir + BallColor.YELLOW.getImage();
        this.deploymentStatus = statusPicsDir + BallColor.GREY.getImage();
        this.testsStatus = statusPicsDir + BallColor.GREY.getImage();
        this.reportStatus = statusPicsDir + build.getResult().color.getImage();
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
