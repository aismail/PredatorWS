package jason.asSemantics;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Trigger;
import jason.asSyntax.Trigger.TEOperator;
import jason.asSyntax.Trigger.TEType;

public class GoalListenerForMetaEvents implements GoalListener {

    private TransitionSystem ts;
    
    public GoalListenerForMetaEvents(final TransitionSystem ts) {
        this.ts = ts;
    }

    public void goalStarted(Event goal) {
        generateGoalStateEvent(goal.getTrigger().getLiteral(), TEType.achieve, GoalStates.started, null);
    }
    
    public void goalFailed(Trigger goal) {
        generateGoalStateEvent(goal.getLiteral(), goal.getType(), GoalStates.failed, null);
    }

    public void goalFinished(Trigger goal) {
        generateGoalStateEvent(goal.getLiteral(), goal.getType(), GoalStates.finished, null);
    }

    public void goalResumed(Trigger goal) {
        generateGoalStateEvent(goal.getLiteral(), goal.getType(), GoalStates.resumed, null);
    }

    public void goalSuspended(Trigger goal, String reason) {
        generateGoalStateEvent(goal.getLiteral(), goal.getType(), GoalStates.suspended, reason);
    }

    private void generateGoalStateEvent(Literal goal, TEType type, GoalStates state, String reason) {
        goal = goal.forceFullLiteralImpl().copy();
        Literal stateAnnot = ASSyntax.createLiteral("state", new Atom(state.toString()));
        if (reason != null)
            stateAnnot.addAnnot( ASSyntax.createStructure("reason", new StringTermImpl(reason)));
        goal.addAnnot( stateAnnot );
        Trigger eEnd = new Trigger(TEOperator.goalState, type, goal);
        if (ts.getAg().getPL().hasCandidatePlan(eEnd)) {
            ts.getC().addEvent(new Event(eEnd, null));
        }
    }

}
