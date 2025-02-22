package com.dpforge.doom.wad;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class BinaryReader {

    private ByteBuffer buffer;

    public BinaryReader(byte[] data) {
        buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    }

    public int getOffset() {
        return buffer.position();
    }

    public int setOffset(int offset) {
        int prevPosition = buffer.position();
        buffer.position(offset);
        return prevPosition;
    }

    public String readAsciiString(int length) {
        byte[] data = new byte[length];
        buffer.get(data);
        return new String(data, StandardCharsets.US_ASCII);
    }

    public String readNullPaddedAsciiString(int length) {
        byte[] data = new byte[length];
        buffer.get(data);
        int realSize = length;
        while (data[realSize - 1] == 0) realSize--;
        return new String(data, 0, realSize, StandardCharsets.US_ASCII);
    }

    public int readInt8() {
        return buffer.get() & 0xFF;
    }

    public int readInt16() {
        return buffer.getShort() & 0xFFFF;
    }

    public int readSignedInt16() {
        return buffer.getShort();
    }

    public int readInt32() {
        return buffer.getInt();
    }

    public byte[] readBytes(int size) {
        byte[] data = new byte[size];
        buffer.get(data);
        return data;
    }
}
