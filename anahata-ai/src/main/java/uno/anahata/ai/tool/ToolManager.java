package uno.anahata.ai.tool;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import uno.anahata.ai.AiConfig;
import uno.anahata.ai.model.tool.AbstractTool;
import uno.anahata.ai.model.tool.AbstractToolCall;
import uno.anahata.ai.model.tool.BadTool;
import uno.anahata.ai.model.tool.ToolPermission;
import uno.anahata.ai.model.tool.Toolkit;
import uno.anahata.ai.model.tool.java.JavaObjectToolkit;

/**
 * Manages the lifecycle of all AI tools, including registration,
 * configuration, and lookup. It stores toolkits and generates the full
 * list of tools on demand.
 *
 * @author pablo
 */
@Slf4j
@Getter
public class ToolManager {
    private static final AtomicInteger callIdGenerator = new AtomicInteger(0);

    private final AiConfig config;
    private final Map<String, Toolkit<?>> toolkits = new HashMap<>();

    public ToolManager(@NonNull AiConfig config) {
        this.config = config;
    }

    /**
     * Scans the given classes for methods annotated with {@link AiTool},
     * creates the corresponding toolkits, and applies any application-wide
     * preferences.
     *
     * @param classes The classes to scan for tools.
     */
    public void registerClasses(Class<?>... classes) {
        log.info("Registering tool classes...");
        for (Class<?> clazz : classes) {
            try {
                JavaObjectToolkit toolkit = new JavaObjectToolkit(clazz);
                toolkits.put(toolkit.getName(), toolkit);
                log.info("Registered toolkit: {}", toolkit.getName());
            } catch (Exception e) {
                log.error("Failed to register toolkit for class: {}", clazz.getName(), e);
            }
        }
        applyPreferences();
    }

    /**
     * The primary factory method for creating a model-agnostic tool call from
     * provider-specific data. This method orchestrates the creation and
     * pre-rejection logic.
     *
     * @param id The unique ID of the tool call (can be null).
     * @param name The name of the tool to call.
     * @param jsonArgs The raw arguments from the model.
     * @return An {@link AbstractToolCall} with its corresponding, possibly pre-rejected, response.
     */
    public AbstractToolCall createToolCall(String id, String name, Map<String, Object> jsonArgs) {
        String callId = (id == null || id.isEmpty()) ? String.valueOf(callIdGenerator.incrementAndGet()) : id;

        Optional<? extends AbstractTool> toolOpt = findToolByName(name);
        
        AbstractTool tool;
        if (toolOpt.isPresent()) {
            tool = toolOpt.get();
        } else {
            tool = new BadTool(name);
        }
        
        AbstractToolCall call = tool.createCall(callId, jsonArgs);

        // Post-creation checks
        Toolkit toolkit = tool.getToolkit();
        if (toolkit != null && !toolkit.isEnabled()) {
            String reason = "Tool call rejected: The toolkit '" + toolkit.getName() + "' is disabled.";
            log.warn(reason);
            call.getResponse().reject(reason);
        }

        if (tool.getPermission() == ToolPermission.DENY_NEVER) {
            String reason = "Tool call rejected: The tool '" + name + "' has a DENY_NEVER permission.";
            log.warn(reason);
            call.getResponse().reject(reason);
        }

        return call;
    }

    private Optional<? extends AbstractTool> findToolByName(String name) {
        return getAllTools().stream()
            .filter(t -> t.getName().equals(name))
            .findFirst();
    }

    public List<Toolkit<?>> getEnabledToolkits() {
        return toolkits.values().stream()
            .filter(Toolkit::isEnabled)
            .collect(Collectors.toList());
    }

    public List<Toolkit<?>> getDisabledToolkits() {
        return toolkits.values().stream()
            .filter(tk -> !tk.isEnabled())
            .collect(Collectors.toList());
    }

    /**
     * Gets a dynamically aggregated list of all tools from all registered toolkits.
     * This is a view and is generated on each call.
     * @return A list of all tools.
     */
    public List<? extends AbstractTool> getAllTools() {
        return toolkits.values().stream()
            .map(Toolkit::getAllTools)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    /**
     * Gets a list of all tools that are currently enabled and allowed to be
     * sent to the model.
     * @return A filtered list of enabled tools.
     */
    public List<? extends AbstractTool> getEnabledTools() {
        return getEnabledToolkits().stream()
            .map(Toolkit::getAllowedTools)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    private void applyPreferences() {
        log.info("Applying application-wide tool preferences...");
        // TODO: Implement logic to apply preferences from config.getPreferences()
        // to each tool in getAllTools().
    }
}
