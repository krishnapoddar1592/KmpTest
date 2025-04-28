// app/src/main/kotlin/com/reflect/app/android/ui/screens/EmotionDetectionScreen.kt
package com.reflect.app.android.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.reflect.app.android.ui.theme.EmotionTheme
import com.reflect.app.ml.Emotion
import com.reflect.app.ml.viewmodel.EmotionDetectionState
import com.reflect.app.ml.viewmodel.EmotionDetectionViewModel
import java.io.File

import java.util.concurrent.Executors

//@Composable
//fun EmotionDetectionScreen(
//    viewModel: EmotionDetectionViewModel = viewModel(),
//    onNavigateBack: () -> Unit
//) {
//    val context = LocalContext.current
//    val lifecycleOwner = LocalLifecycleOwner.current
//    val detectionState by viewModel.detectionState.collectAsState()
//
//    var hasCameraPermission by remember { mutableStateOf(
//        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
//    ) }
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
//
//    Box(modifier = Modifier.fillMaxSize()) {
//        if (hasCameraPermission) {
//            CameraPreview(
//                onImageCaptured = { imageBytes, width, height ->
//                    viewModel.detectEmotion(imageBytes, width, height)
//                }
//            )
//
//            when (val state = detectionState) {
//                is EmotionDetectionState.Loading -> {
//                    CircularProgressIndicator(
//                        modifier = Modifier.align(Alignment.Center)
//                    )
//                }
//                is EmotionDetectionState.Success -> {
//                    Column(
//                        modifier = Modifier
//                            .align(Alignment.BottomCenter)
//                            .padding(16.dp)
//                            .fillMaxWidth()
//                    ) {
//                        Text(
//                            text = "Detected Emotion: ${state.dominantEmotion.name}",
//                            modifier = Modifier.padding(8.dp)
//                        )
//
//                        // Display scores for all emotions
//                        state.emotionScores.forEach { (emotion, score) ->
//                            Row(
//                                modifier = Modifier.fillMaxWidth(),
//                                horizontalArrangement = Arrangement.SpaceBetween
//                            ) {
//                                Text(text = emotion.name)
//                                Text(text = String.format("%.2f", score))
//                            }
//                        }
//                    }
//                }
//                is EmotionDetectionState.Error -> {
//                    Text(
//                        text = state.message,
//                        modifier = Modifier
//                            .align(Alignment.Center)
//                            .padding(16.dp)
//                    )
//                }
//                else -> { /* Initial state - do nothing */ }
//            }
//
//            Button(
//                onClick = onNavigateBack,
//                modifier = Modifier
//                    .align(Alignment.TopStart)
//                    .padding(16.dp)
//            ) {
//                Text("Back")
//            }
//        } else {
//            Column(
//                modifier = Modifier.fillMaxSize(),
//                verticalArrangement = Arrangement.Center,
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Text("Camera permission is required for emotion detection")
//                Spacer(modifier = Modifier.height(16.dp))
//                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
//                    Text("Request Permission")
//                }
//            }
//        }
//    }
//}

@Composable
fun EmotionDetectionScreen(
    viewModel: EmotionDetectionViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val detectionState by viewModel.detectionState.collectAsState()

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

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            CameraPreview(
                onImageCaptureCreated = { capture -> imageCapture = capture }
            )

            Button(
                onClick = {
                    imageCapture?.let { capture ->
                        val photoFile = createTempFile(context.cacheDir)
                        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                        capture.takePicture(
                            outputOptions,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                    val bytes = photoFile.readBytes()
                                    viewModel.detectEmotion(bytes, 0, 0) // Width & height optional or can be parsed if needed
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    exception.printStackTrace()
                                }
                            }
                        )
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text("Detect")
            }

            when (val state = detectionState) {
                is EmotionDetectionState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is EmotionDetectionState.Success -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text("Detected Emotion: ${state.dominantEmotion.name}", color = EmotionTheme.colors.textPrimary)

                        state.emotionScores.forEach { (emotion, score) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(emotion.name, color = EmotionTheme.colors.textPrimary)
                                Text(String.format("%.2f", score), color = EmotionTheme.colors.textPrimary)
                            }
                        }
                    }
                }
                is EmotionDetectionState.Error -> {
                    Text(
                        text = state.message,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {}
            }

            Button(
                onClick = onNavigateBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Text("Back")
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Camera permission is required for emotion detection")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Request Permission")
                }
            }
        }
    }
}
@Composable
fun CameraPreview(
    onImageCaptureCreated: (ImageCapture) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember { PreviewView(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(key1 = lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            onImageCaptureCreated(imageCapture)

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    )
}
fun createTempFile(cacheDir: File): File {
    return File.createTempFile("emotion_image_", ".jpg", cacheDir)
}





//
//@Composable
//fun CameraPreview(
//    onImageCaptured: (ByteArray, Int, Int) -> Unit
//) {
//    val context = LocalContext.current
//    val lifecycleOwner = LocalLifecycleOwner.current
//
//    val previewView = remember { PreviewView(context) }
//    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
//
//    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
//
//    DisposableEffect(key1 = lifecycleOwner) {
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
//
//        cameraProviderFuture.addListener({
//            val cameraProvider = cameraProviderFuture.get()
//
//            val preview = Preview.Builder().build().also {
//                it.setSurfaceProvider(previewView.surfaceProvider)
//            }
//
//            imageCapture = ImageCapture.Builder()
//                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
//                .build()
//
//            val imageAnalysis = ImageAnalysis.Builder()
//                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                .build()
//                .also {
//                    it.setAnalyzer(cameraExecutor) { imageProxy ->
//                        // Process the image and detect emotion
//                        val buffer = imageProxy.planes[0].buffer
//                        val bytes = ByteArray(buffer.remaining())
//                        buffer.get(bytes)
//
//                        onImageCaptured(bytes, imageProxy.width, imageProxy.height)
//
//                        imageProxy.close()
//                    }
//                }
//
//            try {
//                cameraProvider.unbindAll()
//                cameraProvider.bindToLifecycle(
//                    lifecycleOwner,
//                    CameraSelector.DEFAULT_FRONT_CAMERA,
//                    preview,
//                    imageCapture,
//                    imageAnalysis
//                )
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }, ContextCompat.getMainExecutor(context))
//
//        onDispose {
//            cameraExecutor.shutdown()
//        }
//    }
//
//    AndroidView(
//        factory = { previewView },
//        modifier = Modifier.fillMaxSize()
//    )
//}