/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.projectConfiguration;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.BallColor;
import hudson.model.Result;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    
    BuildStepInfo mkverBuild = null;
    BuildStepInfo kw = null;
    BuildStepInfo deployment = null;
    BuildStepInfo tests = null;
    BuildStepInfo reports = null;
    
    BuildSummaryAction(AbstractBuild<?, ?> build) {
        this.build = build;
    }
    
    public String getMkverBuildStatus()
    {
        this.mkverBuild =  buildStepInfoFactory(this.build, null, StepNameEnum.BUILD);
        return mkverBuild.getStatus();
    }
    
    public String getKlocworkStatus()
    {
        this.kw = buildStepInfoFactory(this.build, null, StepNameEnum.KW);
        return kw.getStatus();
    }
    
    public String getDeploymentStatus()
    {
        this.deployment = buildStepInfoFactory(this.build, null, StepNameEnum.DEPLOYMENT);
        return deployment.getStatus();
    }
    
    public String getTestsStatus()
    {
        this.tests = buildStepInfoFactory(this.build, tests, StepNameEnum.TESTS);
        return tests.getStatus();
    }
    
    public String getReportStatus()
    {
        if (this.build.isBuilding() == true)
        {
            return "N/A";
        }
        else if(this.build.getResult().completeBuild == false)
        {
            return "ABORTED";
        }
        else
        {
            // TODO: Insert failed build logic based on fail cretiria
            return "SUCCESS";
        }
    }
    
    public String getMkverBuildStatusImg()
    {
        return buildStepInfoFactory(this.build, mkverBuild, StepNameEnum.BUILD).getImg();
    }
    
    public String getKlocworkStatusImg()
    {
        return buildStepInfoFactory(this.build, kw, StepNameEnum.KW).getImg();
    }
    
    public String getDeploymentStatusImg()
    {
        return buildStepInfoFactory(this.build, deployment, StepNameEnum.DEPLOYMENT).getImg();
    }
    
    public String getTestsStatusImg()
    {
        return buildStepInfoFactory(this.build, tests, StepNameEnum.TESTS).getImg();
    }
    
    public String getReportStatusImg()
    {
        if (this.build.isBuilding() == true)
        {
            return BuildSummaryAction.statusPicsDir + BallColor.GREY.getImage();
        }
        else if(this.build.getResult().completeBuild == false)
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
        return buildStepInfoFactory(this.build, mkverBuild, StepNameEnum.BUILD).getDetails();
    }
    
    public String getKlocworkDetails()
    {
        return buildStepInfoFactory(this.build, kw, StepNameEnum.KW).getDetails();
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

    private BuildStepInfo buildStepInfoFactory(AbstractBuild<?, ?> build,BuildStepInfo buildStepInfo, StepNameEnum stepNameEnum) 
    {
        try
        {
            if (buildStepInfo == null)
            {
                return new BuildStepInfo(build, stepNameEnum);
            }
            return buildStepInfo;
        }
        catch (ScriptPluginInteractionException ex) 
        {
            Logger.getLogger(BuildSummaryProjectAction.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
