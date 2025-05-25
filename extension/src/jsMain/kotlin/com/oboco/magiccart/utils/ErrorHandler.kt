package com.oboco.magiccart.utils

import kotlinx.browser.window

/**
 * Centralized error handling for the Chrome extension
 */
object ErrorHandler {
    
    fun handleApiError(error: Throwable, context: String = ""): String {
        val errorMessage = when {
            error.message?.contains("NetworkError") == true || 
            error.message?.contains("fetch") == true -> {
                "Network connection failed. Please check your internet connection and ensure the backend server is running."
            }
            error.message?.contains("404") == true -> {
                "API endpoint not found. Please verify the backend server is running on the correct port."
            }
            error.message?.contains("500") == true -> {
                "Server error occurred. Please try again later."
            }
            error.message?.contains("timeout") == true -> {
                "Request timed out. Please try again."
            }
            else -> {
                "An unexpected error occurred: ${error.message ?: "Unknown error"}"
            }
        }
        
        logError(error, context)
        return errorMessage
    }
    
    fun handleProductDetectionError(error: Throwable): String {
        logError(error, "Product Detection")
        return when {
            error.message?.contains("querySelector") == true -> {
                "Unable to detect product information on this page. The page structure may have changed."
            }
            else -> {
                "Failed to analyze this product page. Please try refreshing the page."
            }
        }
    }
    
    fun handleBiddingError(error: Throwable): String {
        logError(error, "Bidding")
        return when {
            error.message?.contains("validation") == true -> {
                "Invalid bid amount. Please enter a valid price."
            }
            error.message?.contains("session") == true -> {
                "Bidding session expired. Please start a new negotiation."
            }
            else -> {
                "Failed to process your bid. Please try again."
            }
        }
    }
    
    fun handleExtensionError(error: Throwable): String {
        logError(error, "Extension")
        return when {
            error.message?.contains("permission") == true -> {
                "Permission denied. Please reload the page and try again."
            }
            error.message?.contains("storage") == true -> {
                "Unable to save settings. Please check extension permissions."
            }
            else -> {
                "Extension error occurred. Please reload the page."
            }
        }
    }
    
    private fun logError(error: Throwable, context: String) {
        // Simplified logging for Phase 2.A
        console.error("MagicCart Error [$context]:", error.message ?: "Unknown error")
        console.error("Error details:", error)
        
        // TODO: Phase 2.B - Implement proper error storage and reporting
        // For now, just log to console for debugging
    }
    
    fun showUserFriendlyError(message: String, isTemporary: Boolean = true) {
        // Create a non-intrusive error notification
        val errorDiv = window.document.createElement("div").apply {
            innerHTML = """
                <div style="
                    position: fixed;
                    top: 20px;
                    left: 50%;
                    transform: translateX(-50%);
                    background: #ff6b6b;
                    color: white;
                    padding: 12px 20px;
                    border-radius: 6px;
                    box-shadow: 0 4px 12px rgba(0,0,0,0.2);
                    z-index: 1000000;
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                    font-size: 14px;
                    max-width: 400px;
                    text-align: center;
                ">
                    <strong>MagicCart:</strong> $message
                </div>
            """.trimIndent()
        }
        
        window.document.body?.appendChild(errorDiv)
        
        if (isTemporary) {
            window.setTimeout({
                errorDiv.remove()
            }, 5000)
        }
    }
}