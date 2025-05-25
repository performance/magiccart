// src/main/kotlin/com/oboco/magiccart/domain/repository/VendorRepository.kt
package com.oboco.magiccart.domain.repository

import com.oboco.magiccart.domain.model.Vendor
import com.oboco.magiccart.domain.model.VendorStatus
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface VendorRepository : CrudRepository<Vendor, UUID> {
    fun findByStatus(status: VendorStatus): List<Vendor>
}