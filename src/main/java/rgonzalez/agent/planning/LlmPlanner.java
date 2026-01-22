package rgonzalez.agent.planning;

import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import rgonzalez.agent.llm.LlmResponse;

@Component @Primary
public class LlmPlanner implements Planner {

    private final PlanValidator planValidator;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public LlmPlanner(PlanValidator planValidator) {
        this.planValidator = planValidator;
    }

    @Override
    public Plan createPlan(String goal) {

        // String prompt = buildPrompt(goal);

        // String rawResponse = callLlm(prompt);
        
        // Use mocked LLM response for now
        LlmResponse mockResponse = getMockedLlmResponse();

        Plan plan = parsePlan(mockResponse);

        planValidator.validate(plan);

        return plan;
    }

    private Plan parsePlan(LlmResponse llmResponse) {
        try {
            if (llmResponse == null || llmResponse.getFirstChoiceContent() == null) {
                return new Plan(List.of());
            }
            
            String rawJson = llmResponse.getFirstChoiceContent();
            
            // Parse the JSON response
            JsonNode rootNode = objectMapper.readTree(rawJson);
            JsonNode stepsNode = rootNode.get("steps");
            
            if (stepsNode == null || !stepsNode.isArray()) {
                return new Plan(List.of());
            }
            
            // Extract each step from the JSON array
            java.util.List<PlanStep> steps = new java.util.ArrayList<>();
            for (JsonNode stepNode : stepsNode) {
                String description = stepNode.get("description").asText();
                String toolName = stepNode.get("toolName").asText();
                
                // Extract parameters as a Map
                JsonNode parametersNode = stepNode.get("parameters");
                @SuppressWarnings("unchecked")
                Map<String, Object> parameters = objectMapper.convertValue(parametersNode, Map.class);
                
                steps.add(new PlanStep(description, toolName, parameters));
            }
            
            return new Plan(steps);
        } catch (Exception ex) {
            throw new PlanningException(
                "Invalid response returned by LLM", ex);
        }
    }

/**
 * Mock method that returns a mocked LLM response.
 * This will be replaced with actual LLM API calls in production.
 */
private LlmResponse getMockedLlmResponse() {
    LlmResponse response = new LlmResponse();
    
    LlmResponse.Choice choice = new LlmResponse.Choice();
    LlmResponse.Message message = new LlmResponse.Message();
    String mockJson = "{" +
            "  \"steps\": [" +
            "    {" +
            "      \"description\": \"Search for customers with overdue balance\"," +
            "      \"toolName\": \"customer_search\"," +
            "      \"parameters\": {\"minBalance\": 500, \"status\": \"overdue\", \"limit\": 100}" +
            "    }," +
            "    {" +
            "      \"description\": \"Send reminder emails to identified customers\"," +
            "      \"toolName\": \"send_email_reminder\"," +
            "      \"parameters\": {\"templateType\": \"reminder\", \"subject\": \"Payment Reminder\"}" +
            "    }" +
            "  ]" +
            "}";
    message.setContent(mockJson);
    choice.setMessage(message);
    response.setChoices(new LlmResponse.Choice[]{choice});
    
    return response;
}

@Override
public String getName() {
    return "LlmPlanner";
}

}
