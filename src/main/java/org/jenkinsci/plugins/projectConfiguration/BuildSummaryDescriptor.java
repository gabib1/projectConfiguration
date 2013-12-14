/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.projectConfiguration;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;

/**
 *
 * @author gabi
 */
@Extension
public class BuildSummaryDescriptor extends BuildStepDescriptor<Publisher> {

    public BuildSummaryDescriptor()
    {
        super(BuildSummaryPublisher.class);
        load();
    }
    
    @Override
    public boolean isApplicable(Class<? extends AbstractProject> arg0)
    {
        return true;
    }

    @Override
    public String getDisplayName()
    {
        return "Display build summary";
    }
    
}
