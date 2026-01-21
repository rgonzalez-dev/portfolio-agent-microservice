package rgonzalez.agent.llm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Factory for selecting and creating LLM providers.
 * Manages provider selection and fallback logic.
 */
@Component
public class LlmProviderFactory {

    @Value("${llm.provider:openai}")
    private String preferredProvider;

    private final List<LlmProvider> availableProviders;

    public LlmProviderFactory(List<LlmProvider> availableProviders) {
        this.availableProviders = availableProviders;
    }

    /**
     * Get the default LLM provider based on configuration.
     * Falls back to first available provider if preferred one is not configured.
     */
    public LlmProvider getDefaultProvider() {
        // Try to get the preferred provider
        Optional<LlmProvider> preferred = getProvider(preferredProvider);
        if (preferred.isPresent() && preferred.get().isConfigured()) {
            return preferred.get();
        }

        // Fallback: return first configured provider
        return availableProviders.stream()
                .filter(LlmProvider::isConfigured)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No LLM provider is configured. Please set llm.openai.api-key or other provider credentials."));
    }

    /**
     * Get a specific provider by name.
     */
    public Optional<LlmProvider> getProvider(String name) {
        return availableProviders.stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * Get a provider by type.
     */
    public Optional<LlmProvider> getProvider(LlmProvider.ProviderType type) {
        return availableProviders.stream()
                .filter(p -> p.getProviderType() == type)
                .findFirst();
    }

    /**
     * Get all available providers.
     */
    public List<LlmProvider> getAllProviders() {
        return availableProviders;
    }

    /**
     * Get configured providers (ready to use).
     */
    public List<LlmProvider> getConfiguredProviders() {
        return availableProviders.stream()
                .filter(LlmProvider::isConfigured)
                .toList();
    }

    /**
     * Get status of all providers.
     */
    public String getProvidersStatus() {
        StringBuilder sb = new StringBuilder("LLM Provider Status:\n");
        for (LlmProvider provider : availableProviders) {
            sb.append("- ").append(provider.getName())
                    .append(" (").append(provider.getProviderType()).append("): ")
                    .append(provider.isConfigured() ? "CONFIGURED" : "NOT CONFIGURED")
                    .append("\n");
        }
        return sb.toString();
    }
}
