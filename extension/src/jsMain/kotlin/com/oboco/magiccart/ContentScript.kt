package com.oboco.magiccart

import com.oboco.magiccart.ui.BiddingController
import com.oboco.magiccart.utils.ErrorHandler
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Element
import org.w3c.dom.NodeList

// Global flag to prevent multiple initializations
private var isInitialized = false
private var initializationAttempted = false

/**
 * Content script entry point for MagicCart Chrome extension
 * Detects product pages and injects the bidding overlay
 */
fun initializeMagicCart() {
    if (initializationAttempted) {
        console.log("MagicCart: Initialization already attempted, skipping")
        return
    }
    
    initializationAttempted = true
    // Try immediate initialization
    console.log("MagicCart: Starting initialization...")
    
    // Try immediately
    if (startMagicCart()) {
        return // Success, no need for other attempts
    }
    
    // Also try when DOM is ready (in case content loads later)
    document.addEventListener("DOMContentLoaded", {
        if (!isInitialized) {
            console.log("MagicCart: DOMContentLoaded triggered")
            startMagicCart()
        }
    })
    
    // Also try with a delay for SPA content
    window.setTimeout({
        if (!isInitialized) {
            console.log("MagicCart: Delayed initialization (for dynamic content)")
            startMagicCart()
        }
    }, 2000)
}

private fun startMagicCart(): Boolean {
    console.log("MagicCart: Initializing on ${window.location.hostname}")
    
    try {
        // Detect if we're on a supported product page
        val productInfo = detectProductPage()
        if (productInfo != null) {
            console.log("MagicCart: Product detected", productInfo)
            injectBiddingOverlay(productInfo)
            isInitialized = true
            return true
        } else {
            console.log("MagicCart: No product detected on this page")
            return false
        }
    } catch (e: Exception) {
        console.error("MagicCart: Error during initialization", e)
        ErrorHandler.showUserFriendlyError(ErrorHandler.handleExtensionError(e))
        return false
    }
}

private fun detectProductPage(): ProductInfo? {
    val hostname = window.location.hostname
    
    return try {
        when {
            hostname.contains("amazon.com") -> detectAmazonProduct()
            hostname.contains("bestbuy.com") -> detectBestBuyProduct()
            hostname.contains("walmart.com") -> detectWalmartProduct()
            hostname.contains("target.com") -> detectTargetProduct()
            hostname.contains("ebay.com") -> detectEbayProduct()
            else -> null
        }
    } catch (e: Exception) {
        console.error("MagicCart: Error detecting product", e)
        ErrorHandler.showUserFriendlyError(ErrorHandler.handleProductDetectionError(e))
        null
    }
}

private fun detectAmazonProduct(): ProductInfo? {
    val titleElement = document.querySelector("#productTitle") ?: document.querySelector("[data-automation-id='product-title']")
    val priceElement = document.querySelector(".a-price-whole") ?: document.querySelector(".a-offscreen")
    val categoryElement = document.querySelector("#wayfinding-breadcrumbs_feature_div a")
    
    if (titleElement != null) {
        val priceText = priceElement?.textContent ?: "0"
        val price = extractPrice(priceText)
        
        return ProductInfo(
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

private fun detectBestBuyProduct(): ProductInfo? {
    val titleElement = document.querySelector(".sku-title h1") ?: document.querySelector("[data-automation-id='product-title']")
    val priceElement = document.querySelector(".pricing-price__range .sr-only") 
        ?: document.querySelector(".current-price .sr-only")
        ?: document.querySelector("[data-testid='customer-price'] .sr-only")
    
    if (titleElement != null) {
        val priceText = priceElement?.textContent ?: "0"
        val price = extractPrice(priceText)
        
        return ProductInfo(
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

private fun detectWalmartProduct(): ProductInfo? {
    val titleElement = document.querySelector("[data-automation-id='product-title']")
        ?: document.querySelector("h1[data-automation-id='product-title']")
    val priceElement = document.querySelector("[data-automation-id='product-price'] span")
        ?: document.querySelector(".price-current")
    
    if (titleElement != null) {
        val priceText = priceElement?.textContent ?: "0"
        val price = extractPrice(priceText)
        
        return ProductInfo(
            name = titleElement.textContent?.trim() ?: "Unknown Product",
            currentPrice = price,
            vendor = "Walmart",
            category = detectCategoryFromBreadcrumbs() ?: "General",
            imageUrl = document.querySelector("[data-testid='hero-image-container'] img")?.getAttribute("src")
        )
    }
    return null
}

private fun detectTargetProduct(): ProductInfo? {
    val titleElement = document.querySelector("[data-test='product-title']")
        ?: document.querySelector("h1[data-test='product-title']")
    val priceElement = document.querySelector("[data-test='product-price']")
        ?: document.querySelector(".Price-characteristic")
    
    if (titleElement != null) {
        val priceText = priceElement?.textContent ?: "0"
        val price = extractPrice(priceText)
        
        return ProductInfo(
            name = titleElement.textContent?.trim() ?: "Unknown Product",
            currentPrice = price,
            vendor = "Target",
            category = detectCategoryFromBreadcrumbs() ?: "General",
            imageUrl = document.querySelector("[data-test='product-image'] img")?.getAttribute("src")
        )
    }
    return null
}

private fun detectEbayProduct(): ProductInfo? {
    val titleElement = document.querySelector("#ebay-page-title")
        ?: document.querySelector(".x-item-title-label")
    val priceElement = document.querySelector(".Price-characteristic")
        ?: document.querySelector("#prcIsum")
        ?: document.querySelector(".notranslate")
    
    if (titleElement != null) {
        val priceText = priceElement?.textContent ?: "0"
        val price = extractPrice(priceText)
        
        return ProductInfo(
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

private fun injectBiddingOverlay(productInfo: ProductInfo) {
    try {
        // Check if overlay already exists
        if (document.getElementById("magiccart-overlay-container") != null) {
            console.log("MagicCart: Overlay already exists, skipping injection")
            return
        }
        
        // Create container for bidding overlay
        val container = document.createElement("div").apply {
            id = "magiccart-overlay-container"
        }
        
        document.body?.appendChild(container)
        
        // Create controller and render overlay
        val biddingController = BiddingController(
            productInfo = productInfo,
            onClose = {
                console.log("MagicCart: Closing overlay")
                container.remove()
            }
        )
        
        container.innerHTML = biddingController.getCurrentHtml()
        biddingController.setupEventListeners()
        
        console.log("MagicCart: Enhanced bidding overlay injected successfully")
        
    } catch (e: Exception) {
        console.error("MagicCart: Error injecting overlay", e)
        ErrorHandler.showUserFriendlyError("Failed to display MagicCart interface")
    }
}

data class ProductInfo(
    val name: String,
    val currentPrice: Double,
    val vendor: String,
    val category: String,
    val imageUrl: String?
)