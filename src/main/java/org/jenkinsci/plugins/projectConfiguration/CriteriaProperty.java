/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jenkinsci.plugins.projectConfiguration;

/**
 *
 * @author gavrielk
 */
public class CriteriaProperty
{    
    SeverityEnum severity;
    
    public CriteriaProperty()
    {
    }
    
    public CriteriaProperty(SeverityEnum severity)
    {
        this.severity = severity; 
    }
    
    public void setSevirity(SeverityEnum severity)
    {
        this.severity = severity; 
    }
    
    public SeverityEnum getSeverity()
    {
        return this.severity;
    }
    
    public enum SeverityEnum 
    {
        CRITICAL(1), ERROR(2), WARNING(3), ANY(4);
        
        private final int severityCode;
        SeverityEnum(int severityCode) { this.severityCode = severityCode; }
        public int getSeverityCode() { return this.severityCode; }
    }
}
