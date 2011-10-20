import jason.infra.saci.*;

/**
 * This program creates a new agent for SACI infrastructure. This new agent is
 * named "anotherAg" and enters in a MAS called "createAgDemo" that must be
 * already running.
 */
public class CreateAgDemo {
    public static void main(String[] args) throws Exception {
        // gets a reference to Saci runtime services and
        // calls createAg method. "createAgDemo" is the MAS Id
        // set in the .mas2j project.

        new SaciRuntimeServices("createAgDemo").createAgent(
                "anotherAg", // agent name
                "ag1.asl", // AgentSpeak source
                null, // default agent class
                null, // default architecture class
                null, // default belief base
                null); // default settings
    }
}
