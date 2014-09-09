/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.projectConfiguration;

/**
 *
 * @author oreny
 */
public class MyParameterClass {

    private String paramName;
    private boolean isDefaultValue;

    public MyParameterClass(String paramName, boolean isDefaultValue) 
    {
        this.isDefaultValue = isDefaultValue;
        this.paramName = paramName;
        System.out.println(this.paramName + "value : "+ this.isDefaultValue);
    }
    
    public String getName()
    {
        System.out.println(this.paramName);
        return this.paramName;
    }

    public boolean getValue() 
    {
        System.out.println(this.isDefaultValue);
        return this.isDefaultValue;
    }

}
