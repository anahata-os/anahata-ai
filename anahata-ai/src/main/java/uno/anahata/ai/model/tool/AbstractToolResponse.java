package uno.anahata.ai.model.tool;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import uno.anahata.ai.model.core.AbstractPart;

/**
 * Represents the response of a tool call, designed for deferred execution.
 * The fields are populated by the {@link #execute()} method.
 * @author pablo
 * @param <C> The specific type of the Call this response is for.
 */
@Getter
public abstract class AbstractToolResponse<C extends AbstractToolCall> extends AbstractPart {
    /** The final status of the invocation after execution. */
    @Setter
    private ToolExecutionStatus status;

    /** The result of the invocation if it succeeded, otherwise {@code null}. */
    @Setter
    private Object result;

    /** A descriptive error message if the tool failed or was rejected. */
    @Setter
    private String error;

    /** The time taken to execute the method, in milliseconds. */
    @Setter
    private long executionTimeMillis;
    
    /** A list of log messages captured during the tool's execution. */
    private final List<String> logs = new ArrayList<>();
    
    /** Optional feedback from the user if the tool execution was prompted. */
    @Setter
    private String userFeedback;
    
    /**
     * Gets the original invocation request that this result corresponds to.
     * @return The originating tool call.
     */
    public abstract C getCall();
    
    /**
     * Executes the tool logic. This method is responsible for populating
     * the status, result, error, and executionTimeMillis fields.
     */
    public abstract void execute();

    /**
     * Rejects the tool call before execution, setting the status to NOT_EXECUTED.
     * @param reason The reason for the rejection.
     */
    public void reject(String reason) {
        setStatus(ToolExecutionStatus.NOT_EXECUTED);
        setError(reason);
    }

    /**
     * Clears any log messages captured during execution.
     */
    public void clearLogs() {
        this.logs.clear();
    }
    
    @Override
    public String asText() {
        return "[Tool Response: " + getStatus() + ", Result: " + getResult() + "]";
    }
}