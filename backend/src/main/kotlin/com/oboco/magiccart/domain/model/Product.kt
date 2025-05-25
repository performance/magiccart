// src/main/kotlin/com/oboco/magiccart/domain/model/Product.kt
package com.oboco.magiccart.domain.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Table("products")
data class Product(
    @Id val productId: UUID = UUID.randomUUID(),
    val name: String,
    val brand: String,
    val model: String,
    val category: String, // Could be an enum or a separate table if managed
    val msrp: BigDecimal,
    val specifications: String, // Assumed to be a JSON string, converter will handle to PGobject
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now()
)