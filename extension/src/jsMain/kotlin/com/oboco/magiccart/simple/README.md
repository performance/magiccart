# Simple MagicCart Version

This directory contains a simplified version of the MagicCart extension for testing and mocking purposes.

## Purpose

- **Testing**: Minimal implementation without complex dependencies
- **Mocking**: Simple overlay injection for UI testing
- **Debugging**: Easy to understand codebase for troubleshooting
- **Baseline**: Reference implementation for core product detection logic

## Key Differences from Main Version

| Feature | Main Version | Simple Version |
|---------|--------------|----------------|
| **UI Framework** | React components | Plain HTML/CSS |
| **API Integration** | Full API client | Placeholder only |
| **Error Handling** | Centralized ErrorHandler | Basic console logging |
| **State Management** | Complex bidding states | Simple detection + overlay |
| **Type System** | Shared KMP DTOs | Local data classes |

## Usage

To use the simple version instead of the main version:

1. **Update Main.kt**:
   ```kotlin
   // Replace this:
   import com.oboco.magiccart.initializeMagicCart
   
   // With this:
   import com.oboco.magiccart.simple.initializeSimpleMagicCart
   
   fun main() {
       initializeSimpleMagicCart() // instead of initializeMagicCart()
   }
   ```

2. **Rebuild**: Run `./gradlew :extension:jsBrowserProductionWebpack`

3. **Test**: Load the extension and verify basic functionality

## When to Use Simple Version

- ✅ **Initial testing** of product detection logic
- ✅ **UI/UX mockups** without backend dependencies  
- ✅ **Performance baseline** measurements
- ✅ **Debugging** site-specific detection issues
- ✅ **Demo purposes** when backend is unavailable

## When to Use Main Version

- ✅ **Full feature development** (Phase 2.B+)
- ✅ **API integration** testing
- ✅ **React component** development
- ✅ **Production deployment** preparation
- ✅ **Complex error handling** scenarios

## File Structure

```
simple/
├── SimpleContentScript.kt    # Main detection & overlay logic
├── README.md                # This file
└── (future test files)      # Unit tests, mocks, etc.
```

This structure ensures the simple version can be maintained alongside the main architecture without conflicts.