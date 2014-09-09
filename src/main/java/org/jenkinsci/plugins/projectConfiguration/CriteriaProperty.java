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
public class CriteriaProperty extends JobProperty<AbstractProject<?, ?>> {

    private KWSeverityEnum kwSeverity;
    private boolean kwFailBuildOnCriteria;
    private boolean testsFailBuildOnCriteria;
    private boolean deploymentFailBuildOnCriteria;
    private File f_failCriteria;

    public CriteriaProperty() {
    }

    public CriteriaProperty(String failCritiriaFilePath) {
        f_failCriteria = new File(failCritiriaFilePath);
        initFromFile();
    }

    public void setKWSevirity(KWSeverityEnum kwSeverity) {
        this.kwSeverity = kwSeverity;
    }

    public KWSeverityEnum getKWSeverity() {
        return this.kwSeverity;
    }

    public void setKWCriteria(boolean isEnabled) {
        this.kwFailBuildOnCriteria = isEnabled;
    }

    public boolean getKWCriteria() {
        return this.kwFailBuildOnCriteria;
    }

    public void setTestsCriteria(boolean isEnabled) {
        this.testsFailBuildOnCriteria = isEnabled;
    }

    public boolean getTestsCriteria() {
        return this.testsFailBuildOnCriteria;
    }

    //Oren
    public void setDeploymentCriteria(boolean isEnabled) {
        this.deploymentFailBuildOnCriteria = isEnabled;
    }

    public boolean getDeploymentCriteria() {
        return this.deploymentFailBuildOnCriteria;
    }

    public void saveToFile(String failCriteriaFilePath) {
        f_failCriteria = new File(failCriteriaFilePath);
        saveToFile();
    }

    public void saveToFile() {
        List<String> newFileContent = new ArrayList<>();

        try {
            newFileContent.add("KWSeverityCode=" + this.kwSeverity.getSeverityCode());
            newFileContent.add("KWFailBuildOnCriteria=" + (this.kwFailBuildOnCriteria ? "1" : "0"));
            newFileContent.add("TestsFailBuildOnCriteria=" + (this.testsFailBuildOnCriteria ? "1" : "0"));
            newFileContent.add("DeploymentFailBuildOnCriteria=" + (this.deploymentFailBuildOnCriteria ? "1" : "0"));

            try (PrintWriter pw = new PrintWriter(this.f_failCriteria)) {
                for (String currLine : newFileContent) {
                    pw.println(currLine);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(CriteriaProperty.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void initFromFile() {
        initFromFile(this.f_failCriteria);
    }

    public void initFromFile(File f_failCriteria) {
        InputStream fis;
        BufferedReader br;
        String line;

        if (f_failCriteria.exists() == true) {
            try {
                fis = new FileInputStream(f_failCriteria);
                br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("KWSeverityCode=") == true) {
                        int indexOfEquels = line.indexOf('=') + 1;
                        this.kwSeverity = KWSeverityEnum.values()[Integer.parseInt(line.substring(indexOfEquels)) - 1];
                    }
                    if (line.startsWith("KWFailBuildOnCriteria=") == true) {
                        int indexOfEquels = line.indexOf('=') + 1;
                        String value = line.substring(indexOfEquels);
                        if (value.equals("1") == true) {
                            this.kwFailBuildOnCriteria = true;
                        } else {
                            this.kwFailBuildOnCriteria = false;
                        }
                    }
                    // Oren
                    if (line.startsWith("DeploymentFailBuildOnCriteria=") == true) {
                        int indexOfEquels = line.indexOf('=') + 1;
                        String value = line.substring(indexOfEquels);
                        if (value.equals("1") == true) {
                            this.deploymentFailBuildOnCriteria = true;
                        } else {
                            this.deploymentFailBuildOnCriteria = false;
                        }
                    }
                    if (line.startsWith("TestsFailBuildOnCriteria=") == true) {
                        int indexOfEquels = line.indexOf('=') + 1;
                        String value = line.substring(indexOfEquels);
                        if (value.equals("1") == true) {
                            this.testsFailBuildOnCriteria = true;
                        } else {
                            this.testsFailBuildOnCriteria = false;
                        }
                    }
                }
            } catch (IOException | NumberFormatException ex) {
                System.out.println(ex.getMessage());
            }
        } else {
            kwSeverity = KWSeverityEnum.ERROR;
            kwFailBuildOnCriteria = false;
            testsFailBuildOnCriteria = false;
            deploymentFailBuildOnCriteria = false;
        }
    }

    public boolean isKWCriticalSet() {
        return KWSeverityEnum.CRITICAL.getSeverityCode() <= this.kwSeverity.getSeverityCode();
    }

    public boolean isKWErrorSet() {
        return KWSeverityEnum.ERROR.getSeverityCode() <= this.kwSeverity.getSeverityCode();
    }

    public boolean isKWAnySet() {
        return KWSeverityEnum.ANY.getSeverityCode() <= this.kwSeverity.getSeverityCode();
    }

    public enum KWSeverityEnum {

        CRITICAL(1), ERROR(2), Warning(3), ANY(4);

        private final int severityCode;

        KWSeverityEnum(int severityCode) {
            this.severityCode = severityCode;
        }

        public int getSeverityCode() {
            return this.severityCode;
        }
    }

    public static final JobPropertyDescriptor DESCRIPTOR = new Descriptor();

    @Override
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
