package uno.anahata.ai.tool.schema;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uno.anahata.ai.AiConfig;
import uno.anahata.ai.model.tool.AbstractTool;
import uno.anahata.ai.tool.ToolManager;
import static org.junit.jupiter.api.Assertions.*;

public class SchemaProviderTest {

    private static ToolManager toolManager;

    @BeforeAll
    public static void setUp() {
        AiConfig config = new AiConfig("test-app");
        toolManager = new ToolManager(config);
        toolManager.registerClasses(MockToolkit.class);
    }

    private AbstractTool<?, ?> getTool(String methodName) {
        return toolManager.getAllTools().stream()
            .filter(t -> t.getName().endsWith("." + methodName))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Tool not found: " + methodName));
    }

    @Test
    public void testSimpleReturnTypeSchemaIsClean() {
        AbstractTool<?, ?> tool = getTool("sayHello");
        String schema = tool.getResponseJsonSchema();

        assertNotNull(schema, "Schema should not be null for a tool with a return type.");
        assertFalse(schema.contains("\"message\""), "Schema should not contain internal 'message' property.");
        assertFalse(schema.contains("\"call\""), "Schema should not contain internal 'call' property.");
        assertFalse(schema.contains("\"exception\""), "Schema should not contain internal 'exception' property.");
        assertTrue(schema.contains("\"title\": \"java.lang.String\""), "The result schema for String should be correctly injected.");
    }

    @Test
    public void testVoidReturnTypeSchemaIsNull() {
        AbstractTool<?, ?> tool = getTool("doNothing");
        String returnSchema = tool.getReturnTypeJsonSchema();
        String responseSchema = tool.getResponseJsonSchema();

        assertNull(returnSchema, "Return type schema for void method should be null.");
        assertNotNull(responseSchema, "Response schema for void method should still exist.");
        assertFalse(responseSchema.contains("\"result\""), "Response schema for void method should not have a 'result' property.");
    }

    @Test
    public void testRecursiveReturnTypeSchemaIsCleanAndCorrect() {
        AbstractTool<?, ?> tool = getTool("getTree");
        String schema = tool.getResponseJsonSchema();

        assertNotNull(schema, "Schema for recursive type should not be null.");
        assertFalse(schema.contains("\"message\""), "Schema should not contain internal 'message' property.");
        assertTrue(schema.contains("\"title\": \"uno.anahata.ai.tool.schema.Tree\""), "The result schema for Tree should be correctly injected.");
        assertTrue(schema.contains("Recursive reference to uno.anahata.ai.tool.schema.TreeNode"), "Schema should gracefully handle recursion.");
    }
}
