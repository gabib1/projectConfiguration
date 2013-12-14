/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.projectConfiguration;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 *
 * @author gabi
 */
@SuppressWarnings("unchecked")
public class BuildSummaryPublisher extends Recorder {
    
    String name;
    
    @DataBoundConstructor
    public BuildSummaryPublisher(final String name) {
        this.name = name;
    }
    
    @Override
    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher,
                           final BuildListener listener) {
        build.addAction(new BuildAction(build));
        return true;
    }

    
    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }
    
}
