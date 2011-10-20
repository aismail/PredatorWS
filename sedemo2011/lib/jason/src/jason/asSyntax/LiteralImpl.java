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

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * A Literal extends a Pred with strong negation (~).
 */
public class LiteralImpl extends Pred implements LogicalFormula {

    private static final long serialVersionUID = 1L;
    //private static Logger logger = Logger.getLogger(LiteralImpl.class.getName());
    
    private boolean type = LPos;

    /** creates a positive literal */
    public LiteralImpl(String functor) {
        super(functor);
    }

    /** if pos == true, the literal is positive, otherwise it is negative */
    public LiteralImpl(boolean pos, String functor) {
        super(functor);
        type = pos;
    }

    public LiteralImpl(Literal l) {
        super(l);
        type = !l.negated();
    }
    
    /** if pos == true, the literal is positive, otherwise it is negative */
    public LiteralImpl(boolean pos, Literal l) {
        super(l);
        type = pos;
    }

    protected LiteralImpl(String functor, int terms) {
        super(functor, terms);
    }

    @Override
    public boolean isAtom() {
        return super.isAtom() && !negated();
    }
    
    /** to be overridden by subclasses (as internal action) */
    @Override
    public boolean canBeAddedInBB() {
        return true;
    }
    
    @Override
    public boolean negated() {
        return type == LNeg;
    }
    
    public Literal setNegated(boolean b) {
        type = b;
        resetHashCodeCache();
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;

        if (o instanceof LiteralImpl) {
            final LiteralImpl l = (LiteralImpl) o;
            return type == l.type && hashCode() == l.hashCode() && super.equals(l);
        } else if (o instanceof Atom && !negated()) {
            return super.equals(o);
        }
        return false;
    }

    @Override    
    public String getErrorMsg() {
        String src = getSrcInfo() == null ? "" : " ("+ getSrcInfo() + ")"; 
        return "Error in '"+this+"'"+src;
    }
    
    @Override
    public int compareTo(Term t) {
        if (t == null) 
            return -1;
        if (t.isLiteral()) {
            Literal tl = (Literal)t;
            if (!negated() && tl.negated())
                return -1;
            else if (negated() && !tl.negated())
                return 1;
        }
        return super.compareTo(t);
    }        

    public Term clone() {
        return new LiteralImpl(this);
    }
    
    @Override
    protected int calcHashCode() {
        int result = super.calcHashCode();
        if (negated()) result += 3271;
        return result;
    }

    /** returns [~] super.getPredicateIndicator */
    @Override 
    public PredicateIndicator getPredicateIndicator() {
        if (predicateIndicatorCache == null)
            predicateIndicatorCache = new PredicateIndicator(((type == LPos) ? getFunctor() : "~"+getFunctor()),getArity());
        return predicateIndicatorCache;
    }
    
    public String toString() {
        if (type == LPos)
            return super.toString();
        else
            return "~" + super.toString();
    }

    /** get as XML */
    @Override
    public Element getAsDOM(Document document) {
        Element u = (Element) document.createElement("literal");
        if (negated()) {
            u.setAttribute("negated", negated()+"");
        }
        u.appendChild(super.getAsDOM(document));
        return u;
    }
}
