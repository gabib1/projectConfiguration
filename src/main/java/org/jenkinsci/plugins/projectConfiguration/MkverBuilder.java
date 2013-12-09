package org.jenkinsci.plugins.projectConfiguration;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Messages;
import java.io.BufferedReader;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

// Currently this build step extends Builder but it is better to extend CommandInterpreter,
// because we can look at how Shell has extended CommandInterpreter and do the same, if 
// we do so we will "earn" the same behaviour as Shell build step plus we have a reference
public class MkverBuilder extends Builder{

    private final String name;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public MkverBuilder(String name) {
        this.name = name;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getName() {
        return name;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
                
        try {
            EnvVars envVars = build.getEnvironment(listener);
            String projectName = envVars.get("JOB_NAME");
            String space = " ";
            String mkverCommand = "bash /home/builder/BuildSystem/cc-views/builder_" +
                    projectName + "_int" + space;
            String buildType = envVars.get("Build_Type");
            mkverCommand += "--" + buildType;
            
            listener.getLogger().println(build.getProject().getName());
//            ProcessBuilder processBuilder = new ProcessBuilder("/home/gabi/temp.sh");
//            Process process = processBuilder.start();
//
//            InputStream is = p.getInputStream();
//            InputStreamReader isr = new InputStreamReader(is);
//            BufferedReader br = new BufferedReader(isr);
//            String line;
//            while ((line = br.readLine()) != null) {
//                listener.getLogger().println(line);
//            }
//            
//            //Wait to get exit value
//            int exitValue = p.waitFor();
//            if (exitValue != 0){
//                InputStreamReader errorStreamReader = new InputStreamReader(p.getErrorStream());
//                BufferedReader ebr = new BufferedReader(errorStreamReader);
//                listener.getLogger().println(ebr.readLine());
//                throw new IOException();
//            }
//            System.out.println("\n\nExit Value is " + exitValue);
//            listener.fatalError(Messages.CommandInterpreter_CommandFailed());
//            
//            
//            
        } catch (IOException ex) {
//            Util.displayIOException(ex, listener);
//            ex.printStackTrace(listener.fatalError(Messages.CommandInterpreter_CommandFailed()));
//            listener.getLogger().println("Problem executing mkver.sh\n" + ex.getMessage());
//            return false;
        } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }
        
        return true;
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    /**
     * Descriptor for {@link MkverBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        /**
         * In order to load the persisted global configuration, you have to 
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Mkver build";
        }
    }
}

