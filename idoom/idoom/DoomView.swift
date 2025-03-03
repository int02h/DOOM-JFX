import Cocoa

class DoomView: NSView {
    
    static var instance: DoomView?
    
    override init(frame frameRect: NSRect) {
        super.init(frame: frameRect)
        DoomView.instance = self
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        DoomView.instance = self
    }
    
    override func draw(_ dirtyRect: NSRect) {
        super.draw(dirtyRect)
        guard let context = NSGraphicsContext.current?.cgContext else { return }
        
        let screen = readScreen()
        if (screen.count == 0) { return }

        for x in 0..<SCREEN_WIDTH {
            for y in 0..<SCREEN_HEIGHT {
                // the image should be flipped vertically
                let colorIndex = Int(screen[(SCREEN_HEIGHT - 1 - y) * SCREEN_WIDTH + x])
                context.setFillColor(PALLETTE[colorIndex].cgColor)
                context.fill(CGRect(x: x * SCREEN_SCALE, y: y * SCREEN_SCALE, width: SCREEN_SCALE, height: SCREEN_SCALE))
            }
        }
    }
}
