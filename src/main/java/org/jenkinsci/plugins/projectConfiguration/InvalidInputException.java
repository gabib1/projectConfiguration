/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.projectConfiguration;

/**
 *
 * @author gabi
 */
class InvalidInputException extends Exception {
    
    public InvalidInputException(String message) {
        super(message);
    }
    
}
