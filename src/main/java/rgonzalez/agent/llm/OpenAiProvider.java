package rgonzalez.agent.llm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * OpenAI LLM Provider implementation.
 * Integrates with OpenAI API (ChatGPT, GPT-4, etc.)
 */
@Component
public class OpenAiProvider implements LlmProvider {

    @Value("${llm.openai.api-key:}")
    private String apiKey;

    @Value("${llm.openai.model:gpt-4}")
    private String defaultModel;

    @Value("${llm.openai.api-endpoint:https://api.openai.com}")
    private String endpoint;

    public OpenAiProvider() {
    }

    @Override
    public String getName() {
        return "OpenAI";
    }

    @Override
    public String getDefaultModel() {
        return defaultModel;
    }

    @Override
    public LlmResponse chat(LlmRequest request) throws Exception {
        if (!isConfigured()) {
            throw new IllegalStateException("OpenAI provider is not configured. Set llm.openai.api-key property.");
        }

        // Set default model if not specified
        if (request.getModel() == null) {
            request.setModel(defaultModel);
        }

        try {
            // In a real implementation, this would call the actual OpenAI API
            // For now, we'll simulate the response
            return simulateChatCompletion(request);
        } catch (Exception e) {
            throw new RuntimeException("Failed to call OpenAI API: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty() && !apiKey.equals("");
    }

    @Override
    public int countTokens(String content) {
        // Rough approximation: 1 token ≈ 4 characters
        // More accurate: use OpenAI's tokenizer
        return (int) Math.ceil(content.length() / 4.0);
    }

    @Override
    public ProviderType getProviderType() {
        return ProviderType.OPENAI;
    }

    /**
     * Simulate a chat completion response from OpenAI.
     * In production, this would make an actual HTTP request to OpenAI API.
     */
    private LlmResponse simulateChatCompletion(LlmRequest request) {
        LlmResponse response = new LlmResponse();
        response.setId("chatcmpl-" + System.currentTimeMillis());
        response.setObject("chat.completion");
        response.setCreated(System.currentTimeMillis() / 1000);
        response.setModel(request.getModel());

        // Extract the last user message for response generation
        String lastUserMessage = "";
        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            for (int i = request.getMessages().size() - 1; i >= 0; i--) {
                if ("user".equals(request.getMessages().get(i).getRole())) {
                    lastUserMessage = request.getMessages().get(i).getContent();
                    break;
                }
            }
        }

        // Simulate a response based on the request
        String responseContent = generateSimulatedResponse(lastUserMessage);

        LlmResponse.Choice choice = new LlmResponse.Choice();
        choice.setIndex(0);
        LlmResponse.Message message = new LlmResponse.Message();
        message.setRole("assistant");
        message.setContent(responseContent);
        choice.setMessage(message);
        choice.setFinishReason("stop");

        response.setChoices(new LlmResponse.Choice[]{choice});

        LlmResponse.Usage usage = new LlmResponse.Usage();
        usage.setPromptTokens(countTokens(String.join("\n",
                request.getMessages().stream().map(LlmRequest.LlmMessage::getContent).toArray(String[]::new))));
        usage.setCompletionTokens(countTokens(responseContent));
        usage.setTotalTokens(usage.getPromptTokens() + usage.getCompletionTokens());
        response.setUsage(usage);

        return response;
    }

    /**
     * Generate a simulated response based on user message.
     * In production, this would be the actual LLM response.
     */
    private String generateSimulatedResponse(String userMessage) {
        String lowerMessage = userMessage.toLowerCase();

        if (lowerMessage.contains("search") || lowerMessage.contains("find")) {
            return "I understand you want to search for something. I'll help you with that. "
                    + "What specific criteria would you like me to use for the search?";
        } else if (lowerMessage.contains("email") || lowerMessage.contains("send")) {
            return "I can help you send emails. Here's what I'll do:\n"
                    + "1. Identify the recipients\n"
                    + "2. Prepare the email content\n"
                    + "3. Send the emails\n"
                    + "4. Confirm delivery";
        } else if (lowerMessage.contains("overdue") || lowerMessage.contains("balance")) {
            return "I'll search for customers with overdue balances and prepare reminder communications. "
                    + "Let me gather the data and send out notifications.";
        } else {
            return "I've received your request: \"" + userMessage + "\". "
                    + "I'm processing this and will provide you with detailed assistance.";
        }
    }
}
