/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.projectConfiguration.buildSteps;

import hudson.model.AbstractBuild;
import hudson.model.BallColor;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jenkinsci.plugins.projectConfiguration.BuildSummaryAction;
import static org.jenkinsci.plugins.projectConfiguration.BuildSummaryAction.statusPicsDir;
import org.jenkinsci.plugins.projectConfiguration.exceptions.ScriptPluginInteractionException;

/**
 *
 * @author gabi
 */
public class MkverBuildInfo extends BuildStepInfo {
    
    public MkverBuildInfo(AbstractBuild<?, ?> build) throws ScriptPluginInteractionException
    {
        this.initInfo(build);
    }
    
    @Override
    public final void initInfo(AbstractBuild<?, ?> build) throws ScriptPluginInteractionException {
        try 
        {
            String workspacePath = build.getWorkspace().toURI().getPath() + "/build_" + build.getNumber() + "/";
            File f_mkverBuildStart = new File(workspacePath + "mkverBuild_start_" + build.getNumber() + ".info");
            File f_mkverBuildSuccess = new File(workspacePath + "mkverBuild_success_" + build.getNumber() + ".info");
            File f_mkverBuildFailure = new File(workspacePath + "mkverBuild_failure_" + build.getNumber() + ".info");

            if (f_mkverBuildStart.exists() == true)
            {
                this.status = "In progress";
                this.img = BuildSummaryAction.statusPicsDir + BallColor.GREY_ANIME.getImage();
                this.details = null;
            }
            else
            {
                if (f_mkverBuildSuccess.exists() == true)
                {
                    this.status = "Success";
                    this.img = BuildSummaryAction.statusPicsDir + BallColor.BLUE.getImage();
                    this.details = BuildStepInfo.getDetails("Build");
                }
                else if (f_mkverBuildFailure.exists() == true)
                {
                    this.status = "Failure";
                    this.img = BuildSummaryAction.statusPicsDir + BallColor.RED.getImage();
                    this.details = BuildStepInfo.getDetails("Build");
                }
                else
                {
                    this.status = "Pending";
                    this.img = BuildSummaryAction.statusPicsDir + BallColor.GREY.getImage();
                    this.details = null;
                }
            }
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(KlocworkInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
//        this.status = build.getResult().color.getDescription();
//        this.img = BuildSummaryAction.statusPicsDir + build.getResult().color.getImage();
//        this.details = BuildStepInfo.getDetails("Build");
    }
    
}
