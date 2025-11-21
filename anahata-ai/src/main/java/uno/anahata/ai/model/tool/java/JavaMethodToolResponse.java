package uno.anahata.ai.model.tool.java;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Map;
import lombok.Getter;
import lombok.NonNull;
import uno.anahata.ai.model.tool.AbstractToolResponse;
import uno.anahata.ai.model.tool.ToolExecutionStatus;
import uno.anahata.ai.tool.AiToolException;

/**
 * A rich POJO that captures the complete and final outcome of a single tool
 * call. This class now follows a deferred execution model.
 *
 * @author anahata
 */
@Getter
public class JavaMethodToolResponse extends AbstractToolResponse<JavaMethodToolCall> {

    /**
     * The original invocation request that this result corresponds to.
     */
    @NonNull
    private final JavaMethodToolCall call;

    /** The raw exception thrown during execution, for debugging purposes. */
    private transient Throwable exception;

    public JavaMethodToolResponse(@NonNull JavaMethodToolCall call) {
        this.call = call;
        setStatus(ToolExecutionStatus.PENDING);
    }

    @Override
    public JavaMethodToolCall getCall() {
        return call;
    }

    @Override
    public void execute() {
        long startTime = System.currentTimeMillis();
        try {
            // Correctly get the tool from the call object
            JavaMethodTool tool = getCall().getTool();
            var method = tool.getMethod();
            var toolInstance = tool.getToolInstance();

            Parameter[] methodParameters = method.getParameters();
            Object[] argsToInvoke = new Object[methodParameters.length];
            Map<String, Object> argsFromModel = getCall().getArgs();

            for (int i = 0; i < methodParameters.length; i++) {
                Parameter p = methodParameters[i];
                String paramName = p.getName();
                argsToInvoke[i] = argsFromModel.get(paramName);
            }

            Object result = method.invoke(toolInstance, argsToInvoke);
            
            setResult(result);
            setStatus(ToolExecutionStatus.EXECUTED);

        } catch (Exception e) {
            Throwable cause = (e instanceof InvocationTargetException && e.getCause() != null) ? e.getCause() : e;
            this.exception = cause;
            
            if (cause instanceof AiToolException) {
                setError(cause.getMessage());
            } else {
                setError(cause.toString());
            }
            setStatus(ToolExecutionStatus.FAILED);
        } finally {
            setExecutionTimeMillis(System.currentTimeMillis() - startTime);
        }
    }
}
