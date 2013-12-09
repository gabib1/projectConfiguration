/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.projectConfiguration;

import org.jenkinsci.plugins.projectConfiguration.ProjectConfiguration;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.TransientProjectActionFactory;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author gabi
 */
@Extension
public class ProjectConfigurationActionFactory extends TransientProjectActionFactory {

    @Override
    public Collection<? extends Action> createFor(AbstractProject target) {
        System.out.println("in MkverParametersActionFactory createFor()");
        ArrayList<Action> actions = new ArrayList<Action>();
        
        ProjectConfiguration newAction = new ProjectConfiguration(target);
        actions.add(newAction);
        
        return actions;
    }
    
}
