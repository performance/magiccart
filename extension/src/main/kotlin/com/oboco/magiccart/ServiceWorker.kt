package com.oboco.magiccart

import com.oboco.magiccart.*
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.w3c.fetch.RequestInit
import kotlin.js.Promise

/**
 * Service Worker (background script) for MagicCart extension
 * Handles API calls and state persistence
 */

private val scope = MainScope()
private val json = Json { ignoreUnknownKeys = true }

// Extension API message handling
fun main() {
    // Listen for messages from content scripts
    chrome.runtime.onMessage.addListener { message, sender, sendResponse ->
        scope.launch {
            when (message.type) {
                "fetchBiddingRules" -> {
                    val productId = message.productId as String
                    val result = fetchBiddingRules(productId)
                    sendResponse(result)
                }
                "validateSelection" -> {
                    val request = message.request
                    val result = validateSelection(request)
                    sendResponse(result)
                }
                "saveState" -> {
                    val state = message.state
                    saveState(state)
                    sendResponse(js("{ success: true }"))
                }
                "loadState" -> {
                    val state = loadState()
                    sendResponse(state)
                }
            }
        }
        true // Keep the message channel open for async response
    }
    
    console.log("MagicCart Service Worker initialized")
}

private suspend fun fetchBiddingRules(productId: String): dynamic {
    return try {
        val response = window.fetch(
            "http://localhost:8080/api/bidding-rules?productId=$productId",
            RequestInit(
                method = "GET",
                headers = js("{ 'Content-Type': 'application/json' }")
            )
        ).await()
        
        if (response.ok) {
            val jsonText = response.text().await()
            val biddingRules = json.decodeFromString(BiddingRulesResponse.serializer(), jsonText)
            js("{ success: true, data: $biddingRules }")
        } else {
            js("{ success: false, error: 'HTTP ${response.status}' }")
        }
    } catch (e: Exception) {
        console.error("Error fetching bidding rules:", e)
        js("{ success: false, error: '${e.message}' }")
    }
}

private suspend fun validateSelection(request: dynamic): dynamic {
    return try {
        val response = window.fetch(
            "http://localhost:8080/api/validate-selection",
            RequestInit(
                method = "POST",
                headers = js("{ 'Content-Type': 'application/json' }"),
                body = JSON.stringify(request)
            )
        ).await()
        
        if (response.ok) {
            val jsonText = response.text().await()
            val validationResult = json.decodeFromString(SelectionValidationResponse.serializer(), jsonText)
            js("{ success: true, data: $validationResult }")
        } else {
            js("{ success: false, error: 'HTTP ${response.status}' }")
        }
    } catch (e: Exception) {
        console.error("Error validating selection:", e)
        js("{ success: false, error: '${e.message}' }")
    }
}

private fun saveState(state: dynamic) {
    chrome.storage.local.set(js("{ magicCartState: state }"))
}

private fun loadState(): Promise<dynamic> {
    return chrome.storage.local.get("magicCartState").then { result ->
        result.magicCartState ?: js("{}")
    }
}

// Chrome extension APIs (external declarations)
@JsModule("chrome")
external object chrome {
    object runtime {
        fun onMessage(callback: (dynamic, dynamic, (dynamic) -> Unit) -> Boolean): Unit
        val onMessage: OnMessageEvent
    }
    
    object storage {
        object local {
            fun set(items: dynamic): Promise<Unit>
            fun get(keys: dynamic): Promise<dynamic>
        }
    }
}

external interface OnMessageEvent {
    fun addListener(callback: (dynamic, dynamic, (dynamic) -> Unit) -> Boolean)
}