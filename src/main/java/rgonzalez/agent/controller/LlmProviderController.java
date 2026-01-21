package rgonzalez.agent.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rgonzalez.agent.llm.LlmProvider;
import rgonzalez.agent.llm.LlmProviderFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for LLM Provider management and diagnostics.
 */
@RestController
@RequestMapping("/llm-providers")
@Tag(name = "LLM Providers", description = "APIs for managing and monitoring LLM providers")
public class LlmProviderController {

    private final LlmProviderFactory llmProviderFactory;

    public LlmProviderController(LlmProviderFactory llmProviderFactory) {
        this.llmProviderFactory = llmProviderFactory;
    }

    @GetMapping("/status")
    @Operation(summary = "Get LLM providers status", description = "Check which LLM providers are available and configured")
    @ApiResponse(responseCode = "200", description = "Providers status retrieved")
    public ResponseEntity<Map<String, Object>> getProvidersStatus() {
        Map<String, Object> status = new HashMap<>();

        try {
            LlmProvider defaultProvider = llmProviderFactory.getDefaultProvider();
            status.put("defaultProvider", defaultProvider.getName());
            status.put("defaultModel", defaultProvider.getDefaultModel());
        } catch (Exception e) {
            status.put("defaultProvider", "NONE - " + e.getMessage());
        }

        List<LlmProvider> allProviders = llmProviderFactory.getAllProviders();
        List<Map<String, String>> providersInfo = allProviders.stream()
                .map(provider -> {
                    Map<String, String> info = new HashMap<>();
                    info.put("name", provider.getName());
                    info.put("type", provider.getProviderType().toString());
                    info.put("configured", String.valueOf(provider.isConfigured()));
                    info.put("defaultModel", provider.getDefaultModel());
                    return info;
                })
                .toList();

        status.put("availableProviders", providersInfo);
        status.put("configuredProviders", llmProviderFactory.getConfiguredProviders().size());
        status.put("diagnostics", llmProviderFactory.getProvidersStatus());

        return ResponseEntity.ok(status);
    }

    @GetMapping("/default")
    @Operation(summary = "Get default LLM provider", description = "Get information about the currently configured default LLM provider")
    @ApiResponse(responseCode = "200", description = "Default provider information")
    @ApiResponse(responseCode = "503", description = "No LLM provider configured")
    public ResponseEntity<Map<String, Object>> getDefaultProvider() {
        try {
            LlmProvider provider = llmProviderFactory.getDefaultProvider();

            Map<String, Object> info = new HashMap<>();
            info.put("name", provider.getName());
            info.put("type", provider.getProviderType().toString());
            info.put("defaultModel", provider.getDefaultModel());
            info.put("configured", provider.isConfigured());

            return ResponseEntity.ok(info);
        } catch (IllegalStateException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(503).body(error);
        }
    }
}
