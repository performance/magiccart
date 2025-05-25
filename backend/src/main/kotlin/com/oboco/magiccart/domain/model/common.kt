// src/main/kotlin/com/oboco/magiccart/domain/model/common.kt
package com.oboco.magiccart.domain.model

import java.math.BigDecimal
import java.util.UUID

enum class RuleApplicabilityType {
    CATEGORY, SPECIFIC_PRODUCTS, BUNDLE, VENDOR_WIDE
}

enum class ActionType {
    MATCH_TOTAL_COST, BEAT_TOTAL_COST_BY, MATCH_PRICE, BEAT_ANY_OFFER
}

enum class CheckoutMethod {
    DEEP_API, AFFILIATE_PARAMS, COUPON_CODES, ASSISTED
}

enum class PriceGuaranteeLevel {
    GUARANTEED, LIKELY, MANUAL_VERIFICATION
}

enum class VendorStatus {
    ACTIVE, INACTIVE, SUSPENDED
}

enum class VendorIntegrationLevel {
    DEEP_API, AFFILIATE_PARAMS, COUPON_CODES, ASSISTED
}