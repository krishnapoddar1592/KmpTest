//
//  EmotionDetectionViewModelWrapper.swift
//  KaMPKitiOS
//
//  Created by Krishna Poddar on 26/04/25.
//  Copyright © 2025 Touchlab. All rights reserved.
//


// ios/KaMPKitiOS/EmotionDetectionViewModelWrapper.swift
import SwiftUI
import Vision
import CoreML
import shared
// Helper to convert UIImage → CVPixelBuffer
fileprivate extension UIImage {
    func toCVPixelBuffer(width: Int, height: Int) -> CVPixelBuffer? {
        let attrs: [CFString:Any] = [
            kCVPixelBufferCGImageCompatibilityKey: true,
            kCVPixelBufferCGBitmapContextCompatibilityKey: true
        ]
        var pixelBuffer: CVPixelBuffer?
        let status = CVPixelBufferCreate(kCFAllocatorDefault,
                                         width,
                                         height,
                                         kCVPixelFormatType_32ARGB,
                                         attrs as CFDictionary,
                                         &pixelBuffer)
        guard status == kCVReturnSuccess, let buffer = pixelBuffer else {
            return nil
        }
        CVPixelBufferLockBaseAddress(buffer, [])
        let context = CGContext(data: CVPixelBufferGetBaseAddress(buffer),
                                width: width,
                                height: height,
                                bitsPerComponent: 8,
                                bytesPerRow: CVPixelBufferGetBytesPerRow(buffer),
                                space: CGColorSpaceCreateDeviceRGB(),
                                bitmapInfo: CGImageAlphaInfo.noneSkipFirst.rawValue)
        guard let cgImage = self.cgImage else {
            CVPixelBufferUnlockBaseAddress(buffer, [])
            return nil
        }
        context?.draw(cgImage, in: CGRect(x: 0, y: 0,
                                          width: width, height: height))
        CVPixelBufferUnlockBaseAddress(buffer, [])
        return buffer
    }
}

class EmotionDetectionViewModelWrapper: ObservableObject {
    private let viewModel: EmotionDetectionViewModel
    private var emotionModel: MLModel?
    
    enum ViewState {
        case initial
        case loading
        case success(dominantEmotion: String, emotionScores: [String: Float])
        case error(message: String)
    }
    
    @Published var state: ViewState = .initial
    
    init() {
        // Get the view model from Koin
        self.viewModel = KotlinDependencies().getEmotionDetectionViewModel()
        
        // Load the model
        loadModel()
        
        // Observe the Kotlin viewmodel's state
        observeState()
    }
    
//    private func loadModel() {
////        let config = MLModelConfiguration()
////        guard let modelURL = Bundle.main.url(forResource: "EmotionDetection", withExtension: "mlpackage") else {
////            print("Could not find EmotionDetection.mlpackage in bundle")
////            return
////        }
////        
////        do {
////            let model = try MLModel(contentsOf: modelURL, configuration: config)
////            self.emotionModel = model
////        } catch {
////            print("Error loading model: \(error.localizedDescription)")
////        }
//        
//    }
    private func loadModel() {
        let config = MLModelConfiguration()
        print("=== Bundle Resources ===")
        Bundle.main.paths(
            forResourcesOfType: nil,
            inDirectory: nil
        ).forEach { print($0) }
        print("========================")

//        guard let url = Bundle.main.url(
//                forResource: "EmotionDetection",
//                withExtension: "mlmodelc"
//        ) else {
//            fatalError("❌ EmotionDetection.mlmodelc not found in app bundle")
//        }
        guard let url = Bundle.main.url(
                forResource: "emotion_model_2",
                withExtension: "mlmodelc"
        ) else {
            fatalError("❌ EmotionDetection.mlmodelc not found in app bundle")
        }

        do {
            self.emotionModel = try MLModel(contentsOf: url, configuration: config)
//            print("🔍 Model inputs:")
//            for (name, desc) in self.emotionModel?.modelDescription.inputDescriptionsByName {
//                print(" • \(name): type=\(desc.type),",
//                      "multiArrayShape=\(desc.multiArrayConstraint?.shape ?? []),",
//                      "imageConstraint=\(desc.imageConstraint.map({ "\($0.pixelsWide)x\($0.pixelsHigh)" }) ?? "none")")
//            }
        } catch {
            fatalError("🔴 Failed to load compiled model: \(error)")
        }
    }


    
    private func observeState() {
        Task {
            do {
                for await kotlinState in viewModel.detectionState {
                    await updateState(kotlinState: kotlinState)
                }
            } catch {
                print("Error observing state: \(error)")
            }
        }
    }
    
    @MainActor
    private func updateState(kotlinState: EmotionDetectionState) {
        switch kotlinState {
        case is EmotionDetectionState.Initial:
            state = .initial
        case is EmotionDetectionState.Loading:
            state = .loading
        case let successState as EmotionDetectionState.Success:
            let dominantEmotion = successState.dominantEmotion.name
            
            var emotionScores: [String: Float] = [:]
            for (emotion, score) in successState.emotionScores {
                emotionScores[emotion.name] = Float(score)
            }
            
            state = .success(dominantEmotion: dominantEmotion, emotionScores: emotionScores)
        case let errorState as EmotionDetectionState.Error:
            state = .error(message: errorState.message)
        default:
            break
        }
    }
    
    func detectEmotion(imageData: Data, width: Int, height: Int) {
        print("Reached emotion detection")
        print(imageData)
        print(width)
        print(height)
        state = .loading
        
        // Perform emotion detection
        let results: [Float]
        do {
            results = try processImage(imageData)
        } catch {
            // This will tell you *why* you’re getting zeros
            print(error.localizedDescription)
            state = .error(message: error.localizedDescription)
            return
        }
        
        // Create emotion scores map
        let emotionTypes = ["ANGER","JOY","SADNESS"]
        var emotionScores: [String: Float] = [:]
        
        for (index, emotionType) in emotionTypes.enumerated() {
            let score = index < results.count ? results[index] : 0.0
            emotionScores[emotionType] = Float(score)
        }
        
        // Find dominant emotion
        let dominantEmotionPair = emotionScores.max(by: { $0.value < $1.value })
        let dominantEmotion = dominantEmotionPair?.key ?? "NEUTRAL"
        
        print(dominantEmotion)
        
        // Update the Kotlin view model with the results
        // Convert the results to Kotlin enum and map
        let kotlinEmotionScores = createKotlinEmotionMap(from: emotionScores)
        let kotlinDominantEmotion = getKotlinEmotion(from: dominantEmotion)
        
        // Set the state in Swift directly
        DispatchQueue.main.async {
            self.state = .success(dominantEmotion: dominantEmotion, emotionScores: emotionScores)
        }
    }
    
    
    enum ImageProcessingError: LocalizedError {
        case modelNotLoaded
        case invalidImageData
        case pixelBufferCreationFailed
        case featureProviderInitFailed(Error)
        case predictionFailed(Error)
        case outputMissing
        case outputNotMultiArray

        var errorDescription: String? {
            switch self {
            case .modelNotLoaded:                     return "Model wasn’t loaded."
            case .invalidImageData:                   return "Invalid image data."
            case .pixelBufferCreationFailed:          return "Failed to make CVPixelBuffer."
            case .featureProviderInitFailed(let err): return "Failed to create input provider: \(err)"
            case .predictionFailed(let err):          return "Model prediction failed: \(err)"
            case .outputMissing:                      return "No output feature found."
            case .outputNotMultiArray:                return "Output is not a multi-array."
            }
        }
    }
    /// Directly invoke the Core ML model, bypassing Vision.
//    private func processImage(_ imageData: Data,
//                              width: Int,
//                              height: Int) throws -> [Float]
//    {
//        // 1. Model and UIImage sanity
//        guard let model = emotionModel else {
//            throw ImageProcessingError.modelNotLoaded
//        }
//        guard let uiImage = UIImage(data: imageData) else {
//            throw ImageProcessingError.invalidImageData
//        }
//
//        // 2. Convert to CVPixelBuffer of the size your model expects
//        guard let buffer = uiImage.toCVPixelBuffer(width: 100, height: 100) else {
//            throw ImageProcessingError.pixelBufferCreationFailed
//        }
//
//        // 3. Figure out your model’s input & output names
//        //    (usually modelDescription.inputDescriptionsByName.keys.first!)
//        guard let inputName = model.modelDescription.inputDescriptionsByName.keys.first,
//              let outputName = model.modelDescription.outputDescriptionsByName.keys.first
//        else {
//            throw ImageProcessingError.outputMissing
//        }
//
//        // 4. Wrap into a feature provider
//        let inputFeatures = [ inputName : buffer ]
//        let provider: MLFeatureProvider
//        do {
//            provider = try MLDictionaryFeatureProvider(dictionary: inputFeatures)
//        } catch {
//            throw ImageProcessingError.featureProviderInitFailed(error)
//        }
//
//        // 5. Run the model
//        let prediction: MLFeatureProvider
//        do {
//            prediction = try model.prediction(from: provider)
//        } catch {
//            throw ImageProcessingError.predictionFailed(error)
//        }
//
//        // 6. Extract the multi-array result
//        guard let feature = prediction.featureValue(for: outputName) else {
//            throw ImageProcessingError.outputMissing
//        }
//        guard let multiArray = feature.multiArrayValue else {
//            throw ImageProcessingError.outputNotMultiArray
//        }
//
//        // 7. Copy floats out
//        let count = multiArray.count
//        return (0..<count).map { multiArray[$0].floatValue }
//    }
    private func processImage(_ imageData: Data) throws -> [Float] {
        // 1. Model sanity
        guard let model = emotionModel else {
            throw ImageProcessingError.modelNotLoaded
        }
        guard let uiImage = UIImage(data: imageData) else {
            throw ImageProcessingError.invalidImageData
        }

        // 2. Resize image to 100x100
        guard let resizedImage = uiImage.resized(to: CGSize(width: 100, height: 100)) else {
            throw ImageProcessingError.invalidImageData
        }

        // 3. Get pixel data
        guard let pixelData = resizedImage.cgImage?.dataProvider?.data,
              let dataPtr = CFDataGetBytePtr(pixelData) else {
            throw ImageProcessingError.pixelBufferCreationFailed
        }

        let width = 100
        let height = 100

        // 4. Create MLMultiArray (1,100,100,3)
        let shape: [NSNumber] = [1, 100, 100, 3]
        let inputArray = try MLMultiArray(shape: shape, dataType: .float32)

        // 5. Fill MLMultiArray with normalized RGB values
        var index = 0
        for y in 0..<height {
            for x in 0..<width {
                let pixelIndex = (y * width + x) * 4  // RGBA (4 bytes per pixel)
                let r = Float(dataPtr[pixelIndex]) / 255.0
                let g = Float(dataPtr[pixelIndex + 1]) / 255.0
                let b = Float(dataPtr[pixelIndex + 2]) / 255.0

                inputArray[[0, y as NSNumber, x as NSNumber, 0]] = NSNumber(value: r)
                inputArray[[0, y as NSNumber, x as NSNumber, 1]] = NSNumber(value: g)
                inputArray[[0, y as NSNumber, x as NSNumber, 2]] = NSNumber(value: b)

                index += 1
            }
        }

        // 6. Create feature provider
        let provider = try MLDictionaryFeatureProvider(dictionary: ["input": inputArray])

        // 7. Run prediction
        let prediction = try model.prediction(from: provider)

        // 8. Extract output
        guard let outputArray = prediction.featureValue(for: "Identity")?.multiArrayValue else {
            throw ImageProcessingError.outputMissing
        }
        print(outputArray)

        // 9. Convert MLMultiArray to [Float]
        let outputCount = outputArray.count
        return (0..<outputCount).map { outputArray[$0].floatValue }
    }

    

//    private func processImage(_ imageData: Data,
//                              width: Int,
//                              height: Int) throws -> [Float]
//    {
//        guard let model = emotionModel else {
//            throw ImageProcessingError.modelNotLoaded
//        }
//        guard let uiImage = UIImage(data: imageData) else {
//            throw ImageProcessingError.invalidImageData
//        }
//
//        // —————————————————————————————————————————————
//        // 1) Inspect and log the exact input feature name(s)
//        let inputKeys = Array(model.modelDescription.inputDescriptionsByName.keys)
//        print("🔑 CoreML input feature names: \(inputKeys)")
//        guard let inputName = inputKeys.first else {
//            fatalError("Model has no inputs?")
//        }
//        // —————————————————————————————————————————————
//
//        // 2) Build your MLMultiArray exactly as before...
//        //    (resize uiImage into a 100×100 RGB array, fill mlArray)
//
//        let mlArray = try MLMultiArray(shape: [1,100,100,3], dataType: .float32)
//        // … fill mlArray …
//
//        // —————————————————————————————————————————————
//        // 3) Create the provider using the *actual* inputName
//        let provider = try MLDictionaryFeatureProvider(
//            dictionary: [ inputName : mlArray ]
//        )
//        // —————————————————————————————————————————————
//
//        // 4) Run prediction
//        let out = try model.prediction(from: provider)
//
//        // 5) Extract output exactly as before...
//        guard let outputName = model.modelDescription
//                                .outputDescriptionsByName.keys.first,
//              let feat = out.featureValue(for: outputName),
//              let marray = feat.multiArrayValue
//        else {
//            throw ImageProcessingError.outputMissing
//        }
//        return (0..<marray.count).map { marray[$0].floatValue }
//    }

//    private func processImage(_ imageData: Data, width: Int, height: Int) -> [Float] {
//        guard let model = emotionModel,
//              let image = UIImage(data: imageData) else {
//            return [0, 0, 0, 0] // Return zeros if there's an error
//        }
//        
//        var results = [Float](repeating: 0, count: 7)
//        
//        // Create Vision request
//        guard let visionModel = try? VNCoreMLModel(for: model) else {
//            return results
//        }
//        
//        let semaphore = DispatchSemaphore(value: 0)
//        
//        let request = VNCoreMLRequest(model: visionModel) { request, error in
//            guard let observations = request.results as? [VNCoreMLFeatureValueObservation],
//                  let emotionPredictions = observations.first?.featureValue.multiArrayValue else {
//                semaphore.signal()
//                return
//            }
//            
//            // Extract probabilities from the model output
//            for i in 0..<min(7, emotionPredictions.count) {
//                results[i] = emotionPredictions[i].floatValue
//            }
//            
//            semaphore.signal()
//        }
//        
//        // Perform request
//        guard let cgImage = image.cgImage else {
//            return results
//        }
//        
//        let handler = VNImageRequestHandler(cgImage: cgImage, options: [:])
//        try? handler.perform([request])
//        
//        // Wait for the request to complete
//        _ = semaphore.wait(timeout: .now() + 5.0)
//        
//        return results
//    }
//    
    // Helper methods to convert between Swift and Kotlin types
    private func createKotlinEmotionMap(from swiftMap: [String: Float]) -> [Emotion: Float] {
        var kotlinMap: [Emotion: Float] = [:]
        
        for (key, value) in swiftMap {
            if let emotion = getKotlinEmotion(from: key) {
                kotlinMap[emotion] = value
            }
        }
        
        return kotlinMap
    }
    
    private func getKotlinEmotion(from string: String) -> Emotion? {
        switch string.uppercased() {
        case "Joy": return Emotion.joy
        case "ANGER": return Emotion.anger
        case "SADNESS": return Emotion.sadness
//        case "NEUTRAL": return Emotion.neutral
        default: return nil
        }
    }
}
extension UIImage {
    func resized(to size: CGSize) -> UIImage? {
        UIGraphicsBeginImageContextWithOptions(size, false, 0.0)
        defer { UIGraphicsEndImageContext() }
        draw(in: CGRect(origin: .zero, size: size))
        return UIGraphicsGetImageFromCurrentImageContext()
    }
}



//// Add to KotlinDependencies
//extension KotlinDependencies {
//    func getEmotionDetectionViewModel() -> EmotionDetectionViewModel {
//        return getKoin().get(qualifier: EmotionDetectionViewModel.self) as! EmotionDetectionViewModel
//    }
//}
