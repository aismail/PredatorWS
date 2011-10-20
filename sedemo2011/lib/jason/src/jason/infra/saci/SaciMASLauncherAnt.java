package jason.infra.saci;

import jason.architecture.AgArch;
import jason.bb.DefaultBeliefBase;
import jason.control.ExecutionControlGUI;
import jason.infra.centralised.CentralisedMASLauncherAnt;
import jason.jeditplugin.Config;
import jason.jeditplugin.MASLauncherInfraTier;
import jason.mas2j.AgentParameters;
import jason.mas2j.ClassParameters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import saci.launcher.Launcher;

/**
 * Creates the scripts to launch the MAS using SACI and starts SACI
 * before the agents.
 */
public class SaciMASLauncherAnt extends CentralisedMASLauncherAnt implements MASLauncherInfraTier {

    StartSaci             saciThread;
    Launcher              l;
    boolean               iHaveStartedSaci = false;
    private static Logger logger           = Logger.getLogger(SaciMASLauncherAnt.class.getName());

    public void run() {
        saciThread = new StartSaci(project);
        l = saciThread.getLauncher();
        if (l == null) { // no saci running, start one
            saciThread.start();
            if (!saciThread.waitSaciOk()) {
                return;
            }
            iHaveStartedSaci = true;
        }
        l = saciThread.getLauncher();

        super.run();
    }

    public void stopMAS() {
        if (saciThread.saciOk) {
            // saci is running
            new Thread() {
                public void run() {
                    try {
                        new SaciRuntimeServices(project.getSocName()).stopMAS();
                        if (iHaveStartedSaci) {
                            saciThread.stopSaci();
                        }
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Error stoping saci MAS", e);
                    }
                }
            }.start();
        } else {
            // saci is not running yet
            // stop the start thread
            try {
                saciThread.stopWaitSaciOk();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error stoping saci MAS", e);
            }

        }
        super.stopMAS();
    }

    protected String replaceMarks(String script, boolean debug) {
        String saciJar = Config.get().getSaciJar();
        if (!Config.checkJar(saciJar)) {
            System.err.println("The path to the saci.jar file (" + saciJar + ") was not correctly set. Go to menu Plugin->Options->Jason to configure the path.");
        }

        writeSaciXMLScript(debug);

        script = replace(script, "<PROJECT-RUNNER-CLASS>", "saci.tools.runApplicationScript");
        script = replace(script, "<PROJECT-FILE>", project.getSocName() + ".xml");
        script = replace(script, "<DEBUG>", "");

        String sacipath = "        <pathelement location=\"" + Config.get().getSaciJar() + "\"/>";
        script = replace(script, "<PATH-LIB>", sacipath + "<PATH-LIB>");

        // write start saci script
        String startsaci = 
                  "    <property name=\"saci.main\" value=\"saci.tools.SaciMenu\"/> <!-- use \"saci.launcher.LauncherD\" to run saci without a GUI -->\n"
                + "    <property name=\"saci.remote.host\" value=\"localhost\"/>\n\n"
                + "    <target name=\"saci\">\n" 
                + "       <java classname=\"${saci.main}\" failonerror=\"true\" fork=\"yes\" dir=\"${basedir}\">\n"
                + "          <classpath refid=\"project.classpath\"/>\n" 
                + "          <jvmarg value=\"-Djava.security.policy=jar:file:" + Config.get().getSaciJar()+ "!/policy\"/>\n" 
                + "       </java>\n" 
                + "    </target>\n\n" 
                + "    <target name=\"stop-saci\" >\n" 
                + "       <java classname=\"saci.launcher.StopLauncherD\" >\n"
                + "          <classpath refid=\"project.classpath\"/>\n" 
                + "       </java>\n" 
                + "    </target>\n"
                + "    <target name=\"saci-client\" >\n" 
                + "       <java classname=\"saci.launcher.LauncherD\" failonerror=\"true\" fork=\"yes\" dir=\"${basedir}\">\n"
                + "          <classpath refid=\"project.classpath\"/>\n" 
                + "          <jvmarg value=\"-Djava.security.policy=jar:file:" + Config.get().getSaciJar()+ "!/policy\"/>\n"
                + "          <arg line=\"-connect ${saci.remote.host}\"/>\n"                
                + "       </java>\n" 
                + "    </target>\n";

        script = replace(script, "<OTHER-TASK>", startsaci);

        return super.replaceMarks(script, debug);
    }

    public void writeSaciXMLScript(boolean debug) {
        try {
            String file = project.getDirectory() + File.separator+ project.getSocName() + ".xml";
            writeSaciXMLScript(new PrintStream(new FileOutputStream(file)), debug);
        } catch (Exception e) {
            System.err.println("Error writing XML script!" + e);
        }
    }

    public void writeSaciXMLScript(PrintStream out, boolean debug) {
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.println("<!-- this file was generated by Jason -->");
        out.println("<?xml-stylesheet href=\"http://www.das.ufsc.br/~jomi/jason/saci/applications.xsl\" type=\"text/xsl\" ?>");
        out.println("<saci>");
        out.println("<application id=\"" + project.getSocName() + "\">");

        out.println("<script id=\"run\">\n");

        out.println("\t<killSocietyAgents society.name=\"" + project.getSocName() + "\" />");
        out.println("\t<killFacilitator society.name=\"" + project.getSocName() + "\" />");
        out.println("\t<startSociety society.name=\"" + project.getSocName() + "\" />\n");

        out.println("\t<killSocietyAgents society.name=\"" + project.getSocName() + "-env\" />");
        out.println("\t<killFacilitator society.name=\"" + project.getSocName() + "-env\" />");
        out.println("\t<startSociety society.name=\"" + project.getSocName() + "-env\" />\n");

        // environment
        out.println("\t<startAgent ");
        out.println("\t\tname=\"environment\" ");
        out.println("\t\tsociety.name=\"" + project.getSocName() + "-env\" ");
        ClassParameters tmpEnvClass;
        if (project.getEnvClass() == null) {
            tmpEnvClass = new ClassParameters(jason.environment.Environment.class.getName());
        } else {
            tmpEnvClass = project.getEnvClass();
        }
        String pars = tmpEnvClass.getParametersStr(" ").replaceAll("\"","'");
        out.println("\t\targs=\"" + tmpEnvClass.getClassName() + " " + pars + "\" ");
        // do not use class.getName, it does not work with jason.exe
        out.println("\t\tclass=\"jason.infra.saci.SaciEnvironment\" ");
        if (tmpEnvClass.getHost() != null) {
            out.println("\t\thost=\"" + tmpEnvClass.getHost()+"\"");
        }
        out.println("\t/>");

        project.fixAgentsSrc(null);
        
        // agents
        for (AgentParameters agp: project.getAgents()) {
            out.println(getAgSaciXMLScript(agp, debug, project.getControlClass() != null));
        }

        // controller
        ClassParameters fControlClass = project.getControlClass();
        if (debug && fControlClass == null) {
            fControlClass = new ClassParameters(ExecutionControlGUI.class.getName());
        }
        if (fControlClass != null) {
            out.println("\t<startAgent ");
            out.println("\t\tname=\"controller\" ");
            out.println("\t\tsociety.name=\"" + project.getSocName() + "-env\" ");

            out.println("\t\targs=\"" + fControlClass + " " + fControlClass.getParametersStr(" ") + "\"");
            if (fControlClass.getHost() != null) {
                out.println("\t\thost=" + fControlClass.getHost());
            }
            out.println("\t\tclass=\"jason.infra.saci.SaciExecutionControl\" ");
            out.println("\t/>");
        }
        out.println("\n</script>");
        out.println("</application>");
        out.println("</saci>");
        out.close();
    }

    public String getAgSaciXMLScript(AgentParameters agp, boolean debug, boolean forceSync) {
        StringBuilder s = new StringBuilder("\t<startAgent ");
        s.append("\n\t\tname=\"" + agp.name + "\" ");
        s.append("\n\t\tsociety.name=\"" + project.getSocName() + "\" ");

        s.append("\n\t\tclass=\"jason.infra.saci.SaciAgArch\"");

        ClassParameters tmpAgClass = agp.agClass;
        if (tmpAgClass == null) {
            tmpAgClass = new ClassParameters(jason.asSemantics.Agent.class.getName());
        }
        ClassParameters tmpAgArchClass = agp.archClass;
        if (tmpAgArchClass == null) {
            tmpAgArchClass = new ClassParameters(AgArch.class.getName());
        }
        
        ClassParameters tmpBBClass = agp.getBBClass();
        if (tmpBBClass == null) {
            tmpBBClass = new ClassParameters(DefaultBeliefBase.class.getName());
        }
        String sBBClass = tmpBBClass.toString().replace('\"','$');

        String fname = agp.asSource.toString();
        if (!fname.startsWith(File.separator) && !fname.startsWith(project.getDirectory())) {
        	fname = project.getDirectory() + File.separator + fname;
        }
        File tmpAsSrc = new File(fname);
        s.append("\n\t\targs=\"" + tmpAgArchClass.getClassName() + " " + tmpAgClass.getClassName() +
                " '" + sBBClass + "' " +
                " '" + tmpAsSrc.getAbsolutePath() + "'" + getSaciOptsStr(agp, debug, forceSync) + "\"");
        if (agp.qty > 1) {
            s.append("\n\t\tqty=\"" + agp.qty + "\" ");
        }
        if (agp.getHost() != null) {
            s.append("\n\t\thost=\"" + agp.getHost()+"\"");
        }
        s.append(" />");
        return s.toString().trim();
    }

    String getSaciOptsStr(AgentParameters agp, boolean debug, boolean forceSync) {
        String s = "";
        String v = "";
        if (debug) {
            s += "verbose=2";
            v = ",";
        }
        if (forceSync || debug) {
            s += v + "synchronised=true";
            v = ",";
        }
        Iterator<String> i = agp.getOptions().keySet().iterator();
        while (i.hasNext()) {
            String key = i.next();
            if (!(debug && key.equals("verbose"))) {
                if (!((forceSync || debug) && key.equals("synchronised"))) {
                    s += v + key + "=" + changeQuotes((String) agp.getOptions().get(key));
                    v = ",";
                }
            }
        }
        if (s.length() > 0) {
            s = " options " + s;
        }
        return s;
    }

    String changeQuotes(String s) {
        if (s.startsWith("\"") && s.endsWith("\"")) {
            return "'" + s.substring(1, s.length() - 1) + "'";
        } else {
            return s;
        }
    }
}
