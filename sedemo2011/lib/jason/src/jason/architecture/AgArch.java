// Copyright (C) 2003  Rafael H. Bordini, Jomi F. Hubner, et al.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
// To contact the authors:
// http://www.inf.ufrgs.br/~bordini
// http://www.das.ufsc.br/~jomi
//
//----------------------------------------------------------------------------

package jason.architecture;

import jason.JasonException;
import jason.asSemantics.ActionExec;
import jason.asSemantics.Agent;
import jason.asSemantics.Circumstance;
import jason.asSemantics.Message;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.Literal;
import jason.bb.BeliefBase;
import jason.mas2j.ClassParameters;
import jason.runtime.Settings;

import java.util.List;

/**
 * Base agent architecture class that defines the overall agent architecture;
 * the AS interpreter is the reasoner (a kind of mind) within this
 * architecture (a kind of body).
 * 
 * <p>
 * The agent reasoning cycle (implemented in TransitionSystem class) calls these
 * methods to get perception, action, and communication.
 * 
 * <p>
 * This class normally just calls the AgArchInfraTier methods 
 * implemented by the infrastructure tier (Centralised, JADE, Saci, ...). 
 * However, the user can customise
 * these methods overriding some of them in his/her arch. class.
 */
public class AgArch {

    private TransitionSystem ts = null;

    /**
     * The class that implements the architecture tier for the MAS
     * infrastructure
     */
    private AgArchInfraTier archTier;

    /** the current cycle number, in case of sync execution mode */
    private int cycleNumber = 0;
    
    /**
     * Creates the agent class defined by <i>agClass</i>, default is
     * jason.asSemantics.Agent. 
     * Creates the TS for the agent.
     * Creates the belief base for the agent. 
     */
    public void initAg(String agClass, ClassParameters bbPars, String asSrc, Settings stts) throws JasonException {
        // set the agent
        try {
            Agent ag = (Agent) Class.forName(agClass).newInstance();
            
            new TransitionSystem(ag, new Circumstance(), stts, this);

            BeliefBase bb = (BeliefBase) Class.forName(bbPars.getClassName()).newInstance();
            ag.setBB(bb); // the agent's BB have to be already set for the BB initialisation, and the agent initialised
            ag.initAg(asSrc); // load the source code of the agent

            bb.init(ag, bbPars.getParametersArray());            
        } catch (Exception e) {
            throw new JasonException("as2j: error creating the customised Agent class! - ", e);
        }
    }

    /**
     * A call-back method called by the infrastructure tier 
     * when the agent is about to be killed.
     */
    public void stopAg() {
        ts.getAg().stopAg();
    }

    public void setArchInfraTier(AgArchInfraTier ai) {
        archTier = ai;
    }
    public AgArchInfraTier getArchInfraTier() {
        return archTier;
    }

    public TransitionSystem getTS() {
        return ts;
    }
    public void setTS(TransitionSystem ts) {
        this.ts = ts;
    }

    /** Gets the agent's perception as a list of Literals.
     *  The returned list will be modified by Jason.
     */
    public List<Literal> perceive() {
        return archTier.perceive();
    }

    /** Reads the agent's mailbox and adds messages into 
        the agent's circumstance */
    public void checkMail() {
        archTier.checkMail();
    }

    /**
     * Executes the action <i>action</i> and, when finished, add it back in
     * <i>feedback</i> actions.
     */
    public void act(ActionExec action, List<ActionExec> feedback) {
        archTier.act(action, feedback);
    }

    /** Returns true if the agent can enter in sleep mode. */
    public boolean canSleep() {
        return archTier.canSleep();
    }

    /** Puts the agent in sleep. */
    public void sleep() {
        archTier.sleep();
    }
    
    /** Gets the agent's name */
    public String getAgName() {
        if (archTier == null)
            return "no-named";
        else
            return archTier.getAgName();
    }

    /** Sends a Jason message */
    public void sendMsg(Message m) throws Exception {
        archTier.sendMsg(m);
    }

    /** Broadcasts a Jason message */
    public void broadcast(Message m) throws Exception {
        archTier.broadcast(m);
    }

    /** Checks whether the agent is running */
    public boolean isRunning() {
        return archTier == null || archTier.isRunning();
    }
    
    /** sets the number of the current cycle in the sync execution mode */
    public void setCycleNumber(int cycle) {
        cycleNumber = cycle;
    }
    
    /** gets the current cycle number in case of running in sync execution mode */
    public int getCycleNumber() {
        return cycleNumber;
    }
}
