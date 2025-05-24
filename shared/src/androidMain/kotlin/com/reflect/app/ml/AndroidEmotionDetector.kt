


package com.reflect.app.ml

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Rect
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageProxy
import com.google.firebase.firestore.util.FileUtil
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import kotlinx.coroutines.suspendCancellableCoroutine
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.max

// 1. Updated AndroidEmotionDetector with continuous face detection
class AndroidEmotionDetector(private val context: Context) : EmotionDetector {
    private var interpreter: Interpreter? = null
    private var gpuDelegate: GpuDelegate? = null
    private lateinit var faceDetector: FaceDetector

    // Values based on your model analysis
//    private val INPUT_SIZE1 = 720  // Your model expects 224x224 images
//    private val INPUT_SIZE2 = 480  // Your model expects 224x224 images
//    private val CHANNELS = 3      // RGB input
    private val INPUT_SIZE = 64     // Replace with actual model input size (likely 48x48)
    private val CHANNELS = 1        // Grayscale input, not RGB
    private val EMOTION_COUNT = 7
    //    private val EMOTION_COUNT = 3 // 3 output emotions
//    private val EMOTION_COUNT = 4 // 4 output emotions

    init {
        try {
            // Initialize face detector with high accuracy options
            val options = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL) // Crucial
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                .setMinFaceSize(0.1f) // Example: smallest face is 10% of image width

                .build()

            faceDetector = FaceDetection.getClient(options)

            setupModel()
        } catch (e: Exception) {
            Log.e("EmotionDetector", "Error initializing TFLite model or Face Detector", e)
        }
    }
    @SuppressLint("UnsafeOptInUsageError")
    override suspend fun detectFaceInImageProxy(imageProxyInput: Any): Boolean = suspendCancellableCoroutine { continuation ->
        val imageProxy = imageProxyInput as? ImageProxy ?: run { /* ... handle error & close ... */ return@suspendCancellableCoroutine }
        continuation.invokeOnCancellation { imageProxy.close() }
        val mediaImage = imageProxy.image ?: run { /* ... handle error & close ... */
            Log.e("EmotionDetector", "Invalid ImageProxy type passed to detectFaceInImageProxy.")
            if (continuation.isActive) continuation.resume(false)
            return@suspendCancellableCoroutine}

        try {
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)

            faceDetector.process(inputImage)
                .addOnSuccessListener { faces ->
                    var fullFaceDetected = false
                    if (faces.isNotEmpty()) {
                        for (face in faces) {
                            // --- Apply your checks here ---
                            val hasKeyLandmarks = checkKeyLandmarks(face)
                            val isFrontalEnough = checkHeadPose(face)
                            val isSufficientlyVisible = checkVisibility(face, imageProxy.width, imageProxy.height)
                            // val isLargeEnough = checkSize(face, imageProxy.width, imageProxy.height) // Could be redundant if setMinFaceSize is used

                            if (hasKeyLandmarks && isFrontalEnough && isSufficientlyVisible) {
                                fullFaceDetected = true
                                break // Found one full face, no need to check others
                            }
                        }
                    }
                    if (continuation.isActive) continuation.resume(fullFaceDetected)
                }
                .addOnFailureListener { e ->
                    Log.e("EmotionDetector", "Face detection (ImageProxy) failed", e)
                    if (continuation.isActive) {
                        continuation.resume(false)
                    }
                }
                .addOnCompleteListener {
                    if (!continuation.isCancelled) imageProxy.close()
                }
        } catch (e: Exception) {
            Log.e("EmotionDetector", "Error processing ImageProxy for face detection", e)
            if (continuation.isActive) {
                continuation.resume(false)
            }
            // If an exception occurred here, and it's not cancelled, close the proxy.
            if (!continuation.isCancelled) {
                imageProxy.close()
            }
        }
    }
    private fun checkKeyLandmarks(face: com.google.mlkit.vision.face.Face): Boolean {
        val leftEye = face.getLandmark(FaceLandmark.LEFT_EYE)
        val rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)
        val noseBase = face.getLandmark(FaceLandmark.NOSE_BASE)
        val mouthLeft = face.getLandmark(FaceLandmark.MOUTH_LEFT)
        val mouthRight = face.getLandmark(FaceLandmark.MOUTH_RIGHT)
        return leftEye != null && rightEye != null && noseBase != null && mouthLeft != null && mouthRight != null
    }

    private fun checkHeadPose(face: com.google.mlkit.vision.face.Face): Boolean {
        val MAX_YAW_DEGREES = 25f // Tune this
        val MAX_PITCH_DEGREES = 20f // Tune this
        return Math.abs(face.headEulerAngleY) <= MAX_YAW_DEGREES &&
                Math.abs(face.headEulerAngleX) <= MAX_PITCH_DEGREES
    }

    private fun checkVisibility(face: com.google.mlkit.vision.face.Face, imageWidth: Int, imageHeight: Int): Boolean {
        val faceBoundingBox = face.boundingBox
        if (imageWidth == 0 || imageHeight == 0) return false // Avoid division by zero if image dimensions are not ready

        // Check if bounding box is mostly within image (simplified check)
        val isMostlyInside = faceBoundingBox.left >= 0 &&
                faceBoundingBox.top >= 0 &&
                faceBoundingBox.right <= imageWidth &&
                faceBoundingBox.bottom <= imageHeight

        // More precise check: ensure certain percentage of face area is visible
        val imageRect = android.graphics.Rect(0, 0, imageWidth, imageHeight)
        val visiblePortion = android.graphics.Rect(faceBoundingBox)
        if (visiblePortion.intersect(imageRect)) {
            val visibleArea = visiblePortion.width() * visiblePortion.height()
            val totalFaceArea = faceBoundingBox.width() * faceBoundingBox.height()
            if (totalFaceArea > 0) {
                return (visibleArea.toFloat() / totalFaceArea) >= 0.80f // e.g., 80% of the detected face bbox must be visible
            }
        }
        return false
    }





    // ADD: New method for continuous face detection without emotion analysis
    override suspend fun detectFace(imageData: ByteArray, width: Int, height: Int): Boolean = suspendCancellableCoroutine { continuation ->
        Log.d("EmotionDetector", "Starting face detection - width: $width, height: $height, data size: ${imageData.size}")

        try {
            // Convert ByteArray to Bitmap
            val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                ?: run {
                    Log.e("EmotionDetector", "Failed to decode image data - bitmap is null")
                    continuation.resume(false)
                    return@suspendCancellableCoroutine
                }

            Log.d("EmotionDetector", "Bitmap created successfully - ${bitmap.width}x${bitmap.height}")

            // Check if bitmap meets ML Kit's minimum requirements
            if (bitmap.width < 32 || bitmap.height < 32) {
                Log.e("EmotionDetector", "Bitmap too small for face detection: ${bitmap.width}x${bitmap.height}. ML Kit requires at least 32x32")
                bitmap.recycle()
                continuation.resume(false)
                return@suspendCancellableCoroutine
            }

            // Create InputImage from bitmap
            val image = InputImage.fromBitmap(bitmap, 0)

            faceDetector.process(image)
                .addOnSuccessListener { faces ->
                    Log.d("EmotionDetector", "Face detection completed - found ${faces.size} faces")
                    bitmap.recycle() // Clean up
                    continuation.resume(faces.isNotEmpty())
                }
                .addOnFailureListener { e ->
                    Log.e("EmotionDetector", "Face detection failed", e)
                    bitmap.recycle() // Clean up
                    continuation.resume(false)
                }
        } catch (e: Exception) {
            Log.e("EmotionDetector", "Error in detectFaceInFrame", e)
            continuation.resume(false)
        }
    }

    // Rest of the existing methods remain the same...
    private fun setupModel() {
        try {
//            val fileDescriptor = context.assets.openFd("best_float_final.tflite")
//            val fileDescriptor = context.assets.openFd("model.tflite")
            val fileDescriptor = context.assets.openFd("emotionModel.tflite")
            val inputStream = fileDescriptor.createInputStream()
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            val model = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

            val options = Interpreter.Options()

            // Use GPU if available
            if (CompatibilityList().isDelegateSupportedOnThisDevice) {
                gpuDelegate = GpuDelegate()
                options.addDelegate(gpuDelegate)
            }

            interpreter = Interpreter(model, options)
            inspectModelInputShape()

            Log.d("EmotionDetector", "Model loaded successfully")
        } catch (e: Exception) {
            Log.e("EmotionDetector", "Failed to load model", e)
            interpreter = null
        }
    }


    suspend fun detectFace(bitmap: Bitmap): Rect? = suspendCancellableCoroutine { continuation ->
        val image = InputImage.fromBitmap(bitmap, 0)

        faceDetector.process(image)
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    // Get the first detected face
                    val faceRect = faces[0].boundingBox
                    continuation.resume(faceRect)
                } else {
                    // No face detected
                    Toast.makeText(this.context, "No face detected in the image",Toast.LENGTH_LONG).show()
                    continuation.resume(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e("EmotionDetector", "Face detection failed", e)
                continuation.resumeWithException(e)
            }
    }

    // Helper function to crop face with padding
    private fun cropFaceBitmap(bitmap: Bitmap, faceRect: Rect): Bitmap {
        // Add some padding around the face (20% on each side)
        val padding = (max(faceRect.width(), faceRect.height()) * 0.2).toInt()

        // Create safe bounds that won't exceed the original image dimensions
        val left = maxOf(0, faceRect.left - padding)
        val top = maxOf(0, faceRect.top - padding)
        val right = minOf(bitmap.width, faceRect.right + padding)
        val bottom = minOf(bitmap.height, faceRect.bottom + padding)

        // Create a new rectangle with the safe bounds
        val safeRect = Rect(left, top, right, bottom)

        // Crop the bitmap to the face area
        return Bitmap.createBitmap(
            bitmap,
            safeRect.left,
            safeRect.top,
            safeRect.width(),
            safeRect.height()
        )
    }
//    override suspend fun detectEmotion(imageData: ByteArray, width: Int, height: Int): Map<Emotion, Float> {
//
//        if (interpreter == null) {
//            Log.d("EmotionDetector", "TFLite model not available, using mock data")
//            return mockEmotionData()
//        }
//
//        try {
//            // Decode the image
//            val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
//
//            // Step 1: Detect face
//            val faceRect = detectFace(bitmap)
//
//            // Process the image
//            val processedBitmap = if (faceRect != null) {
//                // Step 2: Crop to face area
//                val faceBitmap = cropFaceBitmap(bitmap, faceRect)
//                // Step 3: Resize to INPUT_SIZE
//                Bitmap.createScaledBitmap(faceBitmap, INPUT_SIZE1, INPUT_SIZE2, true)
//            } else {
//                // No face detected, use the whole image
//                Log.d("EmotionDetector", "No face detected, using full image")
//                Bitmap.createScaledBitmap(bitmap, INPUT_SIZE1, INPUT_SIZE2, true)
//            }
//
//            // Get pixel data from the processed bitmap
//            val pixels = IntArray(INPUT_SIZE1 * INPUT_SIZE2)
//            processedBitmap.getPixels(pixels, 0, INPUT_SIZE1, 0, 0, INPUT_SIZE1, INPUT_SIZE2)
//
//            // Prepare input (NHWC format - batch, height, width, channels)
//            val inputBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE1 * INPUT_SIZE2 * CHANNELS)
//            inputBuffer.order(ByteOrder.nativeOrder())
//
//            // Process each pixel - normalize to [0,1]
//            for (y in 0 until INPUT_SIZE1) {
//                for (x in 0 until INPUT_SIZE2) {
//                    val pixel = pixels[y * INPUT_SIZE2 + x]
//
//                    // Extract RGB values and normalize to [0,1]
//                    val r = (pixel shr 16 and 0xFF) / 255.0f
//                    val g = (pixel shr 8 and 0xFF) / 255.0f
//                    val b = (pixel and 0xFF) / 255.0f
//
//                    // Add normalized RGB values to buffer
//                    inputBuffer.putFloat(r)
//                    inputBuffer.putFloat(g)
//                    inputBuffer.putFloat(b)
//                }
//            }
//
//            // Reset position
//            inputBuffer.flip()
//
//            // Prepare output buffer
//            val outputBuffer = Array(1) { FloatArray(EMOTION_COUNT) }
//
//            // Run inference
//            interpreter?.run(inputBuffer, outputBuffer)
//
//            // Map results to emotions
//            println("anger"+outputBuffer[0][0])
//            println("joy"+outputBuffer[0][1])
//            println("neutral"+outputBuffer[0][2])
//            println("sadness"+outputBuffer[0][3])
//            return mapOf(
//                Emotion.ANGER to outputBuffer[0][0],
//                Emotion.JOY to outputBuffer[0][1],
//                Emotion.NEUTRAL to outputBuffer[0][2],
//                Emotion.SADNESS to outputBuffer[0][3]
//            )
//
//        } catch (e: Exception) {
//            Log.e("EmotionDetector", "Error during emotion detection", e)
//            e.printStackTrace()
//            return mockEmotionData()
//        }
//    }
    private fun inspectModelInputShape() {
        interpreter?.let { interp ->
            val inputTensor = interp.getInputTensor(0)
            val inputShape = inputTensor.shape()
            Log.d("EmotionDetector", "Model input shape: ${inputShape.contentToString()}")
            Log.d("EmotionDetector", "Model input type: ${inputTensor.dataType()}")

            // Update INPUT_SIZE based on the actual model input shape
            // inputShape typically looks like [1, height, width, channels]
            // So INPUT_SIZE should be inputShape[1] (assuming square input)
        }
    }
    private fun toGrayscale(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val grayscaleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(grayscaleBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f) // Convert to grayscale
        val filter = ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = filter
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return grayscaleBitmap
    }


    override suspend fun detectEmotion(imageData: ByteArray, width: Int, height: Int): Map<Emotion, Float> {
        if (interpreter == null) {
            Log.d("EmotionDetector", "TFLite model not available, using mock data")
            return mockEmotionData()
        }

        try {
            // Allocate tensors (equivalent to interpreter.allocate_tensors() in Python)
            interpreter!!.allocateTensors()

            // Decode the image
            val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)

            // Step 1: Detect face
            val faceRect = detectFace(bitmap)

            // Process the image
            val processedBitmap = if (faceRect != null) {
                // Step 2: Crop to face area
                val faceBitmap = cropFaceBitmap(bitmap, faceRect)
                // Step 3: Resize to model input size
                Bitmap.createScaledBitmap(faceBitmap, INPUT_SIZE, INPUT_SIZE, true)
            } else {
                // No face detected, use the whole image
                Log.d("EmotionDetector", "No face detected, using full image")
                Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
            }

            // Convert to grayscale (like in Python version)
            val grayscaleBitmap = toGrayscale(processedBitmap)

            // Get pixel data from grayscale bitmap
            val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
            grayscaleBitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

            // Prepare input buffer (NHWC format for single channel grayscale)
            val inputBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * CHANNELS)
            inputBuffer.order(ByteOrder.nativeOrder())

            // Process each pixel with Python-matching preprocessing
            for (y in 0 until INPUT_SIZE) {
                for (x in 0 until INPUT_SIZE) {
                    val pixel = pixels[y * INPUT_SIZE + x]

                    // Extract grayscale value (since it's already grayscale, all RGB channels are the same)
                    val gray = (pixel and 0xFF)

                    // Normalize to [0,1] like in Python
                    var normalizedValue = gray / 255.0f

                    // Center around 0 like in Python: (value - 0.5) * 2.0
                    normalizedValue = (normalizedValue - 0.5f) * 2.0f

                    // Add to buffer
                    inputBuffer.putFloat(normalizedValue)
                }
            }

            // Properly reset the buffer position
            inputBuffer.rewind()

            // Prepare output buffer (create fresh each time)
            val outputBuffer = Array(1) { FloatArray(EMOTION_COUNT) }

            // Clear any existing outputs (important for avoiding cached results)
            outputBuffer[0].fill(0.0f)

            // Run inference using the correct Android TensorFlow Lite API
            interpreter!!.run(inputBuffer, outputBuffer)

            // The output is now in outputBuffer

            val modelOutputs = outputBuffer[0]

            Log.d("EmotionDetector", "Raw model outputs:")
            Log.d("EmotionDetector", "Index 0 (Angry): ${modelOutputs[0]}")
            Log.d("EmotionDetector", "Index 1 (Disgust→Angry): ${modelOutputs[1]}")
            Log.d("EmotionDetector", "Index 2 (Fear→Sad): ${modelOutputs[2]}")
            Log.d("EmotionDetector", "Index 3 (Happy): ${modelOutputs[3]}")
            Log.d("EmotionDetector", "Index 4 (Sad): ${modelOutputs[4]}")
            Log.d("EmotionDetector", "Index 5 (Surprise→Happy): ${modelOutputs[5]}")
            Log.d("EmotionDetector", "Index 6 (Neutral): ${modelOutputs[6]}")

            // Apply emotion mapping according to emotion_map_4
            // Angry: max of indices 0 (Angry) and 1 (Disgust)
            val angerValue = maxOf(modelOutputs[0], modelOutputs[1])

            // Sad: max of indices 2 (Fear) and 4 (Sad)
            val sadValue = maxOf(modelOutputs[2], modelOutputs[4])

            // Happy: max of indices 3 (Happy) and 5 (Surprise)
            val happyValue = maxOf(modelOutputs[3], modelOutputs[5])

            // Neutral: index 6 (Neutral)
            val neutralValue = modelOutputs[6]

            Log.d("EmotionDetector", "Final mapped emotions:")
            Log.d("EmotionDetector", "Anger: $angerValue")
            Log.d("EmotionDetector", "Happy: $happyValue")
            Log.d("EmotionDetector", "Neutral: $neutralValue")
            Log.d("EmotionDetector", "Sad: $sadValue")

            return mapOf(
                Emotion.ANGER to angerValue,
                Emotion.JOY to happyValue,
                Emotion.NEUTRAL to neutralValue,
                Emotion.SADNESS to sadValue
            )

        } catch (e: Exception) {
            Log.e("EmotionDetector", "Error during emotion detection", e)
            e.printStackTrace()
            return mockEmotionData()
        }
    }    private fun mockEmotionData(): Map<Emotion, Float> {
        return mapOf(
            Emotion.JOY to 0.7f,
            Emotion.SADNESS to 0.1f,
            Emotion.ANGER to 0.025f,
            Emotion.NEUTRAL to 0.025f,
        )
    }

    override fun close() {
        interpreter?.close()
        gpuDelegate?.close()
        // Check if faceDetector is lateinit and initialized before closing
        if (this::faceDetector.isInitialized) {
            faceDetector.close()
        }
        interpreter = null
        gpuDelegate = null
        Log.d("EmotionDetector", "EmotionDetector resources released.")
    }
}
//package com.reflect.app.ml
//
//import android.content.Context
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import android.graphics.Canvas
//import android.graphics.ColorMatrix
//import android.graphics.ColorMatrixColorFilter
//import android.graphics.Paint
//import android.graphics.Rect
//import android.util.Log
//import androidx.annotation.OptIn
//import androidx.camera.core.ExperimentalGetImage
//import androidx.camera.core.ImageProxy
//import com.google.mlkit.vision.common.InputImage
//import com.google.mlkit.vision.face.FaceDetection
//import com.google.mlkit.vision.face.FaceDetector
//import com.google.mlkit.vision.face.FaceDetectorOptions
//import kotlinx.coroutines.suspendCancellableCoroutine
//import org.tensorflow.lite.Interpreter
//import org.tensorflow.lite.gpu.CompatibilityList
//import org.tensorflow.lite.gpu.GpuDelegate
//import java.nio.ByteBuffer
//import java.nio.ByteOrder
//import java.nio.channels.FileChannel
//import kotlin.coroutines.resume
//import kotlin.coroutines.resumeWithException
//
//class AndroidEmotionDetector(private val context: Context) : EmotionDetector {
//    private var interpreter: Interpreter? = null
//    private var gpuDelegate: GpuDelegate? = null
//    private lateinit var faceDetector: FaceDetector
//
//    // Model input parameters
//    private var targetSize = 64  // Will be updated from model
//    private val CHANNELS = 1     // Grayscale input
//    private val EMOTION_COUNT = 7
//
//    // Emotion mappings
//    private val emotions = mapOf(
//        0 to Pair(Emotion.ANGER, "Angry"),
//        1 to Pair(Emotion.ANGER, "Angry"),  // Disgust mapped to Angry
//        2 to Pair(Emotion.SADNESS, "Sad"),  // Fear mapped to Sad
//        3 to Pair(Emotion.JOY, "Happy"),
//        4 to Pair(Emotion.SADNESS, "Sad"),
//        5 to Pair(Emotion.JOY, "Happy"),    // Surprise mapped to Happy
//        6 to Pair(Emotion.NEUTRAL, "Neutral")
//    )
//
//    init {
//        try {
//            // Setup face detector
//            val options = FaceDetectorOptions.Builder()
//                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
//                .setMinFaceSize(0.1f)
//                .build()
//
//            faceDetector = FaceDetection.getClient(options)
//
//            // Load TFLite model
//            setupModel()
//        } catch (e: Exception) {
//            Log.e("EmotionDetector", "Error initializing", e)
//        }
//    }
//
//    private fun setupModel() {
//        try {
//            val fileDescriptor = context.assets.openFd("emotionModel.tflite")
//            val inputStream = fileDescriptor.createInputStream()
//            val fileChannel = inputStream.channel
//            val startOffset = fileDescriptor.startOffset
//            val declaredLength = fileDescriptor.declaredLength
//            val model = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
//
//            val options = Interpreter.Options()
//
//            // Use GPU if available
//            if (CompatibilityList().isDelegateSupportedOnThisDevice) {
//                gpuDelegate = GpuDelegate()
//                options.addDelegate(gpuDelegate)
//            }
//
//            interpreter = Interpreter(model, options)
//
//            // Get the actual input shape from the model
//            getModelInputDetails()
//
//            Log.d("EmotionDetector", "Model loaded successfully. Target size: $targetSize")
//        } catch (e: Exception) {
//            Log.e("EmotionDetector", "Failed to load model", e)
//            interpreter = null
//        }
//    }
//
//    private fun getModelInputDetails() {
//        interpreter?.let { interp ->
//            val inputTensor = interp.getInputTensor(0)
//            val inputShape = inputTensor.shape()
//
//            // Update target size from model (assuming square input)
//            if (inputShape.size >= 2) {
//                targetSize = inputShape[1] // Height
//                Log.d("EmotionDetector", "Model input shape: ${inputShape.contentToString()}")
//            }
//        }
//    }
//
//    @OptIn(ExperimentalGetImage::class)
//    override suspend fun detectFaceInImageProxy(imageProxyInput: Any): Boolean = suspendCancellableCoroutine { continuation ->
//        val imageProxy = imageProxyInput as? ImageProxy ?: run {
//            Log.e("EmotionDetector", "Invalid ImageProxy type")
//            continuation.resume(false)
//            return@suspendCancellableCoroutine
//        }
//
//        continuation.invokeOnCancellation { imageProxy.close() }
//
//        val mediaImage = imageProxy.image ?: run {
//            Log.e("EmotionDetector", "Null image in ImageProxy")
//            continuation.resume(false)
//            imageProxy.close()
//            return@suspendCancellableCoroutine
//        }
//
//        try {
//            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
//
//            faceDetector.process(inputImage)
//                .addOnSuccessListener { faces ->
//                    if (continuation.isActive) continuation.resume(faces.isNotEmpty())
//                }
//                .addOnFailureListener { e ->
//                    Log.e("EmotionDetector", "Face detection failed", e)
//                    if (continuation.isActive) continuation.resume(false)
//                }
//                .addOnCompleteListener {
//                    if (!continuation.isCancelled) imageProxy.close()
//                }
//        } catch (e: Exception) {
//            Log.e("EmotionDetector", "Error processing ImageProxy", e)
//            if (continuation.isActive) continuation.resume(false)
//            if (!continuation.isCancelled) imageProxy.close()
//        }
//    }
//
//    override suspend fun detectFace(imageData: ByteArray, width: Int, height: Int): Boolean = suspendCancellableCoroutine { continuation ->
//        try {
//            val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
//                ?: run {
//                    Log.e("EmotionDetector", "Failed to decode image data")
//                    continuation.resume(false)
//                    return@suspendCancellableCoroutine
//                }
//
//            // Create InputImage from bitmap
//            val image = InputImage.fromBitmap(bitmap, 0)
//
//            faceDetector.process(image)
//                .addOnSuccessListener { faces ->
//                    bitmap.recycle()
//                    continuation.resume(faces.isNotEmpty())
//                }
//                .addOnFailureListener { e ->
//                    Log.e("EmotionDetector", "Face detection failed", e)
//                    bitmap.recycle()
//                    continuation.resume(false)
//                }
//        } catch (e: Exception) {
//            Log.e("EmotionDetector", "Error in detectFace", e)
//            continuation.resume(false)
//        }
//    }
//
//    private fun toGrayscale(bitmap: Bitmap): Bitmap {
//        val width = bitmap.width
//        val height = bitmap.height
//        val grayscaleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//
//        val canvas = Canvas(grayscaleBitmap)
//        val paint = Paint()
//        val colorMatrix = ColorMatrix()
//        colorMatrix.setSaturation(0f) // Convert to grayscale
//        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
//        canvas.drawBitmap(bitmap, 0f, 0f, paint)
//
//        return grayscaleBitmap
//    }
//
//    override suspend fun detectEmotion(imageData: ByteArray, width: Int, height: Int): Map<Emotion, Float> {
//        if (interpreter == null) {
//            Log.d("EmotionDetector", "TFLite model not available, using mock data")
//            return mockEmotionData()
//        }
//
//        try {
//            // Decode the image
//            val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
//                ?: return mockEmotionData()
//
//            // Convert to grayscale first (like in Python)
//            val grayBitmap = toGrayscale(bitmap)
//
//            // Detect faces
//            val faces = detectFaces(grayBitmap)
//
//            if (faces.isEmpty()) {
//                Log.d("EmotionDetector", "No faces detected")
//                bitmap.recycle()
//                grayBitmap.recycle()
//                return mockEmotionData()
//            }
//
//            // Process the first face
//            val (x, y, w, h) = faces[0]
//
//            // Crop the face region
//            val faceBitmap = Bitmap.createBitmap(grayBitmap, x, y, w, h)
//
//            // Resize to target size
//            val resizedFace = Bitmap.createScaledBitmap(faceBitmap, targetSize, targetSize, true)
//            faceBitmap.recycle()
//
//            // Prepare input buffer
//            val inputBuffer = prepareInputBuffer(resizedFace)
//            resizedFace.recycle()
//
//            // Run inference
//            val outputBuffer = Array(1) { FloatArray(EMOTION_COUNT) }
//            interpreter!!.run(inputBuffer, outputBuffer)
//
//            // Map results
//            val predictions = outputBuffer[0]
//
//            Log.d("EmotionDetector", "Raw predictions: ${predictions.contentToString()}")
//
//            // Map to final emotions
//            val angerValue = maxOf(predictions[0], predictions[1])
//            val sadValue = maxOf(predictions[2], predictions[4])
//            val happyValue = maxOf(predictions[3], predictions[5])
//            val neutralValue = predictions[6]
//
//            bitmap.recycle()
//            grayBitmap.recycle()
//
//            return mapOf(
//                Emotion.ANGER to angerValue,
//                Emotion.JOY to happyValue,
//                Emotion.NEUTRAL to neutralValue,
//                Emotion.SADNESS to sadValue
//            )
//        } catch (e: Exception) {
//            Log.e("EmotionDetector", "Error during emotion detection", e)
//            return mockEmotionData()
//        }
//    }
//
//    private fun prepareInputBuffer(bitmap: Bitmap): ByteBuffer {
//        val inputBuffer = ByteBuffer.allocateDirect(4 * targetSize * targetSize * CHANNELS)
//        inputBuffer.order(ByteOrder.nativeOrder())
//
//        // Get pixel data
//        val pixels = IntArray(targetSize * targetSize)
//        bitmap.getPixels(pixels, 0, targetSize, 0, 0, targetSize, targetSize)
//
//        // Process pixels following the Python method
//        for (pixel in pixels) {
//            val gray = pixel and 0xFF
//
//            // Normalize to [0,1] then scale to [-1,1]
//            val normalizedValue = (gray / 255.0f - 0.5f) * 2.0f
//
//            inputBuffer.putFloat(normalizedValue)
//        }
//
//        inputBuffer.rewind()
//        return inputBuffer
//    }
//
//    private suspend fun detectFaces(bitmap: Bitmap): List<IntArray> = suspendCancellableCoroutine { continuation ->
//        try {
//            val image = InputImage.fromBitmap(bitmap, 0)
//
//            faceDetector.process(image)
//                .addOnSuccessListener { faces ->
//                    val faceRects = faces.map { face ->
//                        val box = face.boundingBox
//                        intArrayOf(box.left, box.top, box.width(), box.height())
//                    }
//                    continuation.resume(faceRects)
//                }
//                .addOnFailureListener { e ->
//                    Log.e("EmotionDetector", "Face detection failed", e)
//                    continuation.resume(emptyList())
//                }
//        } catch (e: Exception) {
//            Log.e("EmotionDetector", "Error detecting faces", e)
//            continuation.resume(emptyList())
//        }
//    }
//
//    private fun mockEmotionData(): Map<Emotion, Float> {
//        return mapOf(
//            Emotion.JOY to 0.7f,
//            Emotion.SADNESS to 0.1f,
//            Emotion.ANGER to 0.025f,
//            Emotion.NEUTRAL to 0.025f,
//        )
//    }
//
//    override fun close() {
//        interpreter?.close()
//        gpuDelegate?.close()
//        if (this::faceDetector.isInitialized) {
//            faceDetector.close()
//        }
//        interpreter = null
//        gpuDelegate = null
//        Log.d("EmotionDetector", "EmotionDetector resources released.")
//    }
//}