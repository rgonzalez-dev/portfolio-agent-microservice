package rgonzalez.agent.tool;

import java.util.Map;

/**
 * Interface for tools that can be used by agents.
 */
public interface Tool {

    /**
     * Get the name of the tool.
     */
    String getName();

    /**
     * Get the description of the tool.
     */
    String getDescription();

    /**
     * Get the expected parameters for this tool.
     */
    Map<String, String> getParameters();

    /**
     * Execute the tool with the given parameters.
     */
    String execute(Map<String, Object> parameters) throws Exception;

    /**
     * Execute the tool with parameters and execution context.
     * Default implementation calls execute(parameters) for backward compatibility.
     */
    default String executeWithContext(Map<String, Object> parameters, Map<String, Object> context) throws Exception {
        return execute(parameters);
    }
}
