////// ios/KaMPKitiOS/EmotionDetectionView.swift
////import SwiftUI
////import AVFoundation
////
////struct EmotionDetectionView: View {
////    @StateObject private var viewModel = EmotionDetectionViewModelWrapper()
////    @Environment(\.presentationMode) var presentationMode
////    
////    var body: some View {
////        ZStack {
////            Color.black.edgesIgnoringSafeArea(.all)
////            
////            CameraView(onImageCaptured: { imageData, width, height in
////                viewModel.detectEmotion(imageData: imageData, width: width, height: height)
////            })
////            
////            VStack {
////                HStack {
////                    Button(action: {
////                        presentationMode.wrappedValue.dismiss()
////                    }) {
////                        Text("Back")
////                            .foregroundColor(.white)
////                            .padding()
////                            .background(Color.blue)
////                            .cornerRadius(8)
////                    }
////                    Spacer()
////                }
////                .padding()
////                
////                Spacer()
////                
////                ZStack {
////                    switch viewModel.state {
////                    case .loading:
////                        ProgressView()
////                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
////                            .scaleEffect(2)
////                    
////                    case .success(let dominantEmotion, let emotionScores):
////                        VStack(alignment: .leading, spacing: 8) {
////                            Text("Detected Emotion: \(dominantEmotion)")
////                                .font(.headline)
////                                .foregroundColor(.white)
////                            
////                            ForEach(Array(emotionScores.keys.sorted()), id: \.self) { emotion in
////                                if let score = emotionScores[emotion] {
////                                    HStack {
////                                        Text(emotion)
////                                            .foregroundColor(.white)
////                                        Spacer()
////                                        Text(String(format: "%.2f", score))
////                                            .foregroundColor(.white)
////                                    }
////                                }
////                            }
////                        }
////                        .padding()
////                        .background(Color.black.opacity(0.6))
////                        .cornerRadius(12)
////                    
////                    case .error(let message):
////                        Text(message)
////                            .foregroundColor(.red)
////                            .padding()
////                            .background(Color.black.opacity(0.6))
////                            .cornerRadius(8)
////                    
////                    default:
////                        EmptyView()
////                    }
////                }
////                .padding()
////            }
////        }
////    }
////}
////
////struct CameraView: UIViewRepresentable {
////    var onImageCaptured: (Data, Int, Int) -> Void
////    
////    func makeUIView(context: Context) -> UIView {
////        let view = UIView(frame: UIScreen.main.bounds)
////        let cameraController = CameraController()
////        cameraController.onImageCaptured = onImageCaptured
////        
////        context.coordinator.cameraController = cameraController
////        cameraController.setupCamera(in: view)
////        
////        return view
////    }
////    
////    func updateUIView(_ uiView: UIView, context: Context) {}
////    
////    func makeCoordinator() -> Coordinator {
////        Coordinator()
////    }
////    
////    class Coordinator {
////        var cameraController: CameraController?
////    }
////}
////
////class CameraController: NSObject, AVCaptureVideoDataOutputSampleBufferDelegate {
////    private var captureSession: AVCaptureSession?
////    private let videoDataOutput = AVCaptureVideoDataOutput()
////    private var previewLayer: AVCaptureVideoPreviewLayer?
////    
////    var onImageCaptured: ((Data, Int, Int) -> Void)?
////    private var lastCaptureTime: Date = Date()
////    private let captureInterval: TimeInterval = 1.0
////    
////    func setupCamera(in view: UIView) {
////        captureSession = AVCaptureSession()
////        captureSession?.sessionPreset = .medium
////        
////        guard let frontCamera = AVCaptureDevice.default(.builtInWideAngleCamera, for: .video, position: .front),
////              let input = try? AVCaptureDeviceInput(device: frontCamera) else {
////            return
////        }
////        
////        if captureSession?.canAddInput(input) == true {
////            captureSession?.addInput(input)
////        }
////        
////        if captureSession?.canAddOutput(videoDataOutput) == true {
////            captureSession?.addOutput(videoDataOutput)
////            videoDataOutput.setSampleBufferDelegate(self, queue: DispatchQueue(label: "videoQueue"))
////        }
////        
////        previewLayer = AVCaptureVideoPreviewLayer(session: captureSession!)
////        previewLayer?.videoGravity = .resizeAspectFill
////        previewLayer?.frame = view.bounds
////        
////        if let previewLayer = previewLayer {
////            view.layer.addSublayer(previewLayer)
////        }
////        
////        DispatchQueue.global(qos: .userInitiated).async { [weak self] in
////            self?.captureSession?.startRunning()
////        }
////    }
////    
////    func captureOutput(_ output: AVCaptureOutput, didOutput sampleBuffer: CMSampleBuffer, from connection: AVCaptureConnection) {
////        let currentTime = Date()
////        if currentTime.timeIntervalSince(lastCaptureTime) < captureInterval {
////            return
////        }
////        
////        lastCaptureTime = currentTime
////        
////        guard let imageBuffer = CMSampleBufferGetImageBuffer(sampleBuffer) else {
////            return
////        }
////        
////        let ciImage = CIImage(cvPixelBuffer: imageBuffer)
////        let context = CIContext()
////        guard let cgImage = context.createCGImage(ciImage, from: ciImage.extent) else {
////            return
////        }
////        
////        let uiImage = UIImage(cgImage: cgImage)
////        if let imageData = uiImage.jpegData(compressionQuality: 0.8) {
////            DispatchQueue.main.async {
////                self.onImageCaptured?(imageData, Int(uiImage.size.width), Int(uiImage.size.height))
////            }
////        }
////    }
////}
//
//import SwiftUI
//import AVFoundation
//
//struct EmotionDetectionView: View {
//    @StateObject private var viewModel = EmotionDetectionViewModelWrapper()
//    @Environment(\.presentationMode) var presentationMode
//    @State private var coordinator: CameraView.Coordinator? = nil
//    
//    var body: some View {
//        
//        ZStack {
//            Color.black.edgesIgnoringSafeArea(.all)
//            #if targetEnvironment(simulator)
//            SimulatorCameraView { imageData, width, height in
//                viewModel.detectEmotion(imageData: imageData, width: width, height: height)
//            }
//            #else
//            CameraView(onImageCaptured: { imageData, width, height in
//                viewModel.detectEmotion(imageData: imageData, width: width, height: height)
//            })
//            #endif
//
//            VStack {
//                HStack {
//                    Button(action: {
//                        presentationMode.wrappedValue.dismiss()
//                    }) {
//                        Text("Back")
//                            .foregroundColor(.white)
//                            .padding()
//                            .background(Color.blue)
//                            .cornerRadius(8)
//                    }
//                    Spacer()
//                }
//                .padding()
//
//                Spacer()
//
//                if case .loading = viewModel.state {
//                    ProgressView()
//                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
//                        .scaleEffect(2)
//                }
//
//                if case .success(let dominantEmotion, let emotionScores) = viewModel.state {
//                    VStack(alignment: .leading, spacing: 8) {
//                        Text("Detected Emotion: \(dominantEmotion)")
//                            .font(.headline)
//                            .foregroundColor(.white)
//
//                        ForEach(Array(emotionScores.keys.sorted()), id: \.self) { emotion in
//                            if let score = emotionScores[emotion] {
//                                HStack {
//                                    Text(emotion)
//                                        .foregroundColor(.white)
//                                    Spacer()
//                                    Text(String(format: "%.2f", score))
//                                        .foregroundColor(.white)
//                                }
//                            }
//                        }
//                    }
//                    .padding()
//                    .background(Color.black.opacity(0.7))
//                    .cornerRadius(12)
//                }
//
//                if case .error(let message) = viewModel.state {
//                    Text(message)
//                        .foregroundColor(.red)
//                        .padding()
//                        .background(Color.black.opacity(0.7))
//                        .cornerRadius(8)
//                }
//
//                Button(action: {
//                    coordinator?.cameraController?.captureImage()
//                }) {
//                    Text("Detect Emotion")
//                        .foregroundColor(.white)
//                        .padding()
//                        .background(Color.green)
//                        .cornerRadius(8)
//                }
//                .padding(.bottom, 30)
//            }
//        }
//    }
//}
//#if targetEnvironment(simulator)
//struct SimulatorCameraView: View {
//    var onImageCaptured: (Data, Int, Int) -> Void
//
//    var body: some View {
//        VStack {
//            Image("image_test")
//                .resizable()
//                .aspectRatio(contentMode: .fit)
//                .padding()
//
//            Button("Detect Emotion") {
//                if let uiImage = UIImage(named: "test_face"),
//                   let data = uiImage.jpegData(compressionQuality: 0.8) {
//                    
//                    onImageCaptured(data, Int(uiImage.size.width), Int(uiImage.size.height))
//                }
//            }
//            .padding()
//            .background(Color.blue)
//            .foregroundColor(.white)
//            .cornerRadius(8)
//        }
//    }
//}
//#endif
//
//struct CameraView: UIViewRepresentable {
//    var onImageCaptured: (Data, Int, Int) -> Void
//    @Binding var coordinator: Coordinator?
//
//    func makeUIView(context: Context) -> UIView {
//        let view = UIView(frame: UIScreen.main.bounds)
//        let cameraController = CameraController()
//        cameraController.onImageCaptured = onImageCaptured
//
//        context.coordinator.cameraController = cameraController
//        cameraController.setupCamera(in: view)
//
//        DispatchQueue.main.async {
//            self.coordinator = context.coordinator
//        }
//
//        return view
//    }
//
//    func updateUIView(_ uiView: UIView, context: Context) {}
//
//    func makeCoordinator() -> Coordinator {
//        Coordinator()
//    }
//
//    class Coordinator {
//        var cameraController: CameraController?
//    }
//}
//
//class CameraController: NSObject, AVCaptureVideoDataOutputSampleBufferDelegate {
//    // Public API
//    var onImageCaptured: ((Data, Int, Int) -> Void)?
//    
//    // Internals
//    private var captureSession: AVCaptureSession?
//    private let videoDataOutput = AVCaptureVideoDataOutput()
//    private let ciContext = CIContext()
//    private var previewLayer: AVCaptureVideoPreviewLayer?
//    private var latestBuffer: CMSampleBuffer?
//
//    /// Call this to start everything. It will ask for permission if needed,
//    /// then configure and start the session, and attach a preview layer.
//    func setupCamera(in view: UIView) {
//        switch AVCaptureDevice.authorizationStatus(for: .video) {
//        case .authorized:
//            // Already authorized
//            configureSession(on: view)
//            
//        case .notDetermined:
//            // Ask for permission
//            AVCaptureDevice.requestAccess(for: .video) { granted in
//                guard granted else { return }
//                DispatchQueue.main.async {
//                    self.configureSession(on: view)
//                }
//            }
//            
//        default:
//            // .denied or .restricted — you may want to show an alert
//            return
//        }
//    }
//
//    /// Break out all session/configuration here so setupCamera stays tidy.
//    private func configureSession(on view: UIView) {
//        // 1) Create & configure session
//        let session = AVCaptureSession()
//        session.sessionPreset = .medium
//        
//        // 2) Add front-camera input
//        guard let camera = AVCaptureDevice.default(.builtInWideAngleCamera,
//                                                   for: .video,
//                                                   position: .front),
//              let input = try? AVCaptureDeviceInput(device: camera),
//              session.canAddInput(input) else {
//            return
//        }
//        session.addInput(input)
//        
//        // 3) Add video output for frame capture
//        videoDataOutput.setSampleBufferDelegate(self,
//                                                queue: DispatchQueue(label: "videoQueue"))
//        guard session.canAddOutput(videoDataOutput) else { return }
//        session.addOutput(videoDataOutput)
//        
//        // 4) Create & attach preview layer
//        let preview = AVCaptureVideoPreviewLayer(session: session)
//        preview.videoGravity = .resizeAspectFill
//        preview.frame = view.bounds
//        view.layer.insertSublayer(preview, at: 0)
//        self.previewLayer = preview
//        
//        // 5) Store session and start running
//        self.captureSession = session
//        DispatchQueue.global(qos: .userInitiated).async {
//            session.startRunning()
//        }
//    }
//
//    // MARK: AVCaptureVideoDataOutputSampleBufferDelegate
//    
//    func captureOutput(_ output: AVCaptureOutput,
//                       didOutput sampleBuffer: CMSampleBuffer,
//                       from connection: AVCaptureConnection) {
//        // keep the latest buffer around for manual capture
//        latestBuffer = sampleBuffer
//    }
//    
//    /// Call this (e.g. from your “Detect Emotion” button) to grab one frame
//    func captureImage() {
//        guard let buffer = latestBuffer,
//              let pixelBuffer = CMSampleBufferGetImageBuffer(buffer) else {
//            return
//        }
//        let ciImage = CIImage(cvPixelBuffer: pixelBuffer)
//        guard let cgImage = ciContext.createCGImage(ciImage, from: ciImage.extent) else {
//            return
//        }
//        
//        let uiImage = UIImage(cgImage: cgImage)
//        guard let data = uiImage.jpegData(compressionQuality: 0.8) else {
//            return
//        }
//        
//        onImageCaptured?(data,
//                         Int(uiImage.size.width),
//                         Int(uiImage.size.height))
//    }
//}
//
import SwiftUI
import AVFoundation

struct EmotionDetectionView: View {
    @StateObject private var viewModel = EmotionDetectionViewModelWrapper()
    @Environment(\.presentationMode) var presentationMode
    
    #if !targetEnvironment(simulator)
    @State private var cameraController: CameraController? = nil
    #endif

    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()

            // MARK: - Camera / Simulator Preview
            #if targetEnvironment(simulator)
            SimulatorCameraView { data, w, h in
                viewModel.detectEmotion(imageData: data, width: w, height: h)
            }
            #else
            CameraPreview(
                cameraController: $cameraController,
                onImageCaptured: { data, w, h in
                    viewModel.detectEmotion(imageData: data, width: w, height: h)
                }
            )
            #endif

            // MARK: - UI Overlay
            VStack {
                // Back button
                HStack {
                    Button("Back") {
                        presentationMode.wrappedValue.dismiss()
                    }
                    .padding()
                    .background(Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(8)

                    Spacer()
                }
                .padding()

                Spacer()

                // Loading indicator
                if case .loading = viewModel.state {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                        .scaleEffect(2)
                }

                // Success state
                if case .success(let dominant, let scores) = viewModel.state {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Detected Emotion: \(dominant)")
                            .font(.headline)
                            .foregroundColor(.white)

                        ForEach(scores.keys.sorted(), id: \.self) { emotion in
                            HStack {
                                Text(emotion).foregroundColor(.white)
                                Spacer()
                                Text(String(format: "%.2f", scores[emotion]!))
                                    .foregroundColor(.white)
                            }
                        }
                    }
                    .padding()
                    .background(Color.black.opacity(0.7))
                    .cornerRadius(12)
                }

                // Error state
                if case .error(let msg) = viewModel.state {
                    Text(msg)
                        .foregroundColor(.red)
                        .padding()
                        .background(Color.black.opacity(0.7))
                        .cornerRadius(8)
                }

                // Detect button (only on real device)
                #if !targetEnvironment(simulator)
                Button("Detect Emotion") {
                    cameraController?.captureImage()
                }
                .padding()
                .background(Color.green)
                .foregroundColor(.white)
                .cornerRadius(8)
                .padding(.bottom, 30)
                #endif
 }
        }
    }
}


// MARK: - Simulator Fallback

#if targetEnvironment(simulator)
struct SimulatorCameraView: View {
    var onImageCaptured: (Data, Int, Int) -> Void

    var body: some View {
        VStack {
            // Make sure this matches your asset name
            Image("test5")
                .resizable()
                .aspectRatio(contentMode: .fit)
                .padding()

            Button("Detect Emotion") {
                guard let uiImage = UIImage(named: "test5"),
                      let data = uiImage.jpegData(compressionQuality: 0.8)
                else { return }

                onImageCaptured(data,
                                Int(uiImage.size.width),
                                Int(uiImage.size.height))
            }
            .padding()
            .background(Color.blue)
            .foregroundColor(.white)
            .cornerRadius(8)
        }
    }
}
#endif
