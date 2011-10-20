// ----------------------------------------------------------------------------
// Copyright (C) 2003 Rafael H. Bordini, Jomi F. Hubner, et al.
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
// 
// To contact the authors:
// http://www.inf.ufrgs.br/~bordini
// http://www.das.ufsc.br/~jomi
//
//----------------------------------------------------------------------------

package jason.asSyntax;

import jason.asSemantics.Unifier;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
     A rule is a Literal (head) with a body, as in "a :- b &amp; c".
    
     @navassoc - body - LogicalFormula
 */
public class Rule extends LiteralImpl {

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(Rule.class.getName());

    private LogicalFormula body   = null;

    public Rule(Literal head, LogicalFormula body) {
        super(head);
        if (head.isRule()) {
            logger.log(Level.SEVERE,"The rule head ("+head+") is a rule!", new Exception());
        } else if (isInternalAction()) {
            logger.log(Level.SEVERE,"The rule head ("+head+") can not be an internal action!", new Exception());
        } else if (head == LTrue || head == LFalse) {
            logger.log(Level.SEVERE,"The rule head ("+head+") can not be a true or false!", new Exception());
        }
        this.body = body;
    }

    @Override
    public boolean isRule() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof Rule) {
            Rule r = (Rule) o;
            return super.equals(o) && body.equals(r.body);
        } 
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode() + body.hashCode();
    }
    
    public LogicalFormula getBody() {
        return body;
    }
    
    @Override
    public Literal makeVarsAnnon(Unifier un) {
        if (body instanceof Literal)
            ((Literal)body).makeVarsAnnon(un);
        return super.makeVarsAnnon(un);
    }
    
    public Rule clone() {
        Rule r = new Rule((Literal)super.clone(), (LogicalFormula)body.clone());
        r.predicateIndicatorCache = null;
        r.resetHashCodeCache();
        return r; 
    }

    public Literal headClone() {
        return (Literal)super.clone();
    }
    
    public String toString() {
        return super.toString() + " :- " + body;
    }

    @Override
    public boolean hasVar(VarTerm t) {
        if (super.hasVar(t)) return true;
        return body.hasVar(t);
    }
    
    public void countVars(Map<VarTerm, Integer> c) {
        super.countVars(c);
        body.countVars(c);
    }

    /** get as XML */
    @Override
    public Element getAsDOM(Document document) {
        Element u = (Element) document.createElement("rule");

        Element h = (Element) document.createElement("head");
        h.appendChild(super.getAsDOM(document));
        
        Element b = (Element) document.createElement("context");
        b.appendChild(body.getAsDOM(document));
        
        u.appendChild(h);
        u.appendChild(b);
        return u;
    }
}
