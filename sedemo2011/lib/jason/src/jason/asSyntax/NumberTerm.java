package jason.asSyntax;


/** The interface for numeric terms of AgentSpeak language
 *  
 * @opt nodefillcolor lightgoldenrodyellow
 */
public interface NumberTerm extends Term {

    /** returns the numeric value of the term */
    public double solve();
}
