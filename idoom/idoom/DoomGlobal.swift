//
//  DoomGlobal.swift
//  idoom
//
//  Created by Daniil Popov on 03.03.2025.
//

import Foundation
import Cocoa

let SCREEN_SCALE = 3

var SCREEN_WIDTH = 0
var SCREEN_HEIGHT = 0
var PALLETTE: [NSColor] = Array(repeating: NSColor.black, count: 256);

private var keyEvents: [KeyEvent] = [];
private let keyEventSyncQueue = DispatchQueue(label: "key events", qos: .userInteractive) // Serial queue

func addKeyEvent(_ event: KeyEvent) {
    keyEventSyncQueue.sync {
        keyEvents.append(event)
    }
}

func popAllKeyEvents(to destination: inout [KeyEvent]) {
    keyEventSyncQueue.sync {
        if (!keyEvents.isEmpty) {
            destination = keyEvents
            keyEvents.removeAll(keepingCapacity: true)
        }
    }
}

struct KeyEvent {
    let type: KeyEventType
    let keyCode: Int32
}

enum KeyEventType {
    case up
    case down
}
