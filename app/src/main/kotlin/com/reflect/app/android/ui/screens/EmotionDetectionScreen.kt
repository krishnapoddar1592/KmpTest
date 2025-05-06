// app/src/main/kotlin/com/reflect/app/android/ui/screens/EmotionDetectionScreen.kt
package com.reflect.app.android.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.reflect.app.android.R
import com.reflect.app.android.ui.components.EmotionButton
import com.reflect.app.android.ui.theme.EmotionTheme
import com.reflect.app.ml.viewmodel.EmotionDetectionState
import com.reflect.app.ml.viewmodel.EmotionDetectionViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.Executors


@Composable
fun EmotionDetectionScreen(
    viewModel: EmotionDetectionViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val detectionState by viewModel.detectionState.collectAsState()
    val scansRemaining = remember { mutableStateOf(5) }

    // IMPORTANT: Single imageCapture state that will be shared throughout the composable
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var capturedImagePath by remember { mutableStateOf<String?>(null) }
    var isCameraRestarting by remember { mutableStateOf(false) }



    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    // Reset state when leaving the screen
    DisposableEffect(key1 = Unit) {
        onDispose {
            viewModel.resetState()
        }
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EmotionTheme.colors.background)

    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar


//            Spacer(modifier = Modifier.weight(1f))

            // Camera preview in small box
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1.6f) // Take up proportionally more space for the image
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
//                    .padding(16.dp)
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

                    // Date display
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .background(
                                EmotionTheme.colors.backgroundSecondary.copy(alpha = 0.5f),
                                RoundedCornerShape(24.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,  // Replace with calendar icon
                            contentDescription = "Calendar",
                            tint = EmotionTheme.colors.textPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "25 Feb 2025",
                            color = EmotionTheme.colors.textPrimary,
                            fontSize = 14.sp
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

                // Camera preview in small box positioned to overlap the image
                if (hasCameraPermission) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp)
                            .size(150.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(2.dp, Color.Red, RoundedCornerShape(16.dp))
                    ) {
                        if (capturedImagePath == null) {
                            CameraPreview { capture ->
                                imageCapture = capture
                            }
                        } else {
                            val bitmap = BitmapFactory.decodeFile(capturedImagePath)
                            bitmap?.let {
                                // Mirror the bitmap horizontally
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

//            Spacer(modifier = Modifier.weight(1f))

            // Bottom card with scan button or results
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(EmotionTheme.colors.backgroundSecondary)
                    .padding(top = 42.dp, bottom=32.dp) // Space for bottom navigation
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Align your face with the frame",
                        color = EmotionTheme.colors.textPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    when (val state = detectionState) {
                        is EmotionDetectionState.Loading -> {
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
                        is EmotionDetectionState.Success -> {
                            Box {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Detected Emotion",
                                        color = EmotionTheme.colors.textPrimary,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = state.dominantEmotion.name,
                                        color = EmotionTheme.colors.interactive,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    EmotionButton(
                                        "Restart",
                                        onClick = {
//                                            isCameraRestarting = true
                                            capturedImagePath = null  // reset image
//                                            CoroutineScope(Dispatchers.Main).launch {
//                                                delay(2000) // Simulate restart delay
//                                                isCameraRestarting = false
//                                            }
                                            viewModel.resetState()    // reset detection result


                                        },
                                        modifier = Modifier,
                                        contentColor = EmotionTheme.colors.textPrimary,
                                        enabled = true
                                    )
                                }
                            }
                        }
                        else -> {
                            // Scan button
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .background(EmotionTheme.colors.interactive, CircleShape)
                                    .clickable {
                                        println("Scan button clicked") // Debug print
                                        val captureInstance = imageCapture

                                        if (captureInstance == null) {
                                            println("ERROR: imageCapture is null")
                                            return@clickable
                                        }
                                        println("ImageCapture instance retrieved: $captureInstance")

                                        try {
                                            val photoFile = createTempFile(context.cacheDir)
                                            println("Created temp file: ${photoFile.absolutePath}")

                                            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                                            captureInstance.takePicture(
                                                outputOptions,
                                                ContextCompat.getMainExecutor(context),
                                                object : ImageCapture.OnImageSavedCallback {
                                                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                                        println("Image saved successfully at: ${outputFileResults.savedUri ?: photoFile.absolutePath}")

                                                        try {
                                                            val bytes = photoFile.readBytes()
                                                            viewModel.detectEmotion(bytes, 0, 0)
                                                            capturedImagePath = photoFile.absolutePath  // store image path
                                                            scansRemaining.value -= 1
                                                        } catch (e: Exception) {
                                                            println("Error reading file: ${e.message}")
                                                            e.printStackTrace()
                                                        }
                                                    }

                                                    override fun onError(exception: ImageCaptureException) {
                                                        println("Error capturing image: ${exception.message}")
                                                        exception.printStackTrace()
                                                    }
                                                }
                                            )
                                        } catch (e: Exception) {
                                            println("Error in takePicture process: ${e.message}")
                                            e.printStackTrace()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "SCAN",
                                    color = EmotionTheme.colors.textPrimary,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "${scansRemaining.value}/5 scans remaining today",
                        color = EmotionTheme.colors.textPrimary,
                        fontSize = 16.sp
                    )
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
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                println("Creating image capture instance in CameraPreview")
                onImageCaptureCreated(imageCapture)

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_FRONT_CAMERA,
                        preview,
                        imageCapture
                    )
                    println("Camera bound to lifecycle successfully")
                } catch (e: Exception) {
                    println("Error binding camera use cases: ${e.message}")
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                println("Error getting camera provider: ${e.message}")
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            println("Disposing camera executor")
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    )
}

fun createTempFile(cacheDir: File): File {
    return File.createTempFile("emotion_image_", ".jpg", cacheDir).apply {
        deleteOnExit() // Ensure temp files are cleaned up
    }
}



