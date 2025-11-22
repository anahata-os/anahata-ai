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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 * A rich, self-documenting, abstract representation of a single parameter for a tool method.
 * This is the base class for all tool parameters.
 *
 * @author anahata-gemini-pro-2.5
 * @param <T> The type of the tool this parameter belongs to.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractToolParameter<T extends AbstractTool<?, ?>> {
    /** The tool this parameter belongs to. */
    @NonNull
    protected final T tool;
    
    /** The name of the parameter. */
    @NonNull
    private final String name;

    /** A detailed description of the parameter's purpose and expected format. */
    @NonNull
    private final String description;

    /** A pre-generated, language-agnostic JSON schema for this parameter. */
    @NonNull
    private final String jsonSchema;

    /** Whether this parameter is required for the tool call. */
    private final boolean required;

    /** An optional identifier for a custom UI renderer for this parameter. */
    private final String rendererId;
}
