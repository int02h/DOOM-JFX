package com.dpforge.doom.wad;

public class EndDoom {

    private final byte[] data;

    public EndDoom(byte[] data) {
        this.data = data;
    }

    public String asText() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < data.length / 2; i++) {
            builder.append((char) data[2 * i]);
        }
        return builder.toString();
    }
}
