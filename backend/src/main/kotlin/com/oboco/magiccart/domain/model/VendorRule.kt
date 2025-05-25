// src/main/kotlin/com/oboco/magiccart/domain/model/VendorRule.kt
package com.oboco.magiccart.domain.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("vendor_rules")
data class VendorRule(
    @Id val ruleId: UUID = UUID.randomUUID(),
    val vendorId: UUID,
    val ruleName: String,
    val applicabilityType: RuleApplicabilityType,
    val applicableCategory: String?, // Used if applicabilityType is CATEGORY
    // For BUNDLE applicability, triggerCondition.requiredBundleProductIds implies the scope
    // For SPECIFIC_PRODUCTS, we'll check VendorRuleProduct table separately

    val triggerCondition: TriggerCondition,
    val counterAction: CounterAction,
    val additionalIncentives: Map<String, Boolean>? = emptyMap(),

    val displayTemplateForCounterReason: String?,
    val maxUsagePerSession: Int?,
    val priority: Int,
    val ruleHash: String,
    val active: Boolean = true
)