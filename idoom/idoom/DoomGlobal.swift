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

private var SCREEN: [UInt8] = [];
private let screenSyncQueue = DispatchQueue(label: "screen access queue") // Serial queue

func initScreen(width: Int, height: Int) {
    SCREEN_WIDTH = width
    SCREEN_HEIGHT = height
    screenSyncQueue.sync {
        SCREEN = [UInt8](repeating: 0, count: width * height)
    }
}

func updateScreen(source: UnsafePointer<UInt8>) {
    screenSyncQueue.sync {
        let count = SCREEN.count
        SCREEN.withUnsafeMutableBytes { destPointer in
            memcpy(destPointer.baseAddress!, source, count)
        }
    }
}

func readScreen() -> [UInt8] {
    return screenSyncQueue.sync { SCREEN } // Read inside the queue
}

