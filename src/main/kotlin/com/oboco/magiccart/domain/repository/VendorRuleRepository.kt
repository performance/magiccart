// src/main/kotlin/com/oboco/magiccart/domain/repository/VendorRuleRepository.kt
package com.oboco.magiccart.domain.repository

import com.oboco.magiccart.domain.model.RuleApplicabilityType
import com.oboco.magiccart.domain.model.VendorRule
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface VendorRuleRepository : CrudRepository<VendorRule, UUID> {

    // Find active rules for a specific vendor
    fun findByVendorIdAndActiveIsTrueOrderByPriorityDesc(vendorId: UUID): List<VendorRule>

    // More specific queries to get rules applicable to a context
    // This example fetches VENDOR_WIDE and CATEGORY specific rules for a vendor
    // Rules for SPECIFIC_PRODUCTS will be handled via VendorRuleProductRepository or a join in a more complex query
    @Query("""
        SELECT * FROM vendor_rules vr
        WHERE vr.vendor_id = :vendorId
        AND vr.active = true
        AND (
            vr.applicability_type = 'VENDOR_WIDE' OR
            (vr.applicability_type = 'CATEGORY' AND vr.applicable_category = :category)
        )
        ORDER BY vr.priority DESC
    """)
    fun findActiveVendorAndCategoryRules(
        @Param("vendorId") vendorId: UUID,
        @Param("category") category: String? // Nullable if not filtering by category
    ): List<VendorRule>

    // Find rules explicitly linked to a specific product via the join table
    // (This is a common pattern when you can't directly express the join in Spring Data JDBC's derived queries easily)
    // Alternatively, the service layer can combine results from different queries.
    @Query("""
        SELECT vr.* FROM vendor_rules vr
        JOIN vendor_rule_products vrp ON vr.rule_id = vrp.rule_id
        WHERE vr.vendor_id = :vendorId
        AND vr.active = true
        AND vr.applicability_type = 'SPECIFIC_PRODUCTS'
        AND vrp.product_id = :productId
        ORDER BY vr.priority DESC
    """)
    fun findActiveSpecificProductRules(
        @Param("vendorId") vendorId: UUID,
        @Param("productId") productId: UUID
    ): List<VendorRule>


    // For BUNDLE rules, the logic might be more complex and involve checking
    // `triggerCondition.requiredBundleProductIds`. The service layer might filter these.
    @Query("""
        SELECT * FROM vendor_rules vr
        WHERE vr.vendor_id = :vendorId
        AND vr.active = true
        AND vr.applicability_type = 'BUNDLE'
        ORDER BY vr.priority DESC
    """)
    fun findActiveBundleRulesForVendor(@Param("vendorId") vendorId: UUID): List<VendorRule>

}