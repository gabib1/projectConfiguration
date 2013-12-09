/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.projectConfiguration;

import hudson.model.AbstractProject;
import hudson.model.ChoiceParameterDefinition;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.ProminentProjectAction;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author gabi
 */
public class mkverParamatersAction{ // implements ProminentProjectAction{
//    private final AbstractProject<?, ?> project;
//    
//    mkverParamatersAction(AbstractProject<?, ?> project) {
//        System.out.println("in mkverParametersAction c'tor");
//        this.project = project;
////        ParametersDefinitionProperty parametersDefinitionProperty = new ParametersDefinitionProperty();
////        this.project.addProperty(parametersDefinitionProperty);
//        ParametersDefinitionProperty parametersDefinitionProperty = this.project.getProperty(ParametersDefinitionProperty.class);
//        if (parametersDefinitionProperty != null){
//            if (parametersDefinitionProperty == null){
//                parametersDefinitionProperty = new ParametersDefinitionProperty();
//                try {
//                    this.project.addProperty(parametersDefinitionProperty);
//                } catch (IOException ex) {
//                    System.out.println("Problem adding new empty property");
//                }
//            }
//            List<ParameterDefinition> projectParams = parametersDefinitionProperty.getParameterDefinitions();
//            System.out.println(projectParams.size());
//            String[] buildTypes = new String[3];
//            buildTypes[0] = "build";
//            buildTypes[1] = "rebuild";
//            buildTypes[2] = "clean";
//            ChoiceParameterDefinition buildType = new ChoiceParameterDefinition("Build_type", buildTypes, "Build_type");
//            System.out.println(buildType.getChoices().toString());
//            if (projectParams.contains(buildType) == false){// not good - always false! need to check by name
//                projectParams.add(buildType);
//                try {
//                    this.project.save();
//                } catch (IOException ex) {
//                        System.out.println("Error saving the project");
//                }
//            }
//        }
//    }
//
//    public String getIconFileName() {
//        return null;
//    }
//
//    public String getDisplayName() {
//        return null;
//    }
//
//    public String getUrlName() {
//        return null;
//    }
    
}
