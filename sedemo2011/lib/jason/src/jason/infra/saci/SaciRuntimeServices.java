package jason.infra.saci;

import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.Agent;
import jason.bb.DefaultBeliefBase;
import jason.mas2j.ClassParameters;
import jason.runtime.RuntimeServicesInfraTier;
import jason.runtime.Settings;

import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import saci.CommSAg;
import saci.Facilitator;
import saci.launcher.AgentId;
import saci.launcher.Command;
import saci.launcher.Launcher;
import saci.launcher.LauncherD;

@SuppressWarnings("unchecked")
public class SaciRuntimeServices implements RuntimeServicesInfraTier {

    private static Logger logger  = Logger.getLogger(SaciRuntimeServices.class.getName());

    private String        socName = "";

    private Facilitator   facilitator;

    public SaciRuntimeServices(String name) {
        setSocName(name);
    }

    public void setSocName(String name) {
        socName = name;
        try {
            Launcher l = LauncherD.getLauncher();
            String host = l.getSocietyHost(socName);
            facilitator = CommSAg.connect(host, socName);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting facilitator for " + socName, e);
        }
    }

    public boolean createAgent(String agName, String agSource, String agClass, String archClass, ClassParameters bbPars, Settings stts) throws Exception {
        try {
            logger.fine("Creating saci agent from source " + agSource);

            if (agClass == null)
                agClass = Agent.class.getName();
            if (archClass == null)
                archClass = AgArch.class.getName();
            if (stts == null)
                stts = new Settings();
            if (bbPars == null) {
                bbPars = new ClassParameters(DefaultBeliefBase.class.getName());
            }

            String extraOp = "";
            if (stts.isSync()) {
                extraOp = " options verbose=2,synchronised=true";
            }
            // gets the saci launcher
            Launcher l = LauncherD.getLauncher();
            Command c1 = new Command(Command.START_AGENT);
            c1.addArg("class", SaciAgArch.class.getName());
            c1.addArg("name", agName);
            c1.addArg("society.name", socName);
            String sBBClass = bbPars.toString().replace('\"','$');
            c1.addArg("args", archClass + " " + agClass + " '" + sBBClass + "' "+ agSource + extraOp);
            // c1.addArg("host", "?");
            l.execCommand(c1);
            logger.fine("Agent " + agName + " created!");
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating agent", e);
        }
        return false;
    }
    
    public AgArch clone(Agent source, String archClassName, String agName) throws JasonException {
        throw new JasonException("clone for SACI is not implemented!");
    }
    

    public Set getAgentsNames() {
        try {
            return facilitator.getAllWP().entrySet();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting agents", e);
        }
        return null;
    }
    
    public int getAgentsQty() {
        try {
            return facilitator.getAgQty() - 3; // do not include controller,
                                                // environment, and facilitator
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting agents qty", e);
            return 0;
        }
    }

    public boolean killAgent(String agName) {
        try {
            logger.fine("Killing Saci agent " + agName);

            // gets the saci launcher
            Launcher l = LauncherD.getLauncher();
            Iterator i = l.getAllAgentsID().iterator();
            while (i.hasNext()) {
                AgentId aid = (AgentId) i.next();
                if (aid.getName().equals(agName) && aid.getSociety().equals(socName)) {
                    return l.killAg(aid).booleanValue();
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error killing agent", e);
        }
        return false;
    }

    public void stopMAS() throws Exception {
        Launcher l = LauncherD.getLauncher();
        if (l != null) {
            l.killFacilitatorAgs(socName);
            l.killFacilitator(socName);
            l.killFacilitatorAgs(socName + "-env");
            l.killFacilitator(socName + "-env");
        }
    }
}
