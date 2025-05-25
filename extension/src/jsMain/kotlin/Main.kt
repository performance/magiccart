/**
 * Main entry point for MagicCart Chrome Extension content script
 * 
 * This initializes the full MagicCart architecture:
 * - Product detection across 5 e-commerce sites
 * - Error handling and user feedback
 * - Extensible overlay system ready for React UI
 * - API client integration points
 */

import com.oboco.magiccart.initializeMagicCart

fun main() {
    initializeMagicCart()
}