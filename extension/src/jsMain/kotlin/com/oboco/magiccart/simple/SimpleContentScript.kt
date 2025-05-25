package com.oboco.magiccart

import kotlinx.browser.document
import kotlinx.browser.window

/**
 * Simplified content script for initial testing
 */
fun initializeSimpleMagicCart() {
    window.onload = {
        startMagicCart()
    }
}

private fun startMagicCart() {
    console.log("MagicCart: Initializing on ${window.location.hostname}")
    
    // Detect if we're on a supported product page
    val productInfo = detectProductPage()
    if (productInfo != null) {
        console.log("MagicCart: Product detected", productInfo)
        injectSimpleOverlay(productInfo)
    } else {
        console.log("MagicCart: No product detected on this page")
    }
}

private fun detectProductPage(): SimpleProductInfo? {
    val hostname = window.location.hostname
    
    return when {
        hostname.contains("amazon.com") -> detectAmazonProduct()
        hostname.contains("bestbuy.com") -> detectBestBuyProduct()
        hostname.contains("walmart.com") -> detectWalmartProduct()
        hostname.contains("target.com") -> detectTargetProduct()
        hostname.contains("ebay.com") -> detectEbayProduct()
        else -> null
    }
}

private fun detectAmazonProduct(): SimpleProductInfo? {
    val titleElement = document.querySelector("#productTitle") ?: document.querySelector("[data-automation-id='product-title']")
    val priceElement = document.querySelector(".a-price-whole") ?: document.querySelector(".a-offscreen")
    val categoryElement = document.querySelector("#wayfinding-breadcrumbs_feature_div a")
    
    if (titleElement != null) {
        val priceText = priceElement?.textContent ?: "0"
        val price = extractPrice(priceText)
        
        return SimpleProductInfo(
            name = titleElement.textContent?.trim() ?: "Unknown Product",
            currentPrice = price,
            vendor = "Amazon",
            category = categoryElement?.textContent?.trim() ?: detectCategoryFromTitle(titleElement.textContent ?: ""),
            imageUrl = document.querySelector("#landingImage")?.getAttribute("src") 
                ?: document.querySelector(".a-dynamic-image")?.getAttribute("src")
        )
    }
    return null
}

private fun detectBestBuyProduct(): SimpleProductInfo? {
    val titleElement = document.querySelector(".sku-title h1") ?: document.querySelector("[data-automation-id='product-title']")
    val priceElement = document.querySelector(".pricing-price__range .sr-only") 
        ?: document.querySelector(".current-price .sr-only")
        ?: document.querySelector("[data-testid='customer-price'] .sr-only")
    
    if (titleElement != null) {
        val priceText = priceElement?.textContent ?: "0"
        val price = extractPrice(priceText)
        
        return SimpleProductInfo(
            name = titleElement.textContent?.trim() ?: "Unknown Product",
            currentPrice = price,
            vendor = "Best Buy",
            category = detectCategoryFromBreadcrumbs() ?: "Electronics",
            imageUrl = document.querySelector(".primary-image img")?.getAttribute("src")
                ?: document.querySelector(".carousel-image img")?.getAttribute("src")
        )
    }
    return null
}

private fun detectWalmartProduct(): SimpleProductInfo? {
    val titleElement = document.querySelector("[data-automation-id='product-title']")
        ?: document.querySelector("h1[data-automation-id='product-title']")
    val priceElement = document.querySelector("[data-automation-id='product-price'] span")
        ?: document.querySelector(".price-current")
    
    if (titleElement != null) {
        val priceText = priceElement?.textContent ?: "0"
        val price = extractPrice(priceText)
        
        return SimpleProductInfo(
            name = titleElement.textContent?.trim() ?: "Unknown Product",
            currentPrice = price,
            vendor = "Walmart",
            category = detectCategoryFromBreadcrumbs() ?: "General",
            imageUrl = document.querySelector("[data-testid='hero-image-container'] img")?.getAttribute("src")
        )
    }
    return null
}

private fun detectTargetProduct(): SimpleProductInfo? {
    val titleElement = document.querySelector("[data-test='product-title']")
        ?: document.querySelector("h1[data-test='product-title']")
    val priceElement = document.querySelector("[data-test='product-price']")
        ?: document.querySelector(".Price-characteristic")
    
    if (titleElement != null) {
        val priceText = priceElement?.textContent ?: "0"
        val price = extractPrice(priceText)
        
        return SimpleProductInfo(
            name = titleElement.textContent?.trim() ?: "Unknown Product",
            currentPrice = price,
            vendor = "Target",
            category = detectCategoryFromBreadcrumbs() ?: "General",
            imageUrl = document.querySelector("[data-test='product-image'] img")?.getAttribute("src")
        )
    }
    return null
}

private fun detectEbayProduct(): SimpleProductInfo? {
    val titleElement = document.querySelector("#ebay-page-title")
        ?: document.querySelector(".x-item-title-label")
    val priceElement = document.querySelector(".Price-characteristic")
        ?: document.querySelector("#prcIsum")
        ?: document.querySelector(".notranslate")
    
    if (titleElement != null) {
        val priceText = priceElement?.textContent ?: "0"
        val price = extractPrice(priceText)
        
        return SimpleProductInfo(
            name = titleElement.textContent?.trim() ?: "Unknown Product",
            currentPrice = price,
            vendor = "eBay",
            category = detectCategoryFromBreadcrumbs() ?: "General",
            imageUrl = document.querySelector("#icImg")?.getAttribute("src")
        )
    }
    return null
}

private fun extractPrice(priceText: String): Double {
    val cleanText = priceText.replace("[$,\\s]".toRegex(), "")
        .replace("USD", "")
        .replace("current price", "", ignoreCase = true)
        .trim()
    
    val priceMatch = "\\d+\\.?\\d*".toRegex().find(cleanText)
    return priceMatch?.value?.toDoubleOrNull() ?: 0.0
}

private fun detectCategoryFromTitle(title: String): String {
    val categories = mapOf(
        "laptop" to "Electronics",
        "phone" to "Electronics",
        "tablet" to "Electronics",
        "headphone" to "Electronics",
        "camera" to "Electronics",
        "book" to "Books",
        "shirt" to "Clothing",
        "shoes" to "Clothing",
        "dress" to "Clothing",
        "watch" to "Accessories",
        "bag" to "Accessories"
    )
    
    val lowerTitle = title.lowercase()
    return categories.entries.find { lowerTitle.contains(it.key) }?.value ?: "General"
}

private fun detectCategoryFromBreadcrumbs(): String? {
    val breadcrumbSelectors = arrayOf(
        ".breadcrumb a",
        "[data-testid='breadcrumb'] a",
        ".nav-breadcrumb a",
        ".breadcrumbs a"
    )
    
    for (selector in breadcrumbSelectors) {
        val breadcrumbs = document.querySelectorAll(selector)
        if (breadcrumbs.length > 1) {
            return breadcrumbs.item(1)?.textContent?.trim()
        }
    }
    return null
}

private fun injectSimpleOverlay(productInfo: SimpleProductInfo) {
    // Create simple HTML overlay without React
    val container = document.createElement("div").apply {
        id = "magiccart-overlay-container"
        innerHTML = """
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
                    <button onclick="this.closest('#magiccart-overlay-container').remove()" style="
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
                            Product: ${productInfo.name}
                        </p>
                        <p style="margin: 0 0 8px 0; font-size: 14px; color: #666;">
                            Current Price: $${productInfo.currentPrice}
                        </p>
                        <p style="margin: 0; font-size: 14px; color: #666;">
                            Vendor: ${productInfo.vendor}
                        </p>
                    </div>
                    <button onclick="alert('Negotiation feature coming soon!')" style="
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
                </div>
            </div>
        """.trimIndent()
    }
    
    document.body?.appendChild(container)
}

data class SimpleProductInfo(
    val name: String,
    val currentPrice: Double,
    val vendor: String,
    val category: String,
    val imageUrl: String?
)