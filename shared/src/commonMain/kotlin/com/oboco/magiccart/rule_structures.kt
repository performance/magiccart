package com.oboco.magiccart

import kotlinx.serialization.Serializable

// Reusable range structures
@Serializable
data class AmountRange(val min: Decimal? = null, val max: Decimal? = null)

@Serializable
data class IntRangeValue(val min: Int? = null, val max: Int? = null) // Renamed to avoid conflict with kotlin.ranges.IntRange

@Serializable
data class DecimalRange(val min: Decimal? = null, val max: Decimal? = null)


// Structured data classes for rules
@Serializable
data class TriggerCondition(
    val beatenByAmount: AmountRange? = null,
    val currentRound: IntRangeValue? = null,
    val competitorRating: DecimalRange? = null,
    val inventoryLevel: IntRangeValue? = null,
    val productCategory: String? = null, // For CATEGORY applicability
    val requiredBundleProductIds: List<UuidString>? = null // For BUNDLE applicability
)

@Serializable
data class CounterAction(
    val action: ActionType,
    val modifier: Decimal? = null, // e.g., for MATCH_TOTAL_COST, could be +$5 or -$2
    val amount: Decimal? = null,   // e.g., for BEAT_TOTAL_COST_BY X amount
    val maxDiscountPercent: Decimal? = null // Overall constraint
)

// Additional incentives structure
@Serializable
data class Incentive(val type: String, val enabled: Boolean)