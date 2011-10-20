package jason.infra.saci;

import jason.infra.centralised.CentralisedMASLauncherAnt;
import jason.jeditplugin.Config;
import jason.mas2j.MAS2JProject;

import java.io.File;

import javax.swing.JOptionPane;

import saci.launcher.Launcher;
import saci.launcher.LauncherD;


/**
 * Thread used to start saci (call Ant to run the "saci" task of the
 * build.xml script).
 */
class StartSaci extends Thread {

    boolean saciOk = false;
    Process saciProcess;
    MAS2JProject project;
    boolean stop = false;
    
    StartSaci(MAS2JProject project) {
        super("StartSaci");
        this.project = project;
    }

    Launcher getLauncher() {
        Launcher l = null;
        try {
            l = LauncherD.getLauncher();
            return l;
        } catch (Exception e) {
            return null;
        }
    }
    
    void stopSaci() {
        try {
            getLauncher().stop();
        } catch (Exception e) {
            try {
                saciProcess.destroy();
            } catch (Exception e2) {
            }
        }
        saciProcess = null;
    }

    public void run() {
        //stopSaci();
        try {
            String[] command = getStartCommandArray();
            
            String cmdstr = command[0];
            for (int i=1; i<command.length; i++) {
                cmdstr += " "+command[i];
            }
            System.out.println("Running saci with " + cmdstr);
            
            saciProcess = Runtime.getRuntime().exec(command, null, new File(project.getDirectory()));

            int tryCont = 0;
            while (tryCont < 30 && !stop) {
                tryCont++;
                sleep(1000);
                Launcher l = getLauncher();
                if (l != null) {
                    saciOk = true;
                    stopWaitSaciOk();
                    break;
                }
            }
        } catch (Exception ex) {
            System.err.println("error running saci:" + ex);
        } finally {
            stopWaitSaciOk();
        }
    }

    /** returns the operating system command that runs the MAS */
    public String[] getStartCommandArray() {
        String build = CentralisedMASLauncherAnt.bindir+"build.xml";
        if (hasCBuild()) build = CentralisedMASLauncherAnt.bindir+"c-build.xml";
        return  new String[] {
                    Config.get().getJavaHome()+"bin"+File.separator+"java",
                    "-classpath", 
                    Config.get().getAntLib()+ "ant-launcher.jar",
                    "org.apache.tools.ant.launch.Launcher",
                    "-e",
                    "-f", build,
                    "saci"
                 };
    }

    protected boolean hasCBuild() {
        return new File(project.getDirectory()+ File.separator+ CentralisedMASLauncherAnt.bindir+"c-build.xml").exists();
    }

    synchronized void stopWaitSaciOk() {
        stop = true;
        notifyAll();
    }

    synchronized boolean waitSaciOk() {
        try {
            wait(20000); // waits 20 seconds
            if (!saciOk && !stop) {
                JOptionPane.showMessageDialog(null,
                    "Failed to automatically start saci! \nGo to \""
                    + project.getDirectory()
                    + "\" directory and run 'ant saci'"
                    + " to start saci.\n\nClick 'ok' when saci is running.");
                wait(1000);
                if (!saciOk) {
                    JOptionPane.showMessageDialog(null,
                    "Saci might not be properly installed or configure. Use the centralised architecture to run your MAS");
                }
            }
        } catch (Exception e) {
        }
        return saciOk;
    }
}
