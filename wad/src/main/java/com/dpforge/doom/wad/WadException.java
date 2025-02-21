package com.dpforge.doom.wad;

public class WadException extends Exception {

    public WadException(String message) {
        super(message);
    }

    public WadException(String format, Object... args) {
        this(String.format(format, args));
    }

}
