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
    
    /**
     * Create a new bidding session for a product
     */
    suspend fun createBiddingSession(request: SimpleBiddingRequest): String {
        console.log("ApiClient: Creating bidding session for", request.productName)
        
        try {
            val response = post("/bidding/sessions", request)
            val sessionResponse = parseResponse<BiddingSessionResponse>(response)
            console.log("ApiClient: Created session", sessionResponse.sessionId)
            return sessionResponse.sessionId
        } catch (e: Exception) {
            console.error("ApiClient: Failed to create session", e)
            // For now, return a mock session ID in development
            if (baseUrl.contains("localhost")) {
                console.warn("ApiClient: Using mock session ID for development")
                return "dev-session-12345"
            }
            throw e
        }
    }
    
    /**
     * Submit a bid for the given session
     */
    suspend fun submitBid(sessionId: String, bidAmount: Double): String {
        console.log("ApiClient: Submitting bid", bidAmount, "for session", sessionId)
        
        try {
            val request = SimpleBidRequest(bidAmount = bidAmount, timeLimit = 300)
            val response = post("/bidding/sessions/$sessionId/bids", request)
            val bidResponse = parseResponse<BidSubmissionResponse>(response)
            console.log("ApiClient: Submitted bid", bidResponse.bidId)
            return bidResponse.bidId
        } catch (e: Exception) {
            console.error("ApiClient: Failed to submit bid", e)
            // For now, return a mock bid ID in development
            if (baseUrl.contains("localhost")) {
                console.warn("ApiClient: Using mock bid ID for development")
                return "dev-bid-67890"
            }
            throw e
        }
    }
    
    /**
     * Get vendor offers for the given session
     */
    suspend fun getVendorOffers(sessionId: String): List<String> {
        console.log("ApiClient: Getting vendor offers for session", sessionId)
        
        try {
            val response = get("/bidding/sessions/$sessionId/offers")
            val offersResponse = parseResponse<VendorOffersResponse>(response)
            console.log("ApiClient: Retrieved", offersResponse.offers.size, "offers")
            return offersResponse.offers.map { offer ->
                "${offer.vendorName}: $${offer.discountedPrice} (Save $${offer.savings})"
            }
        } catch (e: Exception) {
            console.error("ApiClient: Failed to get offers", e)
            // For now, return mock offers in development
            if (baseUrl.contains("localhost")) {
                console.warn("ApiClient: Using mock offers for development")
                return listOf(
                    "VendorHub: $75 (Save $15)",
                    "BestDeals: $68 (Save $22)",
                    "QuickSave: $72 (Save $18)"
                )
            }
            throw e
        }
    }
    
    /**
     * Accept a vendor offer
     */
    suspend fun acceptOffer(sessionId: String, offerId: String): Boolean {
        console.log("ApiClient: Accepting offer", offerId, "for session", sessionId)
        
        try {
            val request = AcceptOfferRequest(offerId = offerId)
            val response = post("/bidding/sessions/$sessionId/accept", request)
            val acceptResponse = parseResponse<AcceptOfferResponse>(response)
            console.log("ApiClient: Offer acceptance", if (acceptResponse.success) "successful" else "failed")
            return acceptResponse.success
        } catch (e: Exception) {
            console.error("ApiClient: Failed to accept offer", e)
            // For now, return success in development
            if (baseUrl.contains("localhost")) {
                console.warn("ApiClient: Using mock acceptance for development")
                return true
            }
            throw e
        }
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

@kotlinx.serialization.Serializable
data class BiddingSessionResponse(
    val sessionId: String,
    val status: String
)

@kotlinx.serialization.Serializable
data class BidSubmissionResponse(
    val bidId: String,
    val status: String,
    val message: String
)

@kotlinx.serialization.Serializable
data class VendorOffer(
    val offerId: String,
    val vendorName: String,
    val originalPrice: Double,
    val discountedPrice: Double,
    val savings: Double,
    val validUntil: String
)

@kotlinx.serialization.Serializable
data class VendorOffersResponse(
    val sessionId: String,
    val offers: List<VendorOffer>
)

@kotlinx.serialization.Serializable
data class AcceptOfferRequest(
    val offerId: String
)

@kotlinx.serialization.Serializable
data class AcceptOfferResponse(
    val success: Boolean,
    val message: String,
    val redirectUrl: String? = null
)