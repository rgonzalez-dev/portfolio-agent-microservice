package rgonzalez.agent.toolbox.tools;

import org.springframework.stereotype.Component;

import rgonzalez.agent.toolbox.Tool;

import java.util.HashMap;
import java.util.Map;

/**
 * Tool for searching customers in the database with specific criteria.
 */
@Component
public class CustomerSearchTool implements Tool {

    @Override
    public String getName() {
        return "customer_search";
    }

    @Override
    public String getDescription() {
        return "Search for customers based on specific criteria like overdue balance, status, etc.";
    }

    @Override
    public Map<String, String> getParameters() {
        Map<String, String> params = new HashMap<>();
        params.put("minBalance", "Minimum balance amount (e.g., 500)");
        params.put("status", "Customer status filter (active, overdue, etc.)");
        params.put("limit", "Maximum number of results to return");
        return params;
    }

    @Override
    public String execute(Map<String, Object> parameters) throws Exception {
        // Simulate database query for customers with overdue balance
        Double minBalance = Double.parseDouble(parameters.get("minBalance").toString());
        String status = parameters.getOrDefault("status", "overdue").toString();

        // Mock database query result
        String result = String.format(
                "Found 5 customers with overdue balance >= $%.2f and status '%s':\n" +
                "1. Customer ID: C001, Name: John Doe, Balance: $650.00\n" +
                "2. Customer ID: C002, Name: Jane Smith, Balance: $800.50\n" +
                "3. Customer ID: C003, Name: Bob Johnson, Balance: $550.25\n" +
                "4. Customer ID: C004, Name: Alice Williams, Balance: $1200.00\n" +
                "5. Customer ID: C005, Name: Charlie Brown, Balance: $600.75",
                minBalance, status
        );

        return result;
    }
}
