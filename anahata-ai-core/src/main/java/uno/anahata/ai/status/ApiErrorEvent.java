/*
 * Licensed under the Anahata Software License (ASL) v 108. See the LICENSE file for details. Fora Bara!
 */
package uno.anahata.ai.status;

import lombok.Getter;
import uno.anahata.ai.chat.Chat;

/**
 * An event fired when an API call fails.
 * 
 * @author pablo
 */
@Getter
public class ApiErrorEvent extends ChatStatusEvent {
    
    private final ApiErrorRecord errorRecord;

    public ApiErrorEvent(Chat source, ApiErrorRecord errorRecord) {
        super(source, ChatStatus.WAITING_WITH_BACKOFF, "API Error: " + errorRecord.getException().getMessage());
        this.errorRecord = errorRecord;
    }
}
