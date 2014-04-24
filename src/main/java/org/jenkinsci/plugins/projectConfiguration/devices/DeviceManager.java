/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jenkinsci.plugins.projectConfiguration.devices;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.jenkinsci.plugins.projectConfiguration.ProjectConfiguration;
import org.jenkinsci.plugins.projectConfiguration.devices.Unit.WatchFiles;

/**
 * 
 * @author gavrielk
 * Generate Unit.java and ObjectFactory using unit.xml.
 * First transform it to unit_1.xsd file using some online Generator and then run
 * $xjc -nv unit_1.xsd -p org.jenkinsci.plugins.projectConfiguration.devices -d ./
 * $xjc -nv unit_1.xsd -p org.jenkinsci.plugins.projectConfiguration.devices -d ./
 */
public class DeviceManager {
    
    JAXBContext jaxbContext;
    
    String unitsDirPath = "";
    ArrayList<Unit> deviceList = new ArrayList<>();
    
    public DeviceManager(String unitsDirPath) throws JAXBException
    {
        this.unitsDirPath = unitsDirPath;
        ClassLoader cl = org.jenkinsci.plugins.projectConfiguration.devices.ObjectFactory.class.getClassLoader();
        this.jaxbContext = JAXBContext.newInstance("org.jenkinsci.plugins.projectConfiguration.devices", cl);
    }

    
    public static ArrayList<String>getFieldsNames()
    {
        ArrayList<String> fieldsArr = new ArrayList<String>();
        for (Field f : Unit.class.getDeclaredFields())
        {
            fieldsArr.add(f.getName());
        }
        return fieldsArr;
    }

    public void addNewDevice(Map<String, String[]> map) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, JAXBException 
    {
        Unit u = new Unit();
        Marshaller marshaller = this.jaxbContext.createMarshaller();
        int deviceIndex = -1;                   // -1 means it is a new device that needs to be added at the end of the list
        
        Iterator keySetIt = map.keySet().iterator();
        Object key;
        String[] value;
        while (keySetIt.hasNext() == true)
        {
            key = keySetIt.next().toString();
            value = (String[])map.get(key);
            if (key.equals("watchFiles") == true)
            {
                WatchFiles watchFiles = new WatchFiles();
                watchFiles.watchFile = new ArrayList<String>();
                watchFiles.watchFile.addAll(Arrays.asList(value));
                u.setWatchFiles(watchFiles);
            }
            else if (key.equals("deviceID") == true)
            {
                try{
                    deviceIndex = Integer.parseInt(value[0]);
                }catch(NumberFormatException ex){
                    System.out.println("Failed to parse " + value[0]);
                    Logger.getLogger(ProjectConfiguration.class.getName()).log(Level.SEVERE, null, ex);
                    deviceIndex = -1;
                }
            }
            else if(key.equals("slots") == true)
            {
                System.out.println("slots");
            }
            else
            {
                System.out.println("key: " + key.toString() + ", value: " + value[0]);
                for (Method method : u.getClass().getDeclaredMethods())
                {
                    System.out.println("Method: " + method.getName().toLowerCase());
                    System.out.println("Key: set" + key.toString().toLowerCase());
                    if (method.getName().toLowerCase().matches("set" + key.toString().toLowerCase()))
                    {
                        Class<?> parameter = method.getParameterTypes()[0];
                        System.out.println("Invoking " + method.getName() + " with a " + parameter.getName());
                        if (value[0] == null || value[0].equals(""))
                        {
                            method.invoke(u, "");
                        }
                        else if (parameter.equals(int.class))
                        {
                            method.invoke(u, Integer.parseInt(value[0]));
                        }
                        else if (parameter.equals(String.class))
                        {
                            method.invoke(u, value[0]);
                        }
                        else
                        {
                            throw new IllegalArgumentException("Key is not slots or watchFiles and it is not a string or a an int");
                        }
                        break;
                    }
                }
            }
        }
        if (deviceIndex != -1)
        {
            this.deviceList.set(deviceIndex, u);
        }
        else
        {
            deviceIndex = this.deviceList.size();
            this.deviceList.add(u);
        }
        marshaller.marshal(u, new File(unitsDirPath + "/unit_" + deviceIndex + ".xml"));
    }
    
    public void removeDevice(int index) throws IndexOutOfBoundsException
    {
        this.deviceList.remove(index);
        new File(unitsDirPath + "/unit_" + index + ".xml").delete();
        orderFiles();
    }
    
    public ArrayList<Unit> getDeviceArr() throws JAXBException
    {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setEventHandler(new UnitValidationEventHandler());
        File unitsDir = new File(this.unitsDirPath);
        // Filter to get only file that match the pattern "unit_<unit_number>.xml
        File[] unitsFiles = unitsDir.listFiles(new UnitFileFilter());
        
        if (unitsFiles != null)
        {
            this.deviceList = new ArrayList<Unit>();
            for (File unit : unitsFiles)
            {
                System.out.println("Parsing " + unit.getName());
                Source source = new StreamSource(unit);
                JAXBElement<Unit> unitElement = (JAXBElement<Unit>) unmarshaller.unmarshal(source, Unit.class);
                this.deviceList.add(unitElement.getValue());
            }
        }
        else
        {
            System.out.println("No unit files found under " + unitsDirPath);
        }
        return this.deviceList;
    }
    
    public int getNumberOfDevices()
    {
        return this.deviceList.size();
    }

    // Order the files in squential order unit_1.xml, unti_2.xml...
    // if there is a gap rename the files so there won't be a gap
    private void orderFiles() {
        File unitFilesdir = new File(this.unitsDirPath);
        ArrayList<File> sortedFilesList = sortFilesByName(new ArrayList<File>(Arrays.asList(unitFilesdir.listFiles(new UnitFileFilter()))));
        for (int i = 0; i < sortedFilesList.size(); i++)
        {
            String currentFileName = sortedFilesList.get(i).getName();
            int currentUnitFileNumber = Integer.parseInt(currentFileName.substring(currentFileName.indexOf('_') + 1, currentFileName.indexOf('.')));
            int diff = currentUnitFileNumber - i;
            if (diff > 0)
            {
                sortedFilesList.get(i).renameTo(new File(this.unitsDirPath + "/unit_" + i + ".xml"));
            }
            
        }
    }

    private ArrayList<File> sortFilesByName(ArrayList<File> filesList) {
        Collections.sort(filesList, new Comparator<File>() {
            @Override
            public int compare(File file1, File file2) {
                return file1.getName().compareTo(file2.getName());
            }
        });
        return filesList;
    }
    
    public class UnitValidationEventHandler implements ValidationEventHandler
    {
        @Override
        public boolean handleEvent(ValidationEvent ve) {
            if (ve.getSeverity() == ValidationEvent.FATAL_ERROR
                    || ve.getSeverity() == ValidationEvent.ERROR) {
                ValidationEventLocator locator = ve.getLocator();
                //Print message from valdation event
                System.out.println("Invalid booking document: "
                        + locator.getURL());
                System.out.println("Error: " + ve.getMessage());
                //Output line and column number
                System.out.println("Error at column "
                        + locator.getColumnNumber()
                        + ", line "
                        + locator.getLineNumber());
            }
            return true;
        }
    }
    
    private class UnitFileFilter implements FileFilter
    {
        @Override
        public boolean accept(File pathname) 
        {
            return pathname.isFile() == true && pathname.getName().matches("unit_.*\\.xml");
        }
    }
    
}
