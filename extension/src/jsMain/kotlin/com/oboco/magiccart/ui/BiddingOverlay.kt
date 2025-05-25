package com.oboco.magiccart.ui

import com.oboco.magiccart.ProductInfo
import react.*

external interface BiddingOverlayProps : Props {
    var productInfo: ProductInfo
    var onClose: () -> Unit
    var onStartNegotiation: (ProductInfo) -> Unit
}

val BiddingOverlay = FC<BiddingOverlayProps> { props ->
    useEffect {
        // Create the overlay content programmatically
        val container = kotlinx.browser.document.getElementById("magiccart-overlay-container")
        container?.let { 
            it.innerHTML = """
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
                    <div style="padding: 16px;">
                        <div style="margin-bottom: 16px;">
                            <p style="margin: 0 0 8px 0; font-size: 14px; font-weight: 500; color: #333;">
                                Product: ${props.productInfo.name}
                            </p>
                            <p style="margin: 0 0 8px 0; font-size: 14px; color: #666;">
                                Current Price: $${props.productInfo.currentPrice}
                            </p>
                            <p style="margin: 0 0 8px 0; font-size: 14px; color: #666;">
                                Vendor: ${props.productInfo.vendor}
                            </p>
                            <p style="margin: 0; font-size: 12px; color: #999;">
                                Category: ${props.productInfo.category}
                            </p>
                        </div>
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
                        ">Start Discount Negotiation</button>
                        <div id="magiccart-status" style="
                            margin-top: 12px;
                            padding: 8px;
                            background: #f8f9fa;
                            border-radius: 4px;
                            font-size: 12px;
                            color: #666;
                            text-align: center;
                            display: none;
                        ">Ready for Phase 2.B: API Integration</div>
                    </div>
                </div>
            """.trimIndent()
            
            // Set up event listeners
            val closeBtn = kotlinx.browser.document.getElementById("magiccart-close-btn")
            val startBtn = kotlinx.browser.document.getElementById("magiccart-start-negotiation")
            val statusDiv = kotlinx.browser.document.getElementById("magiccart-status")
            
            closeBtn?.addEventListener("click", { props.onClose() })
            startBtn?.addEventListener("click", { 
                props.onStartNegotiation(props.productInfo)
                statusDiv?.asDynamic()?.style?.display = "block"
            })
        }
        
        cleanup { }
    }
    
    // Return null since we're using imperative DOM manipulation
    null
}