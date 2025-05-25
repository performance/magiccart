// src/main/kotlin/com/oboco/magiccart/dto/BiddingDtos.kt
package com.oboco.magiccart.dto

import com.oboco.magiccart.domain.model.CounterAction
import com.oboco.magiccart.domain.model.PriceGuaranteeLevel
import com.oboco.magiccart.domain.model.TriggerCondition
import com.oboco.magiccart.domain.model.CheckoutMethod // Assuming enums are in domain.model
import java.math.BigDecimal
import java.util.UUID

// --- For GET /api/bidding-rules ---
data class BiddingRulesRequestParams( // Already defined in previous thought, repeating for context
    val productId: UUID,
    val userLocation: String?, // e.g., ZIP code
    val requestedDiscountPercent: BigDecimal?,
    val bundleProductIds: List<UUID>? = null // For future kits
)

data class VendorOfferDto(
    val vendorId: UUID,
    val vendorName: String,
    val vendorRating: BigDecimal?,
    val vendorLogoUrl: String?,
    val offerId: UUID,
    val basePrice: BigDecimal,
    val shippingEstimate: BigDecimal,
    val taxEstimate: BigDecimal,
    var totalCostEstimate: BigDecimal, // Mutable to be calculated
    val deliveryDays: Int,
    val inventoryCount: Int?,
    var currentDiscountPercentFromMsrp: BigDecimal, // Mutable to be calculated
    val incentives: List<String>? = null // e.g., ["free_shipping", "extended_warranty"]
)

data class VendorRuleDto(
    val ruleId: UUID,
    val vendorId: UUID,
    val ruleName: String,
    val triggerCondition: TriggerCondition, // Using the strong type from domain.model
    val counterAction: CounterAction,       // Using the strong type from domain.model
    val additionalIncentives: Map<String, Boolean>?, // e.g. {"free_shipping": true}
    val displayTemplateForCounterReason: String?,
    val maxUsagePerSession: Int?,
    val priority: Int,
    val ruleHash: String
    // Not including applicabilityType/applicableCategory here as client engine uses triggerCondition
)

data class BiddingRulesResponse(
    val productId: UUID,
    val productName: String,
    val productMsrp: BigDecimal,
    val productCategory: String,
    val qualifyingVendorsAndOffers: List<VendorOfferDto>,
    val vendorRules: List<VendorRuleDto>, // All relevant rules for the client engine
    val taxDetails: TaxDetailsDto?, // e.g., rate and applied amount for the location
    val shippingDetailsByVendor: Map<UUID, ShippingDetailDto>? // vendorId -> shipping info
)

data class TaxDetailsDto(
    val location: String,
    val rate: BigDecimal, // e.g., 0.07
    val note: String? = null // e.g., "Estimated sales tax for 90210"
)

data class ShippingDetailDto(
    val cost: BigDecimal,
    val estimatedDeliveryDays: Int, // This might override offer.deliveryDays if shipping method changes
    val note: String? = null
)


// --- For POST /api/validate-selection ---
// (We'll fully implement service later, but define DTOs now)

data class NegotiatedOfferDetailsDto( // What the client thinks the offer is
    val totalCost: BigDecimal,
    val basePrice: BigDecimal,
    val shippingCost: BigDecimal,
    val taxAmount: BigDecimal,
    val discountPercent: BigDecimal,
    val deliveryDays: Int,
    val appliedRuleIds: List<UUID>? = emptyList(),
    val incentives: List<String>? = emptyList()
)

data class BiddingRoundSummaryDto(
    val roundNumber: Int,
    val userSelectedVendorId: UUID?,
    val offersConsideredCount: Int
    // Add more details if needed for audit
)

data class SelectionValidationRequest(
    val sessionId: String, // Client-generated unique ID for this bidding session
    val productId: UUID,
    val selectedVendorId: UUID, // The vendor the user finally chose
    val finalOfferDetails: NegotiatedOfferDetailsDto, // The details of the offer user accepted
    val userLocation: String?,
    val biddingRoundsAudit: List<BiddingRoundSummaryDto>? = emptyList()
)

data class CheckoutInstructionDetailDto(
    val step: Int,
    val description: String,
    val actionUrl: String? = null // e.g., direct link for a coupon application page
)

data class CheckoutInstructionsDto(
    val method: CheckoutMethod,
    val primaryRedirectUrl: String, // Main URL to send the user (e.g. product page or cart)
    val couponCode: String? = null,
    val displayMessageToUser: String, // e.g., "You negotiated $X with Vendor Y."
    val detailedInstructions: List<CheckoutInstructionDetailDto>? = emptyList(),
    val supportContact: String? = null,
    val priceGuaranteeLevel: PriceGuaranteeLevel
)

data class SelectionValidationResponse(
    val isValid: Boolean, // Was the negotiated offer deemed valid by the backend?
    val message: String? = null, // e.g., "Offer validated" or "Price discrepancy noted"
    val checkoutInstructions: CheckoutInstructionsDto,
    val confirmedFinalPrice: BigDecimal, // Price confirmed/re-calculated by backend
    val originalVendorPriceBeforeDiscount: BigDecimal? = null // For savings calculation display
)