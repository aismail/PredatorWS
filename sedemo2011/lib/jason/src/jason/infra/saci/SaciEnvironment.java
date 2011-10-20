//----------------------------------------------------------------------------
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

package jason.infra.saci;

import jason.JasonException;
import jason.asSyntax.Structure;
import jason.environment.Environment;
import jason.environment.EnvironmentInfraTier;
import jason.runtime.RuntimeServicesInfraTier;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import saci.MessageHandler;

/**
 * This class implements the saci version of the environment
 * infrastructure tier. 
 *
 * <p>The Environment is implemented as an agent in a second saci
 * society normally called &lt;MASID&gt;-env. The ordinary agents
 * enter in two societies: the "normal" (used for inter-agent
 * communication, and the environment society (used to get
 * perception). The environment agent enter only in the second.
 */
@SuppressWarnings("unchecked")
public class SaciEnvironment extends saci.Agent implements EnvironmentInfraTier {

    private static final long serialVersionUID = 3076623775045768401L;

    private Environment userEnv;
    static Logger       logger = Logger.getLogger(SaciEnvironment.class.getName());

    public SaciEnvironment() {
    }

    public void informAgsEnvironmentChanged(String... agents) {
        try {
            if (agents.length == 0) {
                saci.Message m = new saci.Message("(tell :content environmentChanged)");
                mbox.broadcast(m);
            } else {
                informAgsEnvironmentChanged(Arrays.asList(agents));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error sending notifyEvents ", e);
        }
    }

    public void informAgsEnvironmentChanged(Collection agentsToNotify) {
        try {
            if (agentsToNotify == null) {
                informAgsEnvironmentChanged();
            } else {
                saci.Message m = new saci.Message("(tell :content environmentChanged)");
                Iterator i = agentsToNotify.iterator();
                while (i.hasNext()) {
                    m.put("receiver", i.next().toString());
                    mbox.sendMsg(m);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error sending notifyEvents ", e);
        }
    }

    public void initAg(String[] args) throws JasonException {
        // create the user environment
        try {
            userEnv = (Environment) Class.forName(args[0]).newInstance();
            userEnv.setEnvironmentInfraTier(this);
            // create parameters array
            String[] p = new String[args.length-1];
            for (int i=0; i<p.length; i++) {
                p[i] = args[i+1];
            }
            userEnv.init(p);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in Saci Environment initAg", e);
            throw new JasonException("The user environment class instantiation '" + args[0] + "' fail!" + e.getMessage());
        }

        try {

            // add a message handler to answer perception asks
            // this handler filter is
            // . content: getPercepts
            // . performative: ask-all
            // . language: all
            // . ontology: AS-Perception
            mbox.addMessageHandler("getPercepts", "ask-all", null, "AS-Perception", new MessageHandler() {
                public boolean processMessage(saci.Message m) {
                    saci.Message r = null;
                    try {
                        r = new saci.Message("(tell)");
                        r.put("receiver", m.get("sender"));
                        r.put("in-reply-to", m.get("reply-with"));
                        r.put("ontology", m.get("ontology"));

                        List percepts = userEnv.getPercepts(m.get("sender").toString());
                        if (percepts != null) {
                            synchronized (percepts) {
                                r.put("content", percepts.toString());
                            }
                        }
                        mbox.sendMsg(r);

                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Error sending message " + r, e);
                    }
                    return true; // no other message handler gives this
                                    // message
                }
            });

            // add a message handler to answer action asks
            // this handler filter is
            // . content: execute
            // . performative: ask
            // . language: all
            // . ontology: AS-Action
            mbox.addMessageHandler("execute", "ask", null, "AS-Action", new MessageHandler() {
                public boolean processMessage(saci.Message m) {
                    saci.Message r = null;
                    try {
                        r = new saci.Message("(tell)");
                        r.put("receiver", m.get("sender"));
                        r.put("in-reply-to", m.get("reply-with"));
                        r.put("ontology", m.get("ontology"));
                        String sender = m.get("sender").toString();
                        Structure action = Structure.parse((String) m.get("action"));
                        userEnv.scheduleAction(sender, action, r);
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Error sending message " + e, e);
                    }
                    return true; // no other message handler gives this message
                }
            });

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error starting agent", e);
        }
    }
    
    public void actionExecuted(String agName, Structure actTerm, boolean success, Object infraData) {
        try {
            saci.Message r = (saci.Message)infraData;
            if (success) {
                r.put("content", "ok");
            } else {
                r.put("content", "error");
            }
            if (mbox != null) {
                mbox.sendMsg(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }                        
    }

    public void stopAg() {
        userEnv.stop();
        super.stopAg();
    }
    
    public RuntimeServicesInfraTier getRuntimeServices() {
        return new SaciRuntimeServices(getSociety());
    }
}
