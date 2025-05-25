// src/main/kotlin/com/oboco/magiccart/service/ShippingService.kt
package com.oboco.magiccart.service

import com.oboco.magiccart.dto.ShippingDetailDto
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.UUID

interface ShippingService {
    fun calculateShipping(vendorId: UUID, productId: UUID, userLocation: String?): ShippingDetailDto
}

@Service("placeholderShippingService")
class PlaceholderShippingService : ShippingService {
    override fun calculateShipping(vendorId: UUID, productId: UUID, userLocation: String?): ShippingDetailDto {
        // MVP: Fixed $5 shipping, 3 days delivery
        return ShippingDetailDto(
            cost = BigDecimal("5.00"),
            estimatedDeliveryDays = 3,
            note = "Standard shipping estimate."
        )
    }
}