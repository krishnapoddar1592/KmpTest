


package com.reflect.app.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.util.FileUtil
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
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
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                .build()

            faceDetector = FaceDetection.getClient(options)

            setupModel()
        } catch (e: Exception) {
            Log.e("EmotionDetector", "Error initializing TFLite model or Face Detector", e)
        }
    }

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

    // Helper function to detect faces using ML Kit, returns as a suspend function
    private suspend fun detectFace(bitmap: Bitmap): Rect? = suspendCancellableCoroutine { continuation ->
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
        faceDetector.close()
        interpreter = null
        gpuDelegate = null
    }
}