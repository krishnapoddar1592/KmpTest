


package com.reflect.app.ml

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
    private val INPUT_SIZE = 224  // Your model expects 224x224 images
    private val CHANNELS = 3      // RGB input
    private val EMOTION_COUNT = 3 // 3 output emotions

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

//    @SuppressLint("UnsafeOptInUsageError") // For imageProxy.image
//    override suspend fun detectFaceInImageProxy(imageProxyInput: Any): Boolean = suspendCancellableCoroutine { continuation ->
//        val imageProxy = imageProxyInput as? ImageProxy
//            ?: run {
//                Log.e("EmotionDetector", "Invalid ImageProxy type passed to detectFaceInImageProxy.")
//                if (continuation.isActive) continuation.resume(false)
//                return@suspendCancellableCoroutine
//            }
//
//        // Primary mechanism for closing if the coroutine is cancelled.
//        continuation.invokeOnCancellation {
//            // Log.d("EmotionDetector", "ImageProxy analysis cancelled, closing proxy.")
//            imageProxy.close()
//        }
//
//        val mediaImage = imageProxy.image
//        if (mediaImage == null) {
//            Log.e("EmotionDetector", "MediaImage is null from ImageProxy.")
//            if (continuation.isActive) continuation.resume(false)
//            // If not cancelled and we are resuming, ensure it's closed as we are done with it.
//            if (!continuation.isCancelled) {
//                imageProxy.close()
//            }
//            return@suspendCancellableCoroutine
//        }
//
//        try {
//            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
//            val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)
//
//            faceDetector.process(inputImage)
//                .addOnSuccessListener { faces ->
//
//                    if (continuation.isActive) {
//                        // Log.d("EmotionDetector", "Face detection (ImageProxy) success: ${faces.size} faces")
//                        continuation.resume(faces.isNotEmpty())
//                    }
//                }
//                .addOnFailureListener { e ->
//                    Log.e("EmotionDetector", "Face detection (ImageProxy) failed", e)
//                    if (continuation.isActive) {
//                        continuation.resume(false)
//                    }
//                }
//                .addOnCompleteListener {
//                    // This ensures the proxy is closed once the ML Kit task is truly complete,
//                    // if the coroutine itself hasn't been cancelled.
//                    if (!continuation.isCancelled) {
//                        // Log.d("EmotionDetector", "ML Kit task complete, closing proxy.")
//                        imageProxy.close()
//                    }
//                }
//        } catch (e: Exception) { // Catches exceptions from fromMediaImage or synchronous part of process()
//            Log.e("EmotionDetector", "Error processing ImageProxy for face detection", e)
//            if (continuation.isActive) {
//                continuation.resume(false)
//            }
//            // If an exception occurred here, and it's not cancelled, close the proxy.
//            if (!continuation.isCancelled) {
//                imageProxy.close()
//            }
//        }
//    }



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
            val fileDescriptor = context.assets.openFd("best_float_final.tflite")
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

    override suspend fun detectEmotion(imageData: ByteArray, width: Int, height: Int): Map<Emotion, Float> {
        if (interpreter == null) {
            Log.d("EmotionDetector", "TFLite model not available, using mock data")
            return mockEmotionData()
        }

        try {
            // Decode the image
            val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)

            // Step 1: Detect face
            val faceRect = detectFace(bitmap)

            // Process the image
            val processedBitmap = if (faceRect != null) {
                // Step 2: Crop to face area
                val faceBitmap = cropFaceBitmap(bitmap, faceRect)
                // Step 3: Resize to INPUT_SIZE
                Bitmap.createScaledBitmap(faceBitmap, INPUT_SIZE, INPUT_SIZE, true)
            } else {
                // No face detected, use the whole image
                Log.d("EmotionDetector", "No face detected, using full image")
                Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
            }

            // Get pixel data from the processed bitmap
            val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
            processedBitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

            // Prepare input (NHWC format - batch, height, width, channels)
            val inputBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * CHANNELS)
            inputBuffer.order(ByteOrder.nativeOrder())

            // Process each pixel - normalize to [0,1]
            for (y in 0 until INPUT_SIZE) {
                for (x in 0 until INPUT_SIZE) {
                    val pixel = pixels[y * INPUT_SIZE + x]

                    // Extract RGB values and normalize to [0,1]
                    val r = (pixel shr 16 and 0xFF) / 255.0f
                    val g = (pixel shr 8 and 0xFF) / 255.0f
                    val b = (pixel and 0xFF) / 255.0f

                    // Add normalized RGB values to buffer
                    inputBuffer.putFloat(r)
                    inputBuffer.putFloat(g)
                    inputBuffer.putFloat(b)
                }
            }

            // Reset position
            inputBuffer.flip()

            // Prepare output buffer
            val outputBuffer = Array(1) { FloatArray(EMOTION_COUNT) }

            // Run inference
            interpreter?.run(inputBuffer, outputBuffer)

            // Map results to emotions
            println("anger"+outputBuffer[0][0])
            println("joy"+outputBuffer[0][1])
            println("sadness"+outputBuffer[0][2])
            return mapOf(
                Emotion.JOY to outputBuffer[0][1],
                Emotion.SADNESS to outputBuffer[0][2],
                Emotion.ANGER to outputBuffer[0][0],
            )

        } catch (e: Exception) {
            Log.e("EmotionDetector", "Error during emotion detection", e)
            e.printStackTrace()
            return mockEmotionData()
        }
    }

    private fun mockEmotionData(): Map<Emotion, Float> {
        return mapOf(
            Emotion.JOY to 0.7f,
            Emotion.SADNESS to 0.1f,
            Emotion.ANGER to 0.05f,
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