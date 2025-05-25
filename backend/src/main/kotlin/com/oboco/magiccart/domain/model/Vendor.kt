// src/main/kotlin/com/oboco/magiccart/domain/model/Vendor.kt
package com.oboco.magiccart.domain.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.util.UUID

@Table("vendors")
data class Vendor(
    @Id val vendorId: UUID = UUID.randomUUID(),
    val name: String,
    val rating: BigDecimal?,
    val logoUrl: String?,
    val integrationLevel: VendorIntegrationLevel = VendorIntegrationLevel.ASSISTED,
    val affiliateBaseUrl: String?,
    val apiEndpoint: String?, // For DEEP_API integration
    val couponApiEndpoint: String?, // For COUPON_CODES integration
    val supportContact: String?,
    val status: VendorStatus = VendorStatus.ACTIVE,
    // To build product URLs for ASSISTED checkout if needed
    val productUrlTemplate: String? // e.g., "https://vendor.com/products/{productId}"
) {
    // Helper function, actual implementation might be more complex
    fun buildProductUrl(productId: UUID): String {
        return productUrlTemplate?.replace("{productId}", productId.toString())
            ?: "https://defaultsearch.com?query=${name}+${productId}" // Fallback
    }
}