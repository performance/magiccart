package com.oboco.magiccart.api

// Temporarily simplified - will integrate with shared module in Phase 2.B
import com.oboco.magiccart.utils.ErrorHandler
import kotlinx.coroutines.await
import kotlinx.browser.window
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response

/**
 * API client for communicating with the MagicCart backend
 */
class ApiClient(private val baseUrl: String = "http://localhost:8080/api") {
    
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    // Placeholder API methods - will be implemented in Phase 2.B
    suspend fun createBiddingSession(productInfo: Any): String {
        console.log("ApiClient: createBiddingSession called", productInfo)
        // TODO: Implement actual API call
        return "mock-session-id"
    }
    
    suspend fun submitBid(sessionId: String, bidAmount: Double): String {
        console.log("ApiClient: submitBid called", sessionId, bidAmount)
        // TODO: Implement actual API call  
        return "mock-bid-id"
    }
    
    suspend fun getVendorOffers(sessionId: String): List<String> {
        console.log("ApiClient: getVendorOffers called", sessionId)
        // TODO: Implement actual API call
        return listOf("mock-offer-1", "mock-offer-2")
    }
    
    suspend fun acceptOffer(sessionId: String, offerId: String): Boolean {
        console.log("ApiClient: acceptOffer called", sessionId, offerId)
        // TODO: Implement actual API call
        return true
    }
    
    private suspend fun get(endpoint: String): Response {
        return window.fetch("$baseUrl$endpoint", RequestInit().apply {
            method = "GET"
            headers = js("""({
                "Content-Type": "application/json",
                "Accept": "application/json"
            })""")
        }).await()
    }
    
    private suspend fun post(endpoint: String, body: Any): Response {
        val jsonBody = when (body) {
            is Unit -> "{}"
            else -> json.encodeToString(body)
        }
        
        return window.fetch("$baseUrl$endpoint", RequestInit().apply {
            method = "POST"
            headers = js("""({
                "Content-Type": "application/json",
                "Accept": "application/json"
            })""")
            this.body = jsonBody
        }).await()
    }
    
    private suspend inline fun <reified T> parseResponse(response: Response): T {
        if (!response.ok) {
            val errorText = response.text().await()
            throw ApiException("HTTP ${response.status}: $errorText")
        }
        
        val responseText = response.text().await()
        return json.decodeFromString<T>(responseText)
    }
}

class ApiException(message: String) : Exception(message)

// TODO: Phase 2.B - Replace with proper shared DTOs from :shared module
// Temporary simplified types for Phase 2.A testing

@kotlinx.serialization.Serializable  
data class SimpleBiddingRequest(
    val productName: String,
    val currentPrice: Double,
    val vendor: String,
    val category: String
)

@kotlinx.serialization.Serializable
data class SimpleBidRequest(
    val bidAmount: Double,
    val timeLimit: Int? = null
)

@kotlinx.serialization.Serializable
data class SimpleOfferResponse(
    val success: Boolean,
    val message: String
)