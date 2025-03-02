import Cocoa

class AppDelegate: NSObject, NSApplicationDelegate {
    
    var window: NSWindow!

    func applicationDidFinishLaunching(_ aNotification: Notification) {
        window = NSWindow(
            contentRect: NSRect(x: 0, y: 0, width: 320, height: 240),
            styleMask: [.titled, .closable, .miniaturizable],
            backing: .buffered,
            defer: false
        )
        window.title = "iDOOM"
        window.makeKeyAndOrderFront(nil)
        window.contentView = DoomView(frame: window.contentView!.bounds)
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



