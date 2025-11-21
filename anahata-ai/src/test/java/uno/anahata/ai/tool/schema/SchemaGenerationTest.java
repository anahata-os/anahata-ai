package uno.anahata.ai.tool.schema;

import uno.anahata.ai.AiConfig;
import uno.anahata.ai.model.tool.AbstractTool;
import uno.anahata.ai.tool.ToolManager;

public class SchemaGenerationTest {

    public static void main(String[] args) throws Exception {
        System.out.println("--- Running Schema Generation Test ---");

        AiConfig config = new AiConfig("test-app");
        ToolManager toolManager = new ToolManager(config);
        
        // Register the mock toolkit
        toolManager.registerClasses(MockToolkit.class);

        System.out.println("\nFound " + toolManager.getAllTools().size() + " tools.");

        for (AbstractTool<?, ?> tool : toolManager.getAllTools()) {
            System.out.println("\n========================================");
            System.out.println("Testing Tool: " + tool.getName());
            System.out.println("----------------------------------------");

            System.out.println("\n1. Returned Type JSON Schema:");
            String returnTypeSchema = tool.getReturnTypeJsonSchema();
            System.out.println(returnTypeSchema != null ? returnTypeSchema : "[null - Correct for void]");

            System.out.println("\n2. Full Response JSON Schema:");
            String responseSchema = tool.getResponseJsonSchema();
            System.out.println(responseSchema != null ? responseSchema : "[null - Should not happen for non-void]");
            System.out.println("========================================");
        }
        
        System.out.println("\n--- Test Complete ---");
    }
}
