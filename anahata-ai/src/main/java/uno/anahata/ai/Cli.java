package uno.anahata.ai;

import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import uno.anahata.ai.model.core.Request;
import uno.anahata.ai.model.core.RequestConfig;
import uno.anahata.ai.model.core.Response;
import uno.anahata.ai.model.core.TextPart;
import uno.anahata.ai.model.core.UserMessage;
import uno.anahata.ai.model.provider.AbstractAiProvider;
import uno.anahata.ai.model.provider.AbstractModel;

/**
 * Encapsulates the logic for an interactive command-line chat session.
 * @author Anahata
 */
public class Cli {

    private final Scanner scanner;
    private final AbstractAiProvider provider;
    private final List<? extends AbstractModel> models;

    public Cli(Scanner scanner, AbstractAiProvider provider) {
        this.scanner = scanner;
        this.provider = provider;
        this.models = provider.getModels();
    }

    public void start() {
        if (models.isEmpty()) {
            System.out.println("No models found for provider '" + provider.getProviderId() + "'. Cannot start chat.");
            return;
        }
        
        System.out.println("\nAvailable Models for Chat:");
        for (int i = 0; i < models.size(); i++) {
            System.out.printf("%d: %s (%s)\n", i + 1, models.get(i).getDisplayName(), models.get(i).getModelId());
        }

        System.out.print("Select a model number: ");
        int modelIndex;
        try {
            modelIndex = Integer.parseInt(scanner.nextLine()) - 1;
            if (modelIndex < 0 || modelIndex >= models.size()) {
                System.out.println("Invalid model number.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return;
        }

        AbstractModel selectedModel = models.get(modelIndex);
        System.out.println("\nStarting chat with '" + selectedModel.getDisplayName() + "'. Type 'exit' or 'quit' to return to the menu.");

        while (true) {
            System.out.print("\nYou: ");
            String userInput = scanner.nextLine();

            if ("exit".equalsIgnoreCase(userInput) || "quit".equalsIgnoreCase(userInput)) {
                break;
            }

            UserMessage userMessage = new UserMessage();
            userMessage.getParts().add(new TextPart(userInput));

            Request request = new Request(selectedModel, Collections.singletonList(userMessage), RequestConfig.builder().build());
            
            System.out.println("Model: ...");
            Response response = provider.generateContent(request);

            response.getCandidates().stream()
                .findFirst()
                .ifPresent(message -> System.out.println(message.asText()));
            
            System.out.println("[Finish Reason: " + response.getFinishReason() + ", Total Tokens: " + response.getTotalTokenCount() + "]");
        }
    }
}