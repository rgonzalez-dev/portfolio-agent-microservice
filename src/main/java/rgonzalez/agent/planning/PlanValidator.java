package rgonzalez.agent.planning;

import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class PlanValidator {

    private static final Set<String> ALLOWED_TOOLS =
            Set.of("customer_search", "send_email_reminder");

    public void validate(Plan plan) {

        if (plan.steps().isEmpty()) {
            throw new PlanningException("Empty plan");
        }

        for (PlanStep step : plan.steps()) {

            if (!ALLOWED_TOOLS.contains(step.toolName())) {
                throw new PlanningException(
                        "Unknown tool: " + step.toolName());
            }

            if (step.parameters() == null) {
                throw new PlanningException(
                        "Missing parameters for tool: " + step.toolName());
            }
        }
    }
}
