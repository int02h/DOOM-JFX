package com.dpforge.doom;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.nio.ByteBuffer;

public class DoomSound {

    private static byte[] audioBytes;
    private static SourceDataLine line;

    private static ByteBuffer mixBuffer;

    public native static ByteBuffer getMixBuffer();

    public static void initSound() {
        mixBuffer = getMixBuffer();

        AudioFormat format = new AudioFormat(11025, 16, 2, true, false);
        audioBytes = new byte[mixBuffer.limit()];

        try {
            // Get a SourceDataLine for playback
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    public static void submitSound() {
        try {
            mixBuffer.position(0);
            mixBuffer.get(audioBytes);

            // Write audio data
            line.write(audioBytes, 0, audioBytes.length);

            // Wait for playback to finish
            line.drain();
            //line.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
