package com.example

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Fully edge-to-edge layout integration
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                var webViewInstance by remember { mutableStateOf<WebView?>(null) }
                
                // Native android back-press handling inside Jetpack Compose
                BackHandler(enabled = webViewInstance?.canGoBack() == true) {
                    webViewInstance?.goBack()
                }
                
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        PortfolioWebViewContainer(
                            onWebViewCreated = { webView ->
                                webViewInstance = webView
                            }
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PortfolioWebViewContainer(
    onWebViewCreated: (WebView) -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                // Configure standard clients
                webViewClient = WebViewClient()
                webChromeClient = WebChromeClient()
                
                // Configure WebSettings for interactive JS portfolio operations
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    allowFileAccess = true
                    allowContentAccess = true
                    databaseEnabled = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                    mediaPlaybackRequiresUserGesture = false
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    
                    // Enable multi-touch zoom if required, but keep clean default scaling
                    builtInZoomControls = false
                    displayZoomControls = false
                }
                
                // Hardware layer optimization
                setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                
                // Expose Android bridge for Gemini API key access
                addJavascriptInterface(object {
                    @android.webkit.JavascriptInterface
                    fun getApiKey(): String {
                        return try {
                            val key = BuildConfig.GEMINI_API_KEY
                            if (key == "MY_GEMINI_API_KEY" || key.isBlank()) "" else key
                        } catch (e: Exception) {
                            ""
                        }
                    }
                }, "AndroidBridge")

                // Load local portfolio asset
                loadUrl("file:///android_asset/index.html")
                
                // Emit web view reference up
                onWebViewCreated(this)
            }
        },
        modifier = modifier.fillMaxSize()
    )
}
