// src/main/kotlin/com/oboco/magiccart/domain/repository/VendorRuleProductRepository.kt
package com.oboco.magiccart.domain.repository

import com.oboco.magiccart.domain.model.VendorRuleProduct
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface VendorRuleProductRepository : CrudRepository<VendorRuleProduct, UUID> {
    fun findByRuleId(ruleId: UUID): List<VendorRuleProduct>
    fun findByProductId(productId: UUID): List<VendorRuleProduct>
    fun findByRuleIdAndProductId(ruleId: UUID, productId: UUID): VendorRuleProduct?
}