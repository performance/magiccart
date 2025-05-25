-- Drop tables if they exist (useful for iterative development)
DROP TABLE IF EXISTS vendor_rule_products CASCADE;
DROP TABLE IF EXISTS vendor_rules CASCADE;
DROP TABLE IF EXISTS vendor_offers CASCADE;
DROP TABLE IF EXISTS vendors CASCADE;
DROP TABLE IF EXISTS products CASCADE;

-- Enable pgcrypto for gen_random_uuid() if not already enabled
-- CREATE EXTENSION IF NOT EXISTS "pgcrypto"; -- Requires superuser typically, or enable in your DB manually

-- Products Table
CREATE TABLE products (
    product_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    brand VARCHAR(255),
    model VARCHAR(255),
    category VARCHAR(100) NOT NULL,
    msrp DECIMAL(12, 2) NOT NULL CHECK (msrp >= 0),
    specifications JSONB, -- Storing as JSONB directly
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Vendors Table
CREATE TABLE vendors (
    vendor_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE, -- Assuming vendor names are unique
    rating DECIMAL(3, 2) CHECK (rating >= 0 AND rating <= 5),
    logo_url TEXT,
    integration_level VARCHAR(50) NOT NULL DEFAULT 'ASSISTED', -- DEEP_API, AFFILIATE_PARAMS, COUPON_CODES, ASSISTED
    affiliate_base_url TEXT,
    api_endpoint TEXT,
    coupon_api_endpoint TEXT,
    support_contact VARCHAR(255),
    product_url_template TEXT, -- e.g., 'https://vendor.com/products/{productId}'
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE, SUSPENDED
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Vendor Offers Table
CREATE TABLE vendor_offers (
    offer_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vendor_id UUID NOT NULL REFERENCES vendors(vendor_id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    base_price DECIMAL(12, 2) NOT NULL CHECK (base_price >= 0),
    shipping_cost DECIMAL(10, 2) NOT NULL CHECK (shipping_cost >= 0),
    tax_rate_applicable DECIMAL(5, 4) NOT NULL CHECK (tax_rate_applicable >= 0 AND tax_rate_applicable <= 1), -- e.g., 0.07 for 7%
    delivery_days INT NOT NULL CHECK (delivery_days >= 0),
    inventory_count INT CHECK (inventory_count >= 0),
    valid_from TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    valid_until TIMESTAMP WITH TIME ZONE,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(vendor_id, product_id, base_price) -- Example constraint to avoid duplicate identical offers, adjust as needed
);

-- Vendor Rules Table
CREATE TABLE vendor_rules (
    rule_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vendor_id UUID NOT NULL REFERENCES vendors(vendor_id) ON DELETE CASCADE,
    rule_name VARCHAR(255) NOT NULL,
    applicability_type VARCHAR(50) NOT NULL, -- CATEGORY, SPECIFIC_PRODUCTS, BUNDLE, VENDOR_WIDE
    applicable_category VARCHAR(100), -- Used if applicability_type is CATEGORY
    trigger_condition JSONB NOT NULL,
    counter_action JSONB NOT NULL,
    additional_incentives JSONB, -- Storing as JSONB
    display_template_for_counter_reason TEXT,
    max_usage_per_session INT CHECK (max_usage_per_session > 0),
    priority INT NOT NULL DEFAULT 0,
    rule_hash VARCHAR(64) NOT NULL, -- SHA-256 hash
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_applicability CHECK (
        (applicability_type = 'CATEGORY' AND applicable_category IS NOT NULL) OR
        (applicability_type != 'CATEGORY')
    )
);

-- Vendor Rule Products (Join Table for SPECIFIC_PRODUCTS rule applicability)
CREATE TABLE vendor_rule_products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), -- Surrogate key
    rule_id UUID NOT NULL REFERENCES vendor_rules(rule_id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    UNIQUE (rule_id, product_id) -- Ensure a rule isn't linked to the same product multiple times
);

-- Bidding Sessions Analytics Table (Optional, as per your refined schema)
CREATE TABLE bidding_sessions_analytics (
    session_id VARCHAR(255) PRIMARY KEY, -- Could be a UUID string
    product_id UUID REFERENCES products(product_id) ON DELETE SET NULL, -- SET NULL if product is deleted
    user_location VARCHAR(50),
    initial_requested_discount_percent DECIMAL(5,2),
    rounds_completed INT,
    final_vendor_id UUID REFERENCES vendors(vendor_id) ON DELETE SET NULL,
    final_price DECIMAL(12,2),
    msrp_at_session_start DECIMAL(12,2),
    savings_achieved DECIMAL(12,2),
    equilibrium_reason VARCHAR(100),
    accepted_offer_method VARCHAR(50), -- e.g., DEEP_API, ASSISTED
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
-- Products
CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_name ON products(name); -- For searching

-- Vendors
CREATE INDEX idx_vendors_status ON vendors(status);

-- Vendor Offers
CREATE INDEX idx_vendor_offers_product_id ON vendor_offers(product_id);
CREATE INDEX idx_vendor_offers_vendor_id ON vendor_offers(vendor_id);
CREATE INDEX idx_vendor_offers_active_valid ON vendor_offers(is_active, valid_from, valid_until);

-- Vendor Rules
CREATE INDEX idx_vendor_rules_vendor_id_active ON vendor_rules(vendor_id, active);
CREATE INDEX idx_vendor_rules_applicability_type ON vendor_rules(applicability_type);
CREATE INDEX idx_vendor_rules_category ON vendor_rules(applicable_category) WHERE applicability_type = 'CATEGORY';

-- Vendor Rule Products
CREATE INDEX idx_vendor_rule_products_rule_id ON vendor_rule_products(rule_id);
CREATE INDEX idx_vendor_rule_products_product_id ON vendor_rule_products(product_id);

-- Bidding Sessions Analytics
CREATE INDEX idx_bidding_sessions_product_id ON bidding_sessions_analytics(product_id);
CREATE INDEX idx_bidding_sessions_final_vendor_id ON bidding_sessions_analytics(final_vendor_id);
CREATE INDEX idx_bidding_sessions_created_at ON bidding_sessions_analytics(created_at);


-- Optional: Functions to automatically update 'updated_at' columns
CREATE OR REPLACE FUNCTION trigger_set_timestamp()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply the trigger to tables with 'updated_at'
CREATE TRIGGER set_timestamp_products
BEFORE UPDATE ON products
FOR EACH ROW
EXECUTE FUNCTION trigger_set_timestamp();

CREATE TRIGGER set_timestamp_vendors
BEFORE UPDATE ON vendors
FOR EACH ROW
EXECUTE FUNCTION trigger_set_timestamp();

CREATE TRIGGER set_timestamp_vendor_offers
BEFORE UPDATE ON vendor_offers
FOR EACH ROW
EXECUTE FUNCTION trigger_set_timestamp();

CREATE TRIGGER set_timestamp_vendor_rules
BEFORE UPDATE ON vendor_rules
FOR EACH ROW
EXECUTE FUNCTION trigger_set_timestamp();