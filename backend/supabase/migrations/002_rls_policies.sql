-- ============================================================================
-- KIDSG ROW LEVEL SECURITY (RLS) POLICIES
-- ============================================================================

-- Enable RLS on all tables
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE addresses ENABLE ROW LEVEL SECURITY;
ALTER TABLE categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE products ENABLE ROW LEVEL SECURITY;
ALTER TABLE product_images ENABLE ROW LEVEL SECURITY;
ALTER TABLE stores ENABLE ROW LEVEL SECURITY;
ALTER TABLE store_inventory ENABLE ROW LEVEL SECURITY;
ALTER TABLE wishlists ENABLE ROW LEVEL SECURITY;
ALTER TABLE wishlist_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE carts ENABLE ROW LEVEL SECURITY;
ALTER TABLE cart_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE coupons ENABLE ROW LEVEL SECURITY;
ALTER TABLE coupon_redemptions ENABLE ROW LEVEL SECURITY;
ALTER TABLE orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE order_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE payments ENABLE ROW LEVEL SECURITY;
ALTER TABLE delivery_tracking ENABLE ROW LEVEL SECURITY;
ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE support_tickets ENABLE ROW LEVEL SECURITY;

-- Helper function to check if current user is admin
CREATE OR REPLACE FUNCTION is_admin()
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM profiles 
        WHERE auth_user_id = auth.uid() 
        AND role = 'ADMIN'
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 1. PROFILES
CREATE POLICY "Users can read their own profile"
    ON profiles FOR SELECT
    USING (auth_user_id = auth.uid() OR is_admin());

CREATE POLICY "Users can update their own profile"
    ON profiles FOR UPDATE
    USING (auth_user_id = auth.uid() OR is_admin());

CREATE POLICY "System/Service can insert profile"
    ON profiles FOR INSERT
    WITH CHECK (auth_user_id = auth.uid() OR is_admin());

-- 2. ADDRESSES
CREATE POLICY "Users can manage their own addresses"
    ON addresses FOR ALL
    USING (user_id IN (SELECT id FROM profiles WHERE auth_user_id = auth.uid()) OR is_admin());

-- 3. CATEGORIES (Publicly readable if active)
CREATE POLICY "Anyone can view active categories"
    ON categories FOR SELECT
    USING (is_active = TRUE OR is_admin());

CREATE POLICY "Admins can manage categories"
    ON categories FOR ALL
    USING (is_admin());

-- 4. PRODUCTS (Publicly readable if active)
CREATE POLICY "Anyone can view active products"
    ON products FOR SELECT
    USING (is_active = TRUE OR is_admin());

CREATE POLICY "Admins can manage products"
    ON products FOR ALL
    USING (is_admin());

-- 5. PRODUCT IMAGES
CREATE POLICY "Anyone can view product images"
    ON product_images FOR SELECT
    USING (TRUE);

CREATE POLICY "Admins can manage product images"
    ON product_images FOR ALL
    USING (is_admin());

-- 6. STORES (Publicly readable if active)
CREATE POLICY "Anyone can view active stores"
    ON stores FOR SELECT
    USING (is_active = TRUE OR is_admin());

CREATE POLICY "Admins can manage stores"
    ON stores FOR ALL
    USING (is_admin());

-- 7. STORE INVENTORY
CREATE POLICY "Anyone can view store inventory"
    ON store_inventory FOR SELECT
    USING (is_available = TRUE OR is_admin());

CREATE POLICY "Admins can manage store inventory"
    ON store_inventory FOR ALL
    USING (is_admin());

-- 8. WISHLISTS & 9. WISHLIST ITEMS
CREATE POLICY "Users can manage their own wishlist"
    ON wishlists FOR ALL
    USING (user_id IN (SELECT id FROM profiles WHERE auth_user_id = auth.uid()) OR is_admin());

CREATE POLICY "Users can manage their own wishlist items"
    ON wishlist_items FOR ALL
    USING (wishlist_id IN (
        SELECT id FROM wishlists WHERE user_id IN (
            SELECT id FROM profiles WHERE auth_user_id = auth.uid()
        )
    ) OR is_admin());

-- 10. CARTS & 11. CART ITEMS
CREATE POLICY "Users can manage their own cart"
    ON carts FOR ALL
    USING (user_id IN (SELECT id FROM profiles WHERE auth_user_id = auth.uid()) OR is_admin());

CREATE POLICY "Users can manage their own cart items"
    ON cart_items FOR ALL
    USING (cart_id IN (
        SELECT id FROM carts WHERE user_id IN (
            SELECT id FROM profiles WHERE auth_user_id = auth.uid()
        )
    ) OR is_admin());

-- 12. COUPONS
CREATE POLICY "Anyone can read active coupons"
    ON coupons FOR SELECT
    USING (is_active = TRUE AND valid_until > NOW() OR is_admin());

CREATE POLICY "Admins can manage coupons"
    ON coupons FOR ALL
    USING (is_admin());

-- 13. COUPON REDEMPTIONS
CREATE POLICY "Users can read their own coupon redemptions"
    ON coupon_redemptions FOR SELECT
    USING (user_id IN (SELECT id FROM profiles WHERE auth_user_id = auth.uid()) OR is_admin());

-- 14. ORDERS & 15. ORDER ITEMS
CREATE POLICY "Users can view their own orders"
    ON orders FOR SELECT
    USING (user_id IN (SELECT id FROM profiles WHERE auth_user_id = auth.uid()) OR is_admin());

CREATE POLICY "Users can create their own orders"
    ON orders FOR INSERT
    WITH CHECK (user_id IN (SELECT id FROM profiles WHERE auth_user_id = auth.uid()) OR is_admin());

CREATE POLICY "Users can view their own order items"
    ON order_items FOR SELECT
    USING (order_id IN (
        SELECT id FROM orders WHERE user_id IN (
            SELECT id FROM profiles WHERE auth_user_id = auth.uid()
        )
    ) OR is_admin());

-- 16. PAYMENTS
CREATE POLICY "Users can view their own payments"
    ON payments FOR SELECT
    USING (user_id IN (SELECT id FROM profiles WHERE auth_user_id = auth.uid()) OR is_admin());

-- 17. DELIVERY TRACKING
CREATE POLICY "Users can view tracking for their own orders"
    ON delivery_tracking FOR SELECT
    USING (order_id IN (
        SELECT id FROM orders WHERE user_id IN (
            SELECT id FROM profiles WHERE auth_user_id = auth.uid()
        )
    ) OR is_admin());

-- 18. NOTIFICATIONS
CREATE POLICY "Users can manage their own notifications"
    ON notifications FOR ALL
    USING (user_id IN (SELECT id FROM profiles WHERE auth_user_id = auth.uid()) OR is_admin());

-- 19. SUPPORT TICKETS
CREATE POLICY "Users can manage their own support tickets"
    ON support_tickets FOR ALL
    USING (user_id IN (SELECT id FROM profiles WHERE auth_user_id = auth.uid()) OR is_admin());
