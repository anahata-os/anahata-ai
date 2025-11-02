package uno.anahata.ai.tool.schema;

import io.swagger.v3.oas.annotations.media.Schema;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import uno.anahata.ai.tool.annotation.AiTool;
import uno.anahata.ai.tool.annotation.AiToolParam;

/**
 * Generates a standardized schema representation for an AI tool method.
 * <p>
 * This class uses reflection to inspect methods annotated with {@link AiTool}
 * and their parameters annotated with {@link AiToolParam} to create a map
 * structure suitable for conversion into a model-specific function declaration
 * (e.g., a Google GenAI FunctionDeclaration).
 */
public class SchemaGenerator {

    /**
     * Generates a schema map for a single tool method.
     *
     * @param method The Java method annotated with @AiTool.
     * @return A map representing the tool's schema, including name, description, and parameters.
     */
    public Map<String, Object> generateSchema(Method method) {
        AiTool aiTool = method.getAnnotation(AiTool.class);
        if (aiTool == null) {
            throw new IllegalArgumentException("Method must be annotated with @AiTool: " + method.getName());
        }

        Map<String, Object> schema = new HashMap<>();
        schema.put("name", method.getName());
        schema.put("description", aiTool.value());
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        
        for (Parameter parameter : method.getParameters()) {
            AiToolParam aiToolParam = parameter.getAnnotation(AiToolParam.class);
            
            // We rely on the -parameters compiler flag to get the parameter name.
            String paramName = parameter.getName();
            
            Map<String, Object> paramSchema = new HashMap<>();
            paramSchema.put("type", getJsonSchemaType(parameter.getType()));
            
            if (aiToolParam != null) {
                paramSchema.put("description", aiToolParam.value());
            } else {
                // If no @AiToolParam is present, we still need a description for the model.
                paramSchema.put("description", "The value for parameter '" + paramName + "'.");
            }
            
            // Check for Swagger @Schema for complex types (POJOs)
            if (parameter.getType().isAnnotationPresent(Schema.class)) {
                // In a full implementation, this would recursively generate the schema for the POJO.
                paramSchema.put("ref", "#/definitions/" + parameter.getType().getSimpleName());
            }
            
            properties.put(paramName, paramSchema);
        }
        
        parameters.put("properties", properties);
        schema.put("parameters", parameters);
        
        return schema;
    }

    /**
     * Maps a Java class type to its corresponding JSON Schema type.
     */
    private String getJsonSchemaType(Class<?> type) {
        if (type.isPrimitive() || type == String.class || Number.class.isAssignableFrom(type)) {
            if (type == String.class || type == char.class || type == Character.class) {
                return "string";
            } else if (type == int.class || type == Integer.class || type == long.class || type == Long.class) {
                return "integer";
            } else if (type == double.class || type == Double.class || type == float.class || type == Float.class) {
                return "number";
            } else if (type == boolean.class || type == Boolean.class) {
                return "boolean";
            }
        } else if (type.isArray() || java.util.Collection.class.isAssignableFrom(type)) {
            return "array";
        }
        // Default to object for all other types (POJOs, etc.)
        return "object";
    }
}
