-- =============================================================================
-- Migration 006: BookBuddys Hyperlocal Platform Schema
-- Additive, Non-Destructive Migration for Founder Dashboard, Manual UPI,
-- Multi-Shop Inventory, Delivery Coordination, and Audit Logging
-- =============================================================================

-- 1. EXTEND ORDER STATUS & PAYMENT STATUS ENUMS
DO $$ BEGIN
    ALTER TYPE order_status ADD VALUE IF NOT EXISTS 'ORDER_RECEIVED' BEFORE 'CONFIRMED';
EXCEPTION WHEN duplicate_object THEN null; END $$;

DO $$ BEGIN
    ALTER TYPE order_status ADD VALUE IF NOT EXISTS 'SHOP_CONFIRMED' BEFORE 'CONFIRMED';
EXCEPTION WHEN duplicate_object THEN null; END $$;

DO $$ BEGIN
    ALTER TYPE order_status ADD VALUE IF NOT EXISTS 'PAYMENT_REQUIRED' BEFORE 'PAYMENT_CONFIRMED';
EXCEPTION WHEN duplicate_object THEN null; END $$;

DO $$ BEGIN
    ALTER TYPE order_status ADD VALUE IF NOT EXISTS 'PAYMENT_SUBMITTED' BEFORE 'PAYMENT_CONFIRMED';
EXCEPTION WHEN duplicate_object THEN null; END $$;

DO $$ BEGIN
    ALTER TYPE order_status ADD VALUE IF NOT EXISTS 'PAYMENT_VERIFIED' AFTER 'PAYMENT_CONFIRMED';
EXCEPTION WHEN duplicate_object THEN null; END $$;

DO $$ BEGIN
    ALTER TYPE order_status ADD VALUE IF NOT EXISTS 'DELIVERY_BOOKED' AFTER 'READY_FOR_PICKUP';
EXCEPTION WHEN duplicate_object THEN null; END $$;

DO $$ BEGIN
    ALTER TYPE order_status ADD VALUE IF NOT EXISTS 'DELIVERY_ASSIGNED' AFTER 'DELIVERY_BOOKED';
EXCEPTION WHEN duplicate_object THEN null; END $$;

DO $$ BEGIN
    ALTER TYPE payment_status ADD VALUE IF NOT EXISTS 'PAYMENT_SUBMITTED';
EXCEPTION WHEN duplicate_object THEN null; END $$;

DO $$ BEGIN
    ALTER TYPE payment_status ADD VALUE IF NOT EXISTS 'PAYMENT_VERIFIED';
EXCEPTION WHEN duplicate_object THEN null; END $$;

-- 2. EXTEND STORES TABLE WITH HYPERLOCAL OPERATION METADATA
ALTER TABLE stores ADD COLUMN IF NOT EXISTS business_hours JSONB DEFAULT '{
    "monday": {"open": "09:00", "close": "21:00", "isOpen": true},
    "tuesday": {"open": "09:00", "close": "21:00", "isOpen": true},
    "wednesday": {"open": "09:00", "close": "21:00", "isOpen": true},
    "thursday": {"open": "09:00", "close": "21:00", "isOpen": true},
    "friday": {"open": "09:00", "close": "21:00", "isOpen": true},
    "saturday": {"open": "09:00", "close": "21:30", "isOpen": true},
    "sunday": {"open": "10:00", "close": "20:00", "isOpen": true}
}'::jsonb;

ALTER TABLE stores ADD COLUMN IF NOT EXISTS is_open_override BOOLEAN DEFAULT NULL;
ALTER TABLE stores ADD COLUMN IF NOT EXISTS temporary_closure_reason TEXT DEFAULT NULL;
ALTER TABLE stores ADD COLUMN IF NOT EXISTS founder_notes TEXT DEFAULT NULL;
ALTER TABLE stores ADD COLUMN IF NOT EXISTS verification_status VARCHAR(50) DEFAULT 'ACTIVE';
ALTER TABLE stores ADD COLUMN IF NOT EXISTS landmark TEXT DEFAULT NULL;
ALTER TABLE stores ADD COLUMN IF NOT EXISTS service_area_geojson JSONB DEFAULT NULL;

-- 3. EXTEND STORE_INVENTORY TABLE FOR PRECISE STOCK & COST TRACKING
ALTER TABLE store_inventory ADD COLUMN IF NOT EXISTS reserved_quantity INT NOT NULL DEFAULT 0;
ALTER TABLE store_inventory ADD COLUMN IF NOT EXISTS low_stock_threshold INT NOT NULL DEFAULT 5;
ALTER TABLE store_inventory ADD COLUMN IF NOT EXISTS cost_price_paise INT DEFAULT NULL;
ALTER TABLE store_inventory ADD COLUMN IF NOT EXISTS founder_notes TEXT DEFAULT NULL;
ALTER TABLE store_inventory ADD COLUMN IF NOT EXISTS last_verified_at TIMESTAMPTZ DEFAULT NOW();

-- Ensure non-negative stock constraint
DO $$ BEGIN
    ALTER TABLE store_inventory ADD CONSTRAINT check_positive_stock CHECK (quantity >= 0);
EXCEPTION WHEN duplicate_object THEN null; END $$;

-- 4. MANUAL UPI PAYMENT SUBMISSIONS & AUDIT VERIFICATION TABLE
CREATE TABLE IF NOT EXISTS payment_submissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    amount NUMERIC(10, 2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL DEFAULT 'UPI',
    transaction_reference VARCHAR(100) NOT NULL, -- UTR or Bank Ref #
    screenshot_url TEXT DEFAULT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_VERIFICATION', -- PENDING_VERIFICATION, VERIFIED, REJECTED
    rejection_reason TEXT DEFAULT NULL,
    verified_by UUID REFERENCES profiles(id) ON DELETE SET NULL,
    verified_at TIMESTAMPTZ DEFAULT NULL,
    notes TEXT DEFAULT NULL,
    submitted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_payment_submissions_order ON payment_submissions(order_id);
CREATE INDEX IF NOT EXISTS idx_payment_submissions_user ON payment_submissions(user_id);
CREATE INDEX IF NOT EXISTS idx_payment_submissions_status ON payment_submissions(status);
CREATE INDEX IF NOT EXISTS idx_payment_submissions_ref ON payment_submissions(transaction_reference);

-- 5. DELIVERY BOOKINGS & COORDINATION TABLE
CREATE TABLE IF NOT EXISTS delivery_bookings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    provider VARCHAR(50) NOT NULL DEFAULT 'MANUAL', -- PORTER, RAPIDO, DUNZO, MANUAL, IN_HOUSE
    booking_reference VARCHAR(100) DEFAULT NULL,
    delivery_fee NUMERIC(10, 2) NOT NULL DEFAULT 30.00,
    rider_name VARCHAR(100) DEFAULT NULL,
    rider_phone VARCHAR(30) DEFAULT NULL,
    vehicle_number VARCHAR(50) DEFAULT NULL,
    pickup_address_snapshot JSONB NOT NULL,
    delivery_address_snapshot JSONB NOT NULL,
    pickup_status VARCHAR(50) NOT NULL DEFAULT 'PENDING_PICKUP', -- PENDING_PICKUP, PICKED_UP, DELIVERED, CANCELLED
    tracking_url TEXT DEFAULT NULL,
    eta_minutes INT DEFAULT NULL,
    founder_notes TEXT DEFAULT NULL,
    booked_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    picked_up_at TIMESTAMPTZ DEFAULT NULL,
    delivered_at TIMESTAMPTZ DEFAULT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_delivery_bookings_order ON delivery_bookings(order_id);
CREATE INDEX IF NOT EXISTS idx_delivery_bookings_provider ON delivery_bookings(provider);
CREATE INDEX IF NOT EXISTS idx_delivery_bookings_status ON delivery_bookings(pickup_status);

-- 6. FOUNDER AUDIT LOG TABLE FOR PRIVILEGED OPERATIONS
CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    admin_id UUID REFERENCES profiles(id) ON DELETE SET NULL,
    action VARCHAR(100) NOT NULL, -- e.g. VERIFY_PAYMENT, ACCEPT_ORDER, ADJUST_STOCK, UPDATE_SHOP, CANCEL_ORDER
    entity_type VARCHAR(50) NOT NULL, -- ORDER, STORE, PRODUCT, PAYMENT, DELIVERY
    entity_id VARCHAR(100) NOT NULL,
    details JSONB DEFAULT '{}'::jsonb,
    ip_address VARCHAR(50) DEFAULT NULL,
    user_agent TEXT DEFAULT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_action ON audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created ON audit_logs(created_at);

-- 7. NOTIFICATION OUTBOX FOR RESILIENT FOUNDER & CUSTOMER NOTIFICATIONS
CREATE TABLE IF NOT EXISTS notification_outbox (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient VARCHAR(255) NOT NULL,
    channel VARCHAR(50) NOT NULL, -- EMAIL, SMS, WHATSAPP, IN_APP
    event_type VARCHAR(100) NOT NULL,
    subject TEXT NOT NULL,
    content TEXT NOT NULL,
    payload JSONB DEFAULT '{}'::jsonb,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING, SENT, FAILED
    retry_count INT NOT NULL DEFAULT 0,
    max_retries INT NOT NULL DEFAULT 3,
    last_error TEXT DEFAULT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    sent_at TIMESTAMPTZ DEFAULT NULL
);

CREATE INDEX IF NOT EXISTS idx_notification_outbox_status ON notification_outbox(status);
CREATE INDEX IF NOT EXISTS idx_notification_outbox_created ON notification_outbox(created_at);

-- 8. FOUNDER PLATFORM CONFIGURATION SETTINGS
CREATE TABLE IF NOT EXISTS platform_settings (
    key VARCHAR(100) PRIMARY KEY,
    value JSONB NOT NULL,
    description TEXT DEFAULT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO platform_settings (key, value, description)
VALUES 
    ('merchant_upi', '{"vpa": "bookbuddys@upi", "payeeName": "BookBuddys Stationery", "qrEnabled": true}'::jsonb, 'UPI QR and VPA configuration for customer checkout'),
    ('delivery_rules', '{"defaultFee": 30, "freeDeliveryThreshold": 199, "maxRadiusKm": 12.0, "defaultEtaMinutes": 45}'::jsonb, 'Hyperlocal delivery pricing and serviceability rules in Bengaluru'),
    ('founder_notifications', '{"notifyEmail": "founder@bookbuddys.in", "notifyPhone": "+919876543210", "channels": ["EMAIL", "IN_APP"]}'::jsonb, 'Notification preferences for new orders and payment verification')
ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value;

-- 9. ENABLE ROW LEVEL SECURITY
ALTER TABLE payment_submissions ENABLE ROW LEVEL SECURITY;
ALTER TABLE delivery_bookings ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE notification_outbox ENABLE ROW LEVEL SECURITY;
ALTER TABLE platform_settings ENABLE ROW LEVEL SECURITY;

-- 10. ROW LEVEL SECURITY POLICIES
-- Payment Submissions: Users can view & create their own submissions; Admins have full access
CREATE POLICY "Users can create payment submission for their orders"
    ON payment_submissions FOR INSERT
    WITH CHECK (
        order_id IN (
            SELECT id FROM orders WHERE user_id IN (
                SELECT id FROM profiles WHERE auth_user_id = auth.uid()
            )
        )
    );

CREATE POLICY "Users can view payment submissions for their own orders"
    ON payment_submissions FOR SELECT
    USING (
        order_id IN (
            SELECT id FROM orders WHERE user_id IN (
                SELECT id FROM profiles WHERE auth_user_id = auth.uid()
            )
        )
        OR EXISTS (
            SELECT 1 FROM profiles WHERE auth_user_id = auth.uid() AND role IN ('ADMIN', 'PARTNER')
        )
    );

CREATE POLICY "Admins can update payment submissions"
    ON payment_submissions FOR UPDATE
    USING (
        EXISTS (
            SELECT 1 FROM profiles WHERE auth_user_id = auth.uid() AND role = 'ADMIN'
        )
    );

-- Delivery Bookings: Customers can view bookings for their orders; Admins can do everything
CREATE POLICY "Users can view delivery bookings for their orders"
    ON delivery_bookings FOR SELECT
    USING (
        order_id IN (
            SELECT id FROM orders WHERE user_id IN (
                SELECT id FROM profiles WHERE auth_user_id = auth.uid()
            )
        )
        OR EXISTS (
            SELECT 1 FROM profiles WHERE auth_user_id = auth.uid() AND role IN ('ADMIN', 'PARTNER', 'DELIVERY_PARTNER')
        )
    );

CREATE POLICY "Admins and partners can insert and update delivery bookings"
    ON delivery_bookings FOR ALL
    USING (
        EXISTS (
            SELECT 1 FROM profiles WHERE auth_user_id = auth.uid() AND role IN ('ADMIN', 'PARTNER')
        )
    );

-- Audit Logs: Admin read/write only
CREATE POLICY "Admins can view audit logs"
    ON audit_logs FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM profiles WHERE auth_user_id = auth.uid() AND role = 'ADMIN'
        )
    );

CREATE POLICY "Admins and system can insert audit logs"
    ON audit_logs FOR INSERT
    WITH CHECK (
        EXISTS (
            SELECT 1 FROM profiles WHERE auth_user_id = auth.uid() AND role = 'ADMIN'
        )
        OR auth.uid() IS NOT NULL
    );

-- Platform Settings: Public can read, Admins can write
CREATE POLICY "Public read platform settings"
    ON platform_settings FOR SELECT
    USING (true);

CREATE POLICY "Admins can update platform settings"
    ON platform_settings FOR ALL
    USING (
        EXISTS (
            SELECT 1 FROM profiles WHERE auth_user_id = auth.uid() AND role = 'ADMIN'
        )
    );
