package comm;

import jason.JasonException;
import jason.architecture.AgArch;
import jason.infra.saci.SaciAgArch;
import jason.mas2j.ClassParameters;
import jason.runtime.Settings;

import java.rmi.RemoteException;

/** 
 * Customisation of an agent architecture to sniff the MAS with 
 * SACI infrastructure. 
 * 
 * @author Jomi
 */
public class SnifferSaci extends AgArch {

    SaciAgArch arch = null;
    
    public void initAg(String agClass, ClassParameters bbPars, String asSrc, Settings stts) throws JasonException {
        super.initAg(agClass, bbPars, asSrc, stts);
    
        // becomes a SACI monitor if infrastructure is Saci
        if (getArchInfraTier() instanceof SaciAgArch) {
            arch = (SaciAgArch)getArchInfraTier();
            try {
                arch.getMBox().setIAmMonitor(true); // i do not need to notify myself

                // subscribes saci-events for its society
                // (if you want to monitor only one agent, send this message only to it)
                arch.getMBox().sendMsg(new saci.Message("(subscribe :ontology saci-events :receiver Facilitator)"));

            } catch (Exception e) {
                getTS().getLogger().warning("Error starting monitor! "+e);
            } 
    }
    }
    
    public void checkMail() {
        if (arch != null) {
            // remove all messages from saci's mail box
            try {
                saci.Message m = arch.getMBox().receive();
                while (m != null) {
                    if (m.getOntology().equals("saci-events")) {
                        // print the sniff message in the console
                        getTS().getLogger().info("Message:"+m);
                    }
                    m = arch.getMBox().receive();
                }
            } catch (RemoteException e) {
                getTS().getLogger().warning("Error reading messages! "+e);
            }
        }
    }
    
}
