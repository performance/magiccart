package com.oboco.magiccart.ui

import com.oboco.magiccart.ProductInfo
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import react.*
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.button

external interface AppProps : Props {
    var productInfo: ProductInfo
}

val App = FC<AppProps> { props ->
    var isVisible by useState(true)
    var isLoading by useState(false)
    var biddingData by useState<String?>(null)
    
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
        
        // Action buttons
        div {
            style = jso {
                display = "flex"
                flexDirection = "column"
                gap = "8px"
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
                    disabled = isLoading
                }
                
                onClick = {
                    isLoading = true
                    scope.launch {
                        try {
                            // TODO: Call API to get bidding rules
                            console.log("Fetching bidding rules for product: ${props.productInfo.name}")
                            biddingData = "Mock bidding data loaded"
                        } catch (e: Exception) {
                            console.error("Error fetching bidding rules:", e)
                        } finally {
                            isLoading = false
                        }
                    }
                }
                
                if (isLoading) +"Loading..." else +"Start Discount Negotiation"
            }
            
            if (biddingData != null) {
                div {
                    style = jso {
                        padding = "12px"
                        backgroundColor = "#f8f9fa"
                        borderRadius = "6px"
                        fontSize = "12px"
                        color = "#666"
                    }
                    +biddingData!!
                }
            }
        }
    }
}