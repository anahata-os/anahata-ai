package uno.anahata.ai.model.tool;

import lombok.Getter;
import lombok.NonNull;

/**
 * The response for a {@link BadToolCall}. Its status is immediately set to
 * {@link ToolExecutionStatus#NOT_EXECUTED} and its execute method is a no-op.
 *
 * @author pablo
 */
@Getter
public class BadToolResponse extends AbstractToolResponse<BadToolCall, BadTool> {

    @NonNull
    private final BadToolCall invocation;

    public BadToolResponse(@NonNull BadToolCall invocation) {
        this.invocation = invocation;
        setStatus(ToolExecutionStatus.NOT_EXECUTED);
        setError("Tool call rejected: The tool '" + invocation.getName() + "' was not found.");
    }

    @Override
    public void execute(BadTool tool) {
        // No-op, as the tool was never found.
    }
}
