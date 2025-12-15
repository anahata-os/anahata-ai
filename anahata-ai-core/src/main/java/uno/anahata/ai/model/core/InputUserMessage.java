/* Licensed under the Anahata Software License (ASL) v 108. See the LICENSE file for details. Força Barça! */
package uno.anahata.ai.model.core;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import uno.anahata.ai.chat.Chat;

/**
 * A specialized UserMessage designed for direct manipulation by UI components like
 * an input panel.
 * <p>
 * This class encapsulates the logic of managing a primary, editable text part,
 * providing convenient methods to get and set its content. This avoids cluttering
 * the generic {@link UserMessage} with UI-specific concerns and prevents
 * unintended side effects on other subclasses like {@link RagMessage}.
 *
 * @author Anahata
 */
@Slf4j
public class InputUserMessage extends UserMessage {

    /**
     * The primary, editable text part of this message.
     */
    @Getter
    private final TextPart editableTextPart;

    public InputUserMessage(Chat chat) {
        super(chat);
        this.editableTextPart = new TextPart(this, "");
    }

    /**
     * Gets the text content of the primary editable part.
     *
     * @return The text content.
     */
    public String getText() {
        return editableTextPart.getText();
    }

    /**
     * Sets the text content of the primary editable part.
     *
     * @param text The new text content.
     */
    public void setText(String text) {
        editableTextPart.setText(text);
    }

    /**
     * Checks if the message is empty. A message is considered empty if it
     * contains no parts other than the initial, empty editable text part.
     *
     * @return {@code true} if the message is empty, {@code false} otherwise.
     */
    public boolean isEmpty() {
        // It's empty if it only contains our editable text part AND that part is empty.
        return getParts().size() == 1 && editableTextPart.getText().isEmpty();
    }
    
    /**
     * Gets a list of all attached {@link BlobPart}s.
     * 
     * @return A list of BlobParts.
     */
    public List<BlobPart> getAttachments() {
        return getParts().stream()
            .filter(p -> p instanceof BlobPart)
            .map(p -> (BlobPart) p)
            .collect(Collectors.toList());
    }
    
    /**
     * Adds a single file path as an attachment to this message.
     * 
     * @param path The file path to attach.
     * @throws Exception if a BlobPart cannot be created from the path (e.g., file read error).
     */
    public void addAttachment(Path path) throws Exception {
        BlobPart.from(this, path);
    }

    /**
     * Adds a collection of file paths as attachments to this message.
     * 
     * @param paths The collection of file paths to attach.
     * @throws Exception if a BlobPart cannot be created from a path (e.g., file read error).
     */
    public void addAttachments(Collection<Path> paths) throws Exception {
        for (Path path : paths) {
            addAttachment(path); // Call the single-path method
        }
    }
}