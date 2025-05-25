// src/main/kotlin/com/oboco/magiccart/domain/converter/JsonbConverters.kt
package com.oboco.magiccart.domain.converter

import com.oboco.magiccart.domain.model.CounterAction
import com.oboco.magiccart.domain.model.TriggerCondition
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.postgresql.util.PGobject
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.stereotype.Component

// Helper function to create PGobject
fun createPgJsonObject(value: String?): PGobject {
    val jsonObject = PGobject()
    jsonObject.type = "jsonb"
    jsonObject.value = value
    return jsonObject
}

// --- TriggerCondition Converters ---
@Component
@WritingConverter
class TriggerConditionToPgObjectConverter(private val objectMapper: ObjectMapper) : Converter<TriggerCondition, PGobject> {
    override fun convert(source: TriggerCondition): PGobject {
        return createPgJsonObject(objectMapper.writeValueAsString(source))
    }
}

@Component
@ReadingConverter
class PgObjectToTriggerConditionConverter(private val objectMapper: ObjectMapper) : Converter<PGobject, TriggerCondition> {
    override fun convert(source: PGobject): TriggerCondition? {
        return source.value?.let { objectMapper.readValue(it, TriggerCondition::class.java) }
    }
}

// --- CounterAction Converters ---
@Component
@WritingConverter
class CounterActionToPgObjectConverter(private val objectMapper: ObjectMapper) : Converter<CounterAction, PGobject> {
    override fun convert(source: CounterAction): PGobject {
        return createPgJsonObject(objectMapper.writeValueAsString(source))
    }
}

@Component
@ReadingConverter
class PgObjectToCounterActionConverter(private val objectMapper: ObjectMapper) : Converter<PGobject, CounterAction> {
    override fun convert(source: PGobject): CounterAction? {
        return source.value?.let { objectMapper.readValue(it, CounterAction::class.java) }
    }
}

// --- Map<String, Boolean> for AdditionalIncentives ---
// We can use a similar pattern if we want to store it as JSONB,
// or Spring Data JDBC might handle Map<String, Boolean> to text with some DBs,
// but for JSONB, a converter is safer.

@Component
@WritingConverter
class IncentivesMapToPgObjectConverter(private val objectMapper: ObjectMapper) : Converter<Map<String, Boolean>, PGobject> {
    override fun convert(source: Map<String, Boolean>): PGobject {
        return createPgJsonObject(objectMapper.writeValueAsString(source))
    }
}

@Component
@ReadingConverter
class PgObjectToIncentivesMapConverter(private val objectMapper: ObjectMapper) : Converter<PGobject, Map<String, Boolean>> {
    override fun convert(source: PGobject): Map<String, Boolean>? {
        return source.value?.let { objectMapper.readValue(it, object : TypeReference<Map<String, Boolean>>() {}) }
    }
}

// --- String for Specifications (Product entity) to PGobject (if you want it strictly JSONB) ---
// If product.specifications is meant to be JSONB and not just text
@Component
@WritingConverter
class SpecificationsStringToPgObjectConverter(private val objectMapper: ObjectMapper) : Converter<String, PGobject> {
    override fun convert(source: String): PGobject {
        // Basic validation: try to parse to ensure it's valid JSON, or just pass through
        try {
            objectMapper.readTree(source) // Validate if it's JSON
        } catch (e: Exception) {
            // Handle invalid JSON string if necessary, or assume it's pre-validated
            // For robustness, you might want to ensure it's a valid JSON object/array string
        }
        return createPgJsonObject(source)
    }
}

@Component
@ReadingConverter
class PgObjectToSpecificationsStringConverter : Converter<PGobject, String> {
    override fun convert(source: PGobject): String? {
        return source.value
    }
}