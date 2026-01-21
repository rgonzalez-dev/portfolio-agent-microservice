package rgonzalez.agent.planning;

import java.util.List;

/**
 * Represents an execution plan containing multiple steps.
 * Each step describes a tool to be executed with specific parameters.
 */
public record Plan(
        List<PlanStep> steps
) {

    /**
     * Get a human-readable description of the plan.
     */
    public String describe() {
        StringBuilder sb = new StringBuilder("Execution Plan (" + steps.size() + " steps):\n");
        for (int i = 0; i < steps.size(); i++) {
            PlanStep step = steps.get(i);
            sb.append(String.format("%d. %s\n", i + 1, step.description()));
        }
        return sb.toString();
    }

    /**
     * Get the list of tool names in order.
     */
    public List<String> getToolNames() {
        return steps.stream()
                .map(PlanStep::toolName)
                .toList();
    }

    /**
     * Check if plan has any steps.
     */
    public boolean isEmpty() {
        return steps.isEmpty();
    }

    /**
     * Check if plan requires any tools.
     */
    public boolean requiresTools() {
        return !steps.isEmpty();
    }
}
