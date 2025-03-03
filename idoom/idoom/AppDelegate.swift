import Cocoa

class AppDelegate: NSObject, NSApplicationDelegate {

    func applicationDidFinishLaunching(_ aNotification: Notification) {
        // Run C game loop in the background
        DispatchQueue.global(qos: .userInteractive).async {
            let initGraphicsPointer: @convention(c) (Int32, Int32) -> Void = initGraphics
            Callback_InitGraphics(initGraphicsPointer);
            
            let setPalettePointer: @convention(c) (UnsafePointer<UInt8>?) -> Void = setPalette
            Callback_SetPalette(setPalettePointer);
            
            let finishUpdatePointer: @convention(c) (UnsafePointer<UInt8>?) -> Void = finishUpdate
            Callback_FinishUpdate(finishUpdatePointer);
            
            D_DoomMain();
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
    
}

private func initGraphics(width: Int32, height: Int32) {
    print("Init Graphics. Width: \(width), height: \(height)")
    DispatchQueue.main.async {
        initScreen(width: Int(width), height: (Int)(height))
        let window = NSWindow(
            contentRect: NSRect(x: 0, y: 0, width: SCREEN_SCALE * Int(width), height: SCREEN_SCALE * Int(height)),
            styleMask: [.titled, .closable, .miniaturizable],
            backing: .buffered,
            defer: false
        )
        window.title = "iDOOM"
        window.makeKeyAndOrderFront(nil)
        window.contentView = DoomView(frame: window.contentView!.bounds)
    }
}

private func setPalette(_ palette: UnsafePointer<UInt8>?) {
    // 256 colors, 3 bytes each (RGB)
    let byteArray = Array(UnsafeBufferPointer(start: palette, count: 3 * 256))
    DispatchQueue.main.async {
        (0..<256).forEach {
            let r = Double(byteArray[3*$0]) / 255;
            let g = Double(byteArray[3*$0 + 1]) / 256;
            let b = Double(byteArray[3*$0 + 2]) / 256;
            PALLETTE[$0] = NSColor(red: r, green: g, blue: b, alpha: 1.0)
        }
    }
}

private func finishUpdate(_ screen: UnsafePointer<UInt8>?) {
    guard let screen = screen else {
        fatalError("screen is nil on finishUpdate")
    }
    updateScreen(source: screen)
    if let view = DoomView.instance {
        DispatchQueue.main.async {
            view.setNeedsDisplay(view.bounds)
        }
    }
}
