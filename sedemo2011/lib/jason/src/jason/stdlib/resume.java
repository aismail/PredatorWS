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
import jason.asSemantics.Circumstance;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.Intention;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.Trigger.TEOperator;
import jason.asSyntax.Trigger.TEType;

import java.util.Iterator;

/**
  <p>Internal action:
  <b><code>.resume(<i>G</i>)</code></b>.
  
  <p>Description: resume goals <i>G</i> that was suspended by <code>.suspend</code>.

  <p>Example:<ul> 

  <li> <code>.resume(go(1,3))</code>: resume the goal of go to location 1,3.

  </ul>

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
  @see jason.stdlib.suspend
  @see jason.stdlib.suspended
   
 */
public class resume extends DefaultInternalAction {

    @Override public int getMinArgs() { return 1; }
    @Override public int getMaxArgs() { return 1; }

    @Override protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (!args[0].isLiteral())
            throw JasonException.createWrongArgument(this,"first argument must be a literal");
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        Trigger      g = new Trigger(TEOperator.add, TEType.achieve, (Literal)args[0]);
        Circumstance C = ts.getC();
        
        Iterator<String> ik = C.getPendingIntentions().keySet().iterator();
        while (ik.hasNext()) {
            String k = ik.next();
            if (k.startsWith(suspend.SUSPENDED_INT)) {
                Intention i = C.getPendingIntentions().get(k);
                if (i.hasTrigger(g, un)) {
                    i.setSuspended(false);
                    ik.remove();
                    
                    // remove the IA .suspend in case of self-suspend
                    if (k.equals(suspend.SELF_SUSPENDED_INT))
                        i.peek().removeCurrentStep();
                    
                    // add it back in I if not in PA
                    if (! C.getPendingActions().containsKey(i.getId()))
                        C.resumeIntention(i);
                }
            }
        }
        return true;
    }        
    
}
