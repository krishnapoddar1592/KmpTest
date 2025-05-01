//// shared/src/androidMain/kotlin/com/reflect/app/ml/AndroidEmotionDetector.kt
//package com.reflect.app.ml
//
//import android.content.Context
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import org.tensorflow.lite.Interpreter
//import org.tensorflow.lite.gpu.CompatibilityList
//import org.tensorflow.lite.gpu.GpuDelegate
//import org.tensorflow.lite.support.common.FileUtil
//import java.nio.ByteBuffer
//import java.nio.ByteOrder
//
//class AndroidEmotionDetector(private val context: Context) : EmotionDetector {
//    private var interpreter: Interpreter? = null
//    private var gpuDelegate: GpuDelegate? = null
//
//    init {
//        val options = Interpreter.Options()
//        // Use GPU if available
//        if (CompatibilityList().isDelegateSupportedOnThisDevice) {
//            gpuDelegate = GpuDelegate()
//            options.addDelegate(gpuDelegate)
//        }
//
//        // Load model
//        val model = FileUtil.loadMappedFile(context, "emotion_detection_model.tflite")
//        println(model.isLoaded)
//        interpreter = Interpreter(model, options)
//    }
//
//
//    override suspend fun detectEmotion(imageData: ByteArray, width: Int, height: Int): Map<Emotion, Float> {
//        // Convert byte array to bitmap
//        val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
//
//        // Resize if needed to match model input
//        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
//
//        // Normalize pixel values and prepare input tensor
//        val inputBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * 3)
//        inputBuffer.order(ByteOrder.nativeOrder())
//
//        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
//        resizedBitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)
//
//        for (pixel in pixels) {
//            // Extract RGB values and normalize to [-1, 1]
//            val r = (pixel shr 16 and 0xFF) / 127.5f - 1
//            val g = (pixel shr 8 and 0xFF) / 127.5f - 1
//            val b = (pixel and 0xFF) / 127.5f - 1
//
//            inputBuffer.putFloat(r)
//            inputBuffer.putFloat(g)
//            inputBuffer.putFloat(b)
//        }
//
//        // Prepare output buffer
//        val outputBuffer = Array(1) { FloatArray(EMOTION_COUNT) }
//
//        // Run inference
//        interpreter?.run(inputBuffer, outputBuffer)
//
//        // Map results to emotions
//        return mapOf(
//            Emotion.ANGER to outputBuffer[0][0],
//            Emotion.JOY to outputBuffer[0][1],
//            Emotion.SADNESS to outputBuffer[0][2],
//            Emotion.NEUTRAL to outputBuffer[0][3]
//        )
//    }
//
//    override fun close() {
//        interpreter?.close()
//        gpuDelegate?.close()
//    }
//
//    companion object {
//        private const val INPUT_SIZE = 224 // Depends on your model
//        private const val EMOTION_COUNT = 4 // Number of emotions your model predicts
//    }
//}
// shared/src/androidMain/kotlin/com/reflect/app/ml/AndroidEmotionDetector.kt
// shared/src/androidMain/kotlin/com/reflect/app/ml/AndroidEmotionDetector.kt
package com.reflect.app.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.firebase.firestore.util.FileUtil
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
//import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class AndroidEmotionDetector(private val context: Context) : EmotionDetector {
    private var interpreter: Interpreter? = null
    private var gpuDelegate: GpuDelegate? = null

    // Values based on your model analysis
    private val INPUT_SIZE = 100  // Your model expects 100x100 images
    private val CHANNELS = 3      // RGB input (not grayscale)
    private val EMOTION_COUNT = 3 // 4 output emotions

    init {
        try {
            setupModel()
        } catch (e: Exception) {
            Log.e("EmotionDetector", "Error initializing TFLite model", e)
        }
    }

    private fun setupModel() {
        try {
            // Load model
//            val model = FileUtil.loadMappedFile(context, "emotion_detection_model.tflite")
//            val fileDescriptor = context.assets.openFd("emotion_detection_model.tflite")
            val fileDescriptor = context.assets.openFd("emotion_model.tflite")
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

    override suspend fun detectEmotion(imageData: ByteArray, width: Int, height: Int): Map<Emotion, Float> {
        if (interpreter == null) {
            Log.d("EmotionDetector", "TFLite model not available, using mock data")
            return mockEmotionData()
        }

        try {
            // Decode the image
            val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)

            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
            val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
            resizedBitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

// Prepare input (NHWC format - batch, height, width, channels)
            val inputBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * CHANNELS)
            inputBuffer.order(ByteOrder.nativeOrder())

// Process each pixel
            for (y in 0 until INPUT_SIZE) {
                for (x in 0 until INPUT_SIZE) {
                    val pixel = pixels[y * INPUT_SIZE + x]

                    // Extract RGB values
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
            // Note: You may need to adjust this mapping based on your model's output order
            println("anger"+outputBuffer[0][0])
            println("joy"+outputBuffer[0][1])
            println("sadness"+outputBuffer[0][2])
            return mapOf(
                Emotion.JOY to outputBuffer[0][1],
                Emotion.SADNESS to outputBuffer[0][2],
                Emotion.ANGER to outputBuffer[0][0],
//                Emotion.NEUTRAL to outputBuffer[0][3]
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
//            Emotion.NEUTRAL to 0.15f
        )
    }

    override fun close() {
        interpreter?.close()
        gpuDelegate?.close()
        interpreter = null
        gpuDelegate = null
    }
}