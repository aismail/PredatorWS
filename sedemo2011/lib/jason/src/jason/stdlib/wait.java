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

package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.Circumstance;
import jason.asSemantics.CircumstanceListener;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.InternalActionLiteral;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.PlanBody;
import jason.asSyntax.PlanBodyImpl;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.PlanBody.BodyType;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
  <p>Internal action: <b><code>.wait(<i>E</i>,<i>T</i>)</code></b>.
  
  <p>Description: suspend the intention for the time specified by <i>T</i> (in
  milliseconds) or until some event <i>E</i> happens. The events follow the 
  AgentSpeak syntax but are enclosed by { and }, e.g. <code>{+bel(33)}</code>,
  <code>{+!go(X,Y)}</code>. 
  
  <p>Parameters:<ul>
  <li><i>+ event</i> (trigger term): the event to wait for.<br/>
  <li>+ timeout (number).<br/>
  </ul>
  
  
  <p>Examples:<ul>
  <li> <code>.wait(1000)</code>: suspend the intention for 1 second.

  <li> <code>.wait({+b(1)})</code>: suspend the intention until the belief
  <code>b(1)</code> is added in the belief base.

  <li> <code>.wait({+!g}, 2000)</code>: suspend the intention until the goal
  <code>g</code> is triggered or 2 seconds have passed, whatever happens
  first. In case the event does not happens in two seconds, the internal action
  fails. 

  <li> <code>.wait({+!g}, 2000, EventTime)</code>: suspend the intention until the goal
  <code>g</code> is triggered or 2 seconds have passed, whatever happens
  first. 
  As this use of .wait has three arguments, in case the event does not happen in 
  two seconds, the internal action does not fail (as in the previous example).
  The third argument will be unified to the 
  elapsed time (in miliseconds) from the start of .wait until the event or timeout. </ul>

  @see jason.stdlib.at

 */
public class wait extends DefaultInternalAction {

    public static final String waitAtom = ".wait"; 

    @Override public boolean canBeUsedInContext() {  return false;  }
    @Override public boolean suspendIntention() { return true;  } 
    
    @Override public int getMinArgs() { return 1; }
    @Override public int getMaxArgs() { return 3; }

    @Override
    public Object execute(final TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        
        long timeout = -1;
        Trigger te = null;
        Term elapsedTime = null;
        
        if (args[0].isNumeric()) {
            // time in milliseconds
            NumberTerm time = (NumberTerm)args[0];
            timeout = (long) time.solve();
        } else { // if (args[0].isString()) 
            // wait for event
            te = Trigger.tryToGetTrigger(args[0]); //ASSyntax.parseTrigger( ((StringTerm) args[0]).getString());
                                                   //te.getLiteral().apply(un);
            
            if (args.length >= 2)
                timeout = (long) ((NumberTerm) args[1]).solve();
            if (args.length == 3)
                elapsedTime = args[2];
        }
        new WaitEvent(te, un, ts, timeout, elapsedTime);
        return true;
    }    

    class WaitEvent implements CircumstanceListener { 
        private Trigger          te;
        private String           sTE; // a string version of TE
        private Unifier          un;
        private Intention        si;
        private TransitionSystem ts;
        private Circumstance     c;
        private boolean          dropped = false;
        private Term             elapsedTimeTerm;
        private long             startTime;
        
        WaitEvent(Trigger te, Unifier un, TransitionSystem ts, long timeout, Term elapsedTimeTerm) {
            this.te = te;
            this.un = un;
            this.ts = ts;
            c = ts.getC();
            si = c.getSelectedIntention();
            this.elapsedTimeTerm = elapsedTimeTerm;

            // register listener
            c.addEventListener(this);
            
            if (te != null) {
                sTE = te.toString();
            } else {
                sTE = "time"+(timeout);
            }
            sTE = si.getId()+"/"+sTE;
            c.addPendingIntention(sTE, si);
            
            startTime = System.currentTimeMillis();

            if (timeout > 0) {
                ts.getAg().getScheduler().schedule(new Runnable() {
                    public void run() {
                        resume(true);
                    }
                }, timeout, TimeUnit.MILLISECONDS);
            }
        }

        void resume(final boolean stopByTimeout) {
            // unregister (for not to receive intentionAdded again)
            c.removeEventListener(this);

            // invoke changes in C latter, so to avoid concurrent changes in C
            ts.runAtBeginOfNextCycle(new Runnable() {
                public void run() {
                    try {
                        // add SI again in C.I if it was not removed and this wait was not dropped
                        if (c.removePendingIntention(sTE) == si && !c.getIntentions().contains(si) && !dropped) {
                            if (stopByTimeout && te != null && elapsedTimeTerm == null) {
                                // fail the .wait by timeout
                                if (si.isSuspended()) { // if the intention was suspended by .suspend
                                    PlanBody body = si.peek().getPlan().getBody();
                                    body.add(1, new PlanBodyImpl(BodyType.internalAction, new InternalActionLiteral(".fail")));
                                    c.addPendingIntention(suspend.SUSPENDED_INT+si.getId(), si);
                                } else {
                                    ts.generateGoalDeletion(si, JasonException.createBasicErrorAnnots("wait_timeout", "timeout in .wait"));
                                }
                            } else {
                                si.peek().removeCurrentStep();
                                
                                if (elapsedTimeTerm != null) {
                                    long elapsedTime = System.currentTimeMillis() - startTime;
                                    un.unifies(elapsedTimeTerm, new NumberTermImpl(elapsedTime));
                                }
                                if (si.isSuspended()) { // if the intention was suspended by .suspend
                                    c.addPendingIntention(suspend.SUSPENDED_INT+si.getId(), si);
                                } else {
                                    c.resumeIntention(si);
                                }
                            }
                        }    
                    } catch (Exception e) {
                        ts.getLogger().log(Level.SEVERE, "Error at .wait thread", e);
                    }
                }
            });
            ts.getUserAgArch().getArchInfraTier().wake();
        }

        public void eventAdded(Event e) {
            if (te != null && !dropped && un.unifies(te, e.getTrigger())) {
                resume(false);
            }
        }

        public void intentionDropped(Intention i) {
            if (i.equals(si)) {
                dropped = true;
                resume(false);
            }
        }

        public void intentionAdded(Intention i) { }
        public void intentionResumed(Intention i) { }
        public void intentionSuspended(Intention i, String reason) { }
        public String toString() {
            return sTE;
        }
    }
}
