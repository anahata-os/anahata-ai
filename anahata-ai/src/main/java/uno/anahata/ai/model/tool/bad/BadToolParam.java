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
package uno.anahata.ai.model.tool.bad;

import uno.anahata.ai.model.tool.java.*;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import lombok.Getter;
import lombok.NonNull;
import uno.anahata.ai.model.tool.AbstractToolParameter;
import uno.anahata.ai.tool.AIToolParam;
import uno.anahata.ai.tool.schema.SchemaProvider;

/**
 * A subclass of AbstractToolParameter that holds Java-specific reflection
 * information, namely the full generic Type of the parameter.
 *
 * @author anahata-gemini-pro-2.5
 */
@Getter
public class BadToolParam extends AbstractToolParameter<BadTool> {

    public BadToolParam(BadTool tool, String name) {
        super(tool, name, "", "", false, null);
    }

    
}