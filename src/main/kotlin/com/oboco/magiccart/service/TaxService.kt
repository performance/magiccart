// src/main/kotlin/com/oboco/magiccart/service/TaxService.kt
package com.oboco.magiccart.service

import com.oboco.magiccart.dto.TaxDetailsDto
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

interface TaxService {
    fun calculateTax(basePrice: BigDecimal, userLocation: String?): TaxCalculationResult
}

data class TaxCalculationResult(
    val taxAmount: BigDecimal,
    val details: TaxDetailsDto?
)

@Service("placeholderTaxService")
class PlaceholderTaxService : TaxService {
    override fun calculateTax(basePrice: BigDecimal, userLocation: String?): TaxCalculationResult {
        // MVP: Fixed 7% tax if location is "US_DEFAULT", otherwise 0
        val rate = if (userLocation == "US_DEFAULT") BigDecimal("0.07") else BigDecimal.ZERO
        val taxAmount = basePrice.multiply(rate).setScale(2, RoundingMode.HALF_UP)
        return TaxCalculationResult(
            taxAmount,
            TaxDetailsDto(
                location = userLocation ?: "N/A",
                rate = rate,
                note = "Estimated sales tax."
            )
        )
    }
}