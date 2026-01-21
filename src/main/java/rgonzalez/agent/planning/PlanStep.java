package rgonzalez.agent.planning;

import java.util.Map;

/**
 * Represents a single step in an execution plan.
 * Contains all information needed to execute a tool.
 */
public record PlanStep(
        String description,
        String toolName,
        Map<String, Object> parameters
) {

    @Override
    public String toString() {
        return String.format("Step: %s [Tool: %s]", description, toolName);
    }
}
