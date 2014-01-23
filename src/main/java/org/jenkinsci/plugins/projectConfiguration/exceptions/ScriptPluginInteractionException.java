/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.projectConfiguration.exceptions;

/**
 *
 * @author gabi
 * Error while reading environment parameters which were declared in the scripts
 */
public class ScriptPluginInteractionException extends InternalException {
    
    public ScriptPluginInteractionException(String message) {
        super(message);
    }
    
}
