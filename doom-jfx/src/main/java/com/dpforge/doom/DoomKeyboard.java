package com.dpforge.doom;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

class DoomKeyboard implements KeyListener {

    private final List<DoomEvent> buffer = new ArrayList<>();

    void getAllCodes(List<DoomEvent> codes) {
        synchronized (buffer) {
            codes.addAll(buffer);
            buffer.clear();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // do nothing
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = getCode(e.getKeyCode());
        if (code > 0) {
            synchronized (buffer) {
                buffer.add(new DoomEvent(DoomEventType.KEY_DOWN, code));
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = getCode(e.getKeyCode());
        if (code > 0) {
            synchronized (buffer) {
                buffer.add(new DoomEvent(DoomEventType.KEY_UP, code));
            }
        }
    }

    private static int getCode(int keyCode) {
        return switch (keyCode) {
            case KeyEvent.VK_LEFT -> 0xac;
            case KeyEvent.VK_RIGHT -> 0xae;
            case KeyEvent.VK_UP -> 0xad;
            case KeyEvent.VK_DOWN -> 0xaf;
            case KeyEvent.VK_ENTER -> 13;
            case KeyEvent.VK_ESCAPE -> 27;
            case KeyEvent.VK_SHIFT -> (0x80 + 0x36);
            case KeyEvent.VK_CONTROL, KeyEvent.VK_Z -> (0x80 + 0x1d);
            case KeyEvent.VK_ALT -> (0x80 + 0x38);
            default -> keyCode;
        };

    }
}