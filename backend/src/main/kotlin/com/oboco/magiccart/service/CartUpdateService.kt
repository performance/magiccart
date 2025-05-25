// src/main/kotlin/com/oboco/magiccart/service/CartUpdateService.kt
package com.oboco.magiccart.service

import com.oboco.magiccart.domain.model.PriceGuaranteeLevel
import com.oboco.magiccart.domain.model.VendorIntegrationLevel
import com.oboco.magiccart.domain.repository.ProductRepository
import com.oboco.magiccart.domain.repository.VendorRepository
import com.oboco.magiccart.dto.CheckoutInstructionDetailDto
import com.oboco.magiccart.dto.CheckoutInstructionsDto
import com.oboco.magiccart.domain.model.CheckoutMethod // Ensure this is correctly imported
import com.oboco.magiccart.dto.NegotiatedOfferDetailsDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

interface CartUpdateService {
    fun generateCheckoutInstructions(
        selectedVendorId: UUID,
        productId: UUID,
        finalOfferDetails: NegotiatedOfferDetailsDto,
        userLocation: String?
    ): CheckoutInstructionsDto
}

@Service
class CartUpdateServiceImpl(
    private val vendorRepository: VendorRepository,
    private val productRepository: ProductRepository // To get product name for messages
) : CartUpdateService {

    private val logger = LoggerFactory.getLogger(CartUpdateServiceImpl::class.java)

    override fun generateCheckoutInstructions(
        selectedVendorId: UUID,
        productId: UUID,
        finalOfferDetails: NegotiatedOfferDetailsDto,
        userLocation: String? // userLocation might be used for more tailored instructions later
    ): CheckoutInstructionsDto {
        logger.info(
            "Generating checkout instructions for vendorId: {}, productId: {}, finalPrice: {}",
            selectedVendorId, productId, finalOfferDetails.totalCost
        )

        val vendor = vendorRepository.findById(selectedVendorId)
            .orElseThrow {
                logger.warn("Vendor not found for checkout instructions: $selectedVendorId")
                NoSuchElementException("Vendor not found: $selectedVendorId")
            }

        val product = productRepository.findById(productId)
            .orElseThrow {
                logger.warn("Product not found for checkout instructions: $productId")
                NoSuchElementException("Product not found: $productId")
            }

        // For Iteration 1, we primarily focus on ASSISTED.
        // Later, this 'when' block will expand based on vendor.integrationLevel.
        return when (vendor.integrationLevel) {
            VendorIntegrationLevel.DEEP_API -> {
                logger.info("Vendor ${vendor.name} has DEEP_API integration (Not implemented in Iteration 1, falling back to ASSISTED).")
                // TODO: Implement generateApiBasedCheckout(vendor, product, finalOfferDetails)
                generateAssistedCheckout(vendor, product, finalOfferDetails) // Fallback for now
            }
            VendorIntegrationLevel.AFFILIATE_PARAMS -> {
                logger.info("Vendor ${vendor.name} has AFFILIATE_PARAMS integration (Not implemented in Iteration 1, falling back to ASSISTED).")
                // TODO: Implement generateParameterizedUrlCheckout(vendor, product, finalOfferDetails)
                generateAssistedCheckout(vendor, product, finalOfferDetails) // Fallback for now
            }
            VendorIntegrationLevel.COUPON_CODES -> {
                logger.info("Vendor ${vendor.name} has COUPON_CODES integration (Not implemented in Iteration 1, falling back to ASSISTED).")
                // TODO: Implement generateCouponCodeCheckout(vendor, product, finalOfferDetails)
                generateAssistedCheckout(vendor, product, finalOfferDetails) // Fallback for now
            }
            VendorIntegrationLevel.ASSISTED -> {
                logger.info("Generating ASSISTED checkout for vendor ${vendor.name}.")
                generateAssistedCheckout(vendor, product, finalOfferDetails)
            }
        }
    }

    private fun generateAssistedCheckout(
        vendor: com.oboco.magiccart.domain.model.Vendor, // Explicitly using domain model
        product: com.oboco.magiccart.domain.model.Product,
        finalOfferDetails: NegotiatedOfferDetailsDto
    ): CheckoutInstructionsDto {
        val negotiatedPriceStr = finalOfferDetails.totalCost.setScale(2).toPlainString()
        val primaryUrl = vendor.buildProductUrl(product.productId) // Uses helper in Vendor entity

        return CheckoutInstructionsDto(
            method = CheckoutMethod.ASSISTED,
            primaryRedirectUrl = primaryUrl,
            couponCode = null, // No coupon for basic assisted
            displayMessageToUser = "You negotiated a price of $$negotiatedPriceStr for '${product.name}' with ${vendor.name}.",
            detailedInstructions = listOf(
                CheckoutInstructionDetailDto(1, "You will be redirected to ${vendor.name}."),
                CheckoutInstructionDetailDto(2, "Add '${product.name}' to your cart."),
                CheckoutInstructionDetailDto(3, "IMPORTANT: Verify the price in your cart matches $$negotiatedPriceStr before payment."),
                CheckoutInstructionDetailDto(4, "If the price does not match, please contact ${vendor.name} support${if (vendor.supportContact != null) " (${vendor.supportContact})" else ""} or look for alternative offers.")
            ),
            supportContact = vendor.supportContact,
            priceGuaranteeLevel = PriceGuaranteeLevel.MANUAL_VERIFICATION
        )
    }

    // Placeholder for future methods - to be implemented in later iterations
    // private fun generateApiBasedCheckout(...) : CheckoutInstructionsDto { ... }
    // private fun generateParameterizedUrlCheckout(...) : CheckoutInstructionsDto { ... }
    // private fun generateCouponCodeCheckout(...) : CheckoutInstructionsDto { ... }
}