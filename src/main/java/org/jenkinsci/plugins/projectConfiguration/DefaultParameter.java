package org.jenkinsci.plugins.projectConfiguration;

import hudson.model.BooleanParameterDefinition;

import hudson.Launcher;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import hudson.tasks.Builder;
import org.kohsuke.stapler.StaplerRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked and a new
 * {@link RPM_Manager} is created. The created instance is persisted to the
 * project configuration XML by using XStream, so this allows you to use
 * instance fields (like {@link #name}) to remember the configuration.
 *
 * <p>
 * When a build is performed, the
 * {@link #perform(AbstractBuild, Launcher, BuildListener)} method will be
 * invoked.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class DefaultParameter {

    /**
     * this is the Jenkins project for example " Genesis-3.1" and holds all the
     * info of it
     *
     */
    private AbstractProject<?, ?> project;

    public DefaultParameter() {
    }

    public DefaultParameter(AbstractProject<?, ?> project) {
        this.project = project;
    }

    /**
     * This method go through all the parameters and return only the boolean
     * ones.
     *
     * @return - list of boolean parameters
     * @throws IOException
     */
    public List<BooleanParameterDefinition> getParameters() throws IOException {

        //TODO
        List<ParameterDefinition> definitions;
        List<BooleanParameterDefinition> booleanParameterList;
        ParametersDefinitionProperty property;

        definitions = new ArrayList<ParameterDefinition>();
        booleanParameterList = new ArrayList<BooleanParameterDefinition>();

        property = this.project.getProperty(ParametersDefinitionProperty.class);

        if (property != null && property.getParameterDefinitions() != null) {
            definitions = property.getParameterDefinitions();
        }

        for (ParameterDefinition parameterDefinition : definitions) {
            if (parameterDefinition instanceof BooleanParameterDefinition) {
                booleanParameterList.add((BooleanParameterDefinition) parameterDefinition);
            }
        }

        return booleanParameterList;
    }

    /**
     * returns the parameters as a name list only the ones with true value
     *
     * @return
     * @throws IOException
     */
    public ArrayList<String> getParametersInList() throws IOException 
    {
        ArrayList<String> namesList = new ArrayList<>();
        for (BooleanParameterDefinition parameter : this.getParameters()) 
        {
            if (parameter.isDefaultValue()) 
            {
                namesList.add(parameter.getName());
            }
        }

        for (String parameter : namesList) 
        {
            System.out.println("namesList#####" + namesList);
        }

        return namesList;
    }

    /**
     * This method gets the req and creates new definition (parameter) list with
     * the given values
     *
     * @param req - the parameters that came from the request
     * @throws ServletException
     * @throws IOException
     */
    public void editDefaultParamsValues(StaplerRequest req)
            throws ServletException, IOException {

        List<ParameterDefinition> NewBooleanParameterValueList;
        List<ParameterDefinition> definitions = new ArrayList<>();
        List<BooleanParameterDefinition> BooleanParameterList;
        ParametersDefinitionProperty property;
        List<ParameterDefinition> nonBooleanProperty;
        List<ParameterDefinition> listToAdd = new ArrayList<>();

        NewBooleanParameterValueList = new ArrayList<ParameterDefinition>();
        definitions = new ArrayList<>();
        BooleanParameterList = getParameters();
        property = this.project.getProperty(ParametersDefinitionProperty.class);

        //for each parameter will check if the default value changed or not
        //then will create a new parameter list and change to the new one
        for (BooleanParameterDefinition parameter : BooleanParameterList) {
            ParameterDefinition NewBooleanParameterDefinition;
            String parameterName = parameter.getName();

            //then the paramter should be default
            if (parameterName.equals(req.getParameter(parameter.getName()))) {
                NewBooleanParameterDefinition
                        = new BooleanParameterDefinition(parameterName, true, "");
            } else {
                NewBooleanParameterDefinition
                        = new BooleanParameterDefinition(parameterName, false, "");
            }

            System.out.println("NewBooleanParameterDefinition:" + NewBooleanParameterDefinition.getName());
            NewBooleanParameterValueList.add(NewBooleanParameterDefinition);
        }

        //after creating a new definition list will delete the old one and 
        //insert the new one than the save option save it to jenkins data base.
        nonBooleanProperty = getNonBooleanDefinitions();
        System.out.println("creating a new list");

        for (ParameterDefinition parameter : nonBooleanProperty) {
            listToAdd.add(parameter);
            System.out.println(parameter.getName());
        }
        for (ParameterDefinition parameter : NewBooleanParameterValueList) {
            listToAdd.add(parameter);
            System.out.println(parameter.getName());

        }

        this.project.removeProperty(property);
        this.project.addProperty(new ParametersDefinitionProperty(listToAdd));
        this.project.save();
    }

    /**
     * return a property of all the non boolean parameters in this project
     *
     * @return
     */
    public List<ParameterDefinition> getNonBooleanDefinitions() {

        List<ParameterDefinition> definitions;
        ParametersDefinitionProperty property;
        List<ParameterDefinition> nonBooleanDefinitions;
        ParametersDefinitionProperty nonBooleanProperty;

        property = this.project.getProperty(ParametersDefinitionProperty.class);
        definitions = null;
        nonBooleanDefinitions = new ArrayList<>();

        if (property != null && property.getParameterDefinitions() != null) 
        {
            definitions = property.getParameterDefinitions();
        }

        for (ParameterDefinition parameterDefinition : definitions) 
        {
            // add only the non boolean parameters
            if (!(parameterDefinition instanceof BooleanParameterDefinition)) 
            {
                nonBooleanDefinitions.add(parameterDefinition);
                System.out.println(parameterDefinition.getName());
            }
        }

        nonBooleanProperty = new ParametersDefinitionProperty(nonBooleanDefinitions);
        return nonBooleanDefinitions;

    }
}
