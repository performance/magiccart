package com.oboco.magiccart

import com.oboco.magiccart.ui.EnhancedApp
import kotlinx.browser.document
import kotlinx.browser.window
import react.create
import react.dom.client.createRoot

/**
 * Content script entry point for MagicCart Chrome extension
 * Detects product pages and injects the bidding overlay
 */
fun initializeMagicCart() {
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
        injectBiddingOverlay(productInfo)
    } else {
        console.log("MagicCart: No product detected on this page")
    }
}

private fun detectProductPage(): ProductInfo? {
    val hostname = window.location.hostname
    val pathname = window.location.pathname
    
    return when {
        hostname.contains("amazon.com") -> detectAmazonProduct()
        hostname.contains("bestbuy.com") -> detectBestBuyProduct()
        hostname.contains("walmart.com") -> detectWalmartProduct()
        hostname.contains("target.com") -> detectTargetProduct()
        hostname.contains("ebay.com") -> detectEbayProduct()
        else -> null
    }
}

private fun detectAmazonProduct(): ProductInfo? {
    // Amazon product page detection logic
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
    // Best Buy product page detection logic
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

private fun injectBiddingOverlay(productInfo: ProductInfo) {
    // Create container for React app
    val container = document.createElement("div").apply {
        id = "magiccart-overlay-container"
        setAttribute("style", """
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 999999;
            width: 400px;
            max-height: 80vh;
            background: white;
            border-radius: 8px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.15);
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
        """.trimIndent())
    }
    
    document.body?.appendChild(container)
    
    // Mount React app
    val root = createRoot(container)
    root.render(EnhancedApp.create {
        this.productInfo = productInfo
    })
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
    // Remove currency symbols and common formatting
    val cleanText = priceText.replace("[$,\\s]".toRegex(), "")
        .replace("USD", "")
        .replace("current price", "", ignoreCase = true)
        .trim()
    
    // Extract first number sequence that looks like a price
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
            // Return the second breadcrumb item (first is usually "Home")
            return breadcrumbs[1]?.textContent?.trim()
        }
    }
    return null
}

data class ProductInfo(
    val name: String,
    val currentPrice: Double,
    val vendor: String,
    val category: String,
    val imageUrl: String?
)