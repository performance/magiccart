-- Clear existing data if re-running (order matters due to foreign keys)
DELETE FROM vendor_rule_products;
DELETE FROM vendor_rules;
DELETE FROM vendor_offers;
DELETE FROM bidding_sessions_analytics; -- If you add data here for testing
DELETE FROM vendors;
DELETE FROM products;

-- PRODUCTS
-- Using specific UUIDs for consistent foreign key references
INSERT INTO products (product_id, name, brand, model, category, msrp, specifications, created_at, updated_at) VALUES
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Sony WH-1000XM5 Noise Cancelling Headphones', 'Sony', 'WH-1000XM5', 'Electronics', 399.99,
  '{"weight": "250g", "bluetooth": "5.2", "battery_life": "30 hours", "features": ["Multipoint", "LDAC", "Adaptive Sound Control"]}'::jsonb,
  NOW(), NOW()),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'Canon EOS R6 Mark II Mirrorless Camera (Body Only)', 'Canon', 'EOS R6 Mark II', 'Cameras', 2499.00,
  '{"sensor_type": "CMOS", "megapixels": "24.2", "iso_range": "100-102400", "video_resolution": "4K 60p"}'::jsonb,
  NOW(), NOW());

-- VENDORS
INSERT INTO vendors (vendor_id, name, rating, logo_url, integration_level, affiliate_base_url, product_url_template, support_contact, status, created_at, updated_at) VALUES
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a21', 'Best Electronics Store', 4.5, 'https://example.com/logos/bestelectronics.png', 'ASSISTED', 'https://bestelectronics.com/track?id=OURAFFID&url=', 'https://bestelectronics.com/product/{productId}', 'support@bestelectronics.com', 'ACTIVE', NOW(), NOW()),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'ProCam Gear', 4.8, 'https://example.com/logos/procam.png', 'ASSISTED', 'https://procamgear.com/partner?ref=OURREF&dest=', 'https://procamgear.com/item/{productId}', 'help@procamgear.com', 'ACTIVE', NOW(), NOW()),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a23', 'Discount Gadgets Co.', 3.9, 'https://example.com/logos/discountgadgets.png', 'ASSISTED', NULL, 'https://discountgadgets.co/product_page.php?pid={productId}', NULL, 'ACTIVE', NOW(), NOW());

-- VENDOR OFFERS
-- Sony WH-1000XM5
INSERT INTO vendor_offers (offer_id, vendor_id, product_id, base_price, shipping_cost, tax_rate_applicable, delivery_days, inventory_count, is_active, created_at, updated_at) VALUES
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a31', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a21', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 348.00, 0.00, 0.07, 2, 50, true, NOW(), NOW()), -- Best Electronics
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a32', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a23', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 340.00, 5.99, 0.07, 5, 15, true, NOW(), NOW()); -- Discount Gadgets

-- Canon EOS R6 Mark II
INSERT INTO vendor_offers (offer_id, vendor_id, product_id, base_price, shipping_cost, tax_rate_applicable, delivery_days, inventory_count, is_active, created_at, updated_at) VALUES
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a21', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 2499.00, 0.00, 0.07, 3, 10, true, NOW(), NOW()), -- Best Electronics
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a34', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 2450.00, 10.50, 0.07, 2, 5, true, NOW(), NOW()); -- ProCam Gear

-- VENDOR RULES
-- Rule 1: Best Electronics - Generic discount for Electronics if beaten
INSERT INTO vendor_rules (rule_id, vendor_id, rule_name, applicability_type, applicable_category, trigger_condition, counter_action, additional_incentives, display_template_for_counter_reason, priority, rule_hash, active, created_at, updated_at) VALUES
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a41', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a21', 'Electronics Price Beat', 'CATEGORY', 'Electronics',
  '{"beatenByAmount": {"min": 0.01, "max": 50.00}}'::jsonb,
  '{"action": "BEAT_TOTAL_COST_BY", "amount": 5.00, "maxDiscountPercent": 15.0}'::jsonb,
  '{"free_shipping": true}'::jsonb,
  'Beat competitor by ${amount_beaten_by_us_plus_5_or_so} with Free Shipping!',
  10, 'hash_electronics_beat_rule', true, NOW(), NOW());

-- Rule 2: ProCam Gear - Aggressive match for Cameras if high rated competitor
INSERT INTO vendor_rules (rule_id, vendor_id, rule_name, applicability_type, applicable_category, trigger_condition, counter_action, additional_incentives, display_template_for_counter_reason, priority, rule_hash, active, created_at, updated_at) VALUES
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a42', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Camera High-Rated Match', 'CATEGORY', 'Cameras',
  '{"competitorRating": {"min": 4.0}, "beatenByAmount": {"min": 10.00, "max": 200.00}}'::jsonb,
  '{"action": "MATCH_TOTAL_COST", "modifier": -2.00, "maxDiscountPercent": 10.0}'::jsonb, -- Match and beat by $2
  '{"expedited_delivery_available": true}'::jsonb,
  'Matched price & offering expedited delivery!',
  20, 'hash_camera_match_rule', true, NOW(), NOW());

-- Rule 3: Discount Gadgets - VENDOR_WIDE rule, always try to beat by a small amount
INSERT INTO vendor_rules (rule_id, vendor_id, rule_name, applicability_type, applicable_category, trigger_condition, counter_action, display_template_for_counter_reason, priority, rule_hash, active, created_at, updated_at) VALUES
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a43', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a23', 'Always Undercut', 'VENDOR_WIDE', NULL,
  '{"beatenByAmount": {"min": 0.01}}'::jsonb, -- Trigger if beaten by any amount
  '{"action": "BEAT_TOTAL_COST_BY", "amount": 1.50, "maxDiscountPercent": 25.0}'::jsonb,
  'We can beat that price!',
  5, 'hash_vendor_undercut_rule', true, NOW(), NOW());

-- Rule 4: ProCam Gear - Rule for specific product (Canon EOS R6 Mark II)
INSERT INTO vendor_rules (rule_id, vendor_id, rule_name, applicability_type, trigger_condition, counter_action, additional_incentives, display_template_for_counter_reason, priority, rule_hash, active, created_at, updated_at) VALUES
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Canon R6M2 Special Offer', 'SPECIFIC_PRODUCTS',
  '{"currentRound": {"min": 2}, "beatenByAmount": {"min": 5.00}}'::jsonb, -- Only trigger from round 2 if beaten
  '{"action": "MATCH_TOTAL_COST", "modifier": 0.00, "maxDiscountPercent": 8.0}'::jsonb, -- Exact match
  '{"free_128gb_sd_card": true}'::jsonb,
  'Matched price + Free 128GB SD Card for your Canon R6M2!',
  15, 'hash_canon_r6m2_specific_rule', true, NOW(), NOW());

-- VENDOR_RULE_PRODUCTS (Link Rule 4 to Canon EOS R6 Mark II)
INSERT INTO vendor_rule_products (id, rule_id, product_id) VALUES
(gen_random_uuid(), 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12');

-- Example of a BUNDLE rule (conceptual, client-side needs to interpret triggerCondition.requiredBundleProductIds)
-- Let's say vendor 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22' (ProCam Gear) offers a bundle for
-- Canon R6M2 ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12') and a lens (assume lens_product_id = 'e0eebc99-...')
-- The rule's trigger_condition would contain something like: "requiredBundleProductIds": ["e0eebc99-..."]
-- And it would only apply if the BiddingRulesRequest contains BOTH product IDs in its bundleProductIds param
-- AND the user is trying to get a discount on the Canon R6M2 (the main product for this rule context).
-- For now, we are just seeding a rule that *could* be a bundle rule.
INSERT INTO vendor_rules (rule_id, vendor_id, rule_name, applicability_type, trigger_condition, counter_action, display_template_for_counter_reason, priority, rule_hash, active, created_at, updated_at) VALUES
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a45', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Camera + Lens Kit Discount', 'BUNDLE',
  '{"requiredBundleProductIds": ["dummy_lens_product_id_for_trigger"], "beatenByAmount": {"min": 20.00}}'::jsonb,
  '{"action": "BEAT_TOTAL_COST_BY", "amount": 50.00, "maxDiscountPercent": 12.0}'::jsonb,
  'Special Kit Discount: Save an extra $50 on your Camera + Lens bundle!',
  25, 'hash_camera_lens_bundle_rule', true, NOW(), NOW());

-- Note: For the bundle rule above, you'd need to create a dummy product for 'dummy_lens_product_id_for_trigger' if your FKs are strict,
-- or adjust the trigger condition not to rely on a product ID that doesn't exist.
-- For client-side logic, `triggerCondition.requiredBundleProductIds` tells the client what other products (by their UUIDs)
-- must *also* be part of the user's discount request (e.g., in the `bundleProductIds` param of the API call,
-- or identified by the client from the user's cart) for this BUNDLE rule to be considered.

SELECT 'Data seeding completed.' as status;