/* Licensed under the Anahata Software License (ASL) v 108. See the LICENSE file for details. Força Barça! */
package uno.anahata.ai.gemini.adapter;

import com.google.genai.types.FunctionResponse;
import com.google.genai.types.FunctionResponsePart;
import com.google.genai.types.Part;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uno.anahata.ai.internal.JacksonUtils;
import uno.anahata.ai.model.core.AbstractPart;
import uno.anahata.ai.model.core.BlobPart;
import uno.anahata.ai.model.core.TextPart;
import uno.anahata.ai.model.tool.AbstractToolResponse;
import uno.anahata.ai.model.tool.ToolResponseAttachment;

/**
 * An object-oriented adapter that converts a single Anahata AbstractPart into a
 * native Google GenAI Part. This class handles simple, one-to-one conversions
 * and the complex conversion of {@link AbstractToolResponse} into a single,
 * rich {@code FunctionResponse} part.
 *
 * @author anahata-ai
 */
@Slf4j
@RequiredArgsConstructor
public class GeminiPartAdapter {
    private final AbstractPart anahataPart;

    /**
     * Performs the conversion for simple, one-to-one Anahata parts.
     * @return The corresponding Google GenAI Part, or null if unsupported.
     */
    public Part toGoogle() {
        if (anahataPart instanceof TextPart) {
            return Part.fromText(((TextPart) anahataPart).getText());
        }
        if (anahataPart instanceof BlobPart) {
            BlobPart blob = (BlobPart) anahataPart;
            // The Gemini API does not have a Part.fromBlob(byte[], mimeType) method.
            // We must use the builder pattern for inline data.
            return Part.builder()
                .inlineData(com.google.genai.types.Blob.builder()
                    .data(blob.getData())
                    .mimeType(blob.getMimeType())
                    .build())
                .build();
        }
        if (anahataPart instanceof AbstractToolResponse) {
            return toGoogleFunctionResponsePart();
        }
        
        log.warn("Unsupported Anahata Part type for Google conversion, skipping: {}", anahataPart.getClass().getSimpleName());
        return null;
    }

    /**
     * Converts an AbstractToolResponse into the main Google FunctionResponse Part,
     * including attachments.
     * @return The corresponding Google FunctionResponse Part.
     */
    private Part toGoogleFunctionResponsePart() {
        AbstractToolResponse<?> anahataResponse = (AbstractToolResponse) anahataPart;
        Map<String, Object> responseMap;
        
        // 1. Determine the JSON payload (output or error)
        if (StringUtils.isNotBlank(anahataResponse.getError())) {
            // If there's an error, the response map must contain the "error" key.
            responseMap = JacksonUtils.convertObjectToMap("error", anahataResponse.getError());
        } else {
            // Otherwise, it was successful, and the map must contain the "output" key.
            responseMap = JacksonUtils.convertObjectToMap("output", anahataResponse.getResult());
        }
        
        // 2. Convert attachments to FunctionResponsePart
        List<FunctionResponsePart> attachmentParts = new ArrayList<>();
        for (ToolResponseAttachment attachment : anahataResponse.getAttachments()) {
            attachmentParts.add(toGoogleAttachmentPart(attachment));
        }

        // 3. Build the FunctionResponse
        FunctionResponse fr = FunctionResponse.builder()
            .name(anahataResponse.getCall().getToolName())
            .response(responseMap)
            .parts(attachmentParts) // Attachments are nested here
            .build();
        
        return Part.builder().functionResponse(fr).build();
    }
    
    /**
     * Converts a ToolResponseAttachment into a Google FunctionResponsePart.
     * @param attachment The attachment to convert.
     * @return The corresponding Google FunctionResponsePart.
     */
    private static FunctionResponsePart toGoogleAttachmentPart(ToolResponseAttachment attachment) {
        return FunctionResponsePart.fromBytes(attachment.getData(), attachment.getMimeType());
    }
}