package com.oboco.magiccart

import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * Chrome extension background service worker
 * Handles extension lifecycle and cross-tab communication
 */
fun main() {
    setupBackgroundHandlers()
}

private fun setupBackgroundHandlers() {
    console.log("MagicCart: Background service worker initialized")
    
    // Handle extension installation
    chrome.runtime.onInstalled.addListener { details ->
        console.log("MagicCart: Extension installed/updated", details.reason)
        
        when (details.reason) {
            "install" -> {
                // Set default settings on first install
                chrome.storage.local.set(js("""({
                    "settings": {
                        "apiEndpoint": "http://localhost:8080/api",
                        "autoDetectProducts": true,
                        "showNotifications": true
                    }
                })"""))
            }
            "update" -> {
                // Handle extension updates
                console.log("Extension updated to version ${chrome.runtime.getManifest().version}")
            }
        }
    }
    
    // Handle messages from content scripts
    chrome.runtime.onMessage.addListener { message, sender, sendResponse ->
        console.log("Background: Received message", message, "from", sender.tab?.url)
        
        when (message.type) {
            "PRODUCT_DETECTED" -> {
                handleProductDetected(message.productInfo, sender)
            }
            "START_BIDDING_SESSION" -> {
                handleStartBiddingSession(message, sendResponse)
            }
            "GET_SETTINGS" -> {
                handleGetSettings(sendResponse)
            }
            "UPDATE_SETTINGS" -> {
                handleUpdateSettings(message.settings, sendResponse)
            }
        }
        
        // Return true to indicate async response
        true
    }
    
    // Handle tab updates to detect navigation to product pages
    chrome.tabs.onUpdated.addListener { tabId, changeInfo, tab ->
        if (changeInfo.status == "complete" && tab.url != null) {
            checkForProductPage(tab.url!!, tabId)
        }
    }
}

private fun handleProductDetected(productInfo: dynamic, sender: dynamic) {
    console.log("Background: Product detected", productInfo)
    
    // Store product info for potential use by popup
    chrome.storage.local.set(js("""({
        "lastDetectedProduct": $productInfo,
        "lastDetectedTab": ${sender.tab?.id}
    })"""))
    
    // Optionally show notification
    chrome.storage.local.get(js("['settings']")) { result ->
        val settings = result.settings as? dynamic
        if (settings?.showNotifications == true) {
            showProductDetectedNotification(productInfo)
        }
    }
}

private fun handleStartBiddingSession(message: dynamic, sendResponse: (dynamic) -> Unit) {
    val scope = MainScope()
    scope.launch {
        try {
            // This would typically involve API calls to backend
            console.log("Background: Starting bidding session for", message.productInfo)
            
            sendResponse(js("""({
                "success": true,
                "sessionId": "${generateSessionId()}"
            })"""))
        } catch (e: Exception) {
            sendResponse(js("""({
                "success": false,
                "error": "${e.message}"
            })"""))
        }
    }
}

private fun handleGetSettings(sendResponse: (dynamic) -> Unit) {
    chrome.storage.local.get(js("['settings']")) { result ->
        sendResponse(result.settings ?: js("""({
            "apiEndpoint": "http://localhost:8080/api",
            "autoDetectProducts": true,
            "showNotifications": true
        })"""))
    }
}

private fun handleUpdateSettings(settings: dynamic, sendResponse: (dynamic) -> Unit) {
    chrome.storage.local.set(js("""({ "settings": $settings })""")) {
        sendResponse(js("""({ "success": true })"""))
    }
}

private fun checkForProductPage(url: String, tabId: Int) {
    val supportedSites = arrayOf(
        "amazon.com", "bestbuy.com", "walmart.com", "target.com", "ebay.com"
    )
    
    val isProductPage = supportedSites.any { site ->
        url.contains(site) && (
            url.contains("/dp/") ||  // Amazon
            url.contains("/product/") ||  // Generic
            url.contains("/p/") ||  // Target/Walmart
            url.contains("/itm/")  // eBay
        )
    }
    
    if (isProductPage) {
        console.log("Background: Potential product page detected:", url)
        // Could inject content script here if needed
    }
}

private fun showProductDetectedNotification(productInfo: dynamic) {
    chrome.notifications.create(js("""({
        "type": "basic",
        "iconUrl": "icons/icon48.png",
        "title": "MagicCart - Product Detected",
        "message": "Found ${productInfo.name} - Click to start negotiation"
    })"""))
}

private fun generateSessionId(): String {
    return "session-${Date.now()}-${(Math.random() * 1000).toInt()}"
}

// Chrome API declarations
@JsName("chrome")
external val chrome: Chrome

external interface Chrome {
    val runtime: Runtime
    val storage: Storage
    val tabs: Tabs
    val notifications: Notifications
}

external interface Runtime {
    val onInstalled: OnInstalled
    val onMessage: OnMessage
    fun getManifest(): Manifest
}

external interface OnInstalled {
    fun addListener(callback: (InstallDetails) -> Unit)
}

external interface OnMessage {
    fun addListener(callback: (message: dynamic, sender: MessageSender, sendResponse: (dynamic) -> Unit) -> Boolean)
}

external interface InstallDetails {
    val reason: String
}

external interface MessageSender {
    val tab: Tab?
}

external interface Tab {
    val id: Int
    val url: String?
}

external interface Manifest {
    val version: String
}

external interface Storage {
    val local: LocalStorage
}

external interface LocalStorage {
    fun set(items: dynamic, callback: (() -> Unit)? = definedExternally)
    fun get(keys: dynamic, callback: (result: dynamic) -> Unit)
}

external interface Tabs {
    val onUpdated: TabsOnUpdated
}

external interface TabsOnUpdated {
    fun addListener(callback: (tabId: Int, changeInfo: dynamic, tab: dynamic) -> Unit)
}

external interface Notifications {
    fun create(options: dynamic)
}