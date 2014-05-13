/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.projectConfiguration;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BallColor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
public class BuildSummaryProjectAction implements Action {
    
    private AbstractProject<?, ?> project;
    
    BuildSummaryAction buildAction;
    
    BuildStepInfo mkverBuild = null;
    BuildStepInfo kw = null;
    BuildStepInfo deployment = null;
    BuildStepInfo tests = null;
    BuildStepInfo reports = null;
    
    public BuildSummaryProjectAction(AbstractProject<?, ?> project)
    {
        this.project = project;
    }
    
    public String getMkverBuildStatus()
    {
        this.mkverBuild =  buildStepInfoFactory(this.project.getLastBuild(), null, StepNameEnum.BUILD);
        return mkverBuild.getStatus();
    }
    
    public String getKlocworkStatus()
    {
        this.kw = buildStepInfoFactory(this.project.getLastBuild(), null, StepNameEnum.KW);
        return kw.getStatus();
    }
    
    public String getDeploymentStatus()
    {
        this.deployment = buildStepInfoFactory(this.project.getLastBuild(), null, StepNameEnum.DEPLOYMENT);
        return deployment.getStatus();
    }
    
    public String getTestsStatus()
    {
        this.tests = buildStepInfoFactory(this.project.getLastBuild(), null, StepNameEnum.TESTS);
        return tests.getStatus();
    }
    
    public String getReportStatus()
    {
        if (this.project.getLastBuild().isBuilding() == true)
        {
            return "N/A";
        }
        else if(this.project.getLastBuild().getResult().completeBuild == false)
        {
            return "ABORTED";
        }
        else
        {
            return "SUCCESS";
        }
    }
    
    
    public String getMkverBuildStatusImg()
    {
        return buildStepInfoFactory(this.project.getLastBuild(), mkverBuild, StepNameEnum.BUILD).getImg();
    }
    
    public String getKlocworkStatusImg()
    {
        return buildStepInfoFactory(this.project.getLastBuild(), kw, StepNameEnum.KW).getImg();
    }
    
    public String getDeploymentStatusImg()
    {
        return buildStepInfoFactory(this.project.getLastBuild(), deployment, StepNameEnum.DEPLOYMENT).getImg();
    }
    
    public String getTestsStatusImg()
    {
        return buildStepInfoFactory(this.project.getLastBuild(), tests, StepNameEnum.TESTS).getImg();
    }
    
    public String getReportStatusImg()
    {
        if (this.project.getLastBuild().isBuilding() == true)
        {
            return BuildSummaryAction.statusPicsDir + BallColor.GREY.getImage();
        }
        else if(this.project.getLastBuild().getResult().completeBuild == false)
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
        return buildStepInfoFactory(this.project.getLastBuild(), mkverBuild, StepNameEnum.BUILD).getDetails();
    }
    
    public String getKlocworkDetails()
    {
        return buildStepInfoFactory(this.project.getLastBuild(), kw, StepNameEnum.KW).getDetails();
    }
    
    public String getDeploymentDetails()
    {
        return buildStepInfoFactory(this.project.getLastBuild(), deployment, StepNameEnum.DEPLOYMENT).getDetails();
    }
    
    public String getTestsDetails()
    {
        return buildStepInfoFactory(this.project.getLastBuild(), tests, StepNameEnum.TESTS).getDetails();
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
