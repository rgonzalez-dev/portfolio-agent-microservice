package rgonzalez.agent.planning;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple rule-based planner that analyzes user intent and creates execution plans.
 * Matches keywords to tools and builds a sequence of plan steps.
 */
@Component
public class SimpleToolPlanner implements Planner {

    @Override
    public String getName() {
        return "SimpleToolPlanner";
    }

    @Override
    public Plan createPlan(String goal) {
        List<PlanStep> steps = new ArrayList<>();
        
        String lowerGoal = goal.toLowerCase();

        // Analyze goal and add appropriate steps
        if (lowerGoal.contains("search") || lowerGoal.contains("find")) {
            steps.add(createCustomerSearchStep(goal));
        }

        if (lowerGoal.contains("email") || lowerGoal.contains("send") || lowerGoal.contains("reminder")) {
            steps.add(createEmailStep(goal));
        }

        if (lowerGoal.contains("overdue") || lowerGoal.contains("balance")) {
            // If both search and email mentioned together, ensure both steps exist
            if (!steps.stream().anyMatch(s -> s.toolName().equals("customer_search"))) {
                steps.add(0, createCustomerSearchStep(goal));
            }
            if (!steps.stream().anyMatch(s -> s.toolName().equals("send_email_reminder"))) {
                steps.add(createEmailStep(goal));
            }
        }

        // Return empty plan if no matching tools found
        return new Plan(steps);
    }

    /**
     * Create a customer search plan step based on the goal.
     */
    private PlanStep createCustomerSearchStep(String goal) {
        Map<String, Object> params = new HashMap<>();
        
        // Extract balance threshold from goal (look for numbers)
        double minBalance = extractBalance(goal);
        if (minBalance <= 0) {
            minBalance = 500.0; // Default
        }
        
        params.put("minBalance", minBalance);
        params.put("status", "overdue");
        params.put("limit", 100);

        return new PlanStep(
                String.format("Search for customers with overdue balance >= $%.2f", minBalance),
                "customer_search",
                params
        );
    }

    /**
     * Create an email reminder plan step.
     */
    private PlanStep createEmailStep(String goal) {
        Map<String, Object> params = new HashMap<>();
        params.put("customerIds", "");  // Will be populated from previous step results
        params.put("templateType", "reminder");
        params.put("subject", "Payment Reminder: Your Account Requires Immediate Attention");

        return new PlanStep(
                "Send reminder emails to identified customers",
                "send_email_reminder",
                params
        );
    }

    /**
     * Extract numeric balance value from goal text.
     */
    private double extractBalance(String goal) {
        // Simple regex to find numbers in the text
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\d+(?:\\.\\d+)?");
        java.util.regex.Matcher matcher = pattern.matcher(goal);
        
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
}
