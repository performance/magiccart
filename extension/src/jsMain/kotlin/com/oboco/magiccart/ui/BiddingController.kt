package com.oboco.magiccart.ui

import com.oboco.magiccart.ProductInfo
import com.oboco.magiccart.api.ApiClient
import com.oboco.magiccart.api.SimpleBiddingRequest
import com.oboco.magiccart.api.SimpleBidRequest
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

enum class BiddingState {
    INITIAL, CREATING_SESSION, BIDDING, SUBMITTING_BID, WAITING_FOR_OFFERS, OFFERS_RECEIVED, COMPLETED, ERROR
}

/**
 * Controller for managing bidding state and API integration without React
 */
class BiddingController(
    private val productInfo: ProductInfo,
    private val onClose: () -> Unit
) {
    private var currentState = BiddingState.INITIAL
    private var sessionId: String? = null
    private var bidAmount = ""
    private var errorMessage: String? = null
    private var offers: List<String> = emptyList()
    private var isLoading = false
    
    private val apiClient = ApiClient()
    private val scope = MainScope()
    
    fun getCurrentHtml(): String {
        return createOverlayHtml(
            productInfo = productInfo,
            biddingState = currentState,
            bidAmount = bidAmount,
            errorMessage = errorMessage,
            offers = offers,
            isLoading = isLoading
        )
    }
    
    fun setupEventListeners() {
        // Close button
        kotlinx.browser.document.getElementById("magiccart-close-btn")?.addEventListener("click", { onClose() })
        
        when (currentState) {
            BiddingState.INITIAL, BiddingState.ERROR -> {
                kotlinx.browser.document.getElementById("magiccart-start-negotiation")?.addEventListener("click", { startNegotiation() })
                kotlinx.browser.document.getElementById("magiccart-retry")?.addEventListener("click", { startNegotiation() })
            }
            
            BiddingState.BIDDING -> {
                val bidInput = kotlinx.browser.document.getElementById("magiccart-bid-input")
                val submitBtn = kotlinx.browser.document.getElementById("magiccart-submit-bid")
                
                bidInput?.addEventListener("input", { event ->
                    val target = event.target.asDynamic()
                    bidAmount = target.value as String
                    updateUI()
                })
                
                submitBtn?.addEventListener("click", { submitBid() })
            }
            
            BiddingState.OFFERS_RECEIVED -> {
                val acceptButtons = kotlinx.browser.document.querySelectorAll(".magiccart-accept-offer")
                for (i in 0 until acceptButtons.length) {
                    val button = acceptButtons.item(i)
                    button?.addEventListener("click", { event ->
                        val target = event.target.asDynamic()
                        val offer = target.getAttribute("data-offer") as String
                        acceptOffer(offer)
                    })
                }
            }
            
            else -> {} // No specific listeners needed for loading states
        }
    }
    
    private fun startNegotiation() {
        scope.launch {
            try {
                currentState = BiddingState.CREATING_SESSION
                isLoading = true
                errorMessage = null
                updateUI()
                
                val request = SimpleBiddingRequest(
                    productName = productInfo.name,
                    currentPrice = productInfo.currentPrice,
                    vendor = productInfo.vendor,
                    category = productInfo.category
                )
                
                val newSessionId = apiClient.createBiddingSession(request)
                sessionId = newSessionId
                currentState = BiddingState.BIDDING
                
            } catch (e: Exception) {
                console.error("Failed to create bidding session:", e)
                errorMessage = "Failed to start negotiation. Please try again."
                currentState = BiddingState.ERROR
            } finally {
                isLoading = false
                updateUI()
            }
        }
    }
    
    private fun submitBid() {
        val currentSessionId = sessionId
        if (currentSessionId != null && bidAmount.isNotBlank()) {
            scope.launch {
                try {
                    currentState = BiddingState.SUBMITTING_BID
                    isLoading = true
                    errorMessage = null
                    updateUI()
                    
                    val bidRequest = SimpleBidRequest(
                        bidAmount = bidAmount.toDouble(),
                        timeLimit = 300 // 5 minutes
                    )
                    
                    apiClient.submitBid(currentSessionId, bidAmount.toDouble())
                    currentState = BiddingState.WAITING_FOR_OFFERS
                    updateUI()
                    
                    // Simulate waiting and then getting offers
                    val callback = {
                        scope.launch {
                            try {
                                val vendorOffers = apiClient.getVendorOffers(currentSessionId)
                                offers = vendorOffers
                                currentState = BiddingState.OFFERS_RECEIVED
                                updateUI()
                            } catch (e: Exception) {
                                console.error("Failed to get offers:", e)
                                errorMessage = "Failed to retrieve offers. Please try again."
                                currentState = BiddingState.ERROR
                                updateUI()
                            }
                        }
                    }
                    kotlinx.browser.window.setTimeout(callback, 3000) // Wait 3 seconds to simulate backend processing
                    
                } catch (e: Exception) {
                    console.error("Failed to submit bid:", e)
                    errorMessage = "Failed to submit bid. Please check your amount and try again."
                    currentState = BiddingState.ERROR
                } finally {
                    isLoading = false
                    updateUI()
                }
            }
        }
    }
    
    private fun acceptOffer(offer: String) {
        val currentSessionId = sessionId
        if (currentSessionId != null) {
            scope.launch {
                try {
                    isLoading = true
                    errorMessage = null
                    updateUI()
                    
                    val success = apiClient.acceptOffer(currentSessionId, offer)
                    if (success) {
                        currentState = BiddingState.COMPLETED
                    } else {
                        errorMessage = "Failed to accept offer. Please try again."
                        currentState = BiddingState.ERROR
                    }
                } catch (e: Exception) {
                    console.error("Failed to accept offer:", e)
                    errorMessage = "Failed to accept offer. Please try again."
                    currentState = BiddingState.ERROR
                } finally {
                    isLoading = false
                    updateUI()
                }
            }
        }
    }
    
    private fun updateUI() {
        val container = kotlinx.browser.document.getElementById("magiccart-overlay-container")
        container?.let {
            it.innerHTML = getCurrentHtml()
            setupEventListeners()
        }
    }
}

private fun createOverlayHtml(
    productInfo: ProductInfo,
    biddingState: BiddingState,
    bidAmount: String,
    errorMessage: String?,
    offers: List<String>,
    isLoading: Boolean
): String {
    val loadingClass = if (isLoading) "loading" else ""
    val disabledStyle = if (isLoading) "opacity: 0.6; pointer-events: none;" else ""
    
    return """
        <div style="
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 999999;
            width: 350px;
            background: white;
            border-radius: 8px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.15);
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            border: 1px solid #e0e0e0;
            $disabledStyle
        ">
            <div style="
                display: flex;
                justify-content: space-between;
                align-items: center;
                padding: 16px;
                border-bottom: 1px solid #f0f0f0;
            ">
                <h3 style="margin: 0; font-size: 18px; color: #333;">🛒 MagicCart</h3>
                <button id="magiccart-close-btn" style="
                    background: transparent;
                    border: none;
                    font-size: 20px;
                    cursor: pointer;
                    padding: 4px;
                ">×</button>
            </div>
            
            ${createProductInfoHtml(productInfo)}
            ${createErrorHtml(errorMessage)}
            ${createContentHtml(biddingState, bidAmount, offers, isLoading)}
        </div>
    """.trimIndent()
}

private fun createProductInfoHtml(productInfo: ProductInfo): String {
    return """
        <div style="padding: 16px; border-bottom: 1px solid #f0f0f0;">
            <p style="margin: 0 0 8px 0; font-size: 14px; font-weight: 500; color: #333;">
                Product: ${productInfo.name}
            </p>
            <p style="margin: 0 0 8px 0; font-size: 14px; color: #666;">
                Current Price: $${productInfo.currentPrice}
            </p>
            <p style="margin: 0; font-size: 12px; color: #999;">
                Vendor: ${productInfo.vendor} • Category: ${productInfo.category}
            </p>
        </div>
    """
}

private fun createErrorHtml(errorMessage: String?): String {
    return if (errorMessage != null) {
        """
            <div style="
                padding: 12px;
                margin: 16px;
                background: #ffe6e6;
                border: 1px solid #ff9999;
                border-radius: 6px;
                font-size: 14px;
                color: #cc0000;
            ">
                $errorMessage
            </div>
        """
    } else ""
}

private fun createContentHtml(
    biddingState: BiddingState,
    bidAmount: String,
    offers: List<String>,
    isLoading: Boolean
): String {
    val loadingSpinner = if (isLoading) "⏳ " else ""
    
    return """
        <div style="padding: 16px;">
            ${when (biddingState) {
                BiddingState.INITIAL -> """
                    <button id="magiccart-start-negotiation" style="
                        width: 100%;
                        padding: 12px 16px;
                        background-color: #007bff;
                        color: white;
                        border: none;
                        border-radius: 6px;
                        font-size: 14px;
                        font-weight: 500;
                        cursor: pointer;
                    ">${loadingSpinner}Start Discount Negotiation</button>
                """
                
                BiddingState.CREATING_SESSION -> """
                    <div style="text-align: center; padding: 20px; color: #666;">
                        ⏳ Creating negotiation session...
                    </div>
                """
                
                BiddingState.BIDDING -> """
                    <div style="display: flex; flex-direction: column; gap: 12px;">
                        <label style="font-size: 14px; font-weight: 500; color: #333;">
                            Your bid amount:
                        </label>
                        <input id="magiccart-bid-input" type="number" value="$bidAmount" placeholder="Enter your bid (USD)" style="
                            padding: 8px 12px;
                            border: 1px solid #ddd;
                            border-radius: 4px;
                            font-size: 14px;
                        ">
                        <button id="magiccart-submit-bid" style="
                            padding: 12px 16px;
                            background-color: ${if (bidAmount.isBlank()) "#ccc" else "#28a745"};
                            color: white;
                            border: none;
                            border-radius: 6px;
                            font-size: 14px;
                            font-weight: 500;
                            cursor: ${if (bidAmount.isBlank()) "not-allowed" else "pointer"};
                        " ${if (bidAmount.isBlank()) "disabled" else ""}>
                            ${loadingSpinner}Submit Bid
                        </button>
                    </div>
                """
                
                BiddingState.SUBMITTING_BID -> """
                    <div style="text-align: center; padding: 20px; color: #666;">
                        ⏳ Submitting your bid...
                    </div>
                """
                
                BiddingState.WAITING_FOR_OFFERS -> """
                    <div style="text-align: center; padding: 20px; color: #666;">
                        ⏳ Waiting for vendor offers...
                    </div>
                """
                
                BiddingState.OFFERS_RECEIVED -> """
                    <div style="display: flex; flex-direction: column; gap: 12px;">
                        <h4 style="margin: 0; font-size: 16px; color: #333;">
                            Vendor Offers (${offers.size})
                        </h4>
                        ${offers.mapIndexed { index, offer ->
                            """
                                <div style="
                                    padding: 12px;
                                    border: 1px solid #ddd;
                                    border-radius: 6px;
                                    background: #f9f9f9;
                                ">
                                    <div style="font-weight: 500; margin-bottom: 8px;">
                                        $offer
                                    </div>
                                    <button class="magiccart-accept-offer" data-offer="$offer" style="
                                        padding: 8px 12px;
                                        background-color: #007bff;
                                        color: white;
                                        border: none;
                                        border-radius: 4px;
                                        font-size: 12px;
                                        cursor: pointer;
                                    ">
                                        Accept Offer
                                    </button>
                                </div>
                            """
                        }.joinToString("")}
                    </div>
                """
                
                BiddingState.COMPLETED -> """
                    <div style="
                        text-align: center;
                        padding: 20px;
                        background: #e6ffe6;
                        border-radius: 6px;
                        color: #006600;
                    ">
                        🎉 Negotiation completed successfully!
                    </div>
                """
                
                BiddingState.ERROR -> """
                    <button id="magiccart-retry" style="
                        width: 100%;
                        padding: 12px 16px;
                        background-color: #ffc107;
                        color: #333;
                        border: none;
                        border-radius: 6px;
                        font-size: 14px;
                        font-weight: 500;
                        cursor: pointer;
                    ">Try Again</button>
                """
            }}
        </div>
    """
}