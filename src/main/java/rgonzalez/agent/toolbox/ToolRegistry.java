package rgonzalez.agent.toolbox;

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Registry for tools that can be used by agents.
 * Manages tool discovery, registration, and retrieval.
 */
@Component
public class ToolRegistry {

    private final Map<String, Tool> tools = new HashMap<>();

    /**
     * Constructor that auto-registers all Tool beans.
     */
    public ToolRegistry(List<Tool> toolBeans) {
        for (Tool tool : toolBeans) {
            register(tool);
        }
    }

    /**
     * Register a tool.
     */
    public void register(Tool tool) {
        tools.put(tool.getName(), tool);
    }

    /**
     * Get a tool by name.
     */
    public Optional<Tool> getTool(String name) {
        return Optional.ofNullable(tools.get(name));
    }

    /**
     * Get all available tools.
     */
    public List<Tool> getAllTools() {
        return new ArrayList<>(tools.values());
    }

    /**
     * Check if a tool exists.
     */
    public boolean hasTool(String name) {
        return tools.containsKey(name);
    }

    /**
     * Get tool descriptions for LLM context.
     */
    public String getToolDescriptions() {
        StringBuilder sb = new StringBuilder("Available tools:\n");
        for (Tool tool : tools.values()) {
            sb.append("\n- ").append(tool.getName()).append(": ").append(tool.getDescription());
            if (!tool.getParameters().isEmpty()) {
                sb.append("\n  Parameters: ");
                tool.getParameters().forEach((key, value) ->
                        sb.append("\n    - ").append(key).append(": ").append(value)
                );
            }
        }
        return sb.toString();
    }
}
