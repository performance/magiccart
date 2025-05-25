// src/main/kotlin/com/oboco/magiccart/domain/repository/ProductRepository.kt
package com.oboco.magiccart.domain.repository

import com.oboco.magiccart.domain.model.Product
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ProductRepository : CrudRepository<Product, UUID> {
    // Custom query methods can be added here if needed
    // e.g., fun findByCategory(category: String): List<Product>
}