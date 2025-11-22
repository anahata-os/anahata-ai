/*
 * Copyright 2025 Anahata.
 *
 * Licensed under the Anahata Software License (ASL) V2.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://github.com/pablo-anahata/anahata-ai-parent/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Força Barça!
 */
package uno.anahata.ai.model.tool;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NonNull;
import uno.anahata.ai.tool.ToolManager;

/**
 * Represents a collection of related tools, parsed from a single Java class.
 * This is the core domain model for a "toolkit".
 *
 * @author anahata-gemini-pro-2.5
 * @param <T> The specific type of AbstractTool contained in this toolkit.
 */
@Getter
public abstract class AbstractToolkit<T extends AbstractTool> {
    @NonNull
    protected final ToolManager toolManager;
    
    protected String name;
    protected String description;
    protected int defaultRetention;
    private boolean enabled = true;

    protected AbstractToolkit(@NonNull ToolManager toolManager) {
        this.toolManager = toolManager;
    }
    
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

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}