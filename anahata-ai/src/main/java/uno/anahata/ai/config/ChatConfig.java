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
package uno.anahata.ai.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import uno.anahata.ai.AiConfig;
import uno.anahata.ai.model.provider.AbstractAiProvider;

/**
 * A model-agnostic, intelligent configuration object for a single chat session.
 * It defines the blueprint for a chat, including which AI providers and tools are available.
 */
@Getter
@Setter
@RequiredArgsConstructor
public class ChatConfig {

    /** A reference to the global, application-wide configuration. */
    @NonNull
    private final AiConfig aiConfig;

    /** The unique identifier for this specific chat session. */
    @NonNull
    private final String sessionId;

    /**
     * The list of AI provider classes available for this chat session.
     * The Chat orchestrator will use this list to discover and instantiate providers.
     */
    private List<Class<? extends AbstractAiProvider>> providerClasses = new ArrayList<>();
    
    /**
     * The list of tool classes to be used in this chat session.
     * This can be overridden by subclasses to provide a custom set of tools.
     */
    private List<Class<?>> toolClasses = new ArrayList<>();

    //<editor-fold defaultstate="collapsed" desc="V3 Context Management">
    /** The default number of user turns a TextPart should be kept in context. */
    private int defaultTextPartTurnsToKeep = 108;
    
    /** The default number of user turns a ToolResponse should be kept in context. */
    private int defaultToolTurnsToKeep = 5;
    
    /** The default number of user turns a BlobPart should be kept in context. */
    private int defaultBlobPartTurnsToKeep = 3;
    
    /** The number of turns a part must be soft-pruned before it is eligible for hard-pruning (permanent deletion). */
    private int hardPruneDelay = 108;
    //</editor-fold>
    
    /**
     * Convenience method to get the host application ID from the parent AiConfig.
     * @return The host application ID.
     */
    public String getHostApplicationId() {
        return aiConfig.getHostApplicationId();
    }
}
