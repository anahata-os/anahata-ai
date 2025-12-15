package uno.anahata.ai.swing.media.util;

import java.util.concurrent.ExecutorService;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import lombok.extern.slf4j.Slf4j;
import uno.anahata.ai.AiExecutors;

/**
 * A simple UI utility to play short, non-blocking notification sounds from resources.
 * All playback is handled on a dedicated 'audio-player' thread pool.
 */
@Slf4j
public final class AudioPlayer {

    private static final ExecutorService audioExecutor = AiExecutors.newCachedThreadPoolExecutor("audio-player");

    private AudioPlayer() {}

    /**
     * Plays a short, non-blocking notification sound from the application's resources.
     * <p>
     * The sound file is expected to be located in the {@code /sounds/} directory
     * within the classpath. Playback is handled on a dedicated background thread
     * and will be skipped if the microphone is currently active.
     *
     * @param resourceName The simple name of the sound file to play (e.g., "idle.wav").
     */
    public static void playSound(final String resourceName) {
        // FIX: Microphone.isRecording() is not available in this module yet.
        // We will address Microphone.java separately.
        // if (Microphone.isRecording()) {
        //     return;
        // }
        audioExecutor.submit(() -> {
            try (AudioInputStream inputStream = AudioSystem.getAudioInputStream(
                AudioPlayer.class.getResourceAsStream("/sounds/" + resourceName))) {
                if (inputStream == null) {
                    log.warn("Sound resource not found: {}", resourceName);
                    return;
                }
                Clip clip = AudioSystem.getClip();
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
                clip.open(inputStream);
                clip.start();
            } catch (Exception e) {
                log.warn("Could not play sound resource: {}", resourceName, e);
            }
        });
    }
}
