/*
 * Licensed under the Anahata Software License (ASL) v 108. See the LICENSE file for details. Fora Bara!
 */
package uno.anahata.ai.status;

import java.time.Instant;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A record of a single API error, including context for retries.
 * @author pablo
 */
@RequiredArgsConstructor
@Getter
public class ApiErrorRecord {
    private final String modelId;
    private final Instant timestamp;
    private final int retryAttempt;
    private final long backoffAmountMs;
    private final Throwable exception;
}
