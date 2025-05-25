package com.oboco.magiccart

import kotlinx.serialization.Serializable

/**
 * Cross-platform decimal type for monetary values
 * Uses Double for KMP compatibility, with proper serialization
 */
@Serializable
data class Decimal(val value: Double) {
    override fun toString(): String = value.toString()
    
    operator fun plus(other: Decimal): Decimal = Decimal(value + other.value)
    operator fun minus(other: Decimal): Decimal = Decimal(value - other.value)
    operator fun times(other: Decimal): Decimal = Decimal(value * other.value)
    operator fun div(other: Decimal): Decimal = Decimal(value / other.value)
    operator fun compareTo(other: Decimal): Int = value.compareTo(other.value)
    
    companion object {
        val ZERO = Decimal(0.0)
        fun fromDouble(value: Double) = Decimal(value)
        fun fromString(value: String) = Decimal(value.toDouble())
    }
}

/**
 * Cross-platform UUID type
 * Uses String representation for KMP compatibility
 */
@Serializable
data class UuidString(val value: String) {
    override fun toString(): String = value
    
    companion object {
        fun randomUUID(): UuidString {
            // Platform-specific UUID generation would be implemented via expect/actual
            return UuidString(kotlin.random.Random.nextLong().toString())
        }
        
        fun fromString(value: String): UuidString = UuidString(value)
    }
}