package jason.infra.centralised;

import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.Agent;
import jason.mas2j.AgentParameters;
import jason.mas2j.ClassParameters;
import jason.runtime.RuntimeServicesInfraTier;
import jason.runtime.Settings;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/** This class implements the centralised version of the runtime services. */
public class CentralisedRuntimeServices implements RuntimeServicesInfraTier {

    private static Logger logger = Logger.getLogger(CentralisedRuntimeServices.class.getName());
    
    private RunCentralisedMAS masRunner;
    
    public CentralisedRuntimeServices(RunCentralisedMAS masRunner) {
        this.masRunner = masRunner;
    }
    
    public boolean createAgent(String agName, String agSource, String agClass, String archClass, ClassParameters bbPars, Settings stts) throws Exception {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Creating centralised agent " + agName + "from source " + agSource + "(agClass=" + agClass + ", archClass=" + archClass + ", settings=" + stts);
        }
        // parameters for ini

        AgentParameters ap = new AgentParameters();
        ap.setAgClass(agClass);
        ap.setArchClass(archClass);
        ap.setBB(bbPars);
        
        if (stts == null) stts = new Settings();
        
        while (masRunner.getAg(agName) != null) {
            agName += "_a";
        }

        CentralisedAgArch agArch = new CentralisedAgArch();
        agArch.setAgName(agName);
        agArch.initAg(ap.archClass.getClassName(), ap.agClass.getClassName(), ap.getBBClass(), agSource, stts, masRunner);
        agArch.setEnvInfraTier(masRunner.getEnvironmentInfraTier());
        agArch.setControlInfraTier(masRunner.getControllerInfraTier());
        masRunner.addAg(agArch);
        
        // create the agent thread
        Thread agThread = new Thread(agArch);
        agArch.setThread(agThread);
        agThread.start(); 

        logger.fine("Agent " + agName + " created!");
        return true;
    }
    
    public AgArch clone(Agent source, String archClassName, String agName) throws JasonException {
        // create a new infra arch
        CentralisedAgArch agArch = new CentralisedAgArch();
        agArch.setAgName(agName);
        agArch.setEnvInfraTier(masRunner.getEnvironmentInfraTier());
        agArch.setControlInfraTier(masRunner.getControllerInfraTier());
        masRunner.addAg(agArch);
        
        agArch.initAg(archClassName, source, masRunner);
        
        // create the agent thread
        Thread agThread = new Thread(agArch);
        agArch.setThread(agThread);
        agThread.start(); 
        return agArch.getUserAgArch();
    }

    public Set<String> getAgentsNames() {
        return masRunner.getAgs().keySet();
    }
    
    public int getAgentsQty() {
        return masRunner.getAgs().keySet().size();
    }

    public boolean killAgent(String agName) {
        logger.fine("Killing centralised agent " + agName);
        CentralisedAgArch ag = masRunner.getAg(agName);
        if (ag != null) {
            ag.stopAg();
            return true;
        } else {
            return false;
        }
    }

    public void stopMAS() throws Exception {
        masRunner.finish();
    }
}
