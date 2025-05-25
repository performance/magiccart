# MagicCart Chrome Extension - Testing Build

This is a minimal working build of the MagicCart Chrome extension for testing.

## What This Extension Does

- **Product Detection**: Automatically detects products on supported e-commerce sites:
  - Amazon
  - Best Buy  
  - Walmart
  - Target
  - eBay

- **Price Extraction**: Extracts product name, current price, vendor, and category information

- **Visual Overlay**: Injects a simple overlay on product pages with:
  - Product information summary
  - "Start Discount Negotiation" button (placeholder for now)

## Installation

1. Open Chrome and go to `chrome://extensions/`
2. Enable "Developer mode" (toggle in top right)
3. Click "Load unpacked"
4. Select this `dist` directory
5. The extension should now be loaded and active

## Testing

1. Navigate to any product page on supported sites (e.g., amazon.com/dp/...)
2. You should see a white overlay box appear in the top-right corner
3. The overlay shows detected product information
4. Check browser console (`F12` > Console) for debug messages

## Browser Console Output

The extension logs its activity to the browser console:
- `MagicCart: Initializing on <hostname>`
- `MagicCart: Product detected` (with product object)
- `MagicCart: No product detected on this page`

## Current Status

✅ **Working:**
- Product detection on all 5 supported sites
- Price extraction with regex parsing
- Category detection from breadcrumbs/title keywords
- DOM injection with styled overlay
- Cross-site compatibility

⏳ **Next Phase (2.B):**
- Backend API integration
- Real bidding workflow
- React-based UI components
- Service worker for background operations
- Settings and storage management

## Technical Details

- **Built with**: Kotlin/JS + Kotlin Multiplatform
- **Bundle size**: ~60KB minified (includes enhanced architecture)
- **Manifest**: Version 3 (Chrome Extensions MV3)
- **Target**: Chrome 88+ (Manifest V3 support)
- **Architecture**: 
  - Centralized error handling
  - API client foundation (placeholder methods)
  - Extensible overlay system
  - Simple and enhanced versions available

## Architecture Highlights

✅ **Error Handling**: Centralized ErrorHandler with user-friendly notifications  
✅ **API Client**: Foundation ready for Phase 2.B backend integration  
✅ **Overlay System**: Enhanced injection with proper error handling  
✅ **Testing Support**: Both simple and enhanced versions maintained  
✅ **Extension Architecture**: Proper Chrome extension patterns