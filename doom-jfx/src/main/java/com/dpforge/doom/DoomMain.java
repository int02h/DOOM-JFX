package com.dpforge.doom;

public class DoomMain {
    public native void print();

    static {
        System.loadLibrary("doom");
    }

    public static void main(String[] args) {
        new DoomMain().print();
    }
}
