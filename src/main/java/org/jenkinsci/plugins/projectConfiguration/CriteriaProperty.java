/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jenkinsci.plugins.projectConfiguration;

import hudson.model.AbstractProject;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gavrielk
 */
public class CriteriaProperty extends JobProperty<AbstractProject<?,?>>
{    
    private KWSeverityEnum kwSeverity;
    private boolean kwFailBuildOnCriteria;
    
    private File f_failCriteria;
    
    public CriteriaProperty()
    {
    }
    
    public CriteriaProperty(String failCritiriaFilePath)
    {
        f_failCriteria = new File(failCritiriaFilePath);
        
        initFromFile();
    }
    
    public void setKWSevirity(KWSeverityEnum kwSeverity)
    {
        this.kwSeverity = kwSeverity;
    }
    
    public KWSeverityEnum getKWSeverity()
    {
        return this.kwSeverity;
    }
    
    public void setKWCriteria(boolean isEnabled)
    {
        this.kwFailBuildOnCriteria = isEnabled;
    }
    
    public boolean setKWCriteria()
    {
        return this.kwFailBuildOnCriteria;
    }
    
    public void saveToFile(String failCriteriaFilePath)
    {
        f_failCriteria = new File(failCriteriaFilePath);
        saveToFile();
    }

    public void saveToFile()
    {
        List<String> newFileContent = new ArrayList<>();
        
        InputStream fis = null;
        try {
            if (f_failCriteria.exists() == true)
            {
                String line;
                fis = new FileInputStream(this.f_failCriteria);
                try (BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")))) {
                    while ((line = br.readLine()) != null)
                    {
                        if(line.startsWith("KWSeverityCode=") == true)
                        {
                            line = "KWSeverityCode=" + this.kwSeverity.getSeverityCode();
                        }
                        if(line.startsWith("KWFailBuildOnCriteria=") == true)
                        {
                            line = "KWFailBuildOnCriteria=" + (this.kwFailBuildOnCriteria ? "1" : "0");
                        }
                        newFileContent.add(line);
                    }
                }
            }
            else
            {
                newFileContent.add("KWSeverityCode=" + this.kwSeverity.getSeverityCode());
                newFileContent.add("KWFailBuildOnCriteria=" + (this.kwFailBuildOnCriteria ? "1" : "0"));
            }
            try (PrintWriter pw = new PrintWriter(this.f_failCriteria)) {
                for (String currLine : newFileContent)
                {
                    pw.println(currLine);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(CriteriaProperty.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fis.close();
            } catch (IOException | NullPointerException ex) {
                Logger.getLogger(CriteriaProperty.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void initFromFile()
    {
        initFromFile(this.f_failCriteria);
    }
    
    public void initFromFile(File f_failCriteria) 
    {
        InputStream fis;
        BufferedReader br;
        String line;

        try 
        {
            fis = new FileInputStream(f_failCriteria);
            br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
            while ((line = br.readLine()) != null) 
            {
                if(line.startsWith("KWSeverityCode=") == true)
                {
                    int indexOfEquels = line.indexOf('=') + 1;
                    this.kwSeverity = KWSeverityEnum.values()[Integer.parseInt(line.substring(indexOfEquels)) - 1];
                }
//                if(line.startsWith("TestsSeverityCode=") == true)
//                {
//                    int indexOfEquels = line.indexOf('=') + 1;
//                    this.testsSeverity = TestsSeverityEnum.valueOf(line.substring(indexOfEquels));
//                }
            }
        }catch(IOException | NumberFormatException ex){
            System.out.println(ex.getMessage());
        }
    }
    
//    public boolean isKWCriteriaSet()
//    {
//        this.owner
//    }

    public boolean isKWCriticalSet()
    {
        return KWSeverityEnum.CRITICAL.getSeverityCode() <= this.kwSeverity.getSeverityCode();
    }

    public boolean isKWErrorSet()
    {
        return KWSeverityEnum.ERROR.getSeverityCode() <= this.kwSeverity.getSeverityCode();
    }

    public boolean isKWAnySet()
    {
        return KWSeverityEnum.ANY.getSeverityCode() <= this.kwSeverity.getSeverityCode();
    }
    
    public enum KWSeverityEnum 
    {
        CRITICAL(1), ERROR(2), Warning(3), ANY(4);
        
        private final int severityCode;
        KWSeverityEnum(int severityCode) { this.severityCode = severityCode; }
        public int getSeverityCode() { return this.severityCode; }
    }
    
    public static final JobPropertyDescriptor DESCRIPTOR = new Descriptor(); 

    public JobPropertyDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

    private static class Descriptor extends JobPropertyDescriptor {

        public Descriptor() {
            super(CriteriaProperty.class);
        }

        @Override
        public String getDisplayName() {
            return "CriteriaProperty";
        }
    }

    
}