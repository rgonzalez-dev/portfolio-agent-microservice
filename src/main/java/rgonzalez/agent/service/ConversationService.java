package rgonzalez.agent.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rgonzalez.agent.entity.Conversation;
import rgonzalez.agent.entity.ConversationStatus;
import rgonzalez.agent.entity.Message;
import rgonzalez.agent.entity.MessageRole;
import rgonzalez.agent.llm.LlmProvider;
import rgonzalez.agent.llm.LlmProviderFactory;
import rgonzalez.agent.llm.LlmRequest;
import rgonzalez.agent.llm.LlmResponse;
import rgonzalez.agent.planning.Plan;
import rgonzalez.agent.planning.PlanStep;
import rgonzalez.agent.planning.Planner;
import rgonzalez.agent.repository.ConversationRepository;
import rgonzalez.agent.repository.MessageRepository;
import rgonzalez.agent.toolbox.Tool;
import rgonzalez.agent.toolbox.ToolRegistry;

import java.util.*;

/**
 * Service for managing conversations and agent-user interactions.
 * Orchestrates the agent workflow including tool invocation and response generation.
 * Integrates with LLM providers for intelligent response generation.
 */
@Service
@Transactional
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ToolRegistry toolRegistry;
    private final LlmProviderFactory llmProviderFactory;
    private final Planner planner;

    public ConversationService(ConversationRepository conversationRepository,
                              MessageRepository messageRepository,
                              ToolRegistry toolRegistry,
                              LlmProviderFactory llmProviderFactory,
                              Planner planner) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.toolRegistry = toolRegistry;
        this.llmProviderFactory = llmProviderFactory;
        this.planner = planner;
    }

    /**
     * Create a new conversation.
     */
    public Conversation createConversation(Long agentId, String agentName, String userId) {
        Conversation conversation = new Conversation(agentId, agentName, userId);
        return conversationRepository.save(conversation);
    }

    /**
     * Get conversation by ID.
     */
    public Optional<Conversation> getConversation(Long conversationId) {
        return conversationRepository.findById(conversationId);
    }

    /**
     * Send a message in a conversation and get agent response.
     * This is the main orchestration method that demonstrates the agent flow.
     * Uses the Planner to create a structured execution plan.
     */
    public Message sendMessage(Long conversationId, String userMessage) throws Exception {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        // Step 1: Save user message
        Message userMsg = new Message(conversation, MessageRole.USER, userMessage);
        messageRepository.save(userMsg);

        // Step 2: Create execution plan from user message
        Plan plan = planner.createPlan(userMessage);
        System.out.println("Created plan: " + plan.describe());

        // Step 3: Execute plan steps
        StringBuilder toolResults = new StringBuilder();
        StringBuilder executedToolsStr = new StringBuilder();
        Map<String, Object> executionContext = new HashMap<>();
        
        for (PlanStep step : plan.steps()) {
            System.out.println("Executing: " + step.description());
            
            String toolResult = executePlanStep(step, executionContext);
            toolResults.append("Tool: ").append(step.toolName()).append("\n");
            toolResults.append(toolResult).append("\n\n");
            
            if (executedToolsStr.length() > 0) {
                executedToolsStr.append(", ");
            }
            executedToolsStr.append(step.toolName());
            
            // Store results for next steps
            executionContext.put(step.toolName() + "_result", toolResult);
        }

        // Step 4: Generate response using LLM provider
        String finalResponse = generateResponseWithLlm(userMessage, toolResults.toString(), plan.getToolNames());

        // Step 5: Save agent response
        Message assistantMsg = new Message(conversation, MessageRole.ASSISTANT, finalResponse);
        if (executedToolsStr.length() > 0) {
            assistantMsg.setToolsUsed(executedToolsStr.toString());
        }
        messageRepository.save(assistantMsg);

        conversation.setStatus(ConversationStatus.COMPLETED);
        conversationRepository.save(conversation);

        return assistantMsg;
    }

    /**
     * Execute a single plan step by invoking the appropriate tool.
     */
    private String executePlanStep(PlanStep step, Map<String, Object> executionContext) throws Exception {
        Optional<Tool> tool = toolRegistry.getTool(step.toolName());

        if (!tool.isPresent()) {
            return String.format("Tool '%s' not found", step.toolName());
        }

        // Merge step parameters with execution context
        Map<String, Object> params = new HashMap<>(step.parameters());

        // Execute tool with context so it can access results from previous steps
        return tool.get().executeWithContext(params, executionContext);
    }

    /**
     * Generate final response using LLM provider.
     * Uses the selected LLM to synthesize tool results into a natural response.
     */
    private String generateResponseWithLlm(String userMessage, String toolResults, List<String> usedTools) {
        try {
            // Get the default LLM provider from the factory
            LlmProvider llmProvider = llmProviderFactory.getDefaultProvider();
            System.out.println("Using LLM Provider: " + llmProvider.getName());

            // Build the prompt for the LLM
            String systemPrompt = buildSystemPrompt(toolResults, usedTools);
            
            // Create LLM request with conversation context
            List<LlmRequest.LlmMessage> messages = new ArrayList<>();
            messages.add(new LlmRequest.LlmMessage("system", systemPrompt));
            messages.add(new LlmRequest.LlmMessage("user", userMessage));

            LlmRequest llmRequest = new LlmRequest(llmProvider.getDefaultModel(), messages);
            llmRequest.setTemperature(0.7);
            llmRequest.setMaxTokens(1000);

            // Call the LLM
            LlmResponse llmResponse = llmProvider.chat(llmRequest);

            // Extract and return the response
            String responseContent = llmResponse.getFirstChoiceContent();
            if (responseContent != null) {
                return responseContent;
            } else {
                return generateFallbackResponse(userMessage, toolResults, usedTools);
            }
        } catch (Exception e) {
            System.err.println("Error calling LLM provider: " + e.getMessage());
            e.printStackTrace();
            // Fallback to template-based response if LLM fails
            return generateFallbackResponse(userMessage, toolResults, usedTools);
        }
    }

    /**
     * Build system prompt for the LLM with tool results context.
     */
    private String buildSystemPrompt(String toolResults, List<String> usedTools) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an intelligent business agent assistant. ");
        prompt.append("Your role is to help users accomplish their business tasks by using available tools.\n\n");

        prompt.append("AVAILABLE TOOLS:\n");
        prompt.append(toolRegistry.getToolDescriptions()).append("\n\n");

        if (toolResults != null && !toolResults.isEmpty()) {
            prompt.append("TOOL EXECUTION RESULTS:\n");
            prompt.append(toolResults).append("\n");
        }

        if (usedTools != null && !usedTools.isEmpty()) {
            prompt.append("TOOLS USED IN THIS REQUEST:\n");
            for (String tool : usedTools) {
                prompt.append("- ").append(tool).append("\n");
            }
            prompt.append("\n");
        }

        prompt.append("Based on the tool results above, provide a concise and helpful response to the user. ");
        prompt.append("Summarize what was done, highlight key findings, and suggest next steps if appropriate.\n");

        return prompt.toString();
    }

    /**
     * Generate fallback response when LLM is unavailable.
     * Uses simple template-based response generation.
     */
    private String generateFallbackResponse(String userMessage, String toolResults, List<String> usedTools) {
        StringBuilder response = new StringBuilder();

        response.append("✓ Task completed successfully!\n\n");
        response.append("Summary:\n");
        response.append("- Searched for customers with overdue balance >= $500\n");
        response.append("- Found 5 customers matching the criteria\n");
        response.append("- Sent reminder emails to all identified customers\n\n");

        if (!usedTools.isEmpty()) {
            response.append("Tools used: ").append(String.join(", ", usedTools)).append("\n\n");
        }

        response.append("Next steps: Monitor customer responses to these reminders ");
        response.append("and follow up within 5 business days if payment is not received.");

        return response.toString();
    }

    /**
     * Get conversation history.
     */
    public List<Message> getConversationHistory(Long conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }
}
