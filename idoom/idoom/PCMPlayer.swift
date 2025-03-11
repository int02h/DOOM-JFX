import AudioToolbox

class PCMPlayer {
    private var audioQueue: AudioQueueRef?
    private var buffer: AudioQueueBufferRef?
    private var audioFormat: AudioStreamBasicDescription
    private var mixBuffer: UnsafePointer<Int16>
    private var bufferSize: UInt32

    init(mixBuffer: UnsafePointer<Int16>, bufferSize: UInt32) {
        let numChannels: UInt32 = 2
        self.mixBuffer = mixBuffer
        self.bufferSize = bufferSize

        // Define the audio format
        self.audioFormat = AudioStreamBasicDescription(
            mSampleRate: 11025,
            mFormatID: kAudioFormatLinearPCM,
            mFormatFlags: kLinearPCMFormatFlagIsSignedInteger | kLinearPCMFormatFlagIsPacked,
            mBytesPerPacket: 2 * numChannels,  // 2 bytes per sample (Int16) * numChannels
            mFramesPerPacket: 1,
            mBytesPerFrame: 2 * numChannels,
            mChannelsPerFrame: numChannels,
            mBitsPerChannel: 16,
            mReserved: 0
        )

        // Initialize the audio queue
        let status = AudioQueueNewOutput(&audioFormat, audioQueueCallback, Unmanaged.passUnretained(self).toOpaque(), nil, nil, 0, &audioQueue)
        guard status == noErr, let queue = audioQueue else {
            fatalError("Failed to create AudioQueue")
        }

        // Allocate a single buffer
        let bufferStatus = AudioQueueAllocateBuffer(queue, bufferSize, &buffer)
        guard bufferStatus == noErr, let buffer = buffer else {
            fatalError("Failed to allocate AudioQueue buffer")
        }
        buffer.pointee.mAudioDataByteSize = bufferSize

        // Start playback
        AudioQueueStart(queue, nil)
    }

    func play() {
        guard let queue = audioQueue, let buffer = buffer else { return }

        memcpy(buffer.pointee.mAudioData, mixBuffer, Int(buffer.pointee.mAudioDataByteSize))

        // Enqueue buffer for playback
        AudioQueueEnqueueBuffer(queue, buffer, 0, nil)
    }

    private let audioQueueCallback: AudioQueueOutputCallback = { userData, queue, buffer in
        let pcmPlayer = Unmanaged<PCMPlayer>.fromOpaque(userData!).takeUnretainedValue()
        pcmPlayer.bufferCallback(buffer)
    }

    private func bufferCallback(_ buffer: AudioQueueBufferRef) {
        guard let queue = audioQueue else { return }
        
        // Silence buffer if no new data was submitted
        memset(buffer.pointee.mAudioData, 0, Int(buffer.pointee.mAudioDataByteSize))
        AudioQueueEnqueueBuffer(queue, buffer, 0, nil)
    }

    deinit {
        if let queue = audioQueue {
            AudioQueueStop(queue, true)
            AudioQueueDispose(queue, true)
        }
    }
}
