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
//----------------------------------------------------------------------------


package jason.stdlib;

import jason.JasonException;
import jason.asSemantics.ActionExec;
import jason.asSemantics.Circumstance;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.Trigger.TEOperator;
import jason.asSyntax.Trigger.TEType;

/**
  <p>Internal action:
  <b><code>.suspend(<i>G</i>)</code></b>.
  
  <p>Description: suspend goals <i>G</i>, i.e., all intentions trying to achieve G will stop 
  running until the internal action <code>.resume</code> change the state of those intentions.  
  A literal <i>G</i>
  is a goal if there is a triggering event <code>+!G</code> in any plan within
  any intention in I, E, PI, or PA.

  <p>Examples:<ul> 

  <li> <code>.suspend(go(1,3))</code>: suspends intentions to go to the location 1,3.
  <li> <code>.suspend</code>: suspends the current intention.

  </ul>

  @see jason.stdlib.suspended
  @see jason.stdlib.intend
  @see jason.stdlib.desire
  @see jason.stdlib.drop_all_desires
  @see jason.stdlib.drop_all_events
  @see jason.stdlib.drop_all_intentions
  @see jason.stdlib.drop_intention
  @see jason.stdlib.drop_desire
  @see jason.stdlib.succeed_goal
  @see jason.stdlib.fail_goal
  @see jason.stdlib.current_intention
  @see jason.stdlib.resume
  
 */
public class suspend extends DefaultInternalAction {
    
    boolean suspendIntention = false;
    public static final String SUSPENDED_INT      = "suspended-";
    public static final String SELF_SUSPENDED_INT = SUSPENDED_INT+"self";

    @Override public int getMinArgs() { return 0; }
    @Override public int getMaxArgs() { return 1; }

    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (args.length == 1 && !args[0].isLiteral())
            throw JasonException.createWrongArgument(this,"first argument must be a literal");
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        suspendIntention = false;
        
        Circumstance C = ts.getC();

        if (args.length == 0) {
            // suspend the current intention
            Intention i = C.getSelectedIntention();
            suspendIntention = true;
            i.setSuspended(true);
            C.addPendingIntention(SELF_SUSPENDED_INT, i);
            return true;
        }
        
        // use the argument to select the intention to suspend.
        
        Trigger      g = new Trigger(TEOperator.add, TEType.achieve, (Literal)args[0]);
        String       k = SUSPENDED_INT+g.getLiteral();
        
        // ** Must test in PA/PI first since some actions (as .suspend) put intention in PI
        
        // suspending from Pending Actions
        for (ActionExec a: C.getPendingActions().values()) {
            Intention i = a.getIntention();
            if (i.hasTrigger(g, un)) {
                i.setSuspended(true);
                C.addPendingIntention(k, i);
            }
        }
        
        // suspending from Pending Intentions
        for (Intention i: C.getPendingIntentions().values()) {
            if (i.hasTrigger(g, un)) { 
                i.setSuspended(true);
            }
        }

        for (Intention i: C.getIntentions()) {
            if (i.hasTrigger(g, un)) {
                i.setSuspended(true);
                C.removeIntention(i);
                C.addPendingIntention(k, i);
            }
        }
        
        // suspending the current intention?
        Intention i = C.getSelectedIntention();
        if (i.hasTrigger(g, un)) {
            suspendIntention = true;
            i.setSuspended(true);
            C.addPendingIntention(SELF_SUSPENDED_INT, i);
        }
            
        // suspending G in Events
        for (Event e: C.getEvents()) {
            i = e.getIntention();
            if ( i != null && 
                    (i.hasTrigger(g, un)) ||       // the goal is in the i's stack of IM
                     un.unifies(e.getTrigger(), g) // the goal is the trigger of the event
                    ) {
                i.setSuspended(true);
                C.removeEvent(e);                    
                C.addPendingIntention(k, i);
            } else if (i == Intention.EmptyInt && un.unifies(e.getTrigger(), g)) { // the case of !!
                ts.getLogger().warning("** NOT IMPLEMENTED ** (suspend of !!)");
            }
        }
        return true;
    }

    @Override
    public boolean suspendIntention() {
        return suspendIntention;
    }
}
