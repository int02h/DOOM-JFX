import Cocoa

class AppDelegate: NSObject, NSApplicationDelegate {
    
    private var isCtrlPressed = false
    private var isShiftPressed = false

    func applicationDidFinishLaunching(_ aNotification: Notification) {
        NSEvent.addLocalMonitorForEvents(matching: [.keyDown, .keyUp, .flagsChanged]) { event in
            switch (event.type) {
            case .keyDown:
                if !event.isARepeat {
                    print("key code down: \(event.keyCode)")
                    addKeyEvent(KeyEvent(type: .down, keyCode: self.getDoomKeyCode(event)))
                }
            case .keyUp:
                if !event.isARepeat {
                    print("key code up: \(event.keyCode)")
                    addKeyEvent(KeyEvent(type: .up, keyCode: self.getDoomKeyCode(event)))
                }
            case .flagsChanged:
                let isCtrlPressedNow = event.modifierFlags.contains(.command)
                if (self.isCtrlPressed != isCtrlPressedNow) {
                    if (isCtrlPressedNow) {
                        print("ctrl down")
                        addKeyEvent(KeyEvent(type: .down, keyCode: (0x80+0x1d)))
                    } else {
                        print("ctrl up")
                        addKeyEvent(KeyEvent(type: .up, keyCode: (0x80+0x1d)))
                    }
                    self.isCtrlPressed = isCtrlPressedNow
                }
                let isShiftPressedNow = event.modifierFlags.contains(.shift)
                if (self.isShiftPressed != isShiftPressedNow) {
                    if (isShiftPressedNow) {
                        print("shift down")
                        addKeyEvent(KeyEvent(type: .down, keyCode: (0x80+0x36)))
                    } else {
                        print("shift up")
                        addKeyEvent(KeyEvent(type: .up, keyCode: (0x80+0x36)))
                    }
                    self.isShiftPressed = isShiftPressedNow
                }
            default: break
            }
            
            // Prevent macOS from handling the event
            return nil  // Returning nil stops the system from processing it
        }
        
        // Run C game loop in the background
        DispatchQueue.global(qos: .userInteractive).async {
            let initGraphicsPointer: @convention(c) (Int32, Int32) -> Void = initGraphics
            Callback_InitGraphics(initGraphicsPointer)
            
            let setPalettePointer: @convention(c) (UnsafePointer<UInt8>?) -> Void = setPalette
            Callback_SetPalette(setPalettePointer)
            
            let startFramePointer: @convention(c) () -> Void = startFrame
            Callback_StartFrame(startFramePointer)
            
            let finishUpdatePointer: @convention(c) (UnsafePointer<UInt8>?) -> Void = finishUpdate
            Callback_FinishUpdate(finishUpdatePointer)
            
            let initSoundPointer: @convention(c) (UnsafePointer<Int16>?, UInt32) -> Void = initSound
            Callback_InitSound(initSoundPointer)
            
            let submitSoundPointer: @convention(c) () -> Void = submitSound
            Callback_SubmitSound(submitSoundPointer)
            
            I_Main(CommandLine.argc, CommandLine.unsafeArgv);
        }
    }
    
    func applicationWillTerminate(_ aNotification: Notification) {
        // Insert code here to tear down your application
    }

    func applicationSupportsSecureRestorableState(_ app: NSApplication) -> Bool {
        return true
    }
    
    // Automatically quit when last window closes
    func applicationShouldTerminateAfterLastWindowClosed(_ sender: NSApplication) -> Bool {
        return true
    }
    
    private func getDoomKeyCode(_ event: NSEvent) -> Int32 {
        if (event.keyCode == 3) { // key f
            let averageTime = Double(totalTime) / Double(totalCount)
            print("Average execution time: \(averageTime / 1_000_000) ms")
        }

        if let asciiCode = event.charactersIgnoringModifiers?.first?.asciiValue {
            return Int32(asciiCode)
        }

        return switch event.keyCode {
        case 0x7B: 0xAC // arrow left
        case 0x7C: 0xAE // arrow right
        case 0x7D: 0xAF// arrow down
        case 0x7E: 0xAD // arrow up
        default: Int32(event.keyCode)
        }
    }
}

var windowRef: NSWindow? // keep window reference to avoid object deallocation

private func initGraphics(width: Int32, height: Int32) {
    print("Init Graphics. Width: \(width), height: \(height)")
    DispatchQueue.main.async {
        SCREEN_WIDTH = Int(width)
        SCREEN_HEIGHT = Int(height)
        let window = NSWindow(
            contentRect: NSRect(x: 0, y: 0, width: SCREEN_SCALE * Int(width), height: SCREEN_SCALE * Int(height)),
            styleMask: [.titled, .closable, .miniaturizable],
            backing: .buffered,
            defer: false
        )
        window.title = "iDOOM"
        window.contentView = DoomView(frame: window.contentView!.bounds)
        window.makeKeyAndOrderFront(nil)
        
        windowRef = window
    }
}

private func setPalette(_ palette: UnsafePointer<UInt8>?) {
    // 256 colors, 3 bytes each (RGB)
    let byteArray = Array(UnsafeBufferPointer(start: palette, count: 3 * 256))
    DispatchQueue.main.async {
        PALLETTE = byteArray
    }
}

private var keyEvents: [KeyEvent] = [];

private func startFrame() {
    keyEvents.removeAll(keepingCapacity: true)
    popAllKeyEvents(to: &keyEvents)
    keyEvents.forEach { event in
        switch event.type {
        case .down: onKeyDown(event.keyCode)
        case .up: onKeyUp(event.keyCode)
        }
    }
}

var totalTime: UInt64 = 0
var totalCount: UInt64 = 0

private func finishUpdate(_ screen: UnsafePointer<UInt8>?) {
    guard let screen = screen else {
        fatalError("screen is nil on finishUpdate")
    }
    
    let start = DispatchTime.now()
    
    if let view = DoomView.instance {
        DispatchQueue.main.sync {
            view.drawGameFrame(screen)
        }
    }
    
    let end = DispatchTime.now()
    let elapsed = end.uptimeNanoseconds - start.uptimeNanoseconds
    totalTime += elapsed
    totalCount += 1
}

var pcmPlayer: PCMPlayer?

private func initSound(_ mixBuffer: UnsafePointer<Int16>?, _ bufferSize: UInt32) {
    guard let mixBuffer = mixBuffer else {
        fatalError("mix buffer is nil")
    }
    pcmPlayer = PCMPlayer(mixBuffer: mixBuffer, bufferSize: UInt32(bufferSize))
}

private func submitSound() {
    guard let pcmPlayer = pcmPlayer else {
        fatalError("PCM player is not initialized")
    }
    pcmPlayer.play()
}
