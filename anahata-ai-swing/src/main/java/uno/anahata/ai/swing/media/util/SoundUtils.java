/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uno.anahata.ai.swing.media.util;

import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author pablo
 */
@Slf4j
public class SoundUtils {
    public static final AudioFormat RECORDING_FORMAT = new AudioFormat(16000, 16, 1, true, true);
    
    /**
     * Calculates the Root Mean Square.
     * 
     * @param audioData
     * @param length
     * @return 
     */
    public static final double calculateRMS(byte[] audioData, int length) {
        long sum = 0;
        for (int i = 0; i < length; i += 2) {  // 16-bit, little-endian
            int sample = (audioData[i + 1] << 8) | (audioData[i] & 0xFF);
            if (sample >= 32768) sample -= 65536;  // Convert to signed
            sum += sample * sample;
        }
        double rms = Math.sqrt(sum / (length / 2.0));
        return rms / 32768.0;  // Normalize to 0.0–1.0
    }
    
    /**
     * Returns a list of all available TargetDataLines that support the default audio format,
     * with the system default line as the first item, wrapped in LineInfo objects for display purposes.
     * @return A list of available MicrophonePanel.LineInfo objects.
     */
    public static List<MicrophonePanel.LineInfo> getAvailableRecordingLines() {
        List<MicrophonePanel.LineInfo> lines = new ArrayList<>();
        MicrophonePanel.LineInfo defaultLineInfo = null;

        // Try to get the system's default TargetDataLine
        TargetDataLine systemDefaultTargetDataLine = null;
        try {
            DataLine.Info defaultInfo = new DataLine.Info(TargetDataLine.class, RECORDING_FORMAT);
            if (AudioSystem.isLineSupported(defaultInfo)) {
                systemDefaultTargetDataLine = (TargetDataLine) AudioSystem.getLine(defaultInfo);
            }
        } catch (LineUnavailableException e) {
            log.warn("System default microphone line not available or supported: {}", e.getMessage());
        }

        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info mixerInfo : mixerInfos) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            Line.Info[] targetLineInfos = mixer.getTargetLineInfo();
            for (Line.Info lineInfo : targetLineInfos) {
                if (lineInfo instanceof TargetDataLine.Info) {
                    TargetDataLine.Info tdLineInfo = (TargetDataLine.Info) lineInfo;
                    if (tdLineInfo.getFormats().length > 0) { 
                        try {
                            TargetDataLine line = (TargetDataLine) mixer.getLine(tdLineInfo);
                            boolean isDefault = (systemDefaultTargetDataLine != null && line.equals(systemDefaultTargetDataLine));
                            MicrophonePanel.LineInfo currentLineInfo = new MicrophonePanel.LineInfo(line, mixerInfo, isDefault);
                            
                            if (isDefault) {
                                defaultLineInfo = currentLineInfo;
                            } else {
                                lines.add(currentLineInfo);
                            }
                        } catch (LineUnavailableException e) {
                            log.warn("Line {} from mixer {} is unavailable: {}", tdLineInfo.toString(), mixerInfo.getName(), e.getMessage());
                        }
                    }
                }
            }
        }
        
        // Add the default line at the beginning if found
        if (defaultLineInfo != null) {
            lines.add(0, defaultLineInfo);
        }
        
        return lines;
    }
}
