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
package uno.anahata.ai.model.tool.java;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import uno.anahata.ai.model.tool.AbstractTool;
import uno.anahata.ai.model.tool.AbstractToolkit;
import uno.anahata.ai.tool.AiTool;
import uno.anahata.ai.tool.AiToolkit;
import uno.anahata.ai.tool.ToolManager;

/**
 * A domain object that parses a Java class via reflection to build a complete,
 * self-contained Toolkit, including all its tools and parameters.
 * <p>
 * This class is the cornerstone of the V2's decoupled tool architecture,
 * separating the parsing of tool metadata from the management and execution of tools.
 */
@Slf4j
@Getter
public class JavaObjectToolkit extends AbstractToolkit<JavaMethodTool> {

    /** The singleton instance of the tool class. */
    private final Object toolInstance;

    /** A list of all declared methods (tools) for this toolkit. */
    private final List<JavaMethodTool> tools;

    /**
     * Constructs a new JavaObjectToolkit by parsing the given class.
     * @param toolManager The parent ToolManager.
     * @param toolClass The class to parse.
     * @throws IllegalArgumentException if the class is not a valid toolkit.
     */
    public JavaObjectToolkit(ToolManager toolManager, Class<?> toolClass) throws Exception {
        super(toolManager);
        
        AiToolkit toolkitAnnotation = toolClass.getAnnotation(AiToolkit.class);
        if (toolkitAnnotation == null) {
            throw new IllegalArgumentException("Class " + toolClass.getName() + " is not annotated with @AiToolkit.");
        }
        
        // Set parent fields
        this.name = toolClass.getSimpleName();
        this.description = toolkitAnnotation.value();
        this.defaultRetention = toolkitAnnotation.retention();
        
        try {
            this.toolInstance = toolClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not instantiate tool class: " + toolClass.getName() + ". It must be public and have a public no-arg constructor.", e);
        }

        this.tools = new ArrayList<>();
        for (Method method : toolClass.getDeclaredMethods()) {
            AiTool toolAnnotation = method.getAnnotation(AiTool.class);
            if (toolAnnotation != null) {
                tools.add(new JavaMethodTool(this, toolInstance, method, toolAnnotation));
            }
        }
    }

    @Override
    public List<JavaMethodTool> getAllTools() {
        return tools;
    }
}