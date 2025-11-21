package uno.anahata.ai.model.tool;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents a collection of related tools, parsed from a single Java class.
 * This is the core domain model for a "toolkit".
 *
 * @author pablo
 * @param <T> The specific type of AbstractTool contained in this toolkit.
 */
@RequiredArgsConstructor
@Getter
public abstract class AbstractToolkit<T extends AbstractTool> {
    public final String name;
    public final String description;
    public boolean enabled = true;
    
    /**
     * Gets all tools declared within this toolkit, regardless of their permission status.
     * @return The complete list of tools.
     */
    public abstract List<T> getAllTools();
    
    /**
     * Gets a list of tools that are allowed to be presented to the model.
     * This filters out tools that have a permanent {@link ToolPermission#DENY_NEVER} permission
     * and also returns an empty list if the entire toolkit is disabled.
     * 
     * @return A filtered list of allowed tools.
     */
    public List<T> getAllowedTools() {
        if (!enabled) {
            return Collections.emptyList();
        }
        return getAllTools().stream()
                .filter(tool -> tool.getPermission() != ToolPermission.DENY_NEVER)
                .collect(Collectors.toList());
    }
}
