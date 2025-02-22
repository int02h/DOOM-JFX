package com.dpforge.doom.wad;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class SoundRenderer {

    private static final int SAMPLE_RATE = 11025;

    void renderWav(File output, byte[] data) throws IOException {
        // Define the WAV file format
        AudioFormat format = new AudioFormat(
                AudioFormat.Encoding.PCM_UNSIGNED, // 8-bit PCM unsigned
                SAMPLE_RATE,    // Sample rate
                8,              // Bits per sample (8-bit)
                1,              // Number of channels (1 = mono, 2 = stereo)
                1,              // Frame size (1 byte per sample for mono)
                SAMPLE_RATE,    // Frame rate
                false           // Little-endian (ignored for 8-bit)
        );

        // Wrap PCM data in an AudioInputStream
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        AudioInputStream audioInputStream = new AudioInputStream(bais, format, data.length);

        // Write the WAV file using the standard library
        AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, output);
    }
}
