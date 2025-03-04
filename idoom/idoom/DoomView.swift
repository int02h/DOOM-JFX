import Cocoa

class DoomView: NSView {
    
    static var instance: DoomView?
    
    var totalTime: UInt64 = 0
    var totalCount: UInt64 = 0
    
    var bitmapContext: CGContext?
    var pixelData: UnsafeMutablePointer<UInt8>?
    
    override init(frame frameRect: NSRect) {
        super.init(frame: frameRect)
        DoomView.instance = self
        setupBitmapContext()
    }
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
        DoomView.instance = self
        setupBitmapContext()
    }
    
    private func setupBitmapContext() {
        let width = SCREEN_WIDTH
        let height = SCREEN_HEIGHT
        let bytesPerPixel = 4
        let bytesPerRow = width * bytesPerPixel
        let bitsPerComponent = 8
        let colorSpace = CGColorSpaceCreateDeviceRGB()
        
        // Allocate memory for the pixel buffer
        pixelData = UnsafeMutablePointer<UInt8>.allocate(capacity: width * height * bytesPerPixel)
        pixelData?.initialize(repeating: 0, count: width * height * bytesPerPixel)
        
        // Create a persistent bitmap context
        bitmapContext = CGContext(
            data: pixelData,
            width: width,
            height: height,
            bitsPerComponent: bitsPerComponent,
            bytesPerRow: bytesPerRow,
            space: colorSpace,
            bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue
        )
    }
    
    override func draw(_ dirtyRect: NSRect) {
        let start = DispatchTime.now()
        super.draw(dirtyRect)
        guard let context = NSGraphicsContext.current?.cgContext else { return }
        
        let screen = readScreen()
        if (screen.count == 0) { return }
        
        guard let pixelData, let bitmapContext else { return }
        
        for y in 0..<SCREEN_HEIGHT {
            for x in 0..<SCREEN_WIDTH {
                let colorIndex = Int(screen[y * SCREEN_WIDTH + x])
                let color = PALLETTE[colorIndex].usingColorSpace(.sRGB) ?? .black
                
                var red: CGFloat = 0, green: CGFloat = 0, blue: CGFloat = 0, alpha: CGFloat = 1
                color.getRed(&red, green: &green, blue: &blue, alpha: &alpha)
                
                let pixelIndex = ((y * SCREEN_WIDTH) + x) * 4
                pixelData[pixelIndex]     = UInt8(red * 255)
                pixelData[pixelIndex + 1] = UInt8(green * 255)
                pixelData[pixelIndex + 2] = UInt8(blue * 255)
                pixelData[pixelIndex + 3] = UInt8(alpha * 255)
            }
        }
        
        context.draw(bitmapContext.makeImage()!, in: bounds)
        
        let end = DispatchTime.now()
        let elapsed = end.uptimeNanoseconds - start.uptimeNanoseconds
        totalTime += elapsed
        totalCount += 1
    }
}
