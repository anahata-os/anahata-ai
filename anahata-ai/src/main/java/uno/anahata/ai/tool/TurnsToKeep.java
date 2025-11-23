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
 * Fora Bara!
 */
package uno.anahata.ai.tool;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A dedicated annotation to explicitly define the retention policy for a tool's
 * call/response pair in the conversation history, in number of user turns.
 * <p>
 * This can be applied at the class level (on an {@code @AiToolkit}) or at the
 * method level (on an {@code @AiTool}). The method-level annotation always
 * takes precedence.
 *
 * @author anahata-ai
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface TurnsToKeep {
    /**
     * The number of user turns the tool call and its response should be
     * retained in the conversation context.
     */
    int value();
}
