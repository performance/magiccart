package com.oboco.magiccart

import kotlinx.serialization.Serializable

@Serializable
enum class RuleApplicabilityType {
    CATEGORY, SPECIFIC_PRODUCTS, BUNDLE, VENDOR_WIDE
}

@Serializable
enum class ActionType {
    MATCH_TOTAL_COST, BEAT_TOTAL_COST_BY, MATCH_PRICE, BEAT_ANY_OFFER
}

@Serializable
enum class CheckoutMethod {
    DEEP_API, AFFILIATE_PARAMS, COUPON_CODES, ASSISTED
}

@Serializable
enum class PriceGuaranteeLevel {
    GUARANTEED, LIKELY, MANUAL_VERIFICATION
}

@Serializable
enum class VendorStatus {
    ACTIVE, INACTIVE, SUSPENDED
}

@Serializable
enum class VendorIntegrationLevel {
    DEEP_API, AFFILIATE_PARAMS, COUPON_CODES, ASSISTED
}