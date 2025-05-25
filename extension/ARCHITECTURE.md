# MagicCart Extension Architecture

## Current Status: Phase 2.A Complete ✅

The MagicCart Chrome extension now has a **solid, extensible architecture** with both simplified and enhanced versions for different use cases.

## 📁 Source Code Organization

```
extension/src/jsMain/kotlin/
├── Main.kt                                    # Entry point
├── com/oboco/magiccart/
│   ├── ContentScript.kt                       # Main enhanced content script
│   ├── simple/
│   │   ├── SimpleContentScript.kt             # Simplified version for testing
│   │   └── README.md                          # Testing documentation
│   ├── api/
│   │   └── ApiClient.kt                       # API client foundation
│   └── utils/
│       └── ErrorHandler.kt                    # Centralized error handling
```

## 🏗️ Architecture Layers

### **1. Entry Point Layer**
- **Main.kt**: Initializes the extension on page load
- **Clean separation**: Easy to switch between simple/enhanced versions

### **2. Content Detection Layer** 
- **Multi-site support**: Amazon, Best Buy, Walmart, Target, eBay
- **Robust selectors**: Multiple fallback CSS selectors per vendor
- **Smart parsing**: Price extraction, category detection
- **Error resilience**: Graceful degradation when detection fails

### **3. UI Injection Layer**
- **DOM integration**: Clean overlay injection without conflicts
- **Responsive design**: Styled overlay with proper positioning
- **Interaction handling**: Close button, future API integration hooks
- **Duplicate prevention**: Checks for existing overlays

### **4. Error Handling Layer**
- **Centralized logging**: Consistent error handling across all components
- **User feedback**: Non-intrusive error notifications
- **Context awareness**: Different error types (API, detection, extension)
- **Debugging support**: Console logging for development

### **5. API Integration Layer**
- **Foundation ready**: API client structure prepared for Phase 2.B
- **Placeholder methods**: Mock implementations for testing
- **Type safety**: Serializable request/response DTOs
- **Async support**: Coroutines-based for non-blocking operations

## 🔧 Key Design Decisions

### **Dual Architecture Approach**
- **Enhanced Version**: Full feature set with error handling, API client, extensible overlay
- **Simple Version**: Minimal implementation for testing, debugging, and performance baselines
- **No conflicts**: Different function names and isolated directories

### **Phase 2.A Constraints Respected**
- **No React**: Avoided complex UI framework dependencies that caused build issues
- **Simplified DTOs**: Local data classes instead of complex shared module types
- **Mock API**: Placeholder implementations ready for real backend integration
- **Build stability**: Prioritized working extension over feature completeness

### **Extension Best Practices**
- **Manifest V3**: Modern Chrome extension standards
- **Minimal permissions**: Only necessary permissions requested
- **Clean manifest**: No references to missing files (background.js, popup.html, etc.)
- **Content script only**: Focused on core product detection functionality

## 🚀 Phase 2.B Integration Points

The architecture is specifically designed to make Phase 2.B development straightforward:

### **1. API Client Enhancement**
```kotlin
// Current (Phase 2.A)
suspend fun createBiddingSession(productInfo: Any): String {
    console.log("ApiClient: createBiddingSession called", productInfo)
    return "mock-session-id"
}

// Future (Phase 2.B) 
suspend fun createBiddingSession(request: CreateBiddingSessionRequest): BiddingSessionDto {
    val response = post("/bidding-sessions", request)
    return parseResponse(response)
}
```

### **2. Shared Module Integration**
```kotlin
// Current: Local DTOs
data class SimpleBiddingRequest(...)

// Future: Shared KMP DTOs  
// Simply import from :shared module
import com.oboco.magiccart.BiddingSessionDto
import com.oboco.magiccart.CreateBiddingSessionRequest
```

### **3. React UI Components**
```kotlin
// Current: HTML string injection
container.innerHTML = """<div>...</div>"""

// Future: React components
root.render(EnhancedApp.create {
    this.productInfo = productInfo
    this.apiClient = apiClient
})
```

### **4. Service Worker Addition**
- Add `background.js` reference to manifest
- Implement Chrome extension messaging
- Background processing for offers/notifications

## 🎯 Testing Strategy

### **Current Working Extension**
- Load `extension/dist/` as unpacked extension
- Test on any supported e-commerce site
- Verify product detection and overlay injection
- Check browser console for detailed logging

### **Development Testing**
- Use `SimpleContentScript.kt` for isolated testing
- Swap in `Main.kt` for different versions
- Console-based debugging with detailed error messages
- No backend dependency for core functionality testing

### **Future Integration Testing**
- API client placeholder methods ready for backend testing
- Error handling paths established for API failures
- Extension architecture supports background processes
- UI hooks prepared for React component integration

## 📊 Build Artifacts

```
extension/dist/
├── content.js          # 60KB minified Kotlin/JS bundle
├── manifest.json       # Clean Chrome extension manifest
├── README.md           # Installation and testing guide
└── test-sites.md       # Test URLs for all supported sites
```

**Ready for immediate Chrome extension installation and testing!** 🛒

The architecture successfully balances **immediate functionality** (working product detection) with **future extensibility** (API integration, React UI, service workers) while maintaining **build stability** and **testing flexibility**.