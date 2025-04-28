//
//  EmotionDetectorBridge.swift
//  KaMPKitiOS
//
//  Created by Krishna Poddar on 26/04/25.
//  Copyright © 2025 Touchlab. All rights reserved.
//


// ios/KaMPKitiOS/EmotionDetectorBridge.swift
import Foundation
import UIKit
import CoreML
import Vision
import shared

@objc class EmotionDetectorBridge: NSObject {
    private var emotionModel: MLModel?
    
    override init() {
        super.init()
        setupModel()
    }
    
    private func setupModel() {
        // Load your .mlmodel
        let modelURL = Bundle.main.url(forResource: "EmotionDetection", withExtension: "mlmodel")!
        do {
            let compiledModelURL = try MLModel.compileModel(at: modelURL)
            emotionModel = try MLModel(contentsOf: compiledModelURL)
        } catch {
            print("Error loading model: \(error.localizedDescription)")
        }
    }
    
    @objc func detectEmotion(imageData: Data, width: Int, height: Int) -> [Float] {
        guard let model = emotionModel,
              let image = UIImage(data: imageData) else {
            return [0, 0, 0, 0, 0, 0, 0] // Return zeros if there's an error
        }
        
        var results = [Float](repeating: 0, count: 7)
        
        // Create Vision request
        guard let visionModel = try? VNCoreMLModel(for: model) else {
            return results
        }
        
        let request = VNCoreMLRequest(model: visionModel) { request, error in
            guard let observations = request.results as? [VNCoreMLFeatureValueObservation],
                  let emotionPredictions = observations.first?.featureValue.multiArrayValue else {
                return
            }
            
            // Extract probabilities from the model output
            for i in 0..<7 {
                results[i] = emotionPredictions[i].floatValue
            }
        }
        
        // Perform request
        guard let cgImage = image.cgImage else {
            return results
        }
        
        let handler = VNImageRequestHandler(cgImage: cgImage, options: [:])
        try? handler.perform([request])
        
        return results
    }
    
    @objc func closeDetector() {
        // Clean up resources
        emotionModel = nil
    }
}
