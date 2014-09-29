/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.projectConfiguration.devices;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.projectConfiguration.ProjectConfiguration;
import org.jenkinsci.plugins.projectConfiguration.devices.Unit.Slots;
import org.jenkinsci.plugins.projectConfiguration.devices.Unit.Slots.Slot;
import org.jenkinsci.plugins.projectConfiguration.devices.Unit.Slots.Slot.SlotDesc;
import org.jenkinsci.plugins.projectConfiguration.devices.Unit.Slots.Slot.SlotDesc.Links;
import org.jenkinsci.plugins.projectConfiguration.devices.Unit.Slots.Slot.SlotDesc.Links.Link;
import org.jenkinsci.plugins.projectConfiguration.devices.Unit.WatchFiles;

/**
 *
 * @author gavrielk Generate Unit.java and ObjectFactory using unit.xml. First
 * transform it to unit_1.xsd file using some online Generator and then run $xjc
 * -nv unit_1.xsd -p org.jenkinsci.plugins.projectConfiguration.devices -d ./
 * $xjc -nv unit_1.xsd -p org.jenkinsci.plugins.projectConfiguration.devices -d
 * ./
 */
public class DeviceManager
{

    JAXBContext jaxbContext;

    String unitsDirPath = "";
    String devicesdToRunTestOnPath;
    String devicesdToRunDeployOnPath;
    ArrayList<Unit> deviceList = new ArrayList<>();

    public DeviceManager(String unitsDirPath) throws JAXBException
    {
        this.unitsDirPath = unitsDirPath;
        this.devicesdToRunTestOnPath = unitsDirPath + "/DevicesThatShouldRunTest";
        this.devicesdToRunDeployOnPath = unitsDirPath + "/DevicesThatShouldRunDeploy";
        ClassLoader cl = org.jenkinsci.plugins.projectConfiguration.devices.ObjectFactory.class.getClassLoader();
        this.jaxbContext = JAXBContext.newInstance("org.jenkinsci.plugins.projectConfiguration.devices", cl);
    }

    public static ArrayList<String> getFieldsNames()
    {
        ArrayList<String> fieldsArr = new ArrayList<String>();
        for (Field f : Unit.class.getDeclaredFields())
        {
            fieldsArr.add(f.getName());
        }
        return fieldsArr;
    }

    public void addNewDevice(Map<String, String[]> map) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, JAXBException, IOException
    {
        System.out.println("*******************************************");
        System.out.println("        *************************");
        System.out.println("              **********");

        System.out.println("In addNewDevice");
        System.out.println("this.deviceList.size()  :     " + this.deviceList.size());

        deviceList.clear();
        getDeviceArr();
        System.out.println("");

        System.out.println("this.deviceList.size()  :     " + this.deviceList.size());
        System.out.println("");
        System.out.println("");
        Unit u = new Unit();
        Marshaller marshaller = this.jaxbContext.createMarshaller();
        ObjectFactory objectFatory = new ObjectFactory();
        int deviceIndex = -1;                   // -1 means it is a new device that needs to be added at the end of the list

        Iterator keySetIt = map.keySet().iterator();
        Object key;
        String[] value;
        WatchFiles watchFiles = new WatchFiles();
        while (keySetIt.hasNext() == true)
        {
            key = keySetIt.next().toString();
            value = (String[]) map.get(key);
            if (key.equals("watchFiles") == true)
            {
                watchFiles.watchFile = new ArrayList<String>();
                watchFiles.watchFile.addAll(Arrays.asList(value));
            } else if (key.equals("deviceID") == true)
            {
                try
                {
                    deviceIndex = Integer.parseInt(value[0]);
                } catch (NumberFormatException ex)
                {
                    System.out.println("Failed to parse " + value[0]);
                    Logger.getLogger(ProjectConfiguration.class.getName()).log(Level.SEVERE, null, ex);
                    deviceIndex = -1;
                }
            } else
            {
                //  System.out.println("key: " + key.toString() + ", value: " + value[0]);
                for (Method method : u.getClass().getDeclaredMethods())
                {
                    // System.out.println("Method: " + method.getName().toLowerCase());
                    // System.out.println("Key: set" + key.toString().toLowerCase());
                    if (method.getName().toLowerCase().matches("set" + key.toString().toLowerCase()))
                    {
                        Class<?> parameter = method.getParameterTypes()[0];
                        //               System.out.println("Invoking " + method.getName() + " with a " + parameter.getName());
                        if (value[0] == null || value[0].equals(""))
                        {
                            method.invoke(u, "");
                        } else if (parameter.equals(int.class))
                        {
                            method.invoke(u, Integer.parseInt(value[0]));
                        } else if (parameter.equals(String.class))
                        {
                            method.invoke(u, value[0]);
                        } else
                        {
                            throw new IllegalArgumentException("Key is not slots or watchFiles and it is not a string or a an int");
                        }
                        break;
                    }
                }
            }
        }

        u.setSlots(objectFatory.createUnitSlots());
        u.setWatchFiles(watchFiles);
        System.out.println("*******************************************************");
        System.out.println("");
        System.out.println("deviceIndex :        " + deviceIndex);
        System.out.println("this.deviceList.size()  :     " + this.deviceList.size());

        System.out.println("");
        System.out.println("*******************************************************");

        if ((deviceIndex != -1) && (deviceIndex < this.deviceList.size()))
        {
            System.out.println(" in if ");
            this.deviceList.set(deviceIndex, u);
        } else
        {
            System.out.println(" in else ");
            deviceIndex = this.deviceList.size();
            this.deviceList.add(u);
        }

        String fileName = "/unit_" + deviceIndex + ".xml";
        marshaller.marshal(u, new File(unitsDirPath + "/unit_" + deviceIndex + ".xml"));
        checkifCopyOrDeleteNeeded(Arrays.toString(map.get("test")), devicesdToRunTestOnPath, fileName);
        checkifCopyOrDeleteNeeded(Arrays.toString(map.get("deploy")), devicesdToRunDeployOnPath, fileName);

    }

    /**
     * checks if we need to copy/delete the device from given folder and do so
     * accordingly
     *
     *
     * @param value - the value is : true or null
     * @param path - The path to folder we want to check with
     * @param filename - the file name (for example "unit-0")
     */
    public void checkifCopyOrDeleteNeeded(String value, String path, String filename)
    {
        System.out.println("");
        System.out.println("in checkifCopyNeeded");
        System.out.println("value = " + value);
        System.out.println("path = " + path);
        System.out.println("filename :   " + filename);
        String fileTocopyPath = unitsDirPath + filename;
        String destination = path + filename;

        System.out.println("file To copy Path     " + fileTocopyPath);
        System.out.println("destination   " + destination);
        System.out.println("");

        if (value.equals("[true]") || value.equals("true"))
        {
            System.out.println("In true");
            try
            {
                FileUtils.copyFile(new File(fileTocopyPath),
                        new File(destination));
            } catch (IOException ex)
            {
                Logger.getLogger(DeviceManager.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else
        {
            File file = new File(destination);
            if (file.exists())
            {
                file.delete();
            }
        }
    }

    public void removeDevice(int index) throws IndexOutOfBoundsException
    {
        this.deviceList.remove(index);
        String fileInDevices = unitsDirPath + "/unit_" + index + ".xml";
        String fileInTestToRunFolder = devicesdToRunTestOnPath + "/unit_" + index + ".xml";
        String fileInDeployToRunFolder = devicesdToRunDeployOnPath + "/unit_" + index + ".xml";
        // inthe devices folder
        new File(fileInDevices).delete();
        new File(fileInTestToRunFolder).delete();
        new File(fileInDeployToRunFolder).delete();
        orderFiles();

    }

    public ArrayList<Unit> getDeviceArr() throws JAXBException, IOException
    {
        //since i were adding a new functiniallty will need to adjust the old 
        //data base to the new one, this function will do so, Oren
        try
        {
            validateTestAndDeployFolders();
        } catch (IOException ex)
        {
            System.err.println("Could not validate the test and deploy folders");
            Logger.getLogger(DeviceManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Oren Until here

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setEventHandler(new UnitValidationEventHandler());
        File unitsDir = new File(this.unitsDirPath);
        if (unitsDir.exists() == false)
        {
            boolean success = unitsDir.mkdir();
            if (success == true)
            {
                throw new IOException("[ERROR] Devices directory doesn't exist and cannot be created under " + unitsDir.getAbsolutePath());
            }
        }
        // Filter to get only file that match the pattern "unit_<unit_number>.xml
        ArrayList<File> unitsFiles = new ArrayList(Arrays.asList(unitsDir.listFiles(new UnitFileFilter())));

        if (unitsFiles.isEmpty() == false)
        {
            this.deviceList = new ArrayList<Unit>();
            unitsFiles = sortFilesByName(unitsFiles);
            for (File unit : unitsFiles)
            {
                System.out.println("Parsing " + unit.getName());
                Source source = new StreamSource(unit);
                JAXBElement<Unit> unitElement = (JAXBElement<Unit>) unmarshaller.unmarshal(source, Unit.class);
                this.deviceList.add(unitElement.getValue());
            }
        } else
        {
            System.out.println("No unit files found under " + unitsDirPath);
        }
        return this.deviceList;
    }

    public int getNumberOfDevices()
    {
        return this.deviceList.size();
    }

    public void addSlot(int deviceID, int slotID, String cardName, String[] linkArr) throws JAXBException
    {
        Marshaller marshaller = this.jaxbContext.createMarshaller();
        ObjectFactory objectFactory = new ObjectFactory();
        Unit u = this.deviceList.get(deviceID);
        boolean slotExists = false;

        if (u.getSlots().getSlot().isEmpty() == false)
        {
            for (Slot slot : u.getSlots().getSlot())
            {
                if (Integer.valueOf(slot.id) == slotID)
                {
                    slot.setSlotDesc(setSlotDesc(objectFactory, cardName, linkArr));
                    slotExists = true;
                }
            }
        }
        if (slotExists == false)
        {
            Slots slotsObject = u.getSlots();
            Slot slot = objectFactory.createUnitSlotsSlot();
            slot.setId(String.valueOf(slotID));

            slot.setSlotDesc(setSlotDesc(objectFactory, cardName, linkArr));

            if (slotsObject.getSlot().add(slot) == true)
            {
                int listSize = slotsObject.getSlot().size();
                System.out.println("List length: " + listSize);
                if (listSize == 0)
                {
                    System.out.println("Failed to add new slot to slotList");
                } else
                {
                    System.out.println("Marshalling device #" + deviceID + " with slotID=" + slotsObject.getSlot().get(0).getId());
                }
            } else
            {
                System.out.println("Failed to add new slot to slotList");
            }
        }

        marshaller.marshal(u, new File(unitsDirPath + "/unit_" + deviceID + ".xml"));
    }

    public void removeSlot(int deviceID, int slotID) throws JAXBException
    {
        Marshaller marshaller = this.jaxbContext.createMarshaller();
//        ObjectFactory objectFactory = new ObjectFactory();
        Unit u = this.deviceList.get(deviceID);
        List<Slot> slotsList = u.getSlots().getSlot();
//        if (slotID < slotsList.size())
//        {
        System.out.println("Removing slotID #" + slotID + " from deviceID #" + deviceID);
        Slot slotToRemove = null;
        for (Slot slot : slotsList)
        {
            if (StringUtils.isBlank(slot.getId()) == false && Integer.parseInt(slot.getId()) == slotID)
            {
                slotToRemove = slot;
            }
        }
        if (slotToRemove != null)
        {
            slotsList.remove(slotToRemove);
        }

        marshaller.marshal(u, new File(unitsDirPath + "/unit_" + deviceID + ".xml"));
    }

    /**
     *
     * Order the files in sequential order unit_1.xml, unti_2.xml... if there is
     * a gap rename the files so there won't be a gap
     *
     * Doing so will maintaining the files in the test and deploy folder
     * matching.
     *
     */
    private void orderFiles()
    {
        File unitFilesdir = new File(this.unitsDirPath);
        ArrayList<File> sortedFilesListForDevice = sortFilesByName(new ArrayList<File>(Arrays.asList(unitFilesdir.listFiles(new UnitFileFilter()))));

        for (int i = 0; i < sortedFilesListForDevice.size(); i++)
        {
            String currentFileName = sortedFilesListForDevice.get(i).getName();
            int currentUnitFileNumber = Integer.parseInt(currentFileName.substring(currentFileName.indexOf('_') + 1, currentFileName.indexOf('.')));
            int diff = currentUnitFileNumber - i;
            if (diff > 0)
            {
                sortedFilesListForDevice.get(i).renameTo(new File(this.unitsDirPath + "/unit_" + i + ".xml"));
            }
            if (this.isDeviceOnTestList(currentFileName))
            {
                new File(this.devicesdToRunTestOnPath + "/" + currentFileName).renameTo(new File(this.devicesdToRunTestOnPath + "/unit_" + i + ".xml"));
            }
            if (this.isDeviceOnDeployList(currentFileName))
            {
                new File(this.devicesdToRunDeployOnPath + "/" + currentFileName).renameTo(new File(this.devicesdToRunDeployOnPath + "/unit_" + i + ".xml"));
            }
        }
    }

    private ArrayList<File> sortFilesByName(ArrayList<File> filesList)
    {
        Collections.sort(filesList, new Comparator<File>()
        {
            @Override
            public int compare(File file1, File file2)
            {
                // Getting the number of each unit e.g. unit_1.xml we should get file1Id=1
                int file1Id = Integer.parseInt(file1.getName().substring(file1.getName().indexOf('_') + 1, file1.getName().indexOf('.')));
                int file2Id = Integer.parseInt(file2.getName().substring(file2.getName().indexOf('_') + 1, file2.getName().indexOf('.')));
                return file1Id - file2Id;
//                return file1.getName().compareTo(file2.getName());
            }
        });
        return filesList;
    }

    private ArrayList<Link> stringToLinks(ObjectFactory objectFactory, String[] linkStrArr)
    {
        ArrayList<Link> linkList = new ArrayList<Link>();
        for (int i = 0; i < linkStrArr.length; i++)
        {
            if (linkStrArr[i] != null)
            {
                Link link = objectFactory.createUnitSlotsSlotSlotDescLinksLink();
                link.setId(String.valueOf(i + 1));
                link.setType(linkStrArr[i]);
                linkList.add(link);
            }
        }
        return linkList;
    }

    public String getSlotCardName(int deviceId, int slotId)
    {
        if (deviceId > this.deviceList.size() - 1)
        {
            System.out.println("[ERROR] device ID(" + deviceId + ") is out of bounds");
        } else
        {
            for (Slot slot : this.deviceList.get(deviceId).getSlots().getSlot())
            {
                if (Integer.valueOf(slot.id) == slotId)
                {
                    return slot.getSlotDesc().getCard();
                }
            }
            System.out.println("Slot #" + slotId + " is not yet configured");
        }
        return "";
    }

    public String getSlotLinkName(int deviceId, int slotId, int linkId)
    {
        if (deviceId > this.deviceList.size() - 1)
        {
            System.out.println("[ERROR] device ID(" + deviceId + ") is out of bounds");
        } else
        {
            for (Slot slot : this.deviceList.get(deviceId).getSlots().getSlot())
            {
                try
                {
                    if (Integer.valueOf(slot.id) == slotId)
                    {
                        for (Link link : slot.getSlotDesc().getLinks().getLink())
                        {
                            if (Integer.valueOf(link.getId()) == linkId)
                            {
                                return link.getType();
                            }
                        }
                    }
                } catch (NumberFormatException ex)
                {
                    // This is OK it means the link is empty due to this the casting fails
                }
            }
        }
        return "";
    }

    private SlotDesc setSlotDesc(ObjectFactory objectFactory, String cardName, String[] linkArr)
    {
        SlotDesc slotDesc = objectFactory.createUnitSlotsSlotSlotDesc();
        slotDesc.setCard(cardName);
        Links links = objectFactory.createUnitSlotsSlotSlotDescLinks();
        if (linkArr != null && linkArr.length > 0)
        {
            links.getLink().addAll(stringToLinks(objectFactory, linkArr));
            slotDesc.setLinks(links);
        }
        return slotDesc;
    }

    /**
     * Uses the validateFolderExist to verify that the test and deploy folders
     * exist
     *
     * @throws IOException
     *
     * Author Oren
     */
    private void validateTestAndDeployFolders() throws IOException
    {
        validateFolderExist(this.devicesdToRunDeployOnPath);
        validateFolderExist(this.devicesdToRunTestOnPath);
    }

    /**
     * Will be used to validate that test and deployment folders exist, Other
     * wise will create them and copied all the devices in to them - it will be
     * called in the first time will open the device configuration in a project
     *
     * @param path - the folder to validate
     *
     * @throws IOException
     *
     * Author Oren
     */
    private void validateFolderExist(String path) throws IOException
    {
        System.out.println("In validateFolderExist");
        File folder = new File(path);

        // first will validate that the folders exist, other wise will create 
        //the folder and copied all the devices (first time we the new version)
//        System.out.println(path);
//        System.out.println("folder.exists()" + folder.exists());
//        System.out.println("folder.isDirectory()" + folder.isDirectory());
//        System.out.println("folder.exists() && !folder.isDirectory()" + (folder.exists() && !folder.isDirectory()));
        if (!(folder.exists() && folder.isDirectory()))
        {
            folder.delete();
            folder.mkdir();
            File unitsDirFile = new File(unitsDirPath);
            File[] listOfFiles = unitsDirFile.listFiles();

            for (File file : listOfFiles)
            {
                if (file.isFile())
                {
                    FileUtils.copyFile(file, new File(folder + "/" + file.getName()));
                }
            }
        }
    }

    public class UnitValidationEventHandler implements ValidationEventHandler
    {

        @Override
        public boolean handleEvent(ValidationEvent ve)
        {
            if (ve.getSeverity() == ValidationEvent.FATAL_ERROR
                    || ve.getSeverity() == ValidationEvent.ERROR)
            {
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

    /**
     * check if a file with the given file name exist in the device to test
     * folder
     *
     * @param fileName
     * @return - true if the file exist, false otherwise author oren
     */
    public boolean isDeviceOnTestList(String fileName)
    {
        //     System.out.println("In checkIfDeviceOnTestList file name =  " + fileName);
        boolean returnValue;
        String path;
        path = this.devicesdToRunTestOnPath + "/" + fileName;
        returnValue = isPathExist(path);
        return returnValue;
    }

    /**
     * check if a file with the given file name exist in the device to deploy
     * folder
     *
     * @param fileName
     * @return - true if the file exist, false otherwise
     *
     * author Oren
     */
    public boolean isDeviceOnDeployList(String fileName)
    {
//        System.out.println("In checkIfDeviceOnTestList file name =  " + fileName);
        boolean returnValue;
        String path;
        path = this.devicesdToRunDeployOnPath + "/" + fileName;
        returnValue = isPathExist(path);
        return returnValue;
    }

    /**
     * check if a file with the given file name exist in the device to deploy
     * folder
     *
     * @param fileName
     * @return - true if the file exist, false otherwise
     *
     * author Oren
     */
    public boolean isPathExist(String path)
    {
        //      System.out.println("In checkIfDeviceInFolder ");
        File file = new File(path);
        Boolean fileExist = file.exists();
        System.out.println("");
        return fileExist;
    }

    /**
     * calls by the through the project configuration every time check box being
     * changed, and make the changed needed by the change
     *
     * @param checkBoxValue
     * @param checkBoxName
     *
     * author Oren
     */
    public void onClickCheckBox(String checkBoxValue, String checkBoxName)
    {
        System.out.println("In onClickCheckBox ");
//        System.out.println("checkBoxName :  " + checkBoxName);
        String unitNumber = checkBoxName.split("-")[1];
//        System.out.println("unitNumber :  " + unitNumber);
        String deviceName = "/unit_" + unitNumber + ".xml";
//        System.out.println("deviceName :  " + deviceName);
        // it can be test or deploy, we need to check to with folder to go
        if (checkBoxName.startsWith("test"))
        {
            checkifCopyOrDeleteNeeded(checkBoxValue, this.devicesdToRunTestOnPath, deviceName);
        } else if (checkBoxName.startsWith("deploy"))
        {
            checkifCopyOrDeleteNeeded(checkBoxValue, this.devicesdToRunDeployOnPath, deviceName);
        }
    }
}
