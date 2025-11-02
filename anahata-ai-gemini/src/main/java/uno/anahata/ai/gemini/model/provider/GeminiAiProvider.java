package uno.anahata.ai.gemini.model.provider;

import com.google.genai.Client;
import com.google.genai.types.ListModelsConfig;
import com.google.genai.types.Model;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import uno.anahata.ai.gemini.model.provider.model.GeminiAiModel;
import uno.anahata.ai.gemini.util.GeminiGsonUtils;
import uno.anahata.ai.model.config.AiConfig;
import uno.anahata.ai.model.provider.AbstractAiProvider;

@Slf4j
public class GeminiAiProvider extends AbstractAiProvider {

    public GeminiAiProvider(AiConfig config) {
        super("gemini", config);
    }

    private Client getClient() {
        String key = getApiKey();
        if (key == null || key.isBlank()) {
            throw new RuntimeException("Gemini API key is not available. Cannot create client. Please check api_keys.text.");
        }
        // Client is created on demand with the selected key
        return new Client.Builder()
            .apiKey(key)
            .build();
    }

    @Override
    public List<GeminiAiModel> listModels() {
        log.info("Listing Gemini Models using GenaiClient...");
        List<GeminiAiModel> models = new ArrayList<>();
        
        try (Client client = getClient()) {
            for (Model googleModel : client.models.list(ListModelsConfig.builder().build())) {
                log.debug("Found GenAI Model:\n\tJSON: {}\n\ttoString: {}", 
                          GeminiGsonUtils.getPrettyPrintGson().toJson(googleModel), 
                          googleModel.toString());
                models.add(toGeminiAiModel(googleModel));
            }
        } catch (Exception e) {
            log.error("Failed to list Gemini models. Error: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
        
        return models;
    }

    private GeminiAiModel toGeminiAiModel(Model googleModel) {
        return new GeminiAiModel(this, googleModel);
    }
}
