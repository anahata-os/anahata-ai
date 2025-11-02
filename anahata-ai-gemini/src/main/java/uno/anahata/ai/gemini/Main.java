package uno.anahata.ai.gemini;

import uno.anahata.ai.gemini.model.provider.GeminiAiProvider;
import java.util.Set;
import uno.anahata.ai.model.config.AiConfig;
import uno.anahata.ai.model.provider.AbstractAiProvider;
import uno.anahata.ai.model.provider.AiProviderRegistry;
import uno.anahata.ai.model.provider.model.AbstractAiModel;

/**
 * A simple test harness to demonstrate the programmatic provider registry.
 * @author anahata
 */
public class Main {

    public static void main(String[] args) {
        // Configure SLF4J Simple Logger to show DEBUG messages
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
        
        System.out.println("Starting Anahata AI Provider Test...");

        // 1. Create the core configuration and registry
        AiConfig config = new AiConfig();
        AiProviderRegistry registry = new AiProviderRegistry();

        // 2. Programmatically create and register our Gemini provider
        AbstractAiProvider geminiProvider = new GeminiAiProvider(config);
        registry.registerProvider(geminiProvider);

        // 3. Use the registry to discover all available models and actions
        System.out.println("\nDiscovering models and actions from all registered providers...");
        
        registry.getProviders().forEach(provider -> {
            System.out.println("----------------------------------------");
            System.out.println("Provider: " + provider.getProviderId());
            System.out.println("----------------------------------------");
            
            // List all models
            for (AbstractAiModel model : provider.listModels()) {
                System.out.printf("  - Model: %s (%s)\n", model.getDisplayName(), model.getModelId());
                System.out.printf("    Supported Actions: %s\n", model.getSupportedActions());
            }
            
            // List all unique supported actions
            Set<String> allActions = provider.getAllSupportedActions();
            System.out.println("\n  Provider-wide Unique Supported Actions:");
            System.out.println("  " + allActions);
        });
        
        System.out.println("\nTest complete.");
    }
}
