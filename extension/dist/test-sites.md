# Test Sites for MagicCart Extension

Use these URLs to test the extension on different e-commerce platforms:

## Amazon
- https://www.amazon.com/dp/B08N5WRWNW (Echo Dot)
- https://www.amazon.com/dp/B07XJ8C8F5 (Fire TV Stick)

## Best Buy
- https://www.bestbuy.com/site/apple-iphone-15-128gb/6525410.p (iPhone)
- https://www.bestbuy.com/site/samsung-galaxy-buds2-pro/6513002.p (Earbuds)

## Walmart
- https://www.walmart.com/ip/Apple-iPhone-15-128GB-Pink/5050904537
- https://www.walmart.com/ip/Samsung-Galaxy-S24-Ultra/5196447411

## Target
- https://www.target.com/p/apple-iphone-15-128gb/-/A-89960100
- https://www.target.com/p/nintendo-switch-console/-/A-52052007

## eBay
- https://www.ebay.com/itm/Apple-iPhone-15-128GB/12345 (replace with actual listing)
- https://www.ebay.com/itm/Samsung-Galaxy-Watch/67890 (replace with actual listing)

## Expected Behavior

1. **Page Load**: Extension automatically detects product pages
2. **Console Messages**: Check browser console (F12) for:
   - "MagicCart: Initializing on [hostname]"
   - "MagicCart: Product detected" (with product object)
   - "MagicCart: No product detected on this page"
3. **Visual Overlay**: White box appears in top-right corner with:
   - Product name
   - Current price
   - Vendor name
   - "Start Discount Negotiation" button
4. **Interaction**: Close button (×) removes overlay

## Troubleshooting

- **No overlay appears**: Check console for errors or "No product detected" message
- **Overlay appears but no data**: Product selectors may have changed; check console for detection errors
- **Extension not loading**: Ensure developer mode is enabled and extension is loaded from correct directory