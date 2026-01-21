package rgonzalez.agent.planning;

public final class PlanSchemas {

    public static final String PLAN_SCHEMA = """
    {
      "type": "object",
      "required": ["steps"],
      "properties": {
        "steps": {
          "type": "array",
          "minItems": 1,
          "maxItems": 5,
          "items": {
            "type": "object",
            "required": ["description", "toolName", "parameters"],
            "properties": {
              "description": { "type": "string" },
              "toolName": { "type": "string" },
              "parameters": {
                "type": "object",
                "additionalProperties": true
              }
            }
          }
        }
      }
    }
    """;

    private PlanSchemas() {}
}
