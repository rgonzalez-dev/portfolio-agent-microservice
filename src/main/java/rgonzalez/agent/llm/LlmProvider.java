package rgonzalez.agent.llm;

/**
 * Interface for LLM (Large Language Model) providers.
 * Abstracts different LLM APIs (OpenAI, Anthropic, local, etc.)
 */
public interface LlmProvider {

    /**
     * Get the name of this provider.
     */
    String getName();

    /**
     * Get the default model for this provider.
     */
    String getDefaultModel();

    /**
     * Send a request to the LLM and get a response.
     */
    LlmResponse chat(LlmRequest request) throws Exception;

    /**
     * Check if the provider is configured and ready.
     */
    boolean isConfigured();

    /**
     * Count tokens in a message (for cost estimation).
     */
    int countTokens(String content);

    /**
     * Get the provider type (e.g., "OPENAI", "ANTHROPIC", "LOCAL").
     */
    ProviderType getProviderType();

    enum ProviderType {
        OPENAI,
        ANTHROPIC,
        LOCAL,
        AZURE,
        OTHER
    }
}
