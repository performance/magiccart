// src/main/kotlin/com/oboco/magiccart/config/JdbcConvertersConfig.kt
package com.oboco.magiccart.config

import com.oboco.magiccart.domain.converter.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration

@Configuration
class JdbcConvertersConfig(
    private val triggerConditionToPgObjectConverter: TriggerConditionToPgObjectConverter,
    private val pgObjectToTriggerConditionConverter: PgObjectToTriggerConditionConverter,
    private val counterActionToPgObjectConverter: CounterActionToPgObjectConverter,
    private val pgObjectToCounterActionConverter: PgObjectToCounterActionConverter,
    private val incentivesMapToPgObjectConverter: IncentivesMapToPgObjectConverter,
    private val pgObjectToIncentivesMapConverter: PgObjectToIncentivesMapConverter,
    private val specificationsStringToPgObjectConverter: SpecificationsStringToPgObjectConverter,
    private val pgObjectToSpecificationsStringConverter: PgObjectToSpecificationsStringConverter
) : AbstractJdbcConfiguration() {

    @Bean
    override fun jdbcCustomConversions(): JdbcCustomConversions {
        return JdbcCustomConversions(
            listOf(
                triggerConditionToPgObjectConverter,
                pgObjectToTriggerConditionConverter,
                counterActionToPgObjectConverter,
                pgObjectToCounterActionConverter,
                incentivesMapToPgObjectConverter,
                pgObjectToIncentivesMapConverter,
                specificationsStringToPgObjectConverter,
                pgObjectToSpecificationsStringConverter
                // Add any other custom converters here
            )
        )
    }
}