// src/main/kotlin/com/oboco/magiccart/service/BiddingRulesService.kt
package com.oboco.magiccart.service

import com.oboco.magiccart.domain.model.RuleApplicabilityType
import com.oboco.magiccart.domain.repository.*
import com.oboco.magiccart.dto.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.util.*

@Service
class BiddingRulesService(
    private val productRepository: ProductRepository,
    private val vendorRepository: VendorRepository,
    private val vendorOfferRepository: VendorOfferRepository,
    private val vendorRuleRepository: VendorRuleRepository,
    private val vendorRuleProductRepository: VendorRuleProductRepository,
    @Qualifier("placeholderTaxService") private val taxService: TaxService,
    @Qualifier("placeholderShippingService") private val shippingService: ShippingService
) {
    private val logger = LoggerFactory.getLogger(BiddingRulesService::class.java)

    fun getBiddingRules(params: BiddingRulesRequestParams): BiddingRulesResponse {
        logger.info("Fetching bidding rules for productId: ${params.productId}, location: ${params.userLocation}")

        val product = productRepository.findById(params.productId)
            .orElseThrow {
                logger.warn("Product not found: ${params.productId}")
                NoSuchElementException("Product not found: ${params.productId}")
            }

        // 1. Find active and valid offers for this product
        val activeRawOffers = vendorOfferRepository.findActiveAndValidOffersByProductId(params.productId, LocalDateTime.now())
        if (activeRawOffers.isEmpty()) {
            logger.info("No active offers found for product: ${params.productId}")
            // Return empty or specific response indicating no offers
            return BiddingRulesResponse(
                productId = product.productId,
                productName = product.name,
                productMsrp = product.msrp,
                productCategory = product.category,
                qualifyingVendorsAndOffers = emptyList(),
                vendorRules = emptyList(),
                taxDetails = null, // Or default based on location if available
                shippingDetailsByVendor = emptyMap()
            )
        }

        val vendorOfferDtos = mutableListOf<VendorOfferDto>()
        val allApplicableVendorRuleDtos = mutableListOf<VendorRuleDto>()
        val shippingDetailsByVendor = mutableMapOf<UUID, ShippingDetailDto>()

        val uniqueVendorIdsFromOffers = activeRawOffers.map { it.vendorId }.distinct()
        val vendorsMap = vendorRepository.findAllById(uniqueVendorIdsFromOffers).associateBy { it.vendorId }

        activeRawOffers.forEach { offer ->
            val vendor = vendorsMap[offer.vendorId]
            if (vendor == null) {
                logger.warn("Vendor details not found for vendorId: ${offer.vendorId} from offer: ${offer.offerId}")
                return@forEach // Skip this offer if vendor data is missing
            }

            val taxResult = taxService.calculateTax(offer.basePrice, params.userLocation)
            val shippingDetail = shippingService.calculateShipping(vendor.vendorId, product.productId, params.userLocation)
            shippingDetailsByVendor[vendor.vendorId] = shippingDetail

            val totalCost = offer.basePrice.add(taxResult.taxAmount).add(shippingDetail.cost)
            val discountFromMsrp = if (product.msrp > BigDecimal.ZERO) {
                (product.msrp.subtract(totalCost))
                    .divide(product.msrp, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal("100"))
            } else BigDecimal.ZERO

            // MVP: For now, we're not filtering by params.requestedDiscountPercent on the backend.
            // The client will handle initial display based on what it receives.

            vendorOfferDtos.add(
                VendorOfferDto(
                    vendorId = vendor.vendorId,
                    vendorName = vendor.name,
                    vendorRating = vendor.rating,
                    vendorLogoUrl = vendor.logoUrl,
                    offerId = offer.offerId,
                    basePrice = offer.basePrice,
                    shippingEstimate = shippingDetail.cost,
                    taxEstimate = taxResult.taxAmount,
                    totalCostEstimate = totalCost.setScale(2, RoundingMode.HALF_UP),
                    deliveryDays = shippingDetail.estimatedDeliveryDays, // Use shipping service's day estimate
                    inventoryCount = offer.inventoryCount,
                    currentDiscountPercentFromMsrp = discountFromMsrp.setScale(2, RoundingMode.HALF_UP)
                    // incentives initially from offer if any, rules might add more client-side
                )
            )

            // 2. Fetch rules for this vendor, applicable to this product context (Simplified for Iteration 1)
            // Iteration 1: Fetch VENDOR_WIDE and CATEGORY rules + SPECIFIC_PRODUCT rules + BUNDLE rules
            // Client-side engine will do the final filtering for BUNDLE based on `triggerCondition.requiredBundleProductIds`
            // and `params.bundleProductIds`

            val vendorAndCategoryRules = vendorRuleRepository.findActiveVendorAndCategoryRules(vendor.vendorId, product.category)
            val specificProductRules = vendorRuleRepository.findActiveSpecificProductRules(vendor.vendorId, product.productId)
            val bundleRules = vendorRuleRepository.findActiveBundleRulesForVendor(vendor.vendorId)
                // Further filter bundle rules if `params.bundleProductIds` is provided and matches rule's requirements
                // For Iteration 1, we pass all, client-side can filter based on actual context.


            (vendorAndCategoryRules + specificProductRules + bundleRules)
                .distinctBy { it.ruleId } // Avoid duplicates if a rule somehow matches multiple criteria
                .forEach { ruleEntity ->
                allApplicableVendorRuleDtos.add(
                    VendorRuleDto(
                        ruleId = ruleEntity.ruleId,
                        vendorId = ruleEntity.vendorId,
                        ruleName = ruleEntity.ruleName,
                        triggerCondition = ruleEntity.triggerCondition, // Already converted object
                        counterAction = ruleEntity.counterAction,       // Already converted object
                        additionalIncentives = ruleEntity.additionalIncentives,
                        displayTemplateForCounterReason = ruleEntity.displayTemplateForCounterReason,
                        maxUsagePerSession = ruleEntity.maxUsagePerSession,
                        priority = ruleEntity.priority,
                        ruleHash = ruleEntity.ruleHash
                    )
                )
            }
        }
        
        // Ensure rules are unique across all vendors if a rule was misconfigured to be global (though rules are vendor-specific)
        // The current DTO list `allApplicableVendorRuleDtos` correctly contains rules per vendor.

        logger.info("Successfully fetched ${vendorOfferDtos.size} offers and ${allApplicableVendorRuleDtos.size} rules for productId: ${params.productId}")
        return BiddingRulesResponse(
            productId = product.productId,
            productName = product.name,
            productMsrp = product.msrp,
            productCategory = product.category,
            qualifyingVendorsAndOffers = vendorOfferDtos.sortedBy { it.totalCostEstimate }, // Sort by price for user
            vendorRules = allApplicableVendorRuleDtos.distinctBy { it.ruleId }, // Ensure overall unique rules sent to client
            taxDetails = taxService.calculateTax(BigDecimal.ONE, params.userLocation).details, // Pass general tax info
            shippingDetailsByVendor = shippingDetailsByVendor
        )
    }
}