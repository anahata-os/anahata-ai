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
public class JavaMethodToolResponse extends AbstractToolResponse<JavaMethodToolCall, JavaMethodTool> {

    /**
     * The original invocation request that this result corresponds to.
     */
    @NonNull
    private final JavaMethodToolCall invocation;

    /** The raw exception thrown during execution, for debugging purposes. */
    private transient Throwable exception;

    public JavaMethodToolResponse(@NonNull JavaMethodToolCall invocation) {
        this.invocation = invocation;
        setStatus(ToolExecutionStatus.PENDING);
    }
    
    /**
     * Sets the initial state of the response if it was pre-rejected by the ToolManager.
     * @param reason The reason for the rejection.
     */
    public void setInitialRejectionReason(String reason) {
        if (getStatus() == ToolExecutionStatus.PENDING) {
            setStatus(ToolExecutionStatus.NOT_EXECUTED);
            setError(reason);
        }
    }

    @Override
    public JavaMethodToolCall getInvocation() {
        return invocation;
    }

    @Override
    public void execute(JavaMethodTool tool) {
        long startTime = System.currentTimeMillis();
        try {
            var method = tool.getMethod();
            var toolInstance = tool.getToolInstance();

            Parameter[] methodParameters = method.getParameters();
            Object[] argsToInvoke = new Object[methodParameters.length];
            Map<String, Object> argsFromModel = getInvocation().getArgs();

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
