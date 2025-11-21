package uno.anahata.ai.model.tool.java;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NonNull;
import uno.anahata.ai.model.tool.AbstractTool;
import uno.anahata.ai.model.tool.ToolParameter;
import uno.anahata.ai.model.tool.ToolPermission;
import uno.anahata.ai.model.tool.Toolkit;

/**
 * A model-agnostic, stateful representation of a single Java method tool.
 * This is a self-contained, executable unit that encapsulates both the
 * definition of the tool and the logic required to invoke it via reflection.
 *
 * @author anahata
 */
@Getter
public class JavaMethodTool extends AbstractTool<JavaMethodToolCall, JavaMethodToolResponse> {
    private static final Gson GSON = new Gson();

    /** The full Java method signature. */
    @NonNull
    private final String javaMethodSignature;

    /** The underlying Java method that this tool represents. */
    @NonNull
    private final Method method;

    /** The singleton instance of the toolkit class, used for invoking non-static methods. */
    private final transient Object toolInstance;

    public JavaMethodTool(
            @NonNull String name,
            @NonNull String description,
            @NonNull ToolPermission permission,
            @NonNull List<ToolParameter> parameters,
            @NonNull String javaMethodSignature,
            @NonNull Method method,
            int retentionTurns,
            Object toolInstance, // Can be null for static methods
            Toolkit toolkit
    ) {
        super(name, description, toolkit, permission, parameters);
        setRetentionTurns(retentionTurns);
        this.javaMethodSignature = javaMethodSignature;
        this.method = method;
        this.toolInstance = toolInstance;
    }

    @Override
    public JavaMethodToolCall createCall(String id, Map<String, Object> jsonArgs) {
        // 1. Pre-flight validation for required parameters
        List<String> missingParams = new ArrayList<>();
        for (ToolParameter param : getParameters()) {
            if (param.isRequired() && !jsonArgs.containsKey(param.getName())) {
                missingParams.add(param.getName());
            }
        }
        if (!missingParams.isEmpty()) {
            String reason = "Tool call rejected: Missing required parameters: " + String.join(", ", missingParams);
            JavaMethodToolCall call = new JavaMethodToolCall(id, this, jsonArgs);
            call.getResponse().reject(reason);
            return call;
        }

        // 2. Convert arguments from JSON types to Java types
        Map<String, Object> convertedArgs = new HashMap<>();
        try {
            for (Parameter p : getMethod().getParameters()) {
                String paramName = p.getName();
                Object rawValue = jsonArgs.get(paramName);
                if (rawValue != null) {
                    String jsonValue = GSON.toJson(rawValue);
                    Object convertedValue = GSON.fromJson(jsonValue, p.getParameterizedType());
                    convertedArgs.put(paramName, convertedValue);
                }
            }
        } catch (JsonSyntaxException e) {
            String reason = "Tool call rejected: Failed to convert arguments. Error: " + e.getMessage();
            JavaMethodToolCall call = new JavaMethodToolCall(id, this, jsonArgs);
            call.getResponse().reject(reason);
            return call;
        }

        // 3. Create the final call object
        return new JavaMethodToolCall(id, this, convertedArgs);
    }
}
