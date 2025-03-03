import Cocoa

class DoomView: NSView {
    
    static var instance: DoomView?
    
    var screen: [UInt8] = []
    
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
        
        if (screen.count == 0) { return }

        for x in 0..<Int(bounds.width) {
            for y in 0..<Int(bounds.height) {
                let colorIndex = Int(screen[(200 - 1 - y) * 320 + x])
                context.setFillColor(PALLETTE[colorIndex].cgColor)
                context.fill(CGRect(x: x, y: y, width: 1, height: 1))
            }
        }
    }
}
