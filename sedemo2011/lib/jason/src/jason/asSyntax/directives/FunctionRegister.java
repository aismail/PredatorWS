package jason.asSyntax.directives;

import jason.asSemantics.Agent;
import jason.asSemantics.ArithFunction;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Pred;
import jason.asSyntax.StringTerm;
import jason.functions.Abs;
import jason.functions.Average;
import jason.functions.Length;
import jason.functions.Max;
import jason.functions.Min;
import jason.functions.Random;
import jason.functions.Round;
import jason.functions.Sqrt;
import jason.functions.StdDev;
import jason.functions.Sum;
import jason.functions.ceil;
import jason.functions.e;
import jason.functions.floor;
import jason.functions.log;
import jason.functions.pi;
import jason.functions.time;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** 
 * This class maintains the set of arithmetic functions available for the AS parser.
 * 
 * @author Jomi
 */
public class FunctionRegister implements Directive {
    static Logger logger = Logger.getLogger(FunctionRegister.class.getName());

    private static Map<String,ArithFunction> functions = new HashMap<String,ArithFunction>();

    // add known global functions (can be computed without an agent reference)
    static {
        addFunction(Abs.class);
        addFunction(Max.class);
        addFunction(Min.class);
        addFunction(Sum.class);
        addFunction(StdDev.class);
        addFunction(Average.class);
        addFunction(Length.class);
        addFunction(Random.class);
        addFunction(Round.class);
        addFunction(Sqrt.class);
        addFunction(pi.class);
        addFunction(e.class);
        addFunction(floor.class);
        addFunction(ceil.class);
        addFunction(log.class);
        addFunction(time.class);
    }
    
    private static void addFunction(Class<? extends ArithFunction> c) {
        try {
            ArithFunction af = c.newInstance();
            functions.put(af.getName(), af);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error registering function "+c.getName(),e);
        }
    }

    public static String checkFunctionName(String fName) {
        if (functions.get(fName) != null)
            return "Can not register the function "+fName+"  twice!";
        else if (fName.indexOf(".") < 0)
            return "The function "+fName+" was not registered! A function must have a '.' in its name.";
        else if (fName.startsWith(".")) 
            return "The function "+fName+" was not registered! An user function name can not start with '.'.";
        else 
            return null;
    }
    
    public static ArithFunction getFunction(String function, int arity) {
        ArithFunction af = functions.get(function);
        if (af != null && af.checkArity(arity))
            return af;
        else
            return null;
    }
    
    @SuppressWarnings("unchecked")
    public Agent process(Pred directive, Agent outerContent, Agent innerContent) {
        if (outerContent == null)
            return null;
        try {
            String id = ((StringTerm)directive.getTerm(0)).getString();
            if (directive.getArity() == 1) {
                // it is implemented in java
                outerContent.addFunction((Class<ArithFunction>)Class.forName(id));
            } else if (directive.getArity() == 3) {
                // is is implemented in AS
                int arity = (int)((NumberTerm)directive.getTerm(1)).solve();
                String predicate = ((StringTerm)directive.getTerm(2)).getString();
                outerContent.addFunction(id, arity, predicate);
            } else {
                // error
                logger.log(Level.SEVERE, "Wrong number of arguments for register_function "+directive);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing directive register_function.",e);
        }
        return null;
    }
}
