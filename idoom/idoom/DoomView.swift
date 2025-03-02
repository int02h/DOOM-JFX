import Cocoa

class DoomView: NSView {
    
    override func draw(_ dirtyRect: NSRect) {
        super.draw(dirtyRect)
        guard let context = NSGraphicsContext.current?.cgContext else { return }

        // Example: Draw a single white pixel at (200, 200)
        context.setFillColor(NSColor.white.cgColor)
        context.fill(CGRect(x: 200, y: 200, width: 10, height: 10))
    }
}
