package com.oboco.magiccart.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import java.math.BigDecimal

@ConfigurationProperties(prefix = "magiccart")
data class MagicCartProperties(
    val tax: TaxProperties = TaxProperties(),
    val shipping: ShippingProperties = ShippingProperties(),
    val vendor: VendorProperties = VendorProperties()
)

data class TaxProperties(
    val defaultRate: BigDecimal = BigDecimal("0.07"),
    val defaultLocation: String = "US_DEFAULT"
)

data class ShippingProperties(
    val defaultCost: BigDecimal = BigDecimal("5.00"),
    val defaultDeliveryDays: Int = 3
)

data class VendorProperties(
    val defaultIntegrationLevel: String = "ASSISTED"
)