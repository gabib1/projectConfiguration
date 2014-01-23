/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.projectConfiguration.buildSteps;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.BallColor;
import hudson.model.TaskListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jenkinsci.plugins.projectConfiguration.BuildSummaryAction;
import org.jenkinsci.plugins.projectConfiguration.exceptions.ScriptPluginInteractionException;

/**
 *
 * @author gabi
 */
public class KlocworkInfo extends BuildStepInfo {
    
    public KlocworkInfo(AbstractBuild<?, ?> build) throws ScriptPluginInteractionException
    {
        this.initInfo(build);
    }
    
    @Override
    public final void initInfo(AbstractBuild<?, ?> build) throws ScriptPluginInteractionException {
        try 
        {
            // Reading variables which are used in the build step script
            String Klocwork = build.getBuildVariableResolver().resolve("Klocwork");
    //        String klocworkState = build.getBuildVariableResolver().resolve("klocworkState");
            
            String workspacePath = build.getWorkspace().toURI().getPath() + "/build_" + build.getNumber() + "/";
            File f_kwStart = new File(workspacePath + "kw_start_" + build.getNumber() + ".info");
            File f_kwSuccess = new File(workspacePath + "kw_success_" + build.getNumber() + ".info");
            File f_kwFailure = new File(workspacePath + "kw_failure_" + build.getNumber() + ".info");
            
    //        String KWExitCode = build.getBuildVariableResolver().resolve("KWExitCode");
            if (Klocwork == null)
            {
                this.status = "N/A";
                this.img = BuildSummaryAction.statusPicsDir + BallColor.GREY.getImage();
                this.details = null;
            }
            else
            {
                if (f_kwStart.exists() == true)
                {
                    this.status = "In progress";
                    this.img = BuildSummaryAction.statusPicsDir + BallColor.GREY_ANIME.getImage();
                    this.details = null;
                }
                else
                {
                    if (f_kwSuccess.exists() == true)
                    {
                        this.status = "Success";
                        this.img = BuildSummaryAction.statusPicsDir + BallColor.BLUE.getImage();
                        this.details = BuildStepInfo.getDetails("Klocwork");
                    }
                    else if (f_kwFailure.exists() == true)
                    {
                        this.status = "Failure";
                        this.img = BuildSummaryAction.statusPicsDir + BallColor.RED.getImage();
                        this.details = BuildStepInfo.getDetails("Klocwork");
                    }
                    else
                    {
                        this.status = "Pending";
                        this.img = BuildSummaryAction.statusPicsDir + BallColor.GREY.getImage();
                        this.details = null;
                    }
                }
            }
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(KlocworkInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
