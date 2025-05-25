// src/main/kotlin/com/oboco/magiccart/domain/model/rule_structures.kt
package com.oboco.magiccart.domain.model

import java.math.BigDecimal

// Reusable range structures
data class AmountRange(val min: BigDecimal? = null, val max: BigDecimal? = null)
data class IntRangeValue(val min: Int? = null, val max: Int? = null) // Renamed to avoid conflict with kotlin.ranges.IntRange
data class BigDecimalRange(val min: BigDecimal? = null, val max: BigDecimal? = null)


// Structured data classes for rules
data class TriggerCondition(
    val beatenByAmount: AmountRange? = null,
    val currentRound: IntRangeValue? = null,
    val competitorRating: BigDecimalRange? = null,
    val inventoryLevel: IntRangeValue? = null,
    val productCategory: String? = null, // For CATEGORY applicability
    val requiredBundleProductIds: List<String>? = null // For BUNDLE applicability
)

data class CounterAction(
    val action: ActionType,
    val modifier: BigDecimal? = null, // e.g., for MATCH_TOTAL_COST, could be +$5 or -$2
    val amount: BigDecimal? = null,   // e.g., for BEAT_TOTAL_COST_BY X amount
    val maxDiscountPercent: BigDecimal? = null // Overall constraint
)

// For additionalIncentives in VendorRule (Map<String, Boolean> is fine for Jackson)
// If you need more structure, you could define a class:
// data class Incentive(val type: String, val value: Any)