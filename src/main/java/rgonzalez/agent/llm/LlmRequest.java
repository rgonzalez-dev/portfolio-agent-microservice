package rgonzalez.agent.llm;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for LLM API calls.
 */
public class LlmRequest {

    private List<LlmMessage> messages;
    private String model;
    private double temperature;
    private int maxTokens;
    private Map<String, Object> tools;

    public LlmRequest() {
    }

    public LlmRequest(String model, List<LlmMessage> messages) {
        this.model = model;
        this.messages = messages;
        this.temperature = 0.7;
        this.maxTokens = 2000;
    }

    public List<LlmMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<LlmMessage> messages) {
        this.messages = messages;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Map<String, Object> getTools() {
        return tools;
    }

    public void setTools(Map<String, Object> tools) {
        this.tools = tools;
    }

    /**
     * Message in LLM conversation format.
     */
    public static class LlmMessage {
        private String role; // "system", "user", "assistant"
        private String content;

        public LlmMessage() {
        }

        public LlmMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
