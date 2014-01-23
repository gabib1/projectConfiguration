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
import org.jenkinsci.plugins.projectConfiguration.buildSteps.KlocworkInfo;
import org.jenkinsci.plugins.projectConfiguration.buildSteps.MkverBuildInfo;
import org.jenkinsci.plugins.projectConfiguration.buildSteps.ReportInfo;
import org.jenkinsci.plugins.projectConfiguration.exceptions.ScriptPluginInteractionException;

/**
 *
 * @author gabi
 */
public class BuildSummaryProjectAction implements Action {
    
    private static final String statusPicsDir = "/jenkins/static/80397a94/images/32x32/";
        
    private AbstractProject<?, ?> project;
    
    BuildSummaryAction buildAction;
    
    public BuildSummaryProjectAction(AbstractProject<?, ?> project)
    {
        this.project = project;
    }
    
    public String getBuildDetails()
    {
        try
        {
            MkverBuildInfo buildInfo = new MkverBuildInfo(this.project.getLastBuild());
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
            KlocworkInfo klocworkInfo = new KlocworkInfo(this.project.getLastBuild());
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
            ReportInfo reportInfo = new ReportInfo(this.project.getLastBuild());
            return reportInfo.getDetails();
        }
        catch (ScriptPluginInteractionException ex) 
        {
            Logger.getLogger(BuildSummaryProjectAction.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public String getMkverBuildStatus()
    {
        try
        {
            MkverBuildInfo buildInfo = new MkverBuildInfo(this.project.getLastBuild());
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
            KlocworkInfo klocworkInfo = new KlocworkInfo(this.project.getLastBuild());
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
        try
        {
            ReportInfo reportInfo = new ReportInfo(project.getLastBuild());
            return reportInfo.getStatus();
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
            MkverBuildInfo buildInfo = new MkverBuildInfo(project.getLastBuild());
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
            KlocworkInfo klocworkInfo = new KlocworkInfo(project.getLastBuild());
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
        try
        {
            ReportInfo reportInfo = new ReportInfo(project.getLastBuild());
            return reportInfo.getImg();
        }
        catch (ScriptPluginInteractionException ex) 
        {
            Logger.getLogger(BuildSummaryProjectAction.class.getName()).log(Level.SEVERE, null, ex);
            return null;
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
    
//    public <T extends Action> T getAction(Class<T> type) {
//    private BuildStepInfo buildStepInfoFactory(Class<Object> buildStepInfoClass) throws InstantiationException
//    {
//        try {
//            Constructor<BuildStepInfo> ctor = buildStepInfoClass.getConstructor(this.project.getLastBuild().getClass());
//    //        buildStepInfoClass.newInstance();
//    //        BuildStepInfo buildStepInfo = new buildStepInfoClass(project.getLastBuild());
//            return ctor.newInstance(this.project.getLastBuild());
//        } catch (NoSuchMethodException | SecurityException | 
//                IllegalAccessException | IllegalArgumentException | 
//                InvocationTargetException ex) {
//            
//            Logger.getLogger(BuildSummaryProjectAction.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        System.out.println("Error while generating BuildStepInfo object");
//        return null;
//    }
    
}
