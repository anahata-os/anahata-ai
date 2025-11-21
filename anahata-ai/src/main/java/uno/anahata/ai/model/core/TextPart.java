package uno.anahata.ai.model.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A concrete Part implementation for simple text content.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TextPart extends Part {
    private String text;
}
