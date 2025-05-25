package com.oboco.magiccart.api

import com.oboco.magiccart.*
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
    
    suspend fun createBiddingSession(request: CreateBiddingSessionRequest): BiddingSessionDto {
        return try {
            val response = post("/bidding-sessions", request)
            parseResponse(response)
        } catch (e: Exception) {
            throw ApiException(ErrorHandler.handleApiError(e, "Create Bidding Session"))
        }
    }
    
    suspend fun submitBid(sessionId: UuidString, request: SubmitBidRequest): BidDto {
        return try {
            val response = post("/bidding-sessions/$sessionId/bids", request)
            parseResponse(response)
        } catch (e: Exception) {
            throw ApiException(ErrorHandler.handleApiError(e, "Submit Bid"))
        }
    }
    
    suspend fun getBiddingSession(sessionId: UuidString): BiddingSessionDto {
        return try {
            val response = get("/bidding-sessions/$sessionId")
            parseResponse(response)
        } catch (e: Exception) {
            throw ApiException(ErrorHandler.handleApiError(e, "Get Bidding Session"))
        }
    }
    
    suspend fun getVendorOffers(sessionId: UuidString): List<VendorOfferDto> {
        return try {
            val response = get("/bidding-sessions/$sessionId/offers")
            parseResponse(response)
        } catch (e: Exception) {
            throw ApiException(ErrorHandler.handleApiError(e, "Get Vendor Offers"))
        }
    }
    
    suspend fun acceptOffer(sessionId: UuidString, offerId: UuidString): AcceptOfferResponse {
        return try {
            val response = post("/bidding-sessions/$sessionId/offers/$offerId/accept", Unit)
            parseResponse(response)
        } catch (e: Exception) {
            throw ApiException(ErrorHandler.handleApiError(e, "Accept Offer"))
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

// Request DTOs specific to the extension
@kotlinx.serialization.Serializable
data class CreateBiddingSessionRequest(
    val productName: String,
    val currentPrice: Decimal,
    val vendor: String,
    val category: String,
    val imageUrl: String? = null,
    val targetPrice: Decimal? = null,
    val maxBudget: Decimal? = null
)

@kotlinx.serialization.Serializable
data class SubmitBidRequest(
    val bidAmount: Decimal,
    val bidType: BidType = BidType.STANDARD,
    val timeLimit: Int? = null // minutes
)

@kotlinx.serialization.Serializable
data class AcceptOfferResponse(
    val success: Boolean,
    val message: String,
    val redirectUrl: String? = null
)