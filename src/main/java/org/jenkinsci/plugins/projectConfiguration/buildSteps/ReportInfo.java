/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.projectConfiguration.buildSteps;

import hudson.model.AbstractBuild;
import hudson.model.BallColor;
import org.jenkinsci.plugins.projectConfiguration.BuildSummaryAction;
import static org.jenkinsci.plugins.projectConfiguration.BuildSummaryAction.statusPicsDir;
import org.jenkinsci.plugins.projectConfiguration.exceptions.ScriptPluginInteractionException;

/**
 *
 * @author gabi
 */
public class ReportInfo extends BuildStepInfo {
    
    public ReportInfo(AbstractBuild<?, ?> build) throws ScriptPluginInteractionException
    {
        this.initInfo(build);
    }
    
    @Override
    public final void initInfo(AbstractBuild<?, ?> build) throws ScriptPluginInteractionException {

        if (build.isBuilding() == true)
        {
            this.status = "Pending";
            this.img = BuildSummaryAction.statusPicsDir + BallColor.GREY.getImage();
            this.details = null;
        }
        else
        {
            this.status = "Success";
            this.img = BuildSummaryAction.statusPicsDir + BallColor.BLUE.getImage();
            this.details = BuildStepInfo.getDetails("Report");
        }
    }
}
