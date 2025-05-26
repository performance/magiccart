// extension/src/jsMain/kotlin/com/oboco/magiccart/background/Background.kt
package com.oboco.magiccart.background

fun main() {
    console.log("MagicCart Background Service Worker Loaded!")
    // Placeholder for chrome.runtime.onMessage.addListener if you want to test messaging later
    // js("chrome.runtime.onMessage.addListener(function(request, sender, sendResponse) { console.log('Message received in background:', request); return false; });")
}