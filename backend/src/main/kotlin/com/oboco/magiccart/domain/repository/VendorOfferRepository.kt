// src/main/kotlin/com/oboco/magiccart/domain/repository/VendorOfferRepository.kt
package com.oboco.magiccart.domain.repository

import com.oboco.magiccart.domain.model.VendorOffer
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface VendorOfferRepository : CrudRepository<VendorOffer, UUID> {

    // Find active offers for a specific product that are currently valid
    @Query("""
        SELECT * FROM vendor_offers vo
        WHERE vo.product_id = :productId
        AND vo.is_active = true
        AND vo.valid_from <= :currentTime
        AND (vo.valid_until IS NULL OR vo.valid_until >= :currentTime)
    """)
    fun findActiveAndValidOffersByProductId(
        @Param("productId") productId: UUID,
        @Param("currentTime") currentTime: LocalDateTime = LocalDateTime.now()
    ): List<VendorOffer>

    fun findByVendorIdAndProductId(vendorId: UUID, productId: UUID): List<VendorOffer>
}