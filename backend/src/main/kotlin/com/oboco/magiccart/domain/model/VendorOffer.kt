// src/main/kotlin/com/oboco/magiccart/domain/model/VendorOffer.kt
package com.oboco.magiccart.domain.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Table("vendor_offers")
data class VendorOffer(
    @Id val offerId: UUID = UUID.randomUUID(),
    val vendorId: UUID,
    val productId: UUID,
    var basePrice: BigDecimal,
    var shippingCost: BigDecimal, // For simplicity, a single value. Could be complex object.
    var taxRateApplicable: BigDecimal, // The rate, not the amount. For user's location.
    var deliveryDays: Int,
    var inventoryCount: Int?,
    val validFrom: LocalDateTime = LocalDateTime.now(),
    var validUntil: LocalDateTime?,
    var isActive: Boolean = true, // To quickly enable/disable offers
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now()
)