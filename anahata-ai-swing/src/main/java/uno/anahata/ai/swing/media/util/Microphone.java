package uno.anahata.ai.swing.media.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * A low-level service for controlling the microphone and playing back recorded audio.
 * This class is NOT an AI tool.
 */
@Slf4j
public final class Microphone {
    private static final AudioFormat FORMAT = new AudioFormat(16000, 16, 1, true, true);
    private TargetDataLine targetDataLine;
    private final AtomicBoolean recording = new AtomicBoolean(false);
    private ByteArrayOutputStream byteArrayOutputStream;

    public Microphone(TargetDataLine targetDataLine) {
        this.targetDataLine = targetDataLine;
    }

    public boolean isRecording() {
        return recording.get();
    }

    public void setTargetDataLine(TargetDataLine newTargetDataLine) {
        if (recording.get()) {
            throw new IllegalStateException("Cannot change microphone line while recording is in progress.");
        }
        if (this.targetDataLine != null && this.targetDataLine.isOpen()) {
            this.targetDataLine.close();
        }
        this.targetDataLine = newTargetDataLine;
    }

    public void startRecording() throws LineUnavailableException {
        if (recording.get()) {
            throw new IllegalStateException("Recording is already in progress");
        }
        targetDataLine.open(FORMAT);
        targetDataLine.start();
        recording.set(true);
        byteArrayOutputStream = new ByteArrayOutputStream();
        
        // Recording now happens on the calling thread (SwingWorker's doInBackground)
        byte[] buffer = new byte[4096];
        int bytesRead;
        try {
            while (recording.get()) {
                bytesRead = targetDataLine.read(buffer, 0, buffer.length);
                if (bytesRead > 0) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
            }
        } catch (Exception e) {
            log.error("Error during audio recording buffer read", e);
        }
    }

    public File stopRecording() throws IOException {
        if (!recording.get()) {
            log.warn("Stop recording called, but recording was not in progress.");
            return null;
        }
        recording.set(false); 
        
        targetDataLine.stop();
        targetDataLine.close();
        
        File tempFile = File.createTempFile("recording", ".wav");
        
        byte[] audioData = byteArrayOutputStream.toByteArray();
        try (ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
             AudioInputStream ais = new AudioInputStream(bais, FORMAT, audioData.length / FORMAT.getFrameSize())) {
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, tempFile);
        }
        return tempFile;
    }
    
    /**
     * Returns a list of all available TargetDataLines that support the default audio format,
     * wrapped in LineInfo objects for display purposes.
     * @return A list of available Microphone.LineInfo objects.
     */
    public static List<LineInfo> getAvailableLines() {
        List<LineInfo> lines = new ArrayList<>();
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info mixerInfo : mixerInfos) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            Line.Info[] targetLineInfos = mixer.getTargetLineInfo();
            for (Line.Info lineInfo : targetLineInfos) {
                if (lineInfo instanceof TargetDataLine.Info) {
                    TargetDataLine.Info tdLineInfo = (TargetDataLine.Info) lineInfo;
                    // Check if the line supports the desired format
                    if (tdLineInfo.getFormats().length > 0) { 
                        try {
                            TargetDataLine line = (TargetDataLine) mixer.getLine(tdLineInfo);
                            lines.add(new LineInfo(line, mixerInfo.getName()));
                        } catch (LineUnavailableException e) {
                            log.warn("Line {} from mixer {} is unavailable: {}", tdLineInfo.toString(), mixerInfo.getName(), e.getMessage());
                        }
                    }
                }
            }
        }
        return lines;
    }

    /**
     * Returns the default TargetDataLine that supports the default audio format,
     * wrapped in a LineInfo object.
     * @return The default Microphone.LineInfo object.
     * @throws LineUnavailableException If the default line is not available or supported.
     */
    public static LineInfo getDefaultLine() throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, FORMAT);
        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("Default microphone line not supported with format: " + FORMAT);
        }
        TargetDataLine defaultLine = (TargetDataLine) AudioSystem.getLine(info);
        // Find the mixer info for the default line to get its name
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info mixerInfo : mixerInfos) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            for (Line.Info lineInfo : mixer.getTargetLineInfo()) {
                if (lineInfo.equals(defaultLine.getLineInfo())) {
                    return new LineInfo(defaultLine, mixerInfo.getName());
                }
            }
        }
        // Fallback if mixer info not found (should not happen for default line)
        return new LineInfo(defaultLine, "Default Microphone");
    }

    /**
     * A simple data class to hold a TargetDataLine and its human-readable display name.
     */
    @Getter
    public static class LineInfo {
        private final TargetDataLine targetDataLine;
        private final String displayName;

        public LineInfo(TargetDataLine targetDataLine, String displayName) {
            this.targetDataLine = targetDataLine;
            this.displayName = displayName;
        }
    }
}
