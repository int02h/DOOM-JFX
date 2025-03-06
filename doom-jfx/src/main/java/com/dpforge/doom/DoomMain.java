package com.dpforge.doom;

public class DoomMain {
    public native void start();

    static {
        System.loadLibrary("jnidoom");
        System.loadLibrary("doom");
    }

    public static void main(String[] args) {
        new DoomMain().start();
    }
}
