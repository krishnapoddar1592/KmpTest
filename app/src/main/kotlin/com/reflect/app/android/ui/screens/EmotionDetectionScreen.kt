package com.reflect.app.android.ui.screens

import android.util.Log
import android.Manifest


import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.ui.viewinterop.AndroidView
import java.util.concurrent.Executors
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.reflect.app.android.R
import com.reflect.app.android.ui.components.EmotionButton
import com.reflect.app.android.ui.theme.EmotionTheme
import com.reflect.app.ml.Emotion
import com.reflect.app.ml.viewmodel.EmotionDetectionViewModel
import com.reflect.app.ml.viewmodel.FaceDetectionState
import com.reflect.app.models.ContextTag
import com.reflect.app.viewmodels.EnhancedEmotionDetectionState
import com.reflect.app.viewmodels.EnhancedEmotionDetectionViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.io.File

//// app/src/main/kotlin/com/reflect/app/android/ui/screens/EmotionDetectionScreen.kt
//package com.reflect.app.android.ui.screens
//
//import android.Manifest
//import android.content.pm.PackageManager
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import android.graphics.ImageFormat
//import android.graphics.Matrix
//import android.graphics.Rect
//import android.graphics.YuvImage
//import android.util.Log
//import android.util.Size
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.annotation.OptIn
//import androidx.camera.core.CameraSelector
//import androidx.camera.core.ExperimentalGetImage
//import androidx.camera.core.ImageAnalysis
//import androidx.camera.core.ImageCapture
//import androidx.camera.core.ImageCaptureException
//import androidx.camera.core.ImageProxy
//import androidx.camera.core.Preview
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.camera.view.PreviewView
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxHeight
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.CircularProgressIndicator
//import androidx.compose.material.Icon
//import androidx.compose.material.IconButton
//import androidx.compose.material.Text
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material3.CardDefaults
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.DisposableEffect
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.alpha
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.asImageBitmap
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalLifecycleOwner
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.LifecycleOwner
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.reflect.app.android.R
//import com.reflect.app.android.ui.components.EmotionButton
//import com.reflect.app.android.ui.theme.EmotionTheme
//import com.reflect.app.ml.viewmodel.EmotionDetectionState
//import com.reflect.app.ml.viewmodel.EmotionDetectionViewModel
//import com.reflect.app.ml.viewmodel.FaceDetectionState
//import kotlinx.coroutines.delay
//import java.io.ByteArrayOutputStream
//import java.io.File
//import java.util.concurrent.Executors
//
//
//@Composable
//fun EmotionDetectionScreen(
//    viewModel: EmotionDetectionViewModel = viewModel(),
//    onNavigateBack: () -> Unit
//) {
//    val context = LocalContext.current
//    val lifecycleOwner = LocalLifecycleOwner.current
//    val detectionState by viewModel.detectionState.collectAsState()
//    val scansRemaining = remember { mutableStateOf(5) }
//
//    // IMPORTANT: Single imageCapture state that will be shared throughout the composable
//    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
//    var capturedImagePath by remember { mutableStateOf<String?>(null) }
//    var isCameraRestarting by remember { mutableStateOf(false) }
//    val faceDetectionState by viewModel.faceDetectionState.collectAsState()
//    val isDetectButtonEnabled = faceDetectionState is FaceDetectionState.FaceDetected
//
//
//
//
//
//    var hasCameraPermission by remember {
//        mutableStateOf(
//            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
//        )
//    }
//    // Reset state when leaving the screen
//    DisposableEffect(key1 = Unit) {
//        onDispose {
//            viewModel.resetState()
//        }
//    }
//
//
//    val permissionLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.RequestPermission()
//    ) { isGranted ->
//        hasCameraPermission = isGranted
//    }
//
//    LaunchedEffect(key1 = true) {
//        if (!hasCameraPermission) {
//            permissionLauncher.launch(Manifest.permission.CAMERA)
//        }
//    }
//    LaunchedEffect(faceDetectionState) {
//        Log.d("EmotionDetectionScreen", "Face detection state changed: ${faceDetectionState.toString()}")
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(EmotionTheme.colors.background)
//
//    ) {
//        Column(
//            modifier = Modifier.fillMaxSize(),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            // Top bar
//
//
////            Spacer(modifier = Modifier.weight(1f))
//
//            // Camera preview in small box
//            Box(
//                modifier = Modifier
//                    .fillMaxHeight()
//                    .weight(1.6f) // Take up proportionally more space for the image
//            ) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
////                    .padding(16.dp)
//                ) {
//                    IconButton(
//                        onClick = onNavigateBack,
//                        modifier = Modifier
//                            .size(48.dp)
//                            .background(EmotionTheme.colors.backgroundSecondary.copy(alpha = 0.5f), CircleShape)
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.ArrowBack,
//                            contentDescription = "Back",
//                            tint = EmotionTheme.colors.textPrimary
//                        )
//                    }
//
//                    // Date display
////                    Row(
////                        modifier = Modifier
////                            .align(Alignment.CenterEnd)
////                            .background(
////                                EmotionTheme.colors.backgroundSecondary.copy(alpha = 0.5f),
////                                RoundedCornerShape(24.dp)
////                            )
////                            .padding(horizontal = 16.dp, vertical = 8.dp),
////                        verticalAlignment = Alignment.CenterVertically
////                    ) {
////                        Icon(
////                            imageVector = Icons.Filled.ArrowBack,  // Replace with calendar icon
////                            contentDescription = "Calendar",
////                            tint = EmotionTheme.colors.textPrimary,
////                            modifier = Modifier.size(16.dp)
////                        )
////                        Spacer(modifier = Modifier.width(8.dp))
////                        Text(
////                            text = "25 Feb 2025",
////                            color = EmotionTheme.colors.textPrimary,
////                            fontSize = 14.sp
////                        )
////                    }
//                }
//                // Background image
//                Image(
//                    painter = painterResource(id = R.drawable.avatar),
//                    contentDescription = "Background illustration",
//                    contentScale = ContentScale.FillHeight,
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .align(Alignment.BottomCenter)
//                )
//
//                // Camera preview in small box positioned to overlap the image
//                if (hasCameraPermission) {
//                    Box(
//                        modifier = Modifier
//                            .align(Alignment.BottomEnd)
//                            .padding(end = 16.dp)
//                            .size(150.dp)
//                            .clip(RoundedCornerShape(16.dp))
//                            .border(2.dp, if(faceDetectionState==FaceDetectionState.NoFaceDetected)Color.Red else Color.Green, RoundedCornerShape(16.dp))
//                    ) {
//                        if (capturedImagePath == null) {
//                            CameraPreviewWithImageCaptureAndAnalysis(
//                                viewModel = viewModel,
//                                onImageCaptureCreated = { capture ->
//                                    imageCapture = capture
//                                    // If you were previously setting the imageCapture on the ViewModel, do it here
////                                    viewModel.setImageCaptureInstance(capture)
//                                },
//                                observedFaceDetectionState = faceDetectionState // Pass the collected state
//                            )
//                        } else {
//                            val bitmap = BitmapFactory.decodeFile(capturedImagePath)
//                            bitmap?.let {
//                                // Mirror the bitmap horizontally
//                                val matrix = Matrix().apply { preScale(-1f, 1f) }
//                                val mirroredBitmap = Bitmap.createBitmap(it, 0, 0, it.width, it.height, matrix, false)
//
//                                Image(
//                                    bitmap = mirroredBitmap.asImageBitmap(),
//                                    contentDescription = "Captured Image",
//                                    contentScale = ContentScale.Crop,
//                                    modifier = Modifier.fillMaxSize()
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//
////            Spacer(modifier = Modifier.weight(1f))
//
//            // Bottom card with scan button or results
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
//                    .background(EmotionTheme.colors.backgroundSecondary)
//                    .padding(top = 42.dp, bottom=32.dp) // Space for bottom navigation
//            ) {
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Text(
//                        text=when (faceDetectionState) {
//                            FaceDetectionState.FaceDetected -> "Face Detected"
//                            FaceDetectionState.NoFaceDetected -> "Align your face with the frame"
//                            FaceDetectionState.Initial -> "Starting..."
//                        },
////                        text = "Align your face with the frame",
//                        color = EmotionTheme.colors.textPrimary,
//                        fontSize = 24.sp,
//                        fontWeight = FontWeight.Bold,
//                        modifier = Modifier.padding(bottom = 24.dp)
//                    )
//
//                    when (val state = detectionState) {
//                        is EmotionDetectionState.Loading -> {
//                            Box(
//                                modifier = Modifier
//                                    .size(120.dp)
//                                    .background(EmotionTheme.colors.interactive, CircleShape),
//                                contentAlignment = Alignment.Center
//                            ) {
//                                CircularProgressIndicator(
//                                    color = EmotionTheme.colors.textPrimary,
//                                    modifier = Modifier.size(60.dp)
//                                )
//                            }
//                        }
//                        is EmotionDetectionState.Success -> {
//                            Box {
//                                Column(
//                                    horizontalAlignment = Alignment.CenterHorizontally
//                                ) {
//                                    Text(
//                                        text = "Detected Emotion",
//                                        color = EmotionTheme.colors.textPrimary,
//                                        fontSize = 18.sp,
//                                        fontWeight = FontWeight.Bold
//                                    )
//
//                                    Spacer(modifier = Modifier.height(8.dp))
//
//                                    Text(
//                                        text = state.dominantEmotion.name,
//                                        color = EmotionTheme.colors.interactive,
//                                        fontSize = 24.sp,
//                                        fontWeight = FontWeight.Bold
//                                    )
//
//                                    Spacer(modifier = Modifier.height(16.dp))
//
//                                    EmotionButton(
//                                        "Restart",
//                                        onClick = {
////                                            isCameraRestarting = true
//                                            capturedImagePath = null  // reset image
////                                            CoroutineScope(Dispatchers.Main).launch {
////                                                delay(2000) // Simulate restart delay
////                                                isCameraRestarting = false
////                                            }
//                                            viewModel.resetState()    // reset detection result
//
//
//                                        },
//                                        modifier = Modifier,
//                                        contentColor = EmotionTheme.colors.textPrimary,
//                                        enabled = true
//                                    )
//                                }
//                            }
//                        }
//                        else -> {
//                            // Scan button
//                            Box(
//                                modifier = Modifier
//                                    .alpha(if (isDetectButtonEnabled) 1f else 0.5f) // Apply alpha: 1f for enabled, 0.5f (or similar) for disabled
//                                    .size(120.dp)
//                                    .background(EmotionTheme.colors.interactive, CircleShape)
//                                    .clickable(
//                                        enabled = isDetectButtonEnabled,
//                                    ) {
//                                        println("Scan button clicked") // Debug print
//                                        val captureInstance = imageCapture
//
//                                        if (captureInstance == null) {
//                                            println("ERROR: imageCapture is null")
//                                            return@clickable
//                                        }
//                                        println("ImageCapture instance retrieved: $captureInstance")
//
//                                        try {
//                                            val photoFile = createTempFile(context.cacheDir)
//                                            println("Created temp file: ${photoFile.absolutePath}")
//
//                                            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
//
//                                            captureInstance.takePicture(
//                                                outputOptions,
//                                                ContextCompat.getMainExecutor(context),
//                                                object : ImageCapture.OnImageSavedCallback {
//                                                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
//                                                        println("Image saved successfully at: ${outputFileResults.savedUri ?: photoFile.absolutePath}")
//
//                                                        try {
//                                                            val bytes = photoFile.readBytes()
//                                                            viewModel.detectEmotion(bytes, 0, 0)
//                                                            capturedImagePath = photoFile.absolutePath  // store image path
//                                                            scansRemaining.value -= 1
//                                                        } catch (e: Exception) {
//                                                            println("Error reading file: ${e.message}")
//                                                            e.printStackTrace()
//                                                        }
//                                                    }
//
//                                                    override fun onError(exception: ImageCaptureException) {
//                                                        println("Error capturing image: ${exception.message}")
//                                                        exception.printStackTrace()
//                                                    }
//                                                }
//                                            )
//                                        } catch (e: Exception) {
//                                            println("Error in takePicture process: ${e.message}")
//                                            e.printStackTrace()
//                                        }
//                                    },
//                                contentAlignment = Alignment.Center,
//
//                            ) {
//                                Text(
//                                    text = "SCAN",
//                                    color = EmotionTheme.colors.textPrimary,
//                                    fontSize = 24.sp,
//                                    fontWeight = FontWeight.Bold
//                                )
//                            }
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    Text(
//                        text = "${scansRemaining.value}/5 scans remaining today",
//                        color = EmotionTheme.colors.textPrimary,
//                        fontSize = 16.sp
//                    )
//                }
//            }
//        }
//    }
//}
//
//
//
//@Composable
//fun CameraPreview(
//    onImageCaptureCreated: (ImageCapture) -> Unit,
//    onFaceDetectionResult: (ByteArray, Int, Int) -> Unit,
//    faceDetectionState: FaceDetectionState
//) {
//    val context = LocalContext.current
//    val lifecycleOwner = LocalLifecycleOwner.current
//    val previewView = remember { PreviewView(context) }
//    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
//
//    // Track camera state
//    var isInitialized by remember { mutableStateOf(false) }
//    var currentImageCapture by remember { mutableStateOf<ImageCapture?>(null) }
//
//    // Frame color based on face detection
//    val frameColor = when (faceDetectionState) {
//        FaceDetectionState.FaceDetected -> Color.Red
//        FaceDetectionState.NoFaceDetected -> Color.Gray
//        FaceDetectionState.Initial -> Color.Transparent
//    }
//
//    // Face detection timer
//    LaunchedEffect(currentImageCapture) {
//        if (currentImageCapture != null) {
//            while (true) {
//                delay(1500) // Every 1.5 seconds
//
//                currentImageCapture?.let { capture ->
//                    val outputFile = java.io.File(context.cacheDir, "face_detection_${System.currentTimeMillis()}.jpg")
//
//                    capture.takePicture(
//                        ImageCapture.OutputFileOptions.Builder(outputFile).build(),
//                        ContextCompat.getMainExecutor(context),
//                        object : ImageCapture.OnImageSavedCallback {
//                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
//                                cameraExecutor.execute { // Run face detection on background thread
//                                    try {
//                                        val imageData = outputFile.readBytes()
//                                        outputFile.delete() // Clean up immediately
//                                        onFaceDetectionResult(imageData, 640, 480)
//                                    } catch (e: Exception) {
//                                        Log.e("CameraPreview", "Error reading face detection image", e)
//                                    }
//                                }
//                            }
//
//                            override fun onError(exception: ImageCaptureException) {
//                                Log.w("CameraPreview", "Face detection capture failed", exception)
//                            }
//                        }
//                    )
//                }
//            }
//        }
//    }
//
//
//    DisposableEffect(lifecycleOwner) {
//        fun startCamera(lifecycleOwner: LifecycleOwner) {
//            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
//
//            cameraProviderFuture.addListener({
//                try {
//                    val cameraProvider = cameraProviderFuture.get()
//
//                    // Unbind any existing use cases
//                    cameraProvider.unbindAll()
//
//                    // Simple preview configuration
//                    val preview = Preview.Builder()
//                        .setTargetResolution(Size(640, 480))
//                        .build()
//
//                    // Simple image capture configuration
//                    val imageCapture = ImageCapture.Builder()
//                        .setTargetResolution(Size(640, 480))
//                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
//                        .build()
//
//                    // Connect preview to PreviewView
//                    preview.setSurfaceProvider(previewView.surfaceProvider)
//
//                    // Bind use cases together
//                    cameraProvider.bindToLifecycle(
//                        lifecycleOwner,
//                        CameraSelector.DEFAULT_FRONT_CAMERA,
//                        preview,
//                        imageCapture
//                    )
//
//                    // Setup successful
//                    currentImageCapture = imageCapture
//                    onImageCaptureCreated(imageCapture)
//                    isInitialized = true
//
//                    Log.d("CameraPreview", "Camera setup completed successfully")
//
//                } catch (e: Exception) {
//                    Log.e("CameraPreview", "Camera setup failed", e)
//                    isInitialized = false
//                }
//            }, ContextCompat.getMainExecutor(context))
//        }
//
//        startCamera(lifecycleOwner)
//
//        onDispose {
//            cameraExecutor.shutdown()
//            currentImageCapture = null
//            isInitialized = false
//        }
//    }
//}
//

//
//
//fun createTempFile(cacheDir: File): File {
//    return File.createTempFile("emotion_image_", ".jpg", cacheDir).apply {
//        deleteOnExit() // Ensure temp files are cleaned up
//    }
//}
//object ImageConverter {
//
//    /**
//     * Converts ImageProxy to ByteArray (JPEG format)
//     * Now properly handles YUV_420_888 format
//     */
//    fun imageProxyToByteArray(imageProxy: ImageProxy): ByteArray {
//        Log.d("ImageConverter", "Converting ImageProxy - format: ${imageProxy.format}, size: ${imageProxy.width}x${imageProxy.height}")
//
//        return try {
//            when (imageProxy.format) {
//                ImageFormat.JPEG -> {
//                    // If it's already JPEG, we can directly use the buffer
//                    val buffer = imageProxy.planes[0].buffer
//                    val bytes = ByteArray(buffer.remaining())
//                    buffer.get(bytes)
//                    Log.d("ImageConverter", "Direct JPEG extraction - size: ${bytes.size}")
//                    bytes
//                }
//                ImageFormat.YUV_420_888 -> {
//                    // Convert YUV to JPEG using YuvImage class
//                    val result = yuvToJpegByteArray(imageProxy)
//                    Log.d("ImageConverter", "YUV conversion - size: ${result.size}")
//                    result
//                }
//                else -> {
//                    // For other formats, use a fallback method
//                    Log.d("ImageConverter", "Using fallback conversion for format: ${imageProxy.format}")
//                    val bitmap = createBitmapFromImageProxy(imageProxy)
//                    if (bitmap != null) {
//                        val result = bitmapToByteArray(bitmap)
//                        bitmap.recycle()
//                        result
//                    } else {
//                        createFallbackImage()
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            Log.e("ImageConverter", "Error converting image", e)
//            createFallbackImage()
//        }
//    }
//
//    /**
//     * Converts YUV_420_888 ImageProxy to JPEG ByteArray
//     * This is the correct way to handle YUV format images
//     */
//    private fun yuvToJpegByteArray(imageProxy: ImageProxy): ByteArray {
//        val yBuffer = imageProxy.planes[0].buffer // Y
//        val uBuffer = imageProxy.planes[1].buffer // U
//        val vBuffer = imageProxy.planes[2].buffer // V
//
//        val ySize = yBuffer.remaining()
//        val uSize = uBuffer.remaining()
//        val vSize = vBuffer.remaining()
//
//        val nv21 = ByteArray(ySize + uSize + vSize)
//
//        // Copy Y data
//        yBuffer.get(nv21, 0, ySize)
//
//        // Copy UV data (interleaved)
//        val uvPixelStride = imageProxy.planes[1].pixelStride // Generally 2 for NV21
//        if (uvPixelStride == 1) {
//            // UV data is contiguous
//            uBuffer.get(nv21, ySize, uSize)
//            vBuffer.get(nv21, ySize + uSize, vSize)
//        } else {
//            // UV data is interleaved (NV21 format)
//            // We need to copy U and V data in the right order
//            val uArray = ByteArray(uSize)
//            val vArray = ByteArray(vSize)
//            uBuffer.get(uArray)
//            vBuffer.get(vArray)
//
//            // Interleave U and V
//            var uvIndex = ySize
//            for (i in 0 until uArray.size step uvPixelStride) {
//                nv21[uvIndex++] = vArray[i] // V comes first in NV21
//                nv21[uvIndex++] = uArray[i] // Then U
//            }
//        }
//
//        // Convert to JPEG using YuvImage
//        val yuvImage = YuvImage(nv21, ImageFormat.NV21, imageProxy.width, imageProxy.height, null)
//        val outputStream = ByteArrayOutputStream()
//        yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), 85, outputStream)
//
//        return outputStream.toByteArray()
//    }
//
//    /**
//     * Alternative method to create bitmap from ImageProxy
//     * Used as fallback for formats other than YUV_420_888
//     */
//    private fun createBitmapFromImageProxy(imageProxy: ImageProxy): Bitmap? {
//        return try {
//            // Try to extract pixel data directly
//            val buffer = imageProxy.planes[0].buffer
//            val pixelStride = imageProxy.planes[0].pixelStride
//            val rowStride = imageProxy.planes[0].rowStride
//            val rowPadding = rowStride - pixelStride * imageProxy.width
//
//            val bitmap = Bitmap.createBitmap(
//                imageProxy.width + rowPadding / pixelStride,
//                imageProxy.height,
//                Bitmap.Config.ARGB_8888
//            )
//
//            buffer.rewind()
//            bitmap.copyPixelsFromBuffer(buffer)
//
//            // Crop to actual size if there's padding
//            if (rowPadding > 0) {
//                val croppedBitmap = Bitmap.createBitmap(bitmap, 0, 0, imageProxy.width, imageProxy.height)
//                bitmap.recycle()
//                croppedBitmap
//            } else {
//                bitmap
//            }
//        } catch (e: Exception) {
//            Log.e("ImageConverter", "Failed to create bitmap from ImageProxy", e)
//            null
//        }
//    }
//
//    /**
//     * Converts Bitmap to ByteArray (JPEG format)
//     */
//    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
//        val outputStream = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
//        return outputStream.toByteArray()
//    }
//
//    /**
//     * Creates a minimal fallback image if conversion fails
//     * Creates a larger image that meets ML Kit's minimum requirements
//     */
//    private fun createFallbackImage(): ByteArray {
//        // Create a 64x64 black image (meets ML Kit's 32x32 minimum)
//        val bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
//        bitmap.eraseColor(android.graphics.Color.BLACK)
//        val outputStream = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
//        bitmap.recycle()
//        return outputStream.toByteArray()
//    }
//}
//
//
// File: app/src/main/kotlin/com/reflect/app/android/ui/screens/UpdatedEmotionDetectionScreen.kt




@Composable
fun EmotionDetectionScreen(
    viewModel: EnhancedEmotionDetectionViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val detectionState by viewModel.detectionState.collectAsState()
    val faceDetectionState by viewModel.faceDetectionState.collectAsState()

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var capturedImagePath by remember { mutableStateOf<String?>(null) }
    val scansRemaining = remember { mutableStateOf(5) }
    val isDetectButtonEnabled = faceDetectionState is FaceDetectionState.FaceDetected

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Handle saved state
    when (val state = detectionState) {
        is EnhancedEmotionDetectionState.Saved -> {
            LaunchedEffect(state.entry) {
                kotlinx.coroutines.delay(2000)
                viewModel.resetState()
                capturedImagePath = null
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "✅",
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Entry Saved Successfully!",
                        style = MaterialTheme.typography.headlineSmall,
                        color = EmotionTheme.colors.interactive,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your emotion has been added to your timeline",
                        style = MaterialTheme.typography.bodyLarge,
                        color = EmotionTheme.colors.textSecondary
                    )
                }
            }
            return
        }
        else -> {
            // Continue with main interface
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EmotionTheme.colors.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Camera section
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1.6f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(48.dp)
                            .background(EmotionTheme.colors.backgroundSecondary.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = EmotionTheme.colors.textPrimary
                        )
                    }
                }

                // Background image
                Image(
                    painter = painterResource(id = R.drawable.avatar),
                    contentDescription = "Background illustration",
                    contentScale = ContentScale.FillHeight,
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.BottomCenter)
                )

                // Camera preview
                if (hasCameraPermission) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp)
                            .size(150.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                2.dp,
                                if (faceDetectionState == FaceDetectionState.NoFaceDetected) Color.Red else Color.Green,
                                RoundedCornerShape(16.dp)
                            )
                    ) {
                        if (capturedImagePath == null) {
                            CameraPreviewWithImageCaptureAndAnalysis(
                                viewModel = viewModel,
                                onImageCaptureCreated = { capture ->
                                    imageCapture = capture
                                },
                                observedFaceDetectionState = faceDetectionState
                            )
                        } else {
                            val bitmap = BitmapFactory.decodeFile(capturedImagePath)
                            bitmap?.let {
                                val matrix = Matrix().apply { preScale(-1f, 1f) }
                                val mirroredBitmap = Bitmap.createBitmap(it, 0, 0, it.width, it.height, matrix, false)

                                Image(
                                    bitmap = mirroredBitmap.asImageBitmap(),
                                    contentDescription = "Captured Image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }

            // Bottom section - now responsive to detection state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(EmotionTheme.colors.backgroundSecondary)
                    .padding(top = 32.dp, bottom = 32.dp)
            ) {
                when (val detectionStateLocal = detectionState) {
                    is EnhancedEmotionDetectionState.Success -> {
                        // Show detection result with inline UI
                        DetectionResultContent(
                            dominantEmotion = detectionStateLocal.dominantEmotion,
                            emotionScores = detectionStateLocal.emotionScores,
                            onSaveEntry = { selectedTags, note ->
                                viewModel.saveEmotionEntry(selectedTags, note)
                            },
                            onRestart = {
                                viewModel.resetState()
                                capturedImagePath = null
                            }
                        )
                    }
                    is EnhancedEmotionDetectionState.Loading -> {
                        // Show loading state
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Analyzing emotion...",
                                color = EmotionTheme.colors.textPrimary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .background(EmotionTheme.colors.interactive, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = EmotionTheme.colors.textPrimary,
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                        }
                    }
                    else -> {
                        // Show scan interface
                        ScanInterfaceContent(
                            faceDetectionState = faceDetectionState,
                            isDetectButtonEnabled = isDetectButtonEnabled,
                            scansRemaining = scansRemaining.value,
                            onScanClick = {
                                val captureInstance = imageCapture ?: return@ScanInterfaceContent

                                try {
                                    val photoFile = createTempFile(context.cacheDir)
                                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                                    captureInstance.takePicture(
                                        outputOptions,
                                        ContextCompat.getMainExecutor(context),
                                        object : ImageCapture.OnImageSavedCallback {
                                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                                try {
                                                    val bytes = photoFile.readBytes()
                                                    viewModel.detectEmotion(bytes, 0, 0, photoFile.absolutePath)
                                                    capturedImagePath = photoFile.absolutePath
                                                    scansRemaining.value -= 1
                                                } catch (e: Exception) {
                                                    println("Error reading file: ${e.message}")
                                                }
                                            }

                                            override fun onError(exception: ImageCaptureException) {
                                                println("Error capturing image: ${exception.message}")
                                            }
                                        }
                                    )
                                } catch (e: Exception) {
                                    println("Error in takePicture process: ${e.message}")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScanInterfaceContent(
    faceDetectionState: FaceDetectionState,
    isDetectButtonEnabled: Boolean,
    scansRemaining: Int,
    onScanClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = when (faceDetectionState) {
                FaceDetectionState.FaceDetected -> "Face Detected"
                FaceDetectionState.NoFaceDetected -> "Align your face with the frame"
                FaceDetectionState.Initial -> "Starting..."
            },
            color = EmotionTheme.colors.textPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Scan button
        Box(
            modifier = Modifier
                .alpha(if (isDetectButtonEnabled) 1f else 0.5f)
                .size(120.dp)
                .background(EmotionTheme.colors.interactive, CircleShape)
                .clickable(enabled = isDetectButtonEnabled) { onScanClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SCAN",
                color = EmotionTheme.colors.textPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "$scansRemaining/5 scans remaining today",
            color = EmotionTheme.colors.textPrimary,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun DetectionResultContent(
    dominantEmotion: Emotion,
    emotionScores: Map<Emotion, Float>,
    onSaveEntry: (selectedTags: List<ContextTag>, note: String) -> Unit,
    onRestart: () -> Unit
) {
    var selectedTags by remember { mutableStateOf(setOf<ContextTag>()) }
    var moodNote by remember { mutableStateOf("") }
    var showAllTags by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    val currentTime = remember {
        val now = Clock.System.now()
        val localDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
        "${localDateTime.hour}:${localDateTime.minute.toString().padStart(2, '0')}"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isExpanded) {
                    Modifier.verticalScroll(rememberScrollState())
                } else Modifier
            )
    ) {
        // Main detection result header - matches your UI
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "I detect",
                style = MaterialTheme.typography.headlineSmall,
                color = EmotionTheme.colors.textPrimary,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = dominantEmotion.name.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.headlineSmall,
                color = getEmotionColor(dominantEmotion),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Context tags section
        Text(
            text = "Add Context Tags:",
            style = MaterialTheme.typography.titleMedium,
            color = EmotionTheme.colors.textPrimary,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tag selection - showing limited tags initially
        val visibleTags = if (showAllTags) ContextTag.values().toList()
        else ContextTag.values().take(4).toList()

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(visibleTags) { tag ->
                ContextTagChip(
                    tag = tag,
                    isSelected = selectedTags.contains(tag),
                    onToggle = {
                        selectedTags = if (selectedTags.contains(tag)) {
                            selectedTags - tag
                        } else {
                            selectedTags + tag
                        }
                    }
                )
            }

            if (!showAllTags && ContextTag.values().size > 4) {
                item {
                    IconButton(
                        onClick = {
                            showAllTags = true
                            isExpanded = true
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                EmotionTheme.colors.backgroundSecondary,
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "More tags",
                            tint = EmotionTheme.colors.interactive
                        )
                    }
                }
            }
        }

        // Expandable content
        if (isExpanded) {
            Spacer(modifier = Modifier.height(24.dp))

            // Time display
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Time of Scan",
                    style = MaterialTheme.typography.bodyLarge,
                    color = EmotionTheme.colors.textSecondary
                )
                Text(
                    text = currentTime,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = EmotionTheme.colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Mood note section
            Text(
                text = "Mood Note",
                style = MaterialTheme.typography.titleMedium,
                color = EmotionTheme.colors.textPrimary,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = moodNote,
                onValueChange = { moodNote = it },
                placeholder = {
                    Text(
                        "I enjoyed a good gym session today and also took my dog for a walk in the park",
                        color = EmotionTheme.colors.textSecondary.copy(alpha = 0.7f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmotionTheme.colors.interactive,
                    unfocusedBorderColor = EmotionTheme.colors.textSecondary.copy(alpha = 0.3f),
                    focusedTextColor = EmotionTheme.colors.textPrimary,
                    unfocusedTextColor = EmotionTheme.colors.textPrimary,
                    cursorColor = EmotionTheme.colors.interactive,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                EmotionButton(
                    text = "Save Entry",
                    onClick = {
                        onSaveEntry(selectedTags.toList(), moodNote)
                    },
                    modifier = Modifier.weight(1f),
                    contentColor = EmotionTheme.colors.textPrimary
                )

                OutlinedButton(
                    onClick = onRestart,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = EmotionTheme.colors.interactive
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        EmotionTheme.colors.interactive
                    )
                ) {
                    Text(
                        text = "Restart",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(16.dp))

            // Expand button when collapsed
            TextButton(
                onClick = { isExpanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Add details and save",
                    color = EmotionTheme.colors.interactive,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun ContextTagChip(
    tag: ContextTag,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        EmotionTheme.colors.interactive
    } else {
        EmotionTheme.colors.backgroundSecondary
    }

    val textColor = if (isSelected) {
        Color.White
    } else {
        EmotionTheme.colors.textPrimary
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .clickable { onToggle() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .then(
                if (!isSelected) {
                    Modifier.border(
                        1.dp,
                        EmotionTheme.colors.textSecondary.copy(alpha = 0.3f),
                        RoundedCornerShape(24.dp)
                    )
                } else Modifier
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = tag.icon,
            fontSize = 16.sp
        )
        Text(
            text = tag.displayName,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor
        )
    }
}

@Composable
fun CameraPreviewWithImageCaptureAndAnalysis(
    viewModel: EnhancedEmotionDetectionViewModel,
    onImageCaptureCreated: (ImageCapture) -> Unit,
    observedFaceDetectionState: FaceDetectionState
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var lastAnalysisTimeMs by remember { mutableStateOf(0L) }
    val analysisIntervalMs = 750L

    DisposableEffect(lifecycleOwner, viewModel) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()

                val preview = Preview.Builder()
                    .setTargetResolution(android.util.Size(640, 480))
                    .build().also { it.setSurfaceProvider(previewView.surfaceProvider) }

                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(android.util.Size(640, 480))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(
                    cameraExecutor,
                    ImageAnalysis.Analyzer { imageProxy ->
                        val currentTimeMs = System.currentTimeMillis()
                        if (currentTimeMs - lastAnalysisTimeMs >= analysisIntervalMs) {
                            lastAnalysisTimeMs = currentTimeMs
                            viewModel.analyzeFaceInImageProxy(imageProxy)
                        } else {
                            imageProxy.close()
                        }
                    }
                )

                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    imageCapture,
                    imageAnalysis
                )
                onImageCaptureCreated(imageCapture)
                Log.d("CameraPreview", "Camera setup with all use cases successful.")

            } catch (e: Exception) {
                Log.e("CameraPreview", "Camera setup failed", e)
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            Log.d("CameraPreview", "Disposing camera preview, shutting down executor.")
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
    }
}

private fun createTempFile(cacheDir: File): File {
    return File.createTempFile("emotion_image_", ".jpg", cacheDir).apply {
        deleteOnExit()
    }
}

private fun getEmotionColor(emotion: Emotion): Color {
    return when (emotion) {
        Emotion.JOY -> Color(0xFFFFD900)
        Emotion.SADNESS -> Color(0xFF1E2761)
        Emotion.ANGER -> Color(0xFFFF4444)
        Emotion.NEUTRAL -> Color(0xFF888888)
    }
}