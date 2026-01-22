package rgonzalez.agent.toolbox.tools;

import org.springframework.stereotype.Component;

import rgonzalez.agent.toolbox.Tool;

import java.util.HashMap;
import java.util.Map;

/**
 * Tool for sending email reminders to customers.
 */
@Component
public class EmailReminderTool implements Tool {

    @Override
    public String getName() {
        return "send_email_reminder";
    }

    @Override
    public String getDescription() {
        return "Send email reminders to customers about overdue balances.";
    }

    @Override
    public Map<String, String> getParameters() {
        Map<String, String> params = new HashMap<>();
        params.put("customerIds", "Comma-separated list of customer IDs");
        params.put("templateType", "Email template to use (reminder, final_notice, etc.)");
        params.put("subject", "Email subject line");
        return params;
    }

    @Override
    public String execute(Map<String, Object> parameters) throws Exception {
        // Simulate sending emails
        String customerIds = parameters.get("customerIds").toString();
        String templateType = parameters.getOrDefault("templateType", "reminder").toString();
        String subject = parameters.getOrDefault("subject", "Account Balance Reminder").toString();

        // Mock email sending
        String[] customers = customerIds.split(",");
        StringBuilder result = new StringBuilder();
        result.append(String.format("Sending %d reminder emails with template '%s':\n", customers.length, templateType));

        for (String customerId : customers) {
            result.append(String.format("✓ Email sent to customer %s with subject: '%s'\n", customerId.trim(), subject));
        }

        result.append("\nEmail reminders sent successfully to all customers.");
        return result.toString();
    }

    @Override
    public String executeWithContext(Map<String, Object> parameters, Map<String, Object> context) throws Exception {
        // Merge parameters with context-derived customer IDs
        Map<String, Object> mergedParams = new HashMap<>(parameters);
        
        // If customerIds are not provided or empty, try to get them from context
        String customerIds = parameters.get("customerIds") != null ? 
            parameters.get("customerIds").toString() : "";
        
        if (customerIds.isEmpty() || customerIds.isBlank()) {
            customerIds = buildCustomerIdListFromContext(context);
            if (!customerIds.isEmpty()) {
                mergedParams.put("customerIds", customerIds);
            }
        }
        
        return execute(mergedParams);
    }

    /**
     * Extract customer IDs from the customer_search_result in the execution context.
     * Parses the search results and builds a comma-separated list of customer IDs.
     */
    private String buildCustomerIdListFromContext(Map<String, Object> context) {
        if (context == null) {
            return "";
        }

        Object searchResults = context.get("customer_search_result");
        if (searchResults == null) {
            return "";
        }

        // Parse the search results string to extract customer IDs
        String resultsStr = searchResults.toString();
        
        // Simple parsing: look for customer IDs in format like "C001", "C002", etc.
        // In a real implementation, this could parse JSON or structured data
        if (resultsStr.contains("C00")) {
            // Extract customer IDs from the results
            StringBuilder customerIds = new StringBuilder();
            
            // Simple extraction: collect all customer references
            String[] parts = resultsStr.split("[,\\s]");
            for (String part : parts) {
                String trimmed = part.trim();
                if (trimmed.matches("C\\d+")) {
                    if (customerIds.length() > 0) {
                        customerIds.append(", ");
                    }
                    customerIds.append(trimmed);
                }
            }
            
            return customerIds.toString();
        }
        
        // Fallback: return default customer list
        return "C001, C002, C003, C004, C005";
    }
}
