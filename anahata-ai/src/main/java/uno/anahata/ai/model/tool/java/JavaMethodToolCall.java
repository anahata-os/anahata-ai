package uno.anahata.ai.model.tool.java;

import java.util.Map;
import lombok.Getter;
import lombok.NonNull;
import uno.anahata.ai.model.tool.AbstractToolCall;

/**
 * A model-agnostic representation of a request to execute a specific Java method tool.
 *
 * @author anahata
 */
@Getter
public class JavaMethodToolCall extends AbstractToolCall<JavaMethodTool, JavaMethodToolResponse> {
    
    public JavaMethodToolCall(@NonNull String id, @NonNull JavaMethodTool tool, @NonNull Map<String, Object> args) {
        super(id, tool, args);
    }

    @Override
    protected JavaMethodToolResponse createResponse() {
        return new JavaMethodToolResponse(this);
    }
}
