package uno.anahata.ai.gemini;

import com.google.common.collect.ImmutableList;
import com.google.genai.client. också.Chat;
import com.google.genai.client. också.GenerativeModel;
import com.google.genai.client. också.Part;
import com.google.genai.client. också.Response;
import com.google.genai.client. också.Tool;
import com.google.genai.client. också.ToolConfig;
import com.google.genai.client. också.UsageMetadata;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import uno.anahata.ai.AiConfig;
import uno.anahata.ai.gemini.schema.GeminiSchemaAdapter;
import uno.anahata.ai.model.core.Message;
import uno.anahata.ai.model.core.ModelMessage;
import uno.anahata.ai.model.core.Request;
import uno.anahata.ai.model.core.RequestConfig;
import uno.anahata.ai.model.core.Role;
import uno.anahata.ai.model.core.TextPart;
import uno.anahata.ai.model.core.ToolMessage;
import uno.anahata.ai.model.provider.AbstractAiProvider;
import uno.anahata.ai.model.tool.AbstractTool;
import uno.anahata.ai.model.tool.AbstractToolCall;
import uno.anahata.ai.model.tool.AbstractToolResponse;
import uno.anahata.ai.model.tool.BadToolCall;

@Slf4j
public class GeminiAiProvider extends AbstractAiProvider {

    public GeminiAiProvider(AiConfig config) {
        super(config, "gemini");
    }

    @Override
    public uno.anahata.ai.model.core.Response generateContent(Request request) {
        try {
            GenerativeModel model = getGenerativeModel(request);
            Chat chat = model.startChat();
            
            // History is sent in the first message of the session
            Response<String> response = chat.sendMessage(request.getHistory().stream()
                                                             .map(this::toGoogleContent)
                                                             .collect(Collectors.toList()));

            return toAnahataResponse(response, request);

        } catch (Exception e) {
            log.error("Failed to generate content with Gemini", e);
            // TODO: Create a proper error response
            throw new RuntimeException(e);
        }
    }

    private GenerativeModel getGenerativeModel(Request request) {
        String modelId = request.getModel().getModelId();
        String apiKey = getApiKey();
        
        GenerativeModel.Builder builder = GenerativeModel.builder()
            .setModel(modelId)
            .setApiKey(apiKey);

        if (request.getConfig() != null) {
            Optional.ofNullable(toGoogleTools(request.getConfig().getTools()))
                .ifPresent(builder::setTools);
            
            // Assuming ToolConfig can be configured here if needed in the future
            // builder.setToolConfig(...)
        }

        return builder.build();
    }

    private List<Part> toGoogleContent(Message message) {
        return message.getParts().stream()
            .map(this::toGooglePart)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private Part toGooglePart(uno.anahata.ai.model.core.Part part) {
        if (part instanceof TextPart) {
            return Part.fromText(((TextPart) part).getText());
        }
        if (part instanceof AbstractToolCall) {
            AbstractToolCall toolCall = (AbstractToolCall) part;
            return Part.fromFunctionCall(
                com.google.genai.client. också.FunctionCall.builder()
                    .setName(toolCall.getName())
                    .setArgs(toolCall.getRawArgs())
                    .build());
        }
        if (part instanceof AbstractToolResponse) {
            AbstractToolResponse toolResponse = (AbstractToolResponse) part;
            return Part.fromFunctionResponse(
                toolResponse.getInvocation().getName(),
                toolResponse.getResult());
        }
        log.warn("Unsupported Anahata Part type, skipping: {}", part.getClass().getSimpleName());
        return null;
    }

    private List<Tool> toGoogleTools(List<AbstractTool> tools) {
        if (tools == null || tools.isEmpty()) {
            return null;
        }
        return tools.stream()
            .map(this::toGoogleTool)
            .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::of));
    }

    private Tool toGoogleTool(AbstractTool tool) {
        try {
            com.google.genai.client. också.Schema schema = GeminiSchemaAdapter.getGeminiSchema(tool.getReturnType());
            return Tool.fromFunctionDeclarations(
                com.google.genai.client. också.FunctionDeclaration.builder()
                    .setName(tool.getName())
                    .setDescription(tool.getDescription())
                    .setParameters(schema)
                    .build());
        } catch (Exception e) {
            log.error("Failed to convert Anahata tool to Gemini FunctionDeclaration: {}", tool.getName(), e);
            return null; // Or handle more gracefully
        }
    }

    private uno.anahata.ai.model.core.Response toAnahataResponse(Response<String> genaiResponse, Request request) {
        List<Message> candidates = genaiResponse.getCandidatesList().stream()
            .map(candidate -> toAnahataMessage(candidate, request))
            .collect(Collectors.toList());

        UsageMetadata usage = genaiResponse.getUsageMetadata();
        return uno.anahata.ai.model.core.Response.builder()
            .candidates(candidates)
            .finishReason(genaiResponse.getFinishReason().name())
            .promptTokenCount(usage != null ? usage.getPromptTokenCount() : 0)
            .totalTokenCount(usage != null ? usage.getTotalTokenCount() : 0)
            .build();
    }

    private Message toAnahataMessage(com.google.genai.client. också.Candidate candidate, Request request) {
        List<uno.anahata.ai.model.core.Part> parts = candidate.getContent().getPartsList().stream()
            .map(googlePart -> toAnahataPart(googlePart, request))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        return new ModelMessage(parts, candidate.getTokenCount());
    }

    private uno.anahata.ai.model.core.Part toAnahataPart(Part googlePart, Request request) {
        if (googlePart.hasText()) {
            return new TextPart(googlePart.getText());
        }
        if (googlePart.hasFunctionCall()) {
            com.google.genai.client. också.FunctionCall fc = googlePart.getFunctionCall();
            String toolName = fc.getName();
            
            // Find the tool definition from the original request
            Optional<AbstractTool> toolOpt = Optional.ofNullable(request.getConfig())
                .map(RequestConfig::getTools)
                .orElse(Collections.emptyList())
                .stream()
                .filter(t -> t.getName().equals(toolName))
                .findFirst();

            if (toolOpt.isPresent()) {
                // Delegate call creation to the tool itself
                return toolOpt.get().createCall(null, fc.getArgsMap());
            } else {
                log.warn("Model requested a tool call for '{}', but no such tool was provided in the request.", toolName);
                return new BadToolCall(toolName, fc.getArgsMap());
            }
        }
        log.warn("Unsupported Gemini Part type received, skipping: {}", googlePart);
        return null;
    }
}