package uno.anahata.ai.model.tool.java;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import uno.anahata.ai.model.tool.ToolParameter;
import uno.anahata.ai.model.tool.ToolPermission;
import uno.anahata.ai.model.tool.Toolkit;
import uno.anahata.ai.tool.AIToolParam;
import uno.anahata.ai.tool.AiTool;
import uno.anahata.ai.tool.AiToolkit;

/**
 * A domain object that parses a Java class via reflection to build a complete,
 * self-contained Toolkit, including all its tools and parameters.
 * <p>
 * This class is the cornerstone of the V2's decoupled tool architecture,
 * separating the parsing of tool metadata from the management and execution of tools.
 */
@Getter
public class JavaObjectToolkit extends Toolkit<JavaMethodTool> {

    /** The singleton instance of the tool class. */
    private final Object toolInstance;

    /** A list of all declared methods (tools) for this toolkit. */
    private final List<JavaMethodTool> tools;

    /**
     * Constructs a new JavaObjectToolkit by parsing the given class.
     * @param toolClass The class to parse.
     * @throws IllegalArgumentException if the class is not a valid toolkit.
     */
    public JavaObjectToolkit(Class<?> toolClass) {
        super(parseToolkitName(toolClass), parseToolkitDescription(toolClass));
        
        try {
            this.toolInstance = toolClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not instantiate tool class: " + toolClass.getName() + ". It must be public and have a public no-arg constructor.", e);
        }

        this.tools = new ArrayList<>();
        AiToolkit toolkitAnnotation = toolClass.getAnnotation(AiToolkit.class);
        int defaultRetention = (toolkitAnnotation != null) ? toolkitAnnotation.retention() : 5;

        for (Method method : toolClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(AiTool.class)) {
                tools.add(buildJavaMethodTool(method, defaultRetention));
            }
        }
    }

    private static String parseToolkitName(Class<?> toolClass) {
        if (!toolClass.isAnnotationPresent(AiToolkit.class)) {
            throw new IllegalArgumentException("Class " + toolClass.getName() + " is not annotated with @AiToolkit.");
        }
        return toolClass.getSimpleName();
    }

    private static String parseToolkitDescription(Class<?> toolClass) {
        return toolClass.getAnnotation(AiToolkit.class).value();
    }

    private JavaMethodTool buildJavaMethodTool(Method method, int defaultRetention) {
        AiTool toolAnnotation = method.getAnnotation(AiTool.class);
        String toolName = name + "." + method.getName();
        int retention = toolAnnotation.retention() != 5 ? toolAnnotation.retention() : defaultRetention;

        List<ToolParameter> parameters = Arrays.stream(method.getParameters())
            .map(this::buildToolParameter)
            .collect(Collectors.toList());

        ToolPermission defaultPermission = toolAnnotation.requiresApproval()
            ? ToolPermission.APPROVE
            : ToolPermission.APPROVE_ALWAYS;

        return new JavaMethodTool(
            toolName,
            toolAnnotation.value(),
            defaultPermission,
            parameters,
            buildMethodSignature(method),
            method,
            retention,
            this.toolInstance, // Pass the instance to the tool for execution
            this
        );
    }

    private ToolParameter buildToolParameter(Parameter p) {
        AIToolParam paramAnnotation = p.getAnnotation(AIToolParam.class);
        if (paramAnnotation == null) {
            throw new IllegalArgumentException("Parameter '" + p.getName() + "' in method '" + p.getDeclaringExecutable().getName() + "' is missing @AIToolParam annotation.");
        }
        return ToolParameter.builder()
            .name(p.getName())
            .description(paramAnnotation.value())
            .type(p.getType())
            .required(paramAnnotation.required())
            .build();
    }

    private String buildMethodSignature(Method m) {
        String signature = Modifier.toString(m.getModifiers())
            + " " + m.getGenericReturnType().getTypeName()
            + " " + m.getName() + "("
            + Arrays.stream(m.getParameters())
            .map(p -> p.getParameterizedType().getTypeName() + " " + p.getName())
            .collect(Collectors.joining(", "))
            + ")";

        if (m.getExceptionTypes().length > 0) {
            signature += " throws " + Arrays.stream(m.getExceptionTypes())
                .map(Class::getCanonicalName)
                .collect(Collectors.joining(", "));
        }
        return signature;
    }

    @Override
    public List<JavaMethodTool> getAllTools() {
        return tools;
    }
}
