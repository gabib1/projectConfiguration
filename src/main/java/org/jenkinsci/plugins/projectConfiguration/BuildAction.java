/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.projectConfiguration;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;

/**
 *
 * @author gabi
 */
public class BuildAction implements Action {
    
    String buildStatus;
    
    public String getBuildStatus()
    {
        return this.buildStatus;
    }
    
    BuildAction(AbstractBuild<?, ?> build) {
        this.buildStatus = build.getResult().color.getDescription();
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
