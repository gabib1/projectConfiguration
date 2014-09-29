/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.projectConfiguration;

import hudson.model.AbstractProject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.jenkinsci.plugins.projectConfiguration.exceptions.InvalidInputException;
import org.kohsuke.stapler.StaplerRequest;

/**
 *
 * @author oreny
 */
public class UtilsClass
{

    /**
     * Reads the given arguments from the request and write them to file
     *
     * @param project
     * @param req
     * @return
     * @throws IOException
     * @throws InvalidInputException
     */
    static boolean CreateParamterFile(AbstractProject<?, ?> project, StaplerRequest req) throws IOException, InvalidInputException
    {

        Scheduled schedule = null;
        String filePath;
        // will be the time of schedule
        String fileName;
        String username;
        String dirPath;
        String hour;
        String minute;
        String meridiem;
        Writer writer = null;
        File file;
        File dirFile;

        username = System.getProperty("user.name");
        dirPath = "/home/" + username + "/BuildSystem/cc-views/"
                + username + "_" + project.getName()
                + "_int/vobs/linux/CI_Conf/schduledRunsParameters";
        hour = req.getParameter("ScheduleTime-hour");
        minute = req.getParameter("ScheduleTime-minute");
        meridiem = req.getParameter("ScheduleTime-meridiem");
        schedule = new Scheduled(minute, hour, meridiem, "New");

        // file name will be "9"hour" for example 91050; for 1050 
        fileName = schedule.convertHourByMeridiem(hour, meridiem) + minute;
        filePath = dirPath + "/" + fileName;
        dirFile = new File(dirPath);

        // if the folder were not created yet will create it
        if (!dirFile.exists())
        {
            dirFile.mkdir();
        }

        file = new File(filePath);
        if (file.exists())
        {
            System.err.println("File named: " + fileName + " already exist in : " + dirPath);
            return false;
        }

        file.createNewFile();

        //read the given argumnets from the req
        Map<String, String> arguments = new HashMap();
        Map<String, String> mkverArguments = new HashMap();

        //this argument is the the opzite to the given value
        // when update_view is on is actaully not sending the noupdate tag
        String noUpdate = (req.getParameter("Update_view") == null) ? "NoUpdate" : null;

        mkverArguments.put("--mk-arch-image", req.getParameter("Make_image"));
        mkverArguments.put("--noupdate", noUpdate);
        mkverArguments.put("--system", req.getParameter("Copy_system_repositories"));
        mkverArguments.put("--mkbl", req.getParameter("Make_baselines"));
        mkverArguments.put("--recbl", req.getParameter("Recommend_baselines"));

        arguments.put("--testing", req.getParameter("Email_VnV"));
        arguments.put("--kw", req.getParameter("Klocwork"));
        arguments.put("--deploy", req.getParameter("Deployment"));
        arguments.put("--tests", req.getParameter("Tests"));

        //for testing purose
        System.out.println("______MKVER arguments are_______");

        for (String key : mkverArguments.keySet())
        {
            System.out.println(key + "  :   " + mkverArguments.get(key));
        }

        System.out.println("________arguments______");

        for (String key : arguments.keySet())
        {
            System.out.println(key + "  :   " + arguments.get(key));
        }

        //writing the data to the file
        writer = new FileWriter(filePath);
        writeToFile(writer, "parameters[0]=\"--mkver\"");
        StringBuilder givenMkverArguments = new StringBuilder();

        boolean isArgumentsWhereGiven = false;
        // mkver arguments should be as one line after mkver
        for (String key : mkverArguments.keySet())
        {
            // only the values that given a value in the request were chosed
            if (!(mkverArguments.get(key) == null))
            {
                System.out.println(key);
                givenMkverArguments.append(key);
                givenMkverArguments.append(" ");
                if (!key.equals("--noupdate"))
                {
                    isArgumentsWhereGiven = true;
                }
            }
        }
        writeToFile(writer, "parameters[1]=\"--rebuild " + givenMkverArguments.toString() + "\"");

        //this argumnets shold be in a uniqe line, will start from counter
        //2 since we must have mkver and mkver argumnets before 
        int i = 2;
        for (String key : arguments.keySet())
        {
            if (!(arguments.get(key) == null))
            {
                System.out.println(key);
                writeToFile(writer, "parameters[" + i + "]=\"" + key + "\"");
                i++;
                //  noupdate is actully when update view is not given so no argumnets where given
                isArgumentsWhereGiven = true;
            }
        }

        writer.close();
        // if we didn't got any argumnets , we should not save the file
        if (!isArgumentsWhereGiven)
        {
            file.delete();
        }

        return true;
    }

    /**
     * Writes the the given value to the file , and adds a new line at the end
     *
     * @param writer
     * @param value
     * @throws IOException
     */
    public static void writeToFile(Writer writer, String value) throws IOException
    {
        writer.write(value + "\n");
        writer.flush();
    }

    /**
     * read the value in line
     *
     * @param line
     * @return
     */
    public static String valuesInLine(String line)
    {
        String value = line.split("=")[1];
        value = value.replaceAll("\"", "");
        return value;

    }

    /**
     * Reads all the parameters found in the given path array
     *
     * @param filePath
     * @return - array of all the values found in the file
     */
    public static ArrayList<String> readValuessFromFile(String filePath)
    {
        //changed   -- test
        ArrayList<String> parametersInFile = new ArrayList<>();
        File profilesFile = new File(filePath);
        if (profilesFile.exists() == true)
        {
            InputStream fis;
            BufferedReader br;
            String line;

            try
            {
                fis = new FileInputStream(profilesFile);
                br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
                //counter to find the second line
                int i = 0;
                while ((line = br.readLine()) != null)
                {
                    if (line.isEmpty() && !(i == 1))
                    {
                        parametersInFile.add(valuesInLine(line));
                        i++;

                    } else
                    {
                        line = valuesInLine(line);
                        String[] arguments = line.split(" ");
                        parametersInFile.addAll(Arrays.asList(arguments));
                        i++;
                    }
                }
                br.close();
            } catch (IOException ex)
            {
            }
        }
        return parametersInFile;
    }

    /**
     * Read the parameter from the given path and returns them as boolean
     * parameter definition
     *
     * @param filePath
     * @return
     */
    public static ArrayList<String> getParametersFromFile(String filePath)
    {

        // is true unless found --noupdate flag
        boolean updateView = true;

        ArrayList<String> parametersInFile = readValuessFromFile(filePath);

        // no file found
        // test -- changed
        if (parametersInFile.isEmpty())
        {
            return parametersInFile;
        }
        //for tetsing  printing all calues from the file
        System.out.println("Parameter read from file ");
        for (String argument : parametersInFile)
        {
            System.out.println(argument);
            if (argument.equals("--noupdate"))
            {
                updateView = false;
            }
        }

        //fill the map with key values
        Map<String, String> arguments = new HashMap();
        arguments.put("--mk-arch-image", "Make_image");
        arguments.put("--system", "Copy_system_repositories");
        arguments.put("--mkbl", "Make_baselines");
        arguments.put("--recbl", "Recommend_baselines");
        arguments.put("--testing", "Email_VnV");
        arguments.put("--kw", "Klocwork");
        arguments.put("--deploy", "Deployment");
        arguments.put("--tests", "Tests");

        // This is uniq becuase it should be true only if it not appears in the file
        //  arguments.put("--noupdate", "Update_view");
        ArrayList<String> argumnetList = new ArrayList<>();

        // For each key will check with all the parameters from the file to see if we have a match
        for (String key : arguments.keySet())
        {
            System.out.println("The Parameter looking for is :   " + key);

            //Test Changed
            for (String parameterFromFile : parametersInFile)
            {
                System.out.println("checking now with : " + parameterFromFile);
                if (key.equals(parameterFromFile))
                {
                    argumnetList.add(arguments.get(key));
                    System.out.println(key + " FOUND ON FILE ADDED WITH TRUE =  " + parameterFromFile);
                    break;
                }
            }
        }

        if (updateView)
        {
            argumnetList.add("Update_view");
        }

        return argumnetList;
    }

    /**
     * gets the time of the schedule from its name
     *
     * @param schduleName
     * @return
     */
    public static String getTimeOfSchduleFromItsName(String schduleName)
    {
        String[] list = schduleName.split(" ");
        String fileName = list[list.length - 1].replace(":", "");
        return fileName;
    }

    public static boolean removeSchduleParametersFile(String path)
    {
        File file = new File(path);
        if (file.exists())
        {
            return file.delete();
        }
        return true;
    }

    /**
     * Checks if the path is file or not
     *
     * @param path - the path for the file
     * @return boolean i
     *
     */
    // Changed
    public static boolean fileExists(String path)
    {
        System.out.println("In UtilClass file Exist");
        File file = new File(path);
        boolean result;
        result = file.exists();
        System.out.println("result value is " + result);
        return result;
    }

    /**
     *
     * @param project
     * @param value
     * @throws java.io.IOException
     */
    public static void writeToTestDependencyFile(String projectName, String value) throws IOException
    {
        System.out.println("In writeToTestDependencyFile");
        System.out.println("Given params , 1 - value:   " + value);
        String userName, filePath;
        FileWriter writer;
        userName = System.getProperty("user.name");
        filePath = "/home/" + userName + "/BuildSystem/cc-views/"
                + userName + "_" + projectName
                + "_int/vobs/linux/CI_Conf/testDependency";
        File file = new File(filePath);
        if (file.exists())
        {
            file.delete();
        }
        writer = new FileWriter(filePath);
        writeToFile(writer, "projectNameForTestDependency=" + value);
        writer.close();

    }

    /**
     *reads the name of project that we saved in the dependency file
     * 
     * @param projectName - this project name
     * @throws java.io.IOException
     */
    public static String readFromTestDependencyFile(String projectName) throws IOException
    {
        System.out.println("In readFromTestDependencyFile");
        String userName, filePath;
        userName = System.getProperty("user.name");
        filePath = "/home/" + userName + "/BuildSystem/cc-views/"
                + userName + "_" + projectName
                + "_int/vobs/linux/CI_Conf/testDependency";

        File file = new File(filePath);
        String dependencyName = "empty";

        if (file.exists() == true)
        {
            InputStream fis;
            BufferedReader br;
            String line;

            fis = new FileInputStream(file);
            br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
            line = br.readLine();
            if (!line.isEmpty())
            {
                dependencyName = line.split("=")[1];
            }
            br.close();

        }
        System.out.println("dependencyName --- :  "  + dependencyName);
        return dependencyName;
    }

    /**
     * deletes the dependency file in the given project
     * 
     * @param projectName  - the name of the project in which we want to delete 
     * the dependency file
     */
    static void removeDependencyFile(String projectName)
    {
        System.out.println("In removeDependencyFile");
        String userName, filePath;
        userName = System.getProperty("user.name");
        filePath = "/home/" + userName + "/BuildSystem/cc-views/"
                + userName + "_" + projectName
                + "_int/vobs/linux/CI_Conf/testDependency";

        File file = new File(filePath);
        file.delete();
    }

}
