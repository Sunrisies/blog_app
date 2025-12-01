package com.sunrise.blog.media

import android.content.Context
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import android.util.Log
import android.view.Gravity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

class GStreamerPlayer(private val context: Context) : SurfaceHolder.Callback {
    private var surface: Surface? = null
    private var isInitialized = false
    private var currentUri: String? = null
    
    companion object {
        private const val TAG = "GStreamerPlayer"
        
        // 加载 GStreamer 库
        init {
            try {
                System.loadLibrary("gstreamer_android")
                Log.d(TAG, "GStreamer library loaded successfully")
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Failed to load GStreamer library", e)
            }
        }
    }
    
    fun initialize() {
        if (isInitialized) return
        
        try {
            // 初始化 GStreamer
            // 注意：这需要在实际应用中正确实现
            // GStreamer.init(context)
            isInitialized = true
            Log.d(TAG, "GStreamer initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize GStreamer", e)
        }
    }
    
    fun setUri(uri: String) {
        if (!isInitialized) {
            initialize()
        }
        
        try {
            currentUri = uri
            Log.d(TAG, "URI set: $uri")
            // 在实际应用中，这里需要设置 GStreamer pipeline 的 URI
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set URI: $uri", e)
        }
    }
    
    fun play() {
        if (!isInitialized) return
        
        try {
            // 在实际应用中，这里需要启动 GStreamer pipeline
            Log.d(TAG, "Playback started for URI: $currentUri")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start playback", e)
        }
    }
    
    fun pause() {
        if (!isInitialized) return
        
        try {
            // 在实际应用中，这里需要暂停 GStreamer pipeline
            Log.d(TAG, "Playback paused")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pause playback", e)
        }
    }
    
    fun stop() {
        if (!isInitialized) return
        
        try {
            // 在实际应用中，这里需要停止 GStreamer pipeline
            Log.d(TAG, "Playback stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop playback", e)
        }
    }
    
    fun setSurface(surface: Surface) {
        this.surface = surface
        Log.d(TAG, "Surface set")
        // 在实际应用中，这里需要将 surface 设置给 GStreamer pipeline
    }
    
    override fun surfaceCreated(holder: SurfaceHolder) {
        setSurface(holder.surface)
    }
    
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Handle surface changes if needed
        Log.d(TAG, "Surface changed: ${width}x${height}")
    }
    
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        surface = null
        Log.d(TAG, "Surface destroyed")
    }
    
    fun cleanup() {
        try {
            stop()
            surface = null
            Log.d(TAG, "Player cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
    
    fun isPlaying(): Boolean {
        // 在实际应用中，这里需要返回 GStreamer pipeline 的状态
        return false
    }
}

@Composable
fun GStreamerPlayerView(
    modifier: Modifier = Modifier,
    onPlayerReady: (GStreamerPlayer) -> Unit = {}
) {
    val context = LocalContext.current
    var gstreamerPlayer by remember { mutableStateOf<GStreamerPlayer?>(null) }
    
    DisposableEffect(Unit) {
        val player = GStreamerPlayer(context)
        gstreamerPlayer = player
        onPlayerReady(player)
        
        onDispose {
            player.cleanup()
        }
    }
    
    AndroidView(
        factory = { ctx ->
            val surfaceView = SurfaceView(ctx)
            val layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            layoutParams.gravity = Gravity.CENTER
            surfaceView.layoutParams = layoutParams
            
            // Set up surface holder callback
            gstreamerPlayer?.let { player ->
                surfaceView.holder.addCallback(player)
            }
            
            surfaceView
        },
        modifier = modifier.fillMaxSize()
    )
}