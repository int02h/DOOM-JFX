package com.dpforge.doom;

record DoomEvent(DoomEventType type, int code) {}

enum DoomEventType {
    KEY_DOWN,
    KEY_UP
}
