// src/main/kotlin/com/oboco/magiccart/web/SelectionValidationController.kt
package com.oboco.magiccart.web

import com.oboco.magiccart.dto.SelectionValidationRequest
import com.oboco.magiccart.dto.SelectionValidationResponse
import com.oboco.magiccart.service.SelectionValidationService
import com.oboco.magiccart.dto.CheckoutInstructionsDto
import com.oboco.magiccart.domain.model.CheckoutMethod
import com.oboco.magiccart.domain.model.PriceGuaranteeLevel
import jakarta.validation.Valid // For Spring Boot 3.x validation
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/validate-selection")
class SelectionValidationController(
    private val selectionValidationService: SelectionValidationService
) {
    private val logger = LoggerFactory.getLogger(SelectionValidationController::class.java)

    @PostMapping
    fun validateSelection(
        @Valid @RequestBody request: SelectionValidationRequest // @Valid enables bean validation if DTOs have annotations
    ): ResponseEntity<SelectionValidationResponse> {
        logger.info("Received selection validation request for session: {}", request.sessionId)
        try {
            val response = selectionValidationService.validateAndProcessSelection(request)
            return if (response.isValid) {
                ResponseEntity.ok(response)
            } else {
                // Even if "isValid" is false due to backend checks, we might still return OK
                // with instructions, but the message will indicate issues.
                // A 400 Bad Request might be if the request DTO itself is malformed.
                ResponseEntity.ok(response) // Client should check the 'isValid' flag and message.
            }
        } catch (e: NoSuchElementException) {
            logger.warn("Resource not found during selection validation (e.g., vendor/product): ${e.message}")
            // Construct a more informative error response if possible
            return ResponseEntity.badRequest().body(
                // Create a minimal error response
                SelectionValidationResponse(
                    isValid = false,
                    message = "Validation Error: ${e.message}",
                    // Provide minimal, safe checkout instructions
                    checkoutInstructions = CheckoutInstructionsDto(
                        method = CheckoutMethod.ASSISTED,
                        primaryRedirectUrl = "https://google.com",
                        displayMessageToUser = "Error occurred during validation",
                        priceGuaranteeLevel = PriceGuaranteeLevel.MANUAL_VERIFICATION
                    ),
                    confirmedFinalPrice = request.finalOfferDetails.totalCost,
                    originalVendorPriceBeforeDiscount = null
                )
            )
        } catch (e: Exception) {
            logger.error("Error processing selection validation for session {}: {}", request.sessionId, e.message, e)
            return ResponseEntity.internalServerError().body(
                SelectionValidationResponse(
                    isValid = false,
                    message = "An unexpected error occurred during validation.",
                    checkoutInstructions = CheckoutInstructionsDto(
                        method = CheckoutMethod.ASSISTED,
                        primaryRedirectUrl = "https://google.com",
                        displayMessageToUser = "Unexpected error occurred",
                        priceGuaranteeLevel = PriceGuaranteeLevel.MANUAL_VERIFICATION
                    ),
                    confirmedFinalPrice = request.finalOfferDetails.totalCost,
                    originalVendorPriceBeforeDiscount = null
                )
            )
        }
    }
}