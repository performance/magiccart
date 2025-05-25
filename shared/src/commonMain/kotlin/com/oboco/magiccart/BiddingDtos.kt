package com.oboco.magiccart

import kotlinx.serialization.Serializable

// --- For GET /api/bidding-rules ---
@Serializable
data class BiddingRulesRequestParams(
    val productId: UuidString,
    val userLocation: String? = null, // e.g., ZIP code
    val requestedDiscountPercent: Decimal? = null,
    val bundleProductIds: List<UuidString>? = null // For bundle rule evaluation
)

@Serializable
data class VendorOfferDto(
    val vendorId: UuidString,
    val vendorName: String,
    val vendorRating: Decimal?,
    val vendorLogoUrl: String?,
    val offerId: UuidString,
    val basePrice: Decimal,
    val shippingEstimate: Decimal,
    val taxEstimate: Decimal,
    val totalCostEstimate: Decimal,
    val deliveryDays: Int,
    val inventoryCount: Int?,
    val currentDiscountPercentFromMsrp: Decimal,
    val incentives: List<String>? = null // e.g., ["free_shipping", "extended_warranty"]
)

@Serializable
data class VendorRuleDto(
    val ruleId: UuidString,
    val vendorId: UuidString,
    val ruleName: String,
    val triggerCondition: TriggerCondition, // Complex nested structure
    val counterAction: CounterAction,       // Complex nested structure
    val additionalIncentives: Map<String, Boolean>?, // e.g., {"free_shipping": true, "extended_warranty": false}
    val displayTemplateForCounterReason: String?, // e.g., "Beat competitor by ${amount_beaten_by_us} with Free Shipping!"
    val maxUsagePerSession: Int?,
    val priority: Int,
    val ruleHash: String
)

@Serializable
data class BiddingRulesResponse(
    val productId: UuidString,
    val productName: String,
    val productMsrp: Decimal,
    val productCategory: String,
    val qualifyingVendorsAndOffers: List<VendorOfferDto>,
    val vendorRules: List<VendorRuleDto>, // All relevant rules for the client engine
    val taxDetails: TaxDetailsDto?, // e.g., rate and applied amount for the location
    val shippingDetailsByVendor: Map<UuidString, ShippingDetailDto>? // vendorId -> shipping info
)

@Serializable
data class TaxDetailsDto(
    val location: String, // e.g., "CA_90210" or "US_DEFAULT"
    val rate: Decimal,    // e.g., 0.075 for 7.5%
    val note: String?     // e.g., "Estimated sales tax for ZIP 90210"
)

@Serializable
data class ShippingDetailDto(
    val cost: Decimal,
    val estimatedDeliveryDays: Int,
    val note: String? // e.g., "Standard shipping estimate."
)

// --- For POST /api/validate-selection ---
@Serializable
data class NegotiatedOfferDetailsDto( // What the client thinks the offer is
    val totalCost: Decimal,
    val basePrice: Decimal,
    val shippingCost: Decimal,
    val taxAmount: Decimal,
    val discountPercent: Decimal,
    val deliveryDays: Int,
    val appliedRuleIds: List<UuidString>? = emptyList(),
    val incentives: List<String>? = emptyList()
)

@Serializable
data class BiddingRoundSummaryDto(
    val roundNumber: Int,
    val userSelectedVendorId: UuidString?,
    val finalOfferFromVendor: VendorOfferDto?
)

@Serializable
data class SelectionValidationRequest(
    val sessionId: String, // Client-generated unique ID for this bidding session
    val productId: UuidString,
    val selectedVendorId: UuidString, // The vendor the user finally chose
    val finalOfferDetails: NegotiatedOfferDetailsDto, // The details of the offer user accepted
    val userLocation: String?,
    val biddingRoundsAudit: List<BiddingRoundSummaryDto>? = emptyList()
)

@Serializable
data class CheckoutInstructionDetailDto(
    val step: Int,
    val description: String,
    val actionUrl: String? = null // e.g., direct link for a coupon application page
)

@Serializable
data class CheckoutInstructionsDto(
    val method: CheckoutMethod,
    val primaryRedirectUrl: String, // Main URL to send the user (e.g. product page or cart)
    val couponCode: String? = null,
    val displayMessageToUser: String, // e.g., "You negotiated $X with Vendor Y."
    val detailedInstructions: List<CheckoutInstructionDetailDto>? = emptyList(),
    val supportContact: String? = null,
    val priceGuaranteeLevel: PriceGuaranteeLevel
)

@Serializable
data class SelectionValidationResponse(
    val isValid: Boolean, // Was the negotiated offer deemed valid by the backend?
    val message: String? = null, // e.g., "Offer validated" or "Price discrepancy noted"
    val checkoutInstructions: CheckoutInstructionsDto,
    val confirmedFinalPrice: Decimal, // Price confirmed/re-calculated by backend
    val originalVendorPriceBeforeDiscount: Decimal? = null // For savings calculation display
)