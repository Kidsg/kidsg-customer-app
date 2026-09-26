-- ============================================================================
-- KIDSG MIGRATION 004: ORDER STATUS HISTORY & SHOP OWNER APP INTEGRATION
-- ============================================================================

-- 0. EXTEND ORDER STATUS ENUM FOR SHOP OWNER ACCEPTANCE
DO $$ BEGIN
    ALTER TYPE order_status ADD VALUE IF NOT EXISTS 'STORE_ACCEPTED' AFTER 'CONFIRMED';
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- 1. ORDER STATUS HISTORY TABLE
CREATE TABLE IF NOT EXISTS order_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    status order_status NOT NULL,
    message TEXT NOT NULL,
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_order_status_history_order ON order_status_history(order_id);
CREATE INDEX IF NOT EXISTS idx_order_status_history_created ON order_status_history(created_at);

-- 2. STORE OWNER RELATIONSHIP
ALTER TABLE stores ADD COLUMN IF NOT EXISTS owner_id UUID REFERENCES profiles(id) ON DELETE SET NULL;

-- 3. ENABLE RLS ON ORDER STATUS HISTORY
ALTER TABLE order_status_history ENABLE ROW LEVEL SECURITY;

-- 4. RLS POLICIES FOR ORDER STATUS HISTORY
CREATE POLICY "Users can view status history for their own orders"
    ON order_status_history FOR SELECT
    USING (
        order_id IN (
            SELECT id FROM orders WHERE user_id IN (
                SELECT id FROM profiles WHERE auth_user_id = auth.uid()
            )
        )
        OR EXISTS (
            SELECT 1 FROM profiles 
            WHERE auth_user_id = auth.uid() 
            AND role IN ('ADMIN', 'PARTNER')
        )
    );

CREATE POLICY "Authorized system and shop owners can insert status history"
    ON order_status_history FOR INSERT
    WITH CHECK (
        EXISTS (
            SELECT 1 FROM profiles 
            WHERE auth_user_id = auth.uid() 
            AND role IN ('ADMIN', 'PARTNER')
        )
        OR order_id IN (
            SELECT id FROM orders WHERE user_id IN (
                SELECT id FROM profiles WHERE auth_user_id = auth.uid()
            )
        )
    );

-- 5. RLS POLICIES FOR SHOP OWNERS (PARTNER ROLE) ON ORDERS
CREATE POLICY "Shop owners can view orders for their stores"
    ON orders FOR SELECT
    USING (
        store_id IN (
            SELECT id FROM stores WHERE owner_id IN (
                SELECT id FROM profiles WHERE auth_user_id = auth.uid()
            )
        )
        OR EXISTS (
            SELECT 1 FROM profiles 
            WHERE auth_user_id = auth.uid() 
            AND role = 'ADMIN'
        )
    );

CREATE POLICY "Shop owners can update order status for their stores"
    ON orders FOR UPDATE
    USING (
        store_id IN (
            SELECT id FROM stores WHERE owner_id IN (
                SELECT id FROM profiles WHERE auth_user_id = auth.uid()
            )
        )
        OR EXISTS (
            SELECT 1 FROM profiles 
            WHERE auth_user_id = auth.uid() 
            AND role = 'ADMIN'
        )
    );
