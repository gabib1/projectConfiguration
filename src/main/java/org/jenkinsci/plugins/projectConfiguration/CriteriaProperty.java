/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jenkinsci.plugins.projectConfiguration;

import java.io.File;

/**
 *
 * @author gavrielk
 */
public class CriteriaProperty
{    
    private KWSeverityEnum kwSeverity;
    private File f_failCriteria;
    
    
    public CriteriaProperty()
    {
    }
    
//    public CriteriaProperty(KWSeverityEnum severity)
//    {
//        this.kwSeverity = severity; 
//    }
    
    public CriteriaProperty(String failCritiriaFilePath)
    {
        f_failCriteria = new File(failCritiriaFilePath);
//        initFromFile(File)
    }
    
    public void setKWSevirity(KWSeverityEnum kwSeverity)
    {
        this.kwSeverity = kwSeverity;
    }
    
    public KWSeverityEnum getKWSeverity()
    {
        return this.kwSeverity;
    }
    
    public void saveToFile(File failCriteriaFail)
    {
        
    }
    
    public enum KWSeverityEnum 
    {
        CRITICAL(1), ERROR(2), WARNING(3), ANY(4);
        
        private final int severityCode;
        KWSeverityEnum(int severityCode) { this.severityCode = severityCode; }
        public int getSeverityCode() { return this.severityCode; }
    }
}
