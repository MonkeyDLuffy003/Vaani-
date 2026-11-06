package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * High-performance HTML, CSS3 & JavaScript animated Vyshu AI Stage.
 * Features Vyshu in her red beret and gradient pleated skirt,
 * with rhythmic beat swaying, breathing physics, floating musical heart sparkles,
 * neon ambient glow, and bidirectional JavaScript Android bridge.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun VyshuWebAnimationStage(
    isPlaying: Boolean,
    bpm: Int,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp,
    onVyshuClick: () -> Unit
) {
    val context = LocalContext.current

    val webView = remember(context) {
        WebView(context).apply {
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setLayerType(WebView.LAYER_TYPE_HARDWARE, null)
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowContentAccess = true
                allowFileAccess = true
                cacheMode = WebSettings.LOAD_DEFAULT
            }
            webViewClient = WebViewClient()
            addJavascriptInterface(VyshuJsBridge(onVyshuClick), "AndroidBridge")
            loadDataWithBaseURL("file:///android_asset/", generateVyshuHtml(), "text/html", "UTF-8", null)
        }
    }

    LaunchedEffect(isPlaying, bpm) {
        val js = "if (window.updateVyshuState) { window.updateVyshuState($isPlaying, $bpm); }"
        webView.evaluateJavascript(js, null)
    }

    DisposableEffect(Unit) {
        onDispose {
            webView.destroy()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF101322))
            .border(1.dp, Color(0xFF2E244E), RoundedCornerShape(20.dp))
    ) {
        AndroidView(
            factory = { webView },
            modifier = Modifier.fillMaxWidth().height(height)
        )
    }
}

class VyshuJsBridge(private val onVyshuClick: () -> Unit) {
    @JavascriptInterface
    fun onVyshuTapped() {
        onVyshuClick()
    }
}

/**
 * HTML5, CSS3 Keyframes & JavaScript for Vyshu AI Animation.
 */
private fun generateVyshuHtml(): String = """
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
<style>
  * { margin: 0; padding: 0; box-sizing: border-box; user-select: none; -webkit-tap-highlight-color: transparent; }
  body {
    background: transparent;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100vh;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  }

  .stage-wrapper {
    position: relative;
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
  }

  /* Radiant Aura behind Vyshu */
  .vyshu-aura {
    position: absolute;
    width: 150px;
    height: 150px;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(168, 85, 247, 0.35) 0%, rgba(239, 68, 68, 0.2) 50%, transparent 75%);
    filter: blur(12px);
    opacity: 0.6;
    transition: opacity 0.5s ease;
    pointer-events: none;
    animation: auraFloat 3s infinite alternate ease-in-out;
  }

  .active-music .vyshu-aura {
    opacity: 1;
    animation: auraPulse var(--beat-duration, 0.5s) infinite alternate ease-in-out;
  }

  @keyframes auraFloat {
    0% { transform: scale(0.95); opacity: 0.5; }
    100% { transform: scale(1.1); opacity: 0.8; }
  }

  @keyframes auraPulse {
    0% { transform: scale(0.95); opacity: 0.65; }
    100% { transform: scale(1.25); opacity: 1; }
  }

  /* Vyshu Character Container */
  .vyshu-box {
    position: relative;
    height: 160px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transform-origin: bottom center;
    transition: transform 0.15s ease;
  }

  .vyshu-img {
    height: 100%;
    width: auto;
    object-fit: contain;
    filter: drop-shadow(0 6px 12px rgba(0, 0, 0, 0.45));
    border-radius: 12px;
    transform-origin: bottom center;
  }

  /* Idle Breathing Animation */
  .vyshu-idle {
    animation: vyshuBreathe 3s infinite alternate ease-in-out;
  }

  @keyframes vyshuBreathe {
    0% { transform: translateY(0px) scale(1, 1); }
    100% { transform: translateY(-4px) scale(1.01, 1.015); }
  }

  /* Beat-Synced Musical Groove Dance Animation */
  .vyshu-dancing {
    animation: vyshuGroove var(--beat-duration, 0.5s) infinite alternate cubic-bezier(0.4, 0, 0.2, 1);
  }

  @keyframes vyshuGroove {
    0% {
      transform: translateY(0px) rotate(-2.5deg) scale(1.02, 0.98);
      filter: drop-shadow(0 4px 10px rgba(168, 85, 247, 0.4));
    }
    50% {
      transform: translateY(-8px) rotate(0deg) scale(0.98, 1.02);
    }
    100% {
      transform: translateY(-14px) rotate(2.5deg) scale(1.01, 0.99);
      filter: drop-shadow(0 8px 18px rgba(239, 68, 68, 0.5));
    }
  }

  /* Floating Hearts & Musical Notes */
  .sparkle {
    position: absolute;
    font-size: 14px;
    pointer-events: none;
    opacity: 0;
  }

  .active-music .sparkle-1 {
    left: 22%;
    top: 25%;
    animation: floatParticle 2.2s infinite ease-out;
  }

  .active-music .sparkle-2 {
    right: 22%;
    top: 35%;
    animation: floatParticle 2.6s infinite ease-out 0.8s;
  }

  @keyframes floatParticle {
    0% { transform: translateY(0px) scale(0.6); opacity: 0; }
    40% { opacity: 0.9; }
    100% { transform: translateY(-38px) scale(1.1); opacity: 0; }
  }

  /* Neon Equalizer Floor */
  .stage-floor {
    display: flex;
    gap: 4px;
    align-items: flex-end;
    height: 12px;
    margin-top: 4px;
  }

  .floor-bar {
    width: 4px;
    background: #a855f7;
    border-radius: 2px;
    height: 3px;
    transition: height 0.1s ease;
  }

  .active-music .floor-bar {
    animation: barPulse 0.45s infinite alternate ease-in-out;
  }

  @keyframes barPulse {
    0% { height: 3px; }
    100% { height: 12px; }
  }
</style>
</head>
<body>
<div class="stage-wrapper" id="stage">
  <div class="vyshu-aura"></div>

  <span class="sparkle sparkle-1">💜</span>
  <span class="sparkle sparkle-2">✨</span>

  <div class="vyshu-box vyshu-idle" id="vyshuBox" onclick="handleTap()">
    <img src="vyshu_character.jpg" class="vyshu-img" id="vyshuImg" alt="Vyshu AI" />
  </div>

  <div class="stage-floor" id="floor">
    <div class="floor-bar" style="background: #a855f7; animation-delay: 0.05s;"></div>
    <div class="floor-bar" style="background: #c084fc; animation-delay: 0.15s;"></div>
    <div class="floor-bar" style="background: #ec4899; animation-delay: 0.25s;"></div>
    <div class="floor-bar" style="background: #f43f5e; animation-delay: 0.10s;"></div>
    <div class="floor-bar" style="background: #a855f7; animation-delay: 0.20s;"></div>
    <div class="floor-bar" style="background: #c084fc; animation-delay: 0.30s;"></div>
  </div>
</div>

<script>
  function handleTap() {
    var box = document.getElementById('vyshuBox');
    box.style.transform = 'scale(0.92)';
    setTimeout(function() {
      box.style.transform = '';
    }, 150);

    if (window.AndroidBridge && window.AndroidBridge.onVyshuTapped) {
      window.AndroidBridge.onVyshuTapped();
    }
  }

  window.updateVyshuState = function(isPlaying, bpm) {
    var stage = document.getElementById('stage');
    var box = document.getElementById('vyshuBox');

    var beatSec = (60 / Math.max(50, Math.min(bpm, 220))).toFixed(2);
    document.documentElement.style.setProperty('--beat-duration', beatSec + 's');

    if (isPlaying) {
      stage.className = 'stage-wrapper active-music';
      box.className = 'vyshu-box vyshu-dancing';
    } else {
      stage.className = 'stage-wrapper';
      box.className = 'vyshu-box vyshu-idle';
    }
  };
</script>
</body>
</html>
""".trimIndent()
