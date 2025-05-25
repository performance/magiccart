// src/main/kotlin/com/oboco/magiccart/service/SelectionValidationService.kt
package com.oboco.magiccart.service

import com.oboco.magiccart.domain.repository.ProductRepository
import com.oboco.magiccart.domain.repository.VendorOfferRepository
import com.oboco.magiccart.domain.repository.VendorRepository
import com.oboco.magiccart.dto.SelectionValidationRequest
import com.oboco.magiccart.dto.SelectionValidationResponse
import com.oboco.magiccart.domain.model.PriceGuaranteeLevel
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

interface SelectionValidationService {
    fun validateAndProcessSelection(request: SelectionValidationRequest): SelectionValidationResponse
}

@Service
class SelectionValidationServiceImpl(
    private val cartUpdateService: CartUpdateService,
    private val vendorRepository: VendorRepository,
    private val productRepository: ProductRepository,
    private val vendorOfferRepository: VendorOfferRepository
    // private val analyticsService: AnalyticsService // To be added later for bidding_sessions_analytics
) : SelectionValidationService {

    private val logger = LoggerFactory.getLogger(SelectionValidationServiceImpl::class.java)

    override fun validateAndProcessSelection(request: SelectionValidationRequest): SelectionValidationResponse {
        logger.info(
            "Validating selection for sessionId: {}, productId: {}, vendorId: {}, finalPrice: {}",
            request.sessionId, request.productId, request.selectedVendorId, request.finalOfferDetails.totalCost
        )

        // Iteration 1: Basic validation - check if entities exist
        val vendorExists = vendorRepository.existsById(request.selectedVendorId)
        val productExists = productRepository.existsById(request.productId)

        if (!vendorExists || !productExists) {
            logger.warn(
                "Validation failed: Vendor or Product not found. SessionId: {}, VendorId: {}, ProductId: {}",
                request.sessionId, request.selectedVendorId, request.productId
            )
            // In a real scenario, you might have a more specific DTO for invalid response
            // For now, we'll still generate assisted checkout but flag as invalid.
            // Or, throw an exception to be caught by controller.
            // Let's return instructions but mark as potentially problematic.
            val dummyInstructions = cartUpdateService.generateCheckoutInstructions(
                request.selectedVendorId, // This might fail if vendorId is totally bogus
                request.productId,
                request.finalOfferDetails,
                request.userLocation
            )
            return SelectionValidationResponse(
                isValid = false,
                message = "Validation failed: Product or Vendor not found. Please double-check the offer.",
                checkoutInstructions = dummyInstructions.copy(
                    displayMessageToUser = "Error: Could not fully validate this offer. Proceed with caution to ${dummyInstructions.primaryRedirectUrl}.",
                    priceGuaranteeLevel = PriceGuaranteeLevel.MANUAL_VERIFICATION
                ),
                confirmedFinalPrice = request.finalOfferDetails.totalCost, // Use client's price if backend can't verify
                originalVendorPriceBeforeDiscount = null // Can't determine without fetching original offer
            )
        }

        // TODO Iteration 2+: More sophisticated validation:
        // 1. Re-fetch the original offer for the product from this vendor.
        // 2. Re-simulate the rule application if `appliedRuleIds` are provided (complex).
        // 3. Check if `finalOfferDetails.totalCost` is plausible given original price and rules.
        // 4. Check inventory if possible (via vendor API in future).

        // For Iteration 1, assume client's negotiated price is what we proceed with for instructions.
        val originalOfferPrice = fetchOriginalOfferPrice(request.productId, request.selectedVendorId)


        val checkoutInstructions = cartUpdateService.generateCheckoutInstructions(
            request.selectedVendorId,
            request.productId,
            request.finalOfferDetails,
            request.userLocation
        )

        // TODO: Log to bidding_sessions_analytics table
        // analyticsService.logSession(request, checkoutInstructions, originalOfferPrice)

        logger.info(
            "Selection validated for sessionId: {}. Checkout method: {}",
            request.sessionId, checkoutInstructions.method
        )

        return SelectionValidationResponse(
            isValid = true, // Iteration 1: Assume valid if entities exist
            message = "Offer selection logged. Proceed to checkout.",
            checkoutInstructions = checkoutInstructions,
            confirmedFinalPrice = request.finalOfferDetails.totalCost, // For now, trust client's final price
            originalVendorPriceBeforeDiscount = originalOfferPrice
        )
    }

    private fun fetchOriginalOfferPrice(productId: UUID, vendorId: UUID): BigDecimal? {
        // Attempt to find an active, valid offer to use as a baseline "original price"
        // This is a simplification; the "original" could be the price when the session started.
        return vendorOfferRepository.findActiveAndValidOffersByProductId(productId, LocalDateTime.now())
            .filter { it.vendorId == vendorId }
            .minByOrNull { it.basePrice } // Take the cheapest current base offer as "original" for now
            ?.let { offer ->
                // Recalculate total cost for this original offer for consistent comparison
                // This requires tax/shipping services again. For simplicity, just return basePrice.
                // In a real system, you'd fully calculate its total cost.
                offer.basePrice
            }
    }
}