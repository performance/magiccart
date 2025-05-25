// src/main/kotlin/com/oboco/magiccart/web/BiddingRulesController.kt
package com.oboco.magiccart.web

import com.oboco.magiccart.dto.BiddingRulesRequestParams
import com.oboco.magiccart.dto.BiddingRulesResponse
import com.oboco.magiccart.service.BiddingRulesService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.util.UUID

@RestController
@RequestMapping("/api/bidding-rules")
class BiddingRulesController(private val biddingRulesService: BiddingRulesService) {
    private val logger = LoggerFactory.getLogger(BiddingRulesController::class.java)

    @GetMapping
    fun getBiddingRules(
        @RequestParam productId: UUID,
        @RequestParam(required = false) userLocation: String?, // e.g. "90210" or "US_DEFAULT" for MVP
        @RequestParam(required = false) requestedDiscountPercent: BigDecimal?,
        @RequestParam(required = false) bundleProductIds: List<UUID>?
    ): ResponseEntity<BiddingRulesResponse> {
        logger.info(
            "Received request for bidding rules. ProductId: {}, Location: {}, ReqDiscount: {}, BundleIds: {}",
            productId, userLocation, requestedDiscountPercent, bundleProductIds
        )
        try {
            val params = BiddingRulesRequestParams(
                productId = productId,
                userLocation = userLocation ?: "US_DEFAULT", // Default location for MVP
                requestedDiscountPercent = requestedDiscountPercent,
                bundleProductIds = bundleProductIds
            )
            val response = biddingRulesService.getBiddingRules(params)
            return ResponseEntity.ok(response)
        } catch (e: NoSuchElementException) {
            logger.warn("Resource not found for getBiddingRules: ${e.message}")
            return ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Error processing getBiddingRules for productId: $productId", e)
            // Consider a more specific error response DTO for the client
            return ResponseEntity.internalServerError().body(null) // Or a structured error response
        }
    }
}