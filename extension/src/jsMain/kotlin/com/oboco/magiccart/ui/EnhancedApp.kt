package com.oboco.magiccart.ui

import com.oboco.magiccart.*
import com.oboco.magiccart.api.ApiClient
import com.oboco.magiccart.api.CreateBiddingSessionRequest
import com.oboco.magiccart.api.SubmitBidRequest
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import react.*
import react.dom.html.ReactHTML.*

external interface EnhancedAppProps : Props {
    var productInfo: ProductInfo
}

enum class BiddingState {
    INITIAL, CREATING_SESSION, BIDDING, WAITING_FOR_OFFERS, VIEWING_OFFERS, COMPLETED
}

val EnhancedApp = FC<EnhancedAppProps> { props ->
    var isVisible by useState(true)
    var biddingState by useState(BiddingState.INITIAL)
    var biddingSession by useState<BiddingSessionDto?>(null)
    var vendorOffers by useState<List<VendorOfferDto>>(emptyList())
    var userBidAmount by useState("")
    var errorMessage by useState<String?>(null)
    
    val apiClient = remember { ApiClient() }
    val scope = MainScope()
    
    if (!isVisible) return@FC
    
    div {
        style = jso {
            padding = "16px"
            background = "#ffffff"
            borderRadius = "8px"
            border = "1px solid #e0e0e0"
            maxWidth = "400px"
        }
        
        // Header
        div {
            style = jso {
                display = "flex"
                justifyContent = "space-between"
                alignItems = "center"
                marginBottom = "12px"
                borderBottom = "1px solid #f0f0f0"
                paddingBottom = "8px"
            }
            
            h2 {
                style = jso {
                    margin = "0"
                    fontSize = "18px"
                    fontWeight = "600"
                    color = "#333"
                }
                +"🛒 MagicCart"
            }
            
            button {
                style = jso {
                    background = "transparent"
                    border = "none"
                    fontSize = "20px"
                    cursor = "pointer"
                    padding = "4px"
                    borderRadius = "4px"
                }
                onClick = { isVisible = false }
                +"×"
            }
        }
        
        // Product info
        div {
            style = jso {
                marginBottom = "16px"
            }
            
            p {
                style = jso {
                    margin = "0 0 8px 0"
                    fontSize = "14px"
                    fontWeight = "500"
                    color = "#333"
                }
                +"Product: ${props.productInfo.name}"
            }
            
            p {
                style = jso {
                    margin = "0 0 8px 0"
                    fontSize = "14px"
                    color = "#666"
                }
                +"Current Price: $${props.productInfo.currentPrice}"
            }
            
            p {
                style = jso {
                    margin = "0"
                    fontSize = "14px"
                    color = "#666"
                }
                +"Vendor: ${props.productInfo.vendor}"
            }
        }
        
        // Error message
        errorMessage?.let { error ->
            div {
                style = jso {
                    padding = "12px"
                    backgroundColor = "#ffe6e6"
                    border = "1px solid #ff9999"
                    borderRadius = "6px"
                    fontSize = "14px"
                    color = "#cc0000"
                    marginBottom = "16px"
                }
                +error
            }
        }
        
        // Main content based on bidding state
        when (biddingState) {
            BiddingState.INITIAL -> renderInitialState(props, scope, apiClient) { session, state ->
                biddingSession = session
                biddingState = state
                errorMessage = null
            }
            BiddingState.CREATING_SESSION -> renderLoadingState("Creating bidding session...")
            BiddingState.BIDDING -> renderBiddingState(biddingSession, userBidAmount, scope, apiClient,
                onBidAmountChange = { userBidAmount = it },
                onBidSubmit = { biddingState = BiddingState.WAITING_FOR_OFFERS },
                onError = { errorMessage = it }
            )
            BiddingState.WAITING_FOR_OFFERS -> renderLoadingState("Waiting for vendor offers...")
            BiddingState.VIEWING_OFFERS -> renderOffersState(vendorOffers, scope, apiClient,
                onOfferAccept = { biddingState = BiddingState.COMPLETED },
                onError = { errorMessage = it }
            )
            BiddingState.COMPLETED -> renderCompletedState()
        }
    }
}

fun ChildrenBuilder.renderInitialState(
    props: EnhancedAppProps,
    scope: MainScope,
    apiClient: ApiClient,
    onSessionCreated: (BiddingSessionDto, BiddingState) -> Unit
) {
    div {
        style = jso {
            display = "flex"
            flexDirection = "column"
            gap = "12px"
        }
        
        button {
            style = jso {
                padding = "12px 16px"
                backgroundColor = "#007bff"
                color = "white"
                border = "none"
                borderRadius = "6px"
                fontSize = "14px"
                fontWeight = "500"
                cursor = "pointer"
            }
            
            onClick = {
                scope.launch {
                    try {
                        val request = CreateBiddingSessionRequest(
                            productName = props.productInfo.name,
                            currentPrice = Decimal(props.productInfo.currentPrice),
                            vendor = props.productInfo.vendor,
                            category = props.productInfo.category,
                            imageUrl = props.productInfo.imageUrl
                        )
                        val session = apiClient.createBiddingSession(request)
                        onSessionCreated(session, BiddingState.BIDDING)
                    } catch (e: Exception) {
                        console.error("Error creating bidding session:", e)
                    }
                }
            }
            
            +"Start Discount Negotiation"
        }
    }
}

fun ChildrenBuilder.renderLoadingState(message: String) {
    div {
        style = jso {
            display = "flex"
            alignItems = "center"
            justifyContent = "center"
            padding = "20px"
            color = "#666"
        }
        +message
    }
}

fun ChildrenBuilder.renderBiddingState(
    session: BiddingSessionDto?,
    bidAmount: String,
    scope: MainScope,
    apiClient: ApiClient,
    onBidAmountChange: (String) -> Unit,
    onBidSubmit: () -> Unit,
    onError: (String) -> Unit
) {
    div {
        style = jso {
            display = "flex"
            flexDirection = "column"
            gap = "12px"
        }
        
        label {
            style = jso {
                fontSize = "14px"
                fontWeight = "500"
                color = "#333"
            }
            +"Your bid amount:"
        }
        
        input {
            type = "number"
            value = bidAmount
            placeholder = "Enter your bid (USD)"
            style = jso {
                padding = "8px 12px"
                border = "1px solid #ddd"
                borderRadius = "4px"
                fontSize = "14px"
            }
            onChange = { event ->
                onBidAmountChange(event.target.value)
            }
        }
        
        button {
            style = jso {
                padding = "12px 16px"
                backgroundColor = "#28a745"
                color = "white"
                border = "none"
                borderRadius = "6px"
                fontSize = "14px"
                fontWeight = "500"
                cursor = "pointer"
                disabled = bidAmount.isEmpty()
            }
            
            onClick = {
                val amount = bidAmount.toDoubleOrNull()
                if (amount != null && session != null) {
                    scope.launch {
                        try {
                            val request = SubmitBidRequest(Decimal(amount))
                            apiClient.submitBid(session.id, request)
                            onBidSubmit()
                        } catch (e: Exception) {
                            onError("Failed to submit bid: ${e.message}")
                        }
                    }
                } else {
                    onError("Please enter a valid bid amount")
                }
            }
            
            +"Submit Bid"
        }
    }
}

fun ChildrenBuilder.renderOffersState(
    offers: List<VendorOfferDto>,
    scope: MainScope,
    apiClient: ApiClient,
    onOfferAccept: () -> Unit,
    onError: (String) -> Unit
) {
    div {
        style = jso {
            display = "flex"
            flexDirection = "column"
            gap = "12px"
        }
        
        h3 {
            style = jso {
                margin = "0"
                fontSize = "16px"
                color = "#333"
            }
            +"Vendor Offers (${offers.size})"
        }
        
        offers.forEach { offer ->
            div {
                style = jso {
                    padding = "12px"
                    border = "1px solid #ddd"
                    borderRadius = "6px"
                    backgroundColor = "#f9f9f9"
                }
                
                div {
                    style = jso { fontWeight = "500" }
                    +"Vendor: ${offer.vendorId}"
                }
                div {
                    +"Price: $${offer.basePrice.value}"
                }
                
                button {
                    style = jso {
                        marginTop = "8px"
                        padding = "8px 12px"
                        backgroundColor = "#007bff"
                        color = "white"
                        border = "none"
                        borderRadius = "4px"
                        fontSize = "12px"
                        cursor = "pointer"
                    }
                    
                    onClick = {
                        scope.launch {
                            try {
                                // apiClient.acceptOffer(sessionId, offer.id)
                                onOfferAccept()
                            } catch (e: Exception) {
                                onError("Failed to accept offer: ${e.message}")
                            }
                        }
                    }
                    
                    +"Accept Offer"
                }
            }
        }
    }
}

fun ChildrenBuilder.renderCompletedState() {
    div {
        style = jso {
            textAlign = "center"
            padding = "20px"
            backgroundColor = "#e6ffe6"
            borderRadius = "6px"
            color = "#006600"
        }
        +"🎉 Negotiation completed successfully!"
    }
}