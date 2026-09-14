# KidsG Database & Schema Documentation

KidsG utilizes **Supabase PostgreSQL** for all relational quick-commerce operations. All sensitive operations, row security policies, and foreign key integrity constraints are enforced at the database level.

---

## Entity Relationship Overview

```text
auth.users ────────┐
                   │
                   ▼
               profiles (1:1)
                   │
         ┌─────────┼──────────┬──────────┬──────────┐
         │         │          │          │          │
         ▼         ▼          ▼          ▼          ▼
    addresses  wishlists    carts     orders   support_tickets
                   │          │          │
                   ▼          ▼          ▼
             wishlist_items cart_items order_items
                   ▲          ▲          ▲
                   │          │          │
                   └──────────┼──────────┘
                              │
                          products ◄── categories
                              ▲
                              │
                       store_inventory ──► stores
                                              ▲
                                              │
                                           orders
                                              │
                                      ┌───────┴───────┐
                                      ▼               ▼
                                   payments   delivery_tracking
```

---

## Table Inventory (All 19 Tables)

| # | Table Name | Primary Key | Description | RLS Policy |
|---|---|---|---|---|
| 1 | `profiles` | UUID | User profile linked to `auth.users`, role, school standard | Own profile only / Admin |
| 2 | `addresses` | UUID | Saved delivery addresses with GPS coordinates & notes | Own addresses only |
| 3 | `categories` | UUID | School stationery taxonomy (Notebooks, Pens, Geometry, etc.) | Public read / Admin write |
| 4 | `products` | UUID | Stationery catalog with prices, MRP, stock, grade level | Public read / Admin write |
| 5 | `product_images` | UUID | Additional product photography carousel | Public read / Admin write |
| 6 | `stores` | UUID | Local partner stationery stores with delivery radius & hours | Public read / Admin write |
| 7 | `store_inventory` | UUID | Current inventory per partner store | Public read / Admin write |
| 8 | `wishlists` | UUID | Saved items bucket per user | Own wishlist only |
| 9 | `wishlist_items` | UUID | Wishlist items linking to products | Own items only |
| 10 | `carts` | UUID | Active school bag per user | Own cart only |
| 11 | `cart_items` | UUID | Bag items with variant and quantity | Own items only |
| 12 | `coupons` | UUID | Promotional promo codes with minimum thresholds & caps | Active public / Admin |
| 13 | `coupon_redemptions` | UUID | Historical coupon redemptions per user | Own redemptions only |
| 14 | `orders` | UUID | Complete order record with frozen financial breakdown | Own orders only |
| 15 | `order_items` | UUID | **Immutable historical snapshot** of price, MRP, and name | Own order items only |
| 16 | `payments` | UUID | Gateway transaction record (UPI, Razorpay, Mock) | Own payments only |
| 17 | `delivery_tracking` | UUID | Real-time rider info, estimated ETA, status milestones | Own order tracking only |
| 18 | `notifications` | UUID | User notification feed (order updates, promotions) | Own notifications only |
| 19 | `support_tickets` | UUID | Parent/student customer support inquiries | Own tickets only |

---

## Order State Machine & Integrity

Transitions between states are validated strictly on the server:

```text
[ PENDING_PAYMENT ] ──► [ PAYMENT_CONFIRMED ] ──► [ CONFIRMED ]
                                                        │
                                                        ▼
[ READY_FOR_PICKUP ] ◄── [ PREPARING ]
        │
        ▼
[ PICKED_UP ] ──► [ OUT_FOR_DELIVERY ] ──► [ DELIVERED ]
```
*Note: Orders may transition to `CANCELLED` before dispatch, and cancelled orders may be marked `REFUNDED`.*

---

## Performance Indexes

- `idx_products_category` on `products(category_id)`
- `idx_products_slug` on `products(slug)`
- `idx_products_is_active` on `products(is_active)`
- `idx_products_featured` on `products(is_featured)` WHERE `is_featured = TRUE`
- `idx_orders_user` on `orders(user_id)`
- `idx_orders_status` on `orders(status)`
- `idx_cart_items_cart` on `cart_items(cart_id)`
- `idx_store_inventory_store` on `store_inventory(store_id)`
- `idx_store_inventory_product` on `store_inventory(product_id)`
- `idx_notifications_user` on `notifications(user_id, is_read)`
