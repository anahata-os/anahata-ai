package uno.anahata.ai.tool.schema;

import java.util.List;
import lombok.Data;

/**
 * A mock object with a variety of field types, designed to test the richness
 * of the generated JSON schema.
 *
 * @author pablo
 */
@Data
public class MockComplexObject {
    private int primitiveField;
    private String stringField;
    private List<String> listField;
    private MockNestedObject nestedObject;
}
