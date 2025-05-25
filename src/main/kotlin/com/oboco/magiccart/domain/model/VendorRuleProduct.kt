// src/main/kotlin/com/oboco/magiccart/domain/model/VendorRuleProduct.kt
package com.oboco.magiccart.domain.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("vendor_rule_products") // The actual join table
data class VendorRuleProduct(
    // A composite primary key (ruleId, productId) is common for join tables in SQL,
    // but Spring Data JDBC often prefers a single @Id. We can use a surrogate key.
    @Id val id: UUID = UUID.randomUUID(),
    val ruleId: UUID, // FK to vendor_rules
    val productId: UUID // FK to products
)