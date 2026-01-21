package rgonzalez.agent.planning;

/**
 * Interface for planning agent actions.
 * Responsible for analyzing goals and creating execution plans.
 */
public interface Planner {

    /**
     * Create an execution plan based on a given goal/user message.
     * 
     * @param goal The user's goal or request
     * @return A Plan containing the steps to execute
     */
    Plan createPlan(String goal);

    /**
     * Get the name/type of this planner.
     */
    String getName();
}
