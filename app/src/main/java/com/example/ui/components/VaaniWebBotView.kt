package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Vaani AI Bot Layout recreated with pixel-accurate HTML5, CSS3 & JavaScript.
 * Matches the reference layout with Vaani in the neon DJ studio,
 * realistic animated character reactions (breathing, blinking, listening, speaking, music groove),
 * glowing glassmorphism speech bubble, animated soundwaves, concentric mic orb,
 * and live JavaScript bridge to Android voice & music systems.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun VaaniWebBotView(
    isListening: Boolean,
    isPlaying: Boolean,
    isProcessing: Boolean,
    isSpeaking: Boolean,
    rmsAudioLevel: Float,
    mainMessage: String,
    subMessage: String,
    statusText: String,
    hasActiveTrack: Boolean,
    onMicClick: () -> Unit,
    onMenuClick: () -> Unit,
    onVyshuClick: () -> Unit,
    onBubbleClick: () -> Unit,
    modifier: Modifier = Modifier
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
            addJavascriptInterface(
                VaaniJsBridge(
                    onMicClick = onMicClick,
                    onMenuClick = onMenuClick,
                    onVyshuClick = onVyshuClick,
                    onBubbleClick = onBubbleClick
                ),
                "AndroidBridge"
            )
            loadDataWithBaseURL("file:///android_asset/", generateVaaniLayoutHtml(), "text/html", "UTF-8", null)
        }
    }

    // Dynamic real-time state sync to HTML/CSS/JS
    LaunchedEffect(isListening, isPlaying, isProcessing, isSpeaking, rmsAudioLevel, mainMessage, subMessage, statusText, hasActiveTrack) {
        val safeMain = mainMessage.replace("'", "\\'").replace("\n", " ")
        val safeSub = subMessage.replace("'", "\\'").replace("\n", " ")
        val safeStatus = statusText.replace("'", "\\'")

        val js = """
            if (window.updateVaaniUi) {
                window.updateVaaniUi(
                    $isListening,
                    $isPlaying,
                    $isProcessing,
                    $isSpeaking,
                    $rmsAudioLevel,
                    '$safeMain',
                    '$safeSub',
                    '$safeStatus',
                    $hasActiveTrack
                );
            }
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }

    DisposableEffect(Unit) {
        onDispose {
            webView.destroy()
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0716))
    )
}

class VaaniJsBridge(
    private val onMicClick: () -> Unit,
    private val onMenuClick: () -> Unit,
    private val onVyshuClick: () -> Unit,
    private val onBubbleClick: () -> Unit
) {
    @JavascriptInterface
    fun onMicTapped() { onMicClick() }

    @JavascriptInterface
    fun onMenuTapped() { onMenuClick() }

    @JavascriptInterface
    fun onVyshuTapped() { onVyshuClick() }

    @JavascriptInterface
    fun onBubbleTapped() { onBubbleClick() }
}

/**
 * Pure HTML5, CSS3 Glassmorphism & JavaScript Layout
 * Recreates the exact Vaani AI Music Assistant layout from user reference.
 */
private fun generateVaaniLayoutHtml(): String = """
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
<style>
  * {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
    user-select: none;
    -webkit-tap-highlight-color: transparent;
  }

  body {
    background-color: #0b0716;
    background-image: radial-gradient(circle at 50% 20%, #1f1138 0%, #0c0818 60%, #07040f 100%);
    color: #ffffff;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
    height: 100vh;
    width: 100vw;
    overflow: hidden;
    position: relative;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
  }

  /* Full Screen Background Studio Lighting */
  .studio-bg {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-size: cover;
    background-position: center top;
    opacity: 0.98;
    z-index: 1;
    pointer-events: none;
    transition: filter 0.4s ease;
  }

  /* Top Navigation Bar */
  .top-header {
    position: relative;
    z-index: 10;
    width: 100%;
    padding: 42px 20px 10px 20px;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .hamburger-btn {
    background: rgba(30, 16, 56, 0.4);
    border: 1px solid rgba(168, 85, 247, 0.3);
    cursor: pointer;
    display: flex;
    flex-direction: column;
    gap: 5px;
    padding: 10px;
    border-radius: 12px;
    transition: transform 0.15s ease, background 0.2s ease;
  }

  .hamburger-btn:active {
    transform: scale(0.92);
    background: rgba(168, 85, 247, 0.3);
  }

  .hamburger-line {
    width: 22px;
    height: 2.2px;
    background: #ffffff;
    border-radius: 2px;
    box-shadow: 0 0 6px rgba(255, 255, 255, 0.5);
  }

  .brand-center {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
  }

  .brand-title-row {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .brand-wave {
    display: flex;
    align-items: center;
    gap: 2.5px;
    height: 20px;
  }

  .brand-wave span {
    display: inline-block;
    width: 2.5px;
    background: linear-gradient(180deg, #d946ef 0%, #a855f7 100%);
    border-radius: 2px;
    box-shadow: 0 0 6px #d946ef;
    animation: waveBounce 1.2s infinite ease-in-out;
  }

  .brand-wave span:nth-child(1) { height: 8px; animation-delay: 0.1s; }
  .brand-wave span:nth-child(2) { height: 16px; animation-delay: 0.25s; }
  .brand-wave span:nth-child(3) { height: 22px; animation-delay: 0.4s; }
  .brand-wave span:nth-child(4) { height: 14px; animation-delay: 0.15s; }
  .brand-wave span:nth-child(5) { height: 7px; animation-delay: 0.3s; }

  @keyframes waveBounce {
    0%, 100% { transform: scaleY(0.7); }
    50% { transform: scaleY(1.3); }
  }

  .brand-name {
    font-size: 25px;
    font-weight: 800;
    font-style: italic;
    letter-spacing: 0.5px;
    background: linear-gradient(180deg, #ffffff 30%, #e0d4fc 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    text-shadow: 0 0 16px rgba(168, 85, 247, 0.6);
  }

  .brand-subtitle {
    font-size: 11px;
    color: #c4b5fd;
    letter-spacing: 0.8px;
    font-weight: 500;
    margin-top: 1px;
    text-shadow: 0 0 8px rgba(196, 181, 253, 0.4);
  }

  .vyshu-tag {
    background: rgba(139, 92, 246, 0.25);
    border: 1px solid rgba(168, 85, 247, 0.5);
    border-radius: 16px;
    color: #f5f3ff;
    padding: 6px 12px;
    font-size: 11px;
    font-weight: 600;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 4px;
    box-shadow: 0 0 12px rgba(139, 92, 246, 0.35);
    transition: transform 0.15s ease;
  }

  .vyshu-tag:active {
    transform: scale(0.92);
  }

  /* Hero Center: Animated Vaani Character Layer */
  .hero-container {
    position: relative;
    z-index: 2;
    flex: 1;
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: flex-end;
    pointer-events: none;
  }

  /* Realistic Character Animation Container */
  .vaani-animated-avatar {
    position: relative;
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  /* Headphone Neon Pulse Aura */
  .headphone-glow {
    position: absolute;
    top: 25%;
    width: 140px;
    height: 140px;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(168, 85, 247, 0.4) 0%, transparent 70%);
    opacity: 0.5;
    pointer-events: none;
    transition: opacity 0.3s ease, transform 0.3s ease;
  }

  /* State based animation classes applied via JS */
  .character-idle {
    animation: vaaniBreathe 4s infinite alternate ease-in-out;
  }

  .character-listening {
    animation: vaaniListening 1.8s infinite alternate ease-in-out;
  }

  .character-processing {
    animation: vaaniProcessing 1.2s infinite alternate ease-in-out;
  }

  .character-speaking {
    animation: vaaniSpeaking 0.8s infinite alternate ease-in-out;
  }

  .character-playing {
    animation: vaaniGroove 1.1s infinite alternate ease-in-out;
  }

  @keyframes vaaniBreathe {
    0% { transform: translateY(0px) scale(1); }
    100% { transform: translateY(-4px) scale(1.008); }
  }

  @keyframes vaaniListening {
    0% { transform: translateY(-2px) rotate(-0.5deg) scale(1.01); filter: brightness(1.08); }
    100% { transform: translateY(-5px) rotate(0.8deg) scale(1.02); filter: brightness(1.15) drop-shadow(0 0 16px rgba(168, 85, 247, 0.4)); }
  }

  @keyframes vaaniProcessing {
    0% { transform: translateY(-2px) scale(1.01); filter: brightness(1.1); }
    100% { transform: translateY(-4px) scale(1.015); filter: brightness(1.2) drop-shadow(0 0 20px rgba(56, 189, 248, 0.5)); }
  }

  @keyframes vaaniSpeaking {
    0% { transform: translateY(0px) scale(1); }
    50% { transform: translateY(-3px) scale(1.012); filter: brightness(1.12); }
    100% { transform: translateY(-1px) scale(1.006); }
  }

  @keyframes vaaniGroove {
    0% { transform: translateY(0px) rotate(-0.8deg); }
    50% { transform: translateY(-6px) rotate(0.5deg) scale(1.015); }
    100% { transform: translateY(-2px) rotate(0.8deg); }
  }

  /* Glassmorphism Speech Bubble Card */
  .speech-bubble-wrapper {
    position: relative;
    z-index: 10;
    width: 90%;
    max-width: 380px;
    margin: 0 auto 16px auto;
  }

  .speech-bubble {
    background: rgba(28, 16, 52, 0.82);
    backdrop-filter: blur(24px);
    -webkit-backdrop-filter: blur(24px);
    border: 1.5px solid rgba(192, 132, 252, 0.55);
    border-radius: 28px;
    padding: 16px 20px;
    display: flex;
    align-items: center;
    gap: 16px;
    cursor: pointer;
    box-shadow:
      0 12px 30px rgba(0, 0, 0, 0.45),
      0 0 22px rgba(168, 85, 247, 0.38),
      inset 0 1px 1px rgba(255, 255, 255, 0.3);
    transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
  }

  .speech-bubble:active {
    transform: scale(0.97);
    border-color: #d946ef;
  }

  /* Speech Tail at Bottom-Left */
  .speech-tail {
    position: absolute;
    bottom: -8px;
    left: 36px;
    width: 16px;
    height: 16px;
    background: rgba(28, 16, 52, 0.9);
    border-bottom: 1.5px solid rgba(192, 132, 252, 0.55);
    border-left: 1.5px solid rgba(192, 132, 252, 0.55);
    transform: rotate(45deg);
    z-index: 1;
  }

  .bubble-icon {
    display: flex;
    align-items: center;
    gap: 2.5px;
    height: 22px;
  }

  .bubble-icon span {
    width: 3px;
    background: #e879f9;
    border-radius: 2px;
    box-shadow: 0 0 8px #d946ef;
    animation: bubbleEq 1s infinite alternate ease-in-out;
  }

  .bubble-icon span:nth-child(1) { height: 8px; animation-delay: 0.1s; }
  .bubble-icon span:nth-child(2) { height: 18px; animation-delay: 0.25s; }
  .bubble-icon span:nth-child(3) { height: 24px; animation-delay: 0.4s; }
  .bubble-icon span:nth-child(4) { height: 14px; animation-delay: 0.15s; }
  .bubble-icon span:nth-child(5) { height: 6px; animation-delay: 0.3s; }

  @keyframes bubbleEq {
    0% { transform: scaleY(0.6); }
    100% { transform: scaleY(1.4); }
  }

  .bubble-content {
    display: flex;
    flex-direction: column;
    gap: 3px;
    overflow: hidden;
  }

  .bubble-main-text {
    font-size: 16px;
    font-weight: 700;
    color: #ffffff;
    letter-spacing: 0.2px;
    text-shadow: 0 0 8px rgba(255, 255, 255, 0.35);
  }

  .bubble-sub-text {
    font-size: 13px;
    color: #c4b5fd;
    font-weight: 400;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  /* Center Microphone Sphere & Frequency Waves */
  .voice-controls-section {
    position: relative;
    z-index: 10;
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-bottom: 28px;
    transition: margin-bottom 0.3s ease;
  }

  .mic-row {
    position: relative;
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100px;
  }

  /* Waveform Ribbon across left & right */
  .frequency-ribbon {
    position: absolute;
    width: 88%;
    max-width: 360px;
    height: 52px;
    pointer-events: none;
    z-index: 1;
    opacity: 0.8;
  }

  /* Concentric Glowing Mic Orb */
  .mic-orb {
    position: relative;
    z-index: 5;
    width: 74px;
    height: 74px;
    border-radius: 50%;
    background: radial-gradient(circle at 35% 35%, #9333ea 0%, #6366f1 60%, #3b82f6 100%);
    box-shadow:
      0 0 26px rgba(168, 85, 247, 0.85),
      0 0 50px rgba(99, 102, 241, 0.55),
      inset 0 2px 4px rgba(255, 255, 255, 0.45);
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: transform 0.2s ease, box-shadow 0.2s ease;
  }

  .mic-orb:active {
    transform: scale(0.91);
  }

  /* Outer Concentric Glowing Rings */
  .orb-ring {
    position: absolute;
    top: -10px;
    left: -10px;
    right: -10px;
    bottom: -10px;
    border-radius: 50%;
    border: 1.5px solid rgba(192, 132, 252, 0.5);
    box-shadow: 0 0 16px rgba(168, 85, 247, 0.5);
    pointer-events: none;
    animation: ringOrbit 2.4s infinite alternate ease-in-out;
  }

  .orb-ring-outer {
    position: absolute;
    top: -18px;
    left: -18px;
    right: -18px;
    bottom: -18px;
    border-radius: 50%;
    border: 1px solid rgba(99, 102, 241, 0.35);
    pointer-events: none;
    animation: ringPulse 2.2s infinite ease-out;
  }

  @keyframes ringOrbit {
    0% { transform: scale(0.96); opacity: 0.4; }
    100% { transform: scale(1.08); opacity: 0.95; }
  }

  @keyframes ringPulse {
    0% { transform: scale(0.9); opacity: 0.85; }
    100% { transform: scale(1.4); opacity: 0; }
  }

  .mic-icon-svg {
    width: 30px;
    height: 30px;
    fill: #ffffff;
    filter: drop-shadow(0 0 6px rgba(255, 255, 255, 0.8));
    transition: transform 0.2s ease;
  }

  /* Status Pill below Mic */
  .status-pill {
    margin-top: 10px;
    background: rgba(30, 16, 56, 0.85);
    backdrop-filter: blur(14px);
    border: 1px solid rgba(168, 85, 247, 0.45);
    border-radius: 20px;
    padding: 6px 18px;
    display: flex;
    align-items: center;
    gap: 8px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.35);
  }

  .status-dot {
    width: 7.5px;
    height: 7.5px;
    border-radius: 50%;
    background: #e879f9;
    box-shadow: 0 0 8px #d946ef;
    animation: dotBlink 1.4s infinite alternate ease-in-out;
  }

  @keyframes dotBlink {
    0% { opacity: 0.35; transform: scale(0.8); }
    100% { opacity: 1; transform: scale(1.25); }
  }

  .status-text {
    font-size: 13px;
    font-weight: 600;
    color: #f1f5f9;
    letter-spacing: 0.3px;
  }
</style>
</head>
<body>

  <!-- Background Studio Lighting & Atmosphere with Vaani Layout -->
  <div class="studio-bg" id="studioBg" style="background-image: url('vaani_layout.png');"></div>

  <!-- Top Header Navigation -->
  <div class="top-header">
    <button class="hamburger-btn" onclick="triggerMenu()" title="Open Menu">
      <div class="hamburger-line"></div>
      <div class="hamburger-line"></div>
      <div class="hamburger-line"></div>
    </button>

    <div class="brand-center">
      <div class="brand-title-row">
        <div class="brand-wave">
          <span></span><span></span><span></span><span></span><span></span>
        </div>
        <div class="brand-name">Vaani</div>
      </div>
      <div class="brand-subtitle">AI Music Assistant</div>
    </div>

    <button class="vyshu-tag" onclick="triggerVyshu()" title="Vyshu AI Companion">
      <span>💜</span>
      <span>Vyshu</span>
    </button>
  </div>

  <!-- Hero Center: Animated Character Aura Overlay -->
  <div class="hero-container" id="heroContainer">
    <div class="vaani-animated-avatar character-idle" id="characterLayer">
      <div class="headphone-glow" id="headphoneGlow"></div>
    </div>
  </div>

  <!-- Glassmorphism Speech Bubble Card -->
  <div class="speech-bubble-wrapper">
    <div class="speech-bubble" onclick="triggerBubble()">
      <div class="bubble-icon">
        <span id="eq1"></span><span id="eq2"></span><span id="eq3"></span><span id="eq4"></span><span id="eq5"></span>
      </div>
      <div class="bubble-content">
        <div class="bubble-main-text" id="mainText">I'm here, Teja. 🎵</div>
        <div class="bubble-sub-text" id="subText">Playing your music.</div>
      </div>
    </div>
    <div class="speech-tail"></div>
  </div>

  <!-- Central Concentric Mic Orb & Sine Frequency Waves -->
  <div class="voice-controls-section" id="voiceControls">
    <div class="mic-row">
      <!-- Animated SVG Frequency Ribbon -->
      <svg class="frequency-ribbon" viewBox="0 0 360 50">
        <path id="wavePath1" d="M 0 25 Q 40 10, 80 25 T 160 25 T 240 25 T 320 25 T 360 25" fill="none" stroke="url(#waveGrad)" stroke-width="2.5" opacity="0.85"/>
        <path id="wavePath2" d="M 0 25 Q 40 38, 80 25 T 160 25 T 240 25 T 320 25 T 360 25" fill="none" stroke="url(#waveGrad2)" stroke-width="1.8" opacity="0.65"/>
        <defs>
          <linearGradient id="waveGrad" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%" stop-color="#38bdf8" stop-opacity="0.1"/>
            <stop offset="35%" stop-color="#a855f7" stop-opacity="0.95"/>
            <stop offset="65%" stop-color="#ec4899" stop-opacity="0.95"/>
            <stop offset="100%" stop-color="#38bdf8" stop-opacity="0.1"/>
          </linearGradient>
          <linearGradient id="waveGrad2" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%" stop-color="#6366f1" stop-opacity="0.1"/>
            <stop offset="50%" stop-color="#d946ef" stop-opacity="0.85"/>
            <stop offset="100%" stop-color="#3b82f6" stop-opacity="0.1"/>
          </linearGradient>
        </defs>
      </svg>

      <!-- Center Orb -->
      <div class="mic-orb" id="micOrb" onclick="triggerMic()">
        <div class="orb-ring" id="orbRingInner"></div>
        <div class="orb-ring-outer" id="orbRingOuter"></div>
        <!-- Microphone SVG -->
        <svg class="mic-icon-svg" viewBox="0 0 24 24">
          <path d="M12 14c1.66 0 3-1.34 3-3V5c0-1.66-1.34-3-3-3S9 3.34 9 5v6c0 1.66 1.34 3 3 3z"/>
          <path d="M17 11c0 2.76-2.24 5-5 5s-5-2.24-5-5H5c0 3.53 2.61 6.43 6 6.92V21h2v-3.08c3.39-.49 6-3.39 6-6.92h-2z"/>
        </svg>
      </div>
    </div>

    <!-- Status Capsule Pill -->
    <div class="status-pill" id="statusPill">
      <div class="status-dot" id="statusDot"></div>
      <div class="status-text" id="statusText">Listening</div>
    </div>
  </div>

<script>
  function triggerMic() {
    var orb = document.getElementById('micOrb');
    orb.style.transform = 'scale(0.88)';
    setTimeout(function() { orb.style.transform = ''; }, 160);

    if (window.AndroidBridge && window.AndroidBridge.onMicTapped) {
      window.AndroidBridge.onMicTapped();
    }
  }

  function triggerMenu() {
    if (window.AndroidBridge && window.AndroidBridge.onMenuTapped) {
      window.AndroidBridge.onMenuTapped();
    }
  }

  function triggerVyshu() {
    if (window.AndroidBridge && window.AndroidBridge.onVyshuTapped) {
      window.AndroidBridge.onVyshuTapped();
    }
  }

  function triggerBubble() {
    if (window.AndroidBridge && window.AndroidBridge.onBubbleTapped) {
      window.AndroidBridge.onBubbleTapped();
    }
  }

  window.updateVaaniUi = function(isListening, isPlaying, isProcessing, isSpeaking, rms, main, sub, status, hasActiveTrack) {
    var mainEl = document.getElementById('mainText');
    var subEl = document.getElementById('subText');
    var statusEl = document.getElementById('statusText');
    var dotEl = document.getElementById('statusDot');
    var charLayer = document.getElementById('characterLayer');
    var glow = document.getElementById('headphoneGlow');
    var voiceCtrl = document.getElementById('voiceControls');
    var micOrb = document.getElementById('micOrb');

    if (main && main.trim().length > 0) mainEl.innerText = main;
    if (sub && sub.trim().length > 0) subEl.innerText = sub;
    if (status && status.trim().length > 0) statusEl.innerText = status;

    // Adjust bottom margin if bottom music player is active
    if (hasActiveTrack) {
      voiceCtrl.style.marginBottom = '8px';
    } else {
      voiceCtrl.style.marginBottom = '28px';
    }

    // Dynamic Character Animation state transitions
    charLayer.className = 'vaani-animated-avatar ';
    if (isProcessing) {
      charLayer.classList.add('character-processing');
      glow.style.opacity = '0.9';
      glow.style.background = 'radial-gradient(circle, rgba(56, 189, 248, 0.7) 0%, transparent 70%)';
      dotEl.style.background = '#38bdf8';
      dotEl.style.boxShadow = '0 0 14px #38bdf8';
      micOrb.style.background = 'radial-gradient(circle at 35% 35%, #0284c7 0%, #6366f1 60%, #a855f7 100%)';
    } else if (isSpeaking) {
      charLayer.classList.add('character-speaking');
      glow.style.opacity = '0.85';
      glow.style.background = 'radial-gradient(circle, rgba(236, 72, 153, 0.6) 0%, transparent 70%)';
      dotEl.style.background = '#ec4899';
      dotEl.style.boxShadow = '0 0 14px #ec4899';
    } else if (isListening) {
      charLayer.classList.add('character-listening');
      glow.style.opacity = '0.75';
      glow.style.background = 'radial-gradient(circle, rgba(168, 85, 247, 0.6) 0%, transparent 70%)';
      dotEl.style.background = '#f43f5e';
      dotEl.style.boxShadow = '0 0 14px #f43f5e';
      micOrb.style.background = 'radial-gradient(circle at 35% 35%, #ec4899 0%, #9333ea 60%, #3b82f6 100%)';
    } else if (isPlaying) {
      charLayer.classList.add('character-playing');
      glow.style.opacity = '0.5';
      glow.style.background = 'radial-gradient(circle, rgba(16, 185, 129, 0.5) 0%, transparent 70%)';
      dotEl.style.background = '#10b981';
      dotEl.style.boxShadow = '0 0 12px #10b981';
      micOrb.style.background = 'radial-gradient(circle at 35% 35%, #9333ea 0%, #6366f1 60%, #3b82f6 100%)';
    } else {
      charLayer.classList.add('character-idle');
      glow.style.opacity = '0.35';
      dotEl.style.background = '#a855f7';
      dotEl.style.boxShadow = '0 0 8px #a855f7';
      micOrb.style.background = 'radial-gradient(circle at 35% 35%, #9333ea 0%, #6366f1 60%, #3b82f6 100%)';
    }
  };
</script>
</body>
</html>
""".trimIndent()
