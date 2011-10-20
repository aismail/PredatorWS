package test;

import static jason.asSyntax.ASSyntax.createNumber;
import static jason.asSyntax.ASSyntax.createStructure;
import static jason.asSyntax.ASSyntax.parseLiteral;
import static jason.asSyntax.ASSyntax.parseTerm;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.LiteralImpl;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.ObjectTerm;
import jason.asSyntax.ObjectTermImpl;
import jason.asSyntax.Plan;
import jason.asSyntax.Pred;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.UnnamedVar;
import jason.asSyntax.VarTerm;
import jason.asSyntax.Trigger.TEOperator;
import jason.asSyntax.Trigger.TEType;
import jason.asSyntax.parser.ParseException;
import jason.bb.BeliefBase;
import jason.bb.DefaultBeliefBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

/** JUnit test case for syntax package */
public class TermTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testEquals() {
        Structure t1, t2, t3;
        t1 = new Structure("pos");
        t2 = new Structure(t1);
        t3 = new Structure("pos");
        assertTrue(t1.equals(t2));
        assertTrue(t1.equals(t3));
        
        t1.addTerm(new Atom("a"));
        assertFalse(t1.equals(t2));
        
        t2.addTerm(new Atom("a"));
        assertEquals(new Atom("a"),new Atom("a"));
        assertTrue(t1.equals(t2));
        assertTrue(t2.equals(t1));
        assertEquals(t1.hashCode(),t2.hashCode());

        Structure targ1 = new Structure("b");
        targ1.addTerm(new Atom("1"));
        Structure targ2 = new Structure("b");
        targ2.addTerm(new Atom("2"));

        t1.addTerm(targ1);
        assertFalse(t1.equals(t2));
        
        Structure targ1a = new Structure("b");
        targ1a.addTerm(new Structure("1"));
        t3.addTerm(new Structure("a"));
        t3.addTerm(targ1a);
        assertTrue(t1.equals(t3));
        
        // tests with variables
        t1.addTerm(new Structure("c"));
        t3.addTerm(new VarTerm("X"));
        assertFalse(t1.equals(t3));
        
        Literal l3 = new LiteralImpl(true, new Pred("pos"));
        l3.addAnnot(BeliefBase.TPercept);
        Literal l4 = new LiteralImpl(true, new Pred("pos"));
        l4.addAnnot(BeliefBase.TPercept);
        assertEquals(l3, l4);
        
        Term tpos = new Atom("pos");
        assertFalse(l3.isAtom());
        assertFalse(l3.equals(tpos));
        assertFalse(tpos.equals(l3));
        //System.out.println(new Term("pos")+"="+l3+" --> "+new Term("pos").equals(l3));

        assertFalse(new Pred("pos").equals(l3));
        assertTrue(new Pred("pos").equalsAsStructure(l3));
        Pred panot = new Pred("pos");
        panot.addAnnot(new Structure("bla"));
        assertTrue(l3.equalsAsStructure(panot));
        
        // basic VarTerm test
        assertTrue(new VarTerm("X").equals(new VarTerm("X")));
        assertFalse(new VarTerm("X").equals(new VarTerm("Y")));
        assertFalse(new VarTerm("X").equals(new Structure("X")));
        
        VarTerm x1 = new VarTerm("X1");
        x1.setValue(new Structure("a"));
        assertFalse(x1.equals(new VarTerm("X1")));
        
        VarTerm x2 = new VarTerm("X2");
        x2.setValue(new Structure("a"));
        assertTrue(x1.equals(x2));
        assertTrue(x2.equals(x1));
        assertEquals(x1.hashCode(), x2.hashCode());
        
        Term ta = new Structure("a");
        assertTrue(x1.equals(ta));
        assertTrue(ta.equals(x1));
        assertEquals(x1.hashCode(), ta.hashCode());
    }

    public void testUnifies() throws ParseException {
        assertTrue(new Unifier().unifies(new Structure("a"), new Structure("a")));
        assertTrue(new Unifier().unifies(ASSyntax.parseTerm("a"), ASSyntax.parseTerm("a")));
        assertTrue(new Unifier().unifies(new Structure("a"), new VarTerm("X")));
        
        Unifier u = new Unifier();
        VarTerm b = new VarTerm("B");
        VarTerm x = new VarTerm("X");
        assertTrue(u.unifies(b, x));
        assertTrue(u.unifies(new Structure("a"), x));
        //System.out.println("u="+u);
        assertEquals(u.get(b).toString(), "a");
        assertEquals(u.get(x).toString(), "a");
        b.apply(u);
        //System.out.println("x="+x);
        //System.out.println("b="+b);
        assertEquals(b.toString(), "a");
        assertEquals(x.toString(), "X");
        
        u = new Unifier();
        Structure t1, t2, t3;
        
        t1 = new Structure("pos");
        t2 = new Structure(t1);
        t3 = new Structure(t1);

        t1.addTerm(new Structure("1"));
        t1.addTerm(new Structure("2"));

        t2.addTerm(new VarTerm("X"));
        t2.addTerm(new VarTerm("X"));
        assertFalse(u.unifies(t1,t2));

        u = new Unifier();
        t3.addTerm(new VarTerm("X"));
        t3.addTerm(new VarTerm("Y"));
        //System.out.println(t1+"="+t3);
        assertTrue( u.unifies(t1,t3));
        //System.out.println("u="+u);
    
        // Test var unified with var
        u = new Unifier();
        VarTerm z1 = new VarTerm("Z1");
        VarTerm z2 = new VarTerm("Z2");
        VarTerm z3 = new VarTerm("Z3");
        VarTerm z4 = new VarTerm("Z4");
        // Z1 = Z2 = Z3 = Z4
        assertTrue(u.unifies(z1,z2));
        assertTrue(u.unifies(z2,z3));
        assertTrue(u.unifies(z2,z4));
        
        assertTrue(z1.isVar()); // z1 is still a var
        assertTrue(z2.isVar()); // z2 is still a var
        
        assertTrue(u.unifies(z2,new Structure("a")));
        //System.out.println("u="+u);
        assertEquals(u.get("Z1").toString(), "a");
        assertEquals(u.get("Z2").toString(), "a");
        assertEquals(u.get("Z3").toString(), "a");
        assertEquals(u.get("Z4").toString(), "a");
    }
    
    public void testAnnotsUnify1() {
        Unifier u = new Unifier();
        Pred p1, p2;
        
        p1 = new Pred("pos");
        p2 = new Pred("pos");

        p1.addTerm(new Structure("1"));
        p2.addTerm(new Structure("1"));
        
        p2.addAnnot(new Structure("percept"));
        //System.out.println("p1="+p1+"; p2="+p2);
        assertTrue(u.unifies(p1, p2));
    }
    
    public void testAnnotsUnify2() {
        Unifier u = new Unifier();
        Pred p1, p2;
        
        p1 = new Pred("pos");
        p2 = new Pred("pos");

        p1.addTerm(new Structure("1"));
        p2.addTerm(new Structure("1"));
        
        p1.addAnnot(new VarTerm("X"));
        p2.addAnnot(new Structure("ag1"));
        
        // pos(1)[X]=pos(1)[ag1]
        assertTrue(u.unifies(p1, p2));
        
        assertEquals(u.get("X").toString(),"ag1");
        
        p1.addAnnot(new Structure("ag2"));
        p2.addAnnot(new VarTerm("Y"));
        u = new Unifier();
        // pos(1)[X,ag2] = pos(1)[ag1,Y]
        //assertTrue(u.unifies(p1, p2));
        //System.out.println("u="+u);
        
        p1.addAnnot(new VarTerm("Z"));
        p2.addAnnot(new Structure("ag2"));
        p2.addAnnot(new Structure("ag4"));
        //System.out.println("p1="+p1+"; p2="+p2);
        u = new Unifier();
        assertTrue(u.unifies(p1, p2));
        //System.out.println("u="+u);

        p1.addAnnot(new VarTerm("X1"));
        p1.addAnnot(new VarTerm("X2"));
        p1.addAnnot(new VarTerm("X3"));
        //System.out.println("p1="+p1+"; p2="+p2);
        u = new Unifier();
        assertFalse(u.unifies(p1, p2));
        //System.out.println("u="+u);

        p1.clearAnnots();
        p1.addAnnot(new Structure("ag2"));
        p2.clearAnnots();
        p2.addAnnot(new Structure("ag1"));
        p2.addAnnot(new Structure("ag2"));
        p2.addAnnot(new Structure("ag3"));
        //System.out.println("p1="+p1+"; p2="+p2);
        u = new Unifier();
        assertTrue(u.unifies(p1, p2));
        //System.out.println("u="+u);
    }
    
    public void testAnnotsUnify3() {
        Literal l1 = Literal.parseLiteral("s(tuesday)");
        Unifier u = new Unifier();
        u.unifies(l1, Literal.parseLiteral("s(Day)"));
        assertEquals(u.get("Day").toString(),"tuesday");
        
        Literal l2 = Literal.parseLiteral("bel[monday]");
        Literal l3 = Literal.parseLiteral("bel[Day]");
        assertFalse(u.unifies(l3, l2));
        assertEquals(u.get("Day").toString(),"tuesday");
    }
    
    public void testAnnotsUnify4() {
        Literal l1 = Literal.parseLiteral("s[A]");
        Literal l2 = Literal.parseLiteral("s[3]");
        Unifier u = new Unifier();
        assertTrue(u.unifies(l1, l2));
        assertEquals(u.get("A").toString(),"3");
    }

    public void testAnnotsUnify5() {
        Literal l1 = Literal.parseLiteral("s[source(self)]");
        Literal l2 = Literal.parseLiteral("s");
        Unifier u = new Unifier();
        assertFalse(u.unifies(l1, l2));
        assertTrue(u.unifies(l2, l1));
    }

    public void testAnnotsUnify6() {
        Literal lp = Literal.parseLiteral("s(1)[b]");
        Literal ln = Literal.parseLiteral("~s(1)[b]");
        assertTrue(lp.isLiteral());
        assertTrue(ln.isLiteral());
        assertFalse(lp.negated());
        assertTrue(ln.negated());
        
        Unifier u = new Unifier();

        // Literal and literal
        assertFalse(u.unifies(lp, ln));
        
        // Literal and predicate
        Pred p = Pred.parsePred("s(1)[b]");
        assertTrue(p.isLiteral());
        assertTrue(u.unifies(lp, p));
        assertFalse(u.unifies((Term)ln, (Term)p));
        assertTrue(u.unifies(Literal.parseLiteral("s(1)"), p));
        assertFalse(u.unifies(p,Literal.parseLiteral("s(1)")));
        
        // Literal and structure
        Structure s = new Structure("s");
        s.addTerm(new NumberTermImpl(1));
        assertTrue(u.unifies(s,lp));
        assertFalse(u.unifies(lp,s));
        assertFalse(u.unifies(ln, s));
        assertFalse(u.unifies(s,ln));
        
        // Literal and Atom
        Atom a = new Atom("s");
        assertFalse(u.unifies(lp, a));
        assertFalse(u.unifies(ln, a));
        assertTrue(u.unifies(a, Literal.parseLiteral("s")));
        assertTrue(Literal.parseLiteral("s").isAtom());
        assertTrue(Literal.parseLiteral("s").equals(a));
        assertTrue(u.unifies(Literal.parseLiteral("s"), a));
        assertFalse(u.unifies(Literal.parseLiteral("~s"), a));
     
        // Predicate and structure
        assertTrue(u.unifies(s, p));
        assertFalse(u.unifies(p,s));
        
        // Predicate and atom
        assertFalse(u.unifies(a, p));
        assertFalse(u.unifies(p, a));
        assertTrue(u.unifies(Pred.parsePred("s"), a));
        assertFalse(u.unifies(Pred.parsePred("s[b]"), a));
        assertTrue(u.unifies(a,Pred.parsePred("s[b]")));
    }

    public void testAnnotsUnify7() throws ParseException {
        // p[a,b,c,d] = p[a,c|R] - ok and R=[b,d]
        Term t1 = parseTerm("p[c,a,b,d,c]");
        Term t2 = parseTerm("p[c,a,c|R]");
        Unifier u = new Unifier();
        assertTrue(u.unifies(t1, t2));
        assertEquals("[b,d]",u.get("R").toString());
        
        // p[a,c|R] = p[a,b,c,d] - ok and R=[b,d]
        u = new Unifier();
        assertTrue(u.unifies(t2, t1));
        assertEquals(u.get("R").toString(),"[b,d]");

        // p[H|R] = p[a,b,c,d] - ok and R=[b,c,d], H=a
        Term t3 = parseTerm("p[H|R]");
        u = new Unifier();
        assertTrue(u.unifies(t1, t3));
        assertEquals(u.get("H").toString(),"a");
        assertEquals(u.get("R").toString(),"[b,c,d]");
    }  

    public void testApplyAnnots() throws ParseException {
        Term t1 = parseTerm("p[a,X,c,d]");
        Unifier u = new Unifier();
        u.unifies(new VarTerm("X"), new Atom("z"));
        t1.apply(u);
        assertEquals("p[a,c,d,z]",t1.toString());
        
        t1 = parseTerm("p[X,b,c,d]");
        t1.apply(u);
        assertEquals("p[b,c,d,z]",t1.toString());

        t1 = parseTerm("p[a,b,c,X]");
        t1.apply(u);
        assertEquals("p[a,b,c,z]",t1.toString());
    }
    
    public void testTrigger() {
        Pred p1 = new Pred("pos");

        p1.addTerm(new VarTerm("X"));
        p1.addTerm(new VarTerm("Y"));
    }
    
    public void testTriggetAnnot() throws ParseException {
        Literal content = Literal.parseLiteral("~alliance");
        content.addSource(new Structure("ag1"));
        Literal received = new LiteralImpl(Literal.LPos, new Pred("received"));
        received.addTerm(new Structure("ag1"));
        received.addTerm(new Structure("tell"));
        received.addTerm(content);
        received.addTerm(new Structure("id1"));
        
        Trigger t1 = new Trigger(TEOperator.add, TEType.belief, received);

        Literal received2 = new LiteralImpl(Literal.LPos, new Pred("received"));
        received2.addTerm(new VarTerm("S"));
        received2.addTerm(new Structure("tell"));
        received2.addTerm(new VarTerm("C"));
        received2.addTerm(new VarTerm("M"));
        
        Trigger t2 = new Trigger(TEOperator.add, TEType.belief, received2);
        
        //System.out.println("t1 = "+t1);
        //System.out.println("t2 = "+t2);
        Unifier u = new Unifier();
        assertTrue(u.unifies(t1,t2));
        //System.out.println(u);
        t2.apply(u);
        //System.out.println("t2 with apply = "+t2);
        
        assertEquals(t1.toString(), t2.toString());
        
        Trigger t3 = ASSyntax.parseTrigger("+!bid_normally(1)");
        Trigger t4 = ASSyntax.parseTrigger("+!bid_normally(N)");
        u = new Unifier();
        u.unifies(t3,t4);
        //System.out.println("u="+u);
        assertEquals(u.get("N").toString(), "1");
        
    }
    
    public void testLiteralUnify() {
        Literal content = Literal.parseLiteral("~alliance");
        content.addSource(new Structure("ag1"));
        Literal l1 = new LiteralImpl(Literal.LPos, new Pred("received"));
        l1.addTerm(new Structure("ag1"));
        l1.addTerm(new Structure("tell"));
        l1.addTerm(content);
        l1.addTerm(new Structure("id1"));

        
        Literal l2 = Literal.parseLiteral("received(S,tell,C,M)");
        Unifier u = new Unifier();
        assertTrue(u.unifies(l1,l2));
        //System.out.println(u);
        l2.apply(u);
        //System.out.println("l2 with apply = "+l2);
        assertEquals(l1.toString(), l2.toString());
        
        assertFalse(new Unifier().unifies(Literal.parseLiteral("c(x)"), Literal.parseLiteral("c(20)")));
        assertTrue(new Unifier().unifies(Literal.parseLiteral("c(20)"), Literal.parseLiteral("c(20)")));
        assertTrue(new Unifier().unifies(Literal.parseLiteral("c(X)"), Literal.parseLiteral("c(20)")));
        
        assertTrue(new Unifier().unifies(Literal.parseLiteral("c(t)"), Literal.parseLiteral("c(t)")));
        assertTrue(new Unifier().unifies(Literal.parseLiteral("~c(t)"), Literal.parseLiteral("~c(t)")));
        assertFalse(new Unifier().unifies(Literal.parseLiteral("c(t)"), Literal.parseLiteral("~c(t)")));
        assertFalse(new Unifier().unifies(Literal.parseLiteral("~c(t)"), Literal.parseLiteral("c(t)")));
    }
    
    public void testSubsetAnnot() throws ParseException {
        Pred p1 = Pred.parsePred("p1(t1,t2)[a1,a(2,3),a(3)]");
        assertTrue(p1.hasAnnot(parseTerm("a1")));
        assertTrue(p1.hasAnnot(parseTerm("a(2,3)")));
        assertTrue(p1.hasAnnot(parseTerm("a(3)")));
        assertFalse(p1.hasAnnot(parseTerm("a(4)")));
        assertFalse(p1.hasAnnot(parseTerm("a")));
        assertFalse(p1.hasAnnot(parseTerm("4")));
        assertTrue(p1.hasSubsetAnnot(p1));
        assertTrue(p1.hasSubsetAnnot(p1, new Unifier()));        
        
        Pred p2 = Pred.parsePred("p2(t1,t2)[a(2,3),a(3)]");
        assertTrue(p2.hasSubsetAnnot(p1));
        assertFalse(p1.hasSubsetAnnot(p2));
        assertTrue(p2.hasSubsetAnnot(p1, new Unifier()));
        assertFalse(p1.hasSubsetAnnot(p2, new Unifier()));
        
        Literal l1 = Literal.parseLiteral("pos[source(ag3)]");
        Literal l2 = Literal.parseLiteral("pos[source(ag1),source(ag2)]");
        assertFalse(l1.hasSubsetAnnot(l2));
        assertFalse(l2.hasSubsetAnnot(l1));
        assertFalse(l1.hasSubsetAnnot(l2, new Unifier()));
        assertFalse(l2.hasSubsetAnnot(l1, new Unifier()));
        
        l1 = Literal.parseLiteral("pos[a,b,source(ag1),source(percept)]");
        l2 = Literal.parseLiteral("pos[a]");
        assertFalse(l1.hasSubsetAnnot(l2));
        assertTrue(l2.hasSubsetAnnot(l1));
        assertFalse(l1.hasSubsetAnnot(l2, new Unifier()));
        assertTrue(l2.hasSubsetAnnot(l1, new Unifier()));
                                                 
        Pred p3 = Pred.parsePred("p2(t1,t2)[a(A,_),a(X)]");
        Unifier u = new Unifier();
        assertTrue(p3.hasSubsetAnnot(p2,u));
        assertEquals(u.get("A").toString(),"2");
        assertEquals(u.get("X").toString(),"3");
        assertTrue(p3.hasSubsetAnnot(p1,u));
        
        Pred p4 = Pred.parsePred("p1(t1,t2)[a1|T]");

        //List<Unifier> r = new ArrayList<Unifier>();
        //assertTrue(p1.getSubsetAnnots(p4.getAnnots(),new Unifier(),r));
        //assertEquals(r.get(0).get("T").toString(), "[a(2,3),a(3)]");
        
        u = new Unifier();
        assertTrue(p1.hasSubsetAnnot(p4, u));
        assertEquals(u.get("T").toString(), "[a(3),a(2,3)]");

        Pred p5 = Pred.parsePred("p1(t1,t2)[a1|[a(2,3),a(3)]]");
        u = new Unifier();
        assertTrue(p1.hasSubsetAnnot(p5, u));

        Pred p6 = Pred.parsePred("p1(t1,t2)[a(3)|T]");
        u = new Unifier();
        assertTrue(p6.hasSubsetAnnot(p1, u));
        assertEquals(u.get("T").toString(), "[a1,a(2,3)]");
        assertTrue(p1.hasSubsetAnnot(p6, u));

        Pred p7 = Pred.parsePred("p1(t1,t2)[A|T]");
        u = new Unifier();
        assertTrue(p7.hasSubsetAnnot(p1, u));
        assertEquals("a1", u.get("A").toString() );
        assertEquals("[a(3),a(2,3)]", u.get("T").toString());
        assertTrue(p1.hasSubsetAnnot(p7, u));

        u = new Unifier();
        assertTrue(p1.hasSubsetAnnot(p7, u));
        assertEquals("a1", u.get("A").toString() );
        assertEquals("[a(3),a(2,3)]", u.get("T").toString());

        // test many vars
        l1 = Literal.parseLiteral(                         "p[4,W,a,b,X,Y,e,Z]");
        assertTrue( Literal.parseLiteral("p[4]").hasSubsetAnnot(l1));
        assertTrue( Literal.parseLiteral("p[4]").hasSubsetAnnot(l1, new Unifier()));
        u = new Unifier();
        assertTrue(l1.hasSubsetAnnot( Literal.parseLiteral("p[4,5,a,b,c,d,e,f]"), u));
        assertEquals( 4, u.size()); // all var of l1 needs a value
        assertEquals("5", u.get("W").toString());
        assertEquals("c", u.get("X").toString());
        assertEquals("d", u.get("Y").toString());
        assertEquals("f", u.get("Z").toString());

        l1 = Literal.parseLiteral(                         "p[4,W,a,b,X,Y,e,Z,U]");
        u = new Unifier();
        assertFalse(l1.hasSubsetAnnot( Literal.parseLiteral("p[4,5,a,b,c,d,e,f]"), u));

        // test clone of annot used in hasSubsetAnnot
        l1 = Literal.parseLiteral("p[b(3),a(Y)|T]");
        l2 = Literal.parseLiteral("p[a(1),c(H),b(X),d(4)]");
        u = new Unifier();
        assertTrue( l2.hasSubsetAnnot(l1, u));
        assertEquals( "1", u.get("Y").toString());
        assertEquals( "3", u.get("X").toString());
        assertEquals( "[c(H),d(4)]", u.get("T").toString());
        // if we apply H n l2, we should not change H on the unifier
        u.unifies(new Atom("xx"), new VarTerm("H"));
        l2.apply(u);
        assertEquals( "[c(H),d(4)]", u.get("T").toString());

        // test clone of pannot used in hasSubsetAnnot
        l1 = Literal.parseLiteral("p[b(3),a(Y)|T]");
        l2 = Literal.parseLiteral("p[a(1),c(H),b(X),d(4)]");
        u = new Unifier();
        assertTrue( l1.hasSubsetAnnot(l2, u));
        assertEquals( "1", u.get("Y").toString());
        assertEquals( "3", u.get("X").toString());
        assertEquals( "[c(H),d(4)]", u.get("T").toString());
        // if we apply H n l2, we should not change H on the unifier
        u.unifies(new Atom("xx"), new VarTerm("H"));
        l2.apply(u);
        assertEquals( "[c(H),d(4)]", u.get("T").toString());    
    }
    
    public void testAnnotUnifAsList() {
        Pred p1 = Pred.parsePred("p[b(2),x]");
        Pred p2 = Pred.parsePred("p[a,b(2),c]");
        Unifier u = new Unifier();
        
        assertFalse(u.unifies(p1,p2));
        
        p1 = Pred.parsePred("p(t1,t2)[z,a(1),a(2,3),a(3)]");
        p2 = Pred.parsePred("p(t1,B)[a(X)|R]");

        assertTrue(u.unifies(p2,p1));
        assertEquals("[z,a(3),a(2,3)]", u.get("R").toString());
        
        u = new Unifier();
        assertTrue(u.unifies(p1,p2));
        //System.out.println(u+"-"+p2);
        p2.apply(u);
        assertEquals("p(t1,t2)[z,a(1),a(3),a(2,3)]", p2.toString());
    }
    
    public void testCompare() throws ParseException {
        // order is: numbers, strings, lists, literals (by arity, functor, terms, annots), variables
        // variables must be the last, subsetannots requires that
        
        Pred p1 = Pred.parsePred("a");
        Pred p2 = Pred.parsePred("b");
        
        assertEquals(p1.compareTo(p2), -1);
        assertEquals(p1.compareTo(null), -1);
        assertEquals(p2.compareTo(p1), 1);
        assertEquals(p1.compareTo(p1), 0);
        
        p1 = Pred.parsePred("a(3)[3]");
        p2 = Pred.parsePred("a(3)[10]");
        Pred p3 = Pred.parsePred("a(3)[10]");        
        assertEquals(1, p2.compareTo(p1));
        assertEquals(-1, p1.compareTo(p2));
        assertEquals(0, p2.compareTo(p3));
        
        Term p4 = parseTerm("a");
        assertTrue(p1.compareTo(p4) > 0);
        assertTrue(p4.compareTo(p1) < 0);

        Literal l1 = Literal.parseLiteral("~a(3)");
        Literal l2 = Literal.parseLiteral("a(3)");
        Literal l3 = Literal.parseLiteral("a(10)[5]");
        assertTrue(l1.compareTo(l2) == 1);
        assertTrue(l1.compareTo(l3) == 1);
        assertTrue(l2.compareTo(l3) == -1);

        assertTrue(l2.compareTo(new Atom("g")) > 0);
        assertTrue(new Atom("g").compareTo(l2) < 0);
        assertTrue(new Atom("g").compareTo(new Atom("g")) == 0);

        ListTerm lt1 = ListTermImpl.parseList("[3,10]");
        ListTerm lt2 = ListTermImpl.parseList("[3,4]");
        ListTerm lt3 = ListTermImpl.parseList("[1,1,1]");
        assertTrue(lt1.compareTo(lt2) > 0);
        assertTrue(lt1.compareTo(lt3) < 0);
        assertTrue(lt1.compareTo(p1) < 0);
        
        ListTerm l = ListTermImpl.parseList("[C,b(4),A,4,b(1,1),\"x\",[],[c],[a],[b,c],[a,b],~a(3),a(e,f),b,a(3),b(3),a(10)[30],a(10)[5],a,a(d,e)]");
        Collections.sort(l);
        assertEquals("[4,\"x\",[],[a],[c],[a,b],[b,c],a,b,a(3),a(10)[5],a(10)[30],b(3),b(4),a(d,e),a(e,f),b(1,1),~a(3),A,C]", l.toString());
        
        l = ListTermImpl.parseList("[b,[1,1,1],c,10,g,casa,f(10),5,[3,10],f(4),[3,4]]");
        Collections.sort(l);
        assertEquals("[5,10,[3,4],[3,10],[1,1,1],b,c,casa,g,f(4),f(10)]",l.toString());
        
        Term t1 = ASSyntax.createNumber(10);
        Term t2 = ASSyntax.createNumber(10);
        assertEquals(0, t1.compareTo(t2));
        Term v1 = new VarTerm("X1");
        Unifier u = new Unifier();
        u.unifies(v1, t1);
        v1.apply(u);
        assertEquals(0, v1.compareTo(t2));
        assertEquals(0, t2.compareTo(v1));
        
        assertTrue(new StringTermImpl("string").compareTo(new NumberTermImpl(1)) > 0);
        assertTrue(new NumberTermImpl(1).compareTo(new StringTermImpl("string")) < 0);
        assertTrue(new StringTermImpl("string").compareTo(new ListTermImpl()) < 0);
        assertTrue(new ListTermImpl().compareTo(new StringTermImpl("string")) > 0);
    }
    
    public void testUnify4() throws ParseException {
        Term a1 = ASSyntax.parseTerm("a(1)");
        Term a2 = ASSyntax.parseTerm("a(X+1)");
        Unifier u = new Unifier();
        u.unifies(new VarTerm("X"),new NumberTermImpl(0));
        assertFalse(a1.equals(a2));   
    }

    public void testUnify5() {
        Structure s1 = Structure.parse("a(X,Y,10)");
        Structure s2 = Structure.parse("a(1,2,1)");
        Unifier u = new Unifier();
        assertFalse(u.unifies(s1,s2));
        assertEquals(u.size(),0);
    }

    public void testMakeVarAnnon1() {
        Literal l1 = Literal.parseLiteral("likes(jane,X,peter)");
        Literal l2 = Literal.parseLiteral("likes(X,Y,Y)");
        Literal l3 = Literal.parseLiteral("likes(X,Y,X)");
        Literal l4 = Literal.parseLiteral("likes(Z,Y,Y)");
        Unifier u = new Unifier();                
        assertFalse(u.unifies(l1, l2));
        u.clear();      
        assertFalse(u.unifies(l1, l3));
        u.clear();      
        assertTrue(u.unifies(l1, l4));
        
        l2.makeVarsAnnon();
        u.clear();      
        assertTrue(u.unifies(l1, l2));      

        l3.makeVarsAnnon();
        u.clear();
        assertFalse(u.unifies(l1, l3));     

        l4.makeVarsAnnon();
        u.clear();      
        assertTrue(u.unifies(l1, l4));      
    }
    
    public void testMakeVarAnnon2() {
        Literal l1 = Literal.parseLiteral("calc(AgY,QuadY2,QuadY2)");
        Literal l2 = Literal.parseLiteral("calc(32,33,V)");
        Unifier u = new Unifier();
        assertTrue(u.unifies(l1, l2));
        l2.makeVarsAnnon();
        u.clear();
        assertTrue(u.unifies(l1, l2));
        
        // ensure that the anonymized instance of AgY unified to 32
        assertEquals(u.get((VarTerm) l1.getTerm(0)), new NumberTermImpl(32));
        // ensure that the first anonymized instance of QuadY2 unifies to 33
        assertEquals(u.get((VarTerm) l1.getTerm(1)), new NumberTermImpl(33));
        // ensure that the second anonymized instance of QuadY2 unifies to 33
        assertEquals(u.get((VarTerm) l1.getTerm(2)), new NumberTermImpl(33));
        // ensure that the anonymized instance of V unifies to 33
        assertEquals(u.get((VarTerm) l2.getTerm(2)), new NumberTermImpl(33));
        
        l2.apply(u);
        assertEquals("calc(32,33,33)", l2.toString());
        l1.apply(u);
        assertEquals("calc(32,33,33)", l1.toString());

    }

    public void testMakeVarAnnon3() {
        Literal l1 = Literal.parseLiteral("calc(AgY,X)[vl(X),source(AgY),bla(Y),X]");
        l1.makeVarsAnnon();
        Map<VarTerm, Integer> v = new HashMap<VarTerm, Integer>();
        l1.countVars(v);
        assertEquals(3, v.size());
        assertEquals("vl("+l1.getTerm(1)+")",l1.getAnnots("vl").get(0).toString());
        
        l1 = Literal.parseLiteral("calc(a)[a,b|T]");
        l1.makeVarsAnnon();
        assertTrue(l1.toString().contains("_"));
        assertFalse("calc(a)[a,b|T]".equals(l1.toString()));
    }
    
    public void testMakeVarAnnon4() {
        Literal l = Literal.parseLiteral("p(X)");
        Unifier u = new Unifier();
        u.unifies(new UnnamedVar(4), new VarTerm("X"));
        u.unifies(new VarTerm("X"), new UnnamedVar(2));
        u.unifies(new UnnamedVar(2), new VarTerm("Y"));
        u.unifies(new UnnamedVar(10), new VarTerm("Y"));
        u.unifies(new VarTerm("X"), new VarTerm("Z"));
        /*
        Iterator<VarTerm> i = u.binds(new VarTerm("X"));
        while (i.hasNext()) {
            System.out.println(i.next());
        }
        */
        l.makeVarsAnnon(u);
        // ensure that X derefs to _10
        assertTrue(u.deref(new VarTerm("X")).equals(new UnnamedVar(10)));
        // ensure that unifying a value with X will bind a value for all aliases as well.
        Term val = new StringTermImpl("value");
        u.unifies(new VarTerm("X"), val);
        assertTrue(u.get(new VarTerm("X")).equals(val));
        assertTrue(u.get(new VarTerm("Y")).equals(val));
        assertTrue(u.get(new VarTerm("Z")).equals(val));
        assertTrue(u.get(new UnnamedVar(4)).equals(val));
        assertTrue(u.get(new UnnamedVar(2)).equals(val));
        assertTrue(u.get(new UnnamedVar(10)).equals(val));
    }

    public void testMakeVarAnnon5() {
        Literal l = Literal.parseLiteral("p(X,Y)[s(Y)]");
        Unifier u = new Unifier();
        u.unifies(new VarTerm("X"), new VarTerm("Y"));
        l.makeVarsAnnon(u);
        assertEquals(l.getTerm(0), l.getTerm(1));
        assertEquals("[s("+l.getTerm(0)+")]", l.getAnnots().toString());
    }
    
    public void testMakeVarAnnon6() {
        UnnamedVar v1 = new UnnamedVar();
        UnnamedVar v2 = new UnnamedVar();
        
        Literal l = ASSyntax.createLiteral("p", v1, v2, ASSyntax.createList(v1,v2));
        l.makeVarsAnnon();
        
        // vars inside the list should be replaced as var placed in term0 and term1
        assertEquals(l.getTerm(0).toString(), ((ListTerm)l.getTerm(2)).get(0).toString());
    }
    
    // test from Tim Cleaver
    public void testMakeVarsAnnon6() {
        // if we make a literal anonymous multiple times, the instances should not
        // be equal but should eb unifiable.
        Literal literal = Literal.parseLiteral("literal(Variable, _)");
        List<Literal> literals = new ArrayList<Literal>();
        literals.add(literal);
        // create a list of anonymized l1s
        for (int i = 0; i < 5; i++) {
            literals.add((literal.copy()).makeVarsAnnon());
        }
        // ensure that all the anonymizations of Variable are different
        // ensure that all the anonymizations of _ are different
        // ensure that all pairs are unifiable
        for (Literal l1 : literals) {
            for (Literal l2 : literals) {
                if (l1 == l2) {
                    continue;
                }
                assertFalse(l1.getTerm(0).equals(l2.getTerm(0)));
                assertFalse(l1.getTerm(1).equals(l2.getTerm(1)));
                assertTrue(new Unifier().unifies(l1, l2));
            }
        }
    }
    
    public void testAddAnnots() {
        Literal p1 = Literal.parseLiteral("p1");
        Literal p2 = Literal.parseLiteral("p2[a1,a2]");
        Literal p3 = Literal.parseLiteral("p3[a2,a3,a4,a8,a5,a6,a7]");
        
        assertEquals("p3[a2,a3,a4,a5,a6,a7,a8]",p3.toString()); // annots should be ordered
        p1.addAnnots(Literal.parseLiteral("p").getAnnots());
        assertFalse(p1.hasAnnot());
        
        p1.addAnnots(p2.getAnnots());
        assertEquals(p1.getAnnots(),p2.getAnnots());
        
        p1.addAnnots(p3.getAnnots());
        assertEquals(8,p1.getAnnots().size());
    }
    
    public void testGetSources() {
        Literal p1 = Literal.parseLiteral("p1");
        assertEquals(0, p1.getSources().size());
        
        assertEquals(1, Literal.parseLiteral("p2[source(a)]").getSources().size());

        Literal p2 = Literal.parseLiteral("p2[a1,source(ag1),a2,source(ag2),source(ag3)]");
        assertEquals(3, p2.getSources().size());
        
        assertEquals("[ag1,ag2,ag3]",p2.getSources().toString());   
    }
    
    public void testImportAnnots() {
        Literal p1 = Literal.parseLiteral("p1");
        Literal p2 = Literal.parseLiteral("p2[a1,a2]");
        Literal p3 = Literal.parseLiteral("p3[a2,a3,a8,a4,a5,a6,a7]");
        
        assertTrue(p1.importAnnots(p2));
        assertEquals(2,p1.getAnnots().size());
        assertEquals(2,p2.getAnnots().size());

        assertFalse(p1.importAnnots(p2));
        assertEquals(2,p1.getAnnots().size());
        assertEquals(0,p2.getAnnots().size());
        
        assertTrue(p1.importAnnots(p3));
        assertEquals(8,p1.getAnnots().size());
        assertEquals(6,p3.getAnnots().size());

        assertFalse(p1.importAnnots(p3));
        assertFalse(p1.importAnnots(p2));
        
        assertTrue(p2.importAnnots(p1));

        assertEquals(8,p2.getAnnots().size());
    }
    
    public void testGetTermsArray() {
        Structure s2 = createStructure("a");
        s2.addTerms(createNumber(1), createNumber(2), createNumber(3));
        Term[] a = s2.getTermsArray();
        assertEquals(3,a.length);
        assertEquals("1",a[0].toString());
        assertEquals("3",a[2].toString());
    }
    
    public void testIALiteral() throws ParseException {
        Literal l = parseLiteral(".print(a)");
        assertTrue(l.isInternalAction());
        
        l = Literal.parseLiteral("print(a)");
        assertFalse(l.isInternalAction());

        l = Literal.parseLiteral("p.rint(a)");
        assertTrue(l.isInternalAction());
    }
    
    
    public void testCloneStructureFromAtom() {
        Structure s = new Structure(new Atom("b"));
        assertFalse(s.isArithExpr());
        assertEquals(0,s.getArity());
    }

    public void testHasVar() throws ParseException {
        Literal l = parseLiteral("a(Test,X,Y,b(g([V1,X,V2,V1]),c))[b,source(Y),B,kk(_),oo(oo(OO))]");
        assertTrue(l.hasVar(new VarTerm("X")));
        assertTrue(l.hasVar(new VarTerm("V2")));
        assertTrue(l.hasVar(new VarTerm("OO")));
        assertFalse(l.hasVar(new VarTerm("O")));
    }
    
    public void testSingletonVars() {
        Literal l = Literal.parseLiteral("a(10)");
        assertEquals(0, l.getSingletonVars().size());
        
        l = Literal.parseLiteral("a(X)");
        assertEquals(1, l.getSingletonVars().size());

        l = Literal.parseLiteral("a(X,X)");
        assertEquals(0, l.getSingletonVars().size());

        l = Literal.parseLiteral("a(_X,_Y,_X)");
        assertEquals(0, l.getSingletonVars().size());

        l = Literal.parseLiteral("a(X,Y,b(g(X),c))");
        assertEquals(1, l.getSingletonVars().size());
        assertEquals("Y", l.getSingletonVars().get(0).toString());

        l = Literal.parseLiteral("a(X,Y,b(g(X),c))[b,source(Y)]");
        assertEquals(0, l.getSingletonVars().size());
        
        l = Literal.parseLiteral("a(Test,X,Y,b(g(X),c))[b,source(Y),B,kk(U)]");
        assertEquals(3, l.getSingletonVars().size());
        
        l = Literal.parseLiteral("a(Test,X,Y,b(g([V1,X,V2,V1]),c))[b,source(Y),B,kk(_)]");
        assertEquals(3, l.getSingletonVars().size());

        Plan p = Plan.parse("+e(X) : X > 10 <- .print(ok).");
        assertEquals(0, p.getSingletonVars().size());
        
        p = Plan.parse("+e(x) : X > 10 <- .print(ok).");
        assertEquals(1, p.getSingletonVars().size());
        assertEquals("X", p.getSingletonVars().get(0).toString());

        p = Plan.parse("+e(x) : a(X) & X > 10 <- .print(ok).");
        assertEquals(0, p.getSingletonVars().size());
        
        p = Plan.parse("+e(x) : a(X) & X > 10 <- .print(W).");
        assertEquals(1, p.getSingletonVars().size());
        
        p = Plan.parse("+e(x) : a(X, { +!g(W) <- true }) & X > 10 <- .print(W).");
        assertEquals(0, p.getSingletonVars().size());

        p = Plan.parse("+e(x) : a(X) & X > 10 <- .print(_).");
        assertEquals(0, p.getSingletonVars().size());
    }   
    
    
    public void testAtomParsing() throws ParseException {
        Literal l = parseLiteral("b");
        assertTrue(l instanceof Literal);
        assertTrue(l.isAtom());

        // if is atom, can be cast to Atom
        @SuppressWarnings("unused")
        Atom x = (Atom)l;
        
        l = parseLiteral("b(10,a,c(10,x))[ant1,source(c)]");
        assertTrue(l.getTerm(1) instanceof Atom);
        assertFalse(l.getTerm(2).isAtom());
        assertTrue(l.getAnnots().get(0) instanceof Atom);
        
        l =  parseLiteral("b(a.r)"); // internal actions should not be atoms
        assertFalse(l.getTerm(0).isAtom());
    }

    // Tests sent by Tim Cleaver (in jason-bugs list)
    public void testObTerm() {
        ObjectTerm term = new ObjectTermImpl("test");
        assertTrue(term.equals(term));

        assertTrue(new Unifier().unifies(new ObjectTermImpl("test"), new ObjectTermImpl("test")));
        String string = "test";
        assertTrue(new Unifier().unifies(new ObjectTermImpl(string), new ObjectTermImpl(string)));

        BeliefBase base = new DefaultBeliefBase();
        base.add(ASSyntax.createLiteral("test", new ObjectTermImpl("test")));
        Iterator<Literal> iterator = base.getCandidateBeliefs(ASSyntax.createLiteral("test", new ObjectTermImpl("test")), new Unifier());
        assertTrue(iterator != null && iterator.hasNext());

        Literal result = iterator.next();
        assertTrue(result.getFunctor().equals("test"));
        assertTrue(result.getTerm(0).getClass().equals(ObjectTermImpl.class));
        assertTrue(result.getTerm(0).equals(new ObjectTermImpl("test")));

        assertFalse(iterator.hasNext());

    }
    
    public void testVarObTerm() {
        ObjectTerm term = new ObjectTermImpl("test");
        VarTerm variable = new VarTerm("Variable");
        variable.setValue(term);
        assertTrue(term.equals(variable));
        assertTrue(variable.equals(term));
        assertTrue(variable.equals(new ObjectTermImpl("test")));
    }    
}
