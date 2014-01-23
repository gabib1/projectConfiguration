/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.projectConfiguration.exceptions;

/**
 *
 * @author gabi
 * This type of errors are non user dependent errors which occur during internal process.
 * For example reading some configuration file or inracting with the scripts
 */
public class InternalException extends Exception{
    
    public InternalException(String message) {
        super(message);
    }
    
}
