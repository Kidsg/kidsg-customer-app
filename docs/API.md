# KidsG API Documentation

The KidsG API is a high-performance RESTful API deployed on Vercel Serverless Functions and backed by Supabase PostgreSQL and Supabase Auth.

## Base URLs

- **Development:** `http://localhost:3000` (Local) / `http://10.0.2.2:3000` (Android Emulator)
- **Staging:** `https://kidsg-api-staging.vercel.app`
- **Production:** `https://api.kidsg.in`

---

## Standard Response Format

All endpoints return a uniform JSON envelope:

### Success Response
```json
{
  "success": true,
  "data": { ... },
  "message": "Operation completed successfully",
  "errorCode": null
}
```

### Error Response
```json
{
  "success": false,
  "data": null,
  "message": "Product is out of stock",
  "errorCode": "OUT_OF_STOCK"
}
```

### Standard Error Codes
| Code | HTTP Status | Description |
|---|---|---|
| `UNAUTHORIZED` | 401 | Missing or invalid Bearer session token |
| `FORBIDDEN` | 403 | Insufficient role permissions |
| `NOT_FOUND` | 404 | Resource does not exist |
| `VALIDATION_ERROR` | 400 | Request body failed Zod schema validation |
| `RATE_LIMITED` | 429 | Exceeded request limit (cooldown required) |
| `INTERNAL_ERROR` | 500 | Unexpected server exception |

---

## Authentication & Headers

Protected endpoints require an `Authorization` header with a Bearer token:
```http
Authorization: Bearer <access_token>
Content-Type: application/json
```

---

## Endpoint Specifications

### 1. System Health

#### `GET /api/health`
Checks server vitality and returns environment metadata.

- **Authentication:** Public
- **Response:**
  ```json
  {
    "success": true,
    "data": {
      "status": "ok",
      "environment": "development",
      "appEnv": "DEV",
      "version": "1.0.0",
      "timestamp": "2026-09-14T08:59:23.039Z"
    }
  }
  ```

---

### 2. Authentication

#### `POST /api/auth/send-otp`
Sends a 6-digit verification code to the student's mobile number.

- **Authentication:** Public
- **Rate Limit:** 5 requests per minute
- **Request Body:**
  ```json
  {
    "phone": "+919876543210"
  }
  ```
- **Response:**
  ```json
  {
    "success": true,
    "data": {
      "sent": true,
      "expiresInSeconds": 300
    },
    "message": "OTP sent successfully"
  }
  ```

#### `POST /api/auth/verify-otp`
Verifies the 6-digit code and issues a Bearer session token.

- **Authentication:** Public
- **Request Body:**
  ```json
  {
    "phone": "+919876543210",
    "otp": "437860"
  }
  ```
- **Response:**
  ```json
  {
    "success": true,
    "data": {
      "verified": true,
      "token": "dev-token-user_dev_default",
      "user": {
        "id": "user_dev_default",
        "phone": "+919876543210",
        "role": "CUSTOMER",
        "firstName": "Aarav",
        "lastName": "Sharma"
      }
    }
  }
  ```

#### `GET /api/auth/me`
Retrieves the authenticated user profile and saved addresses.

- **Authentication:** `Bearer <token>`
- **Response:**
  ```json
  {
    "success": true,
    "data": {
      "user": { "id": "user_dev_default", "role": "CUSTOMER" },
      "profile": { "selectedClass": "Class 7", "selectedSchool": "National Public School" },
      "addresses": [ ... ]
    }
  }
  ```

---

### 3. Categories & Products

#### `GET /api/categories`
Returns all active stationery categories.

- **Authentication:** Public
- **Response:**
  ```json
  {
    "success": true,
    "data": [
      {
        "id": "cat_notebooks",
        "name": "Notebooks & Registers",
        "slug": "notebooks",
        "iconName": "notebook",
        "displayOrder": 1,
        "isActive": true
      }
    ]
  }
  ```

#### `GET /api/products`
Retrieves products with search, pagination, and category filtering.

- **Query Parameters:**
  - `search`: Keyword string
  - `category`: Category slug or UUID
  - `page`: Page number (default: 1)
  - `limit`: Items per page (default: 20)
  - `sort`: `price_asc` | `price_desc`
- **Response:**
  ```json
  {
    "success": true,
    "data": {
      "products": [
        {
          "id": "prod_classmate_single_line",
          "name": "Classmate Pulse Spiral Single Line Notebook",
          "brand": "Classmate",
          "price": 95,
          "mrp": 110,
          "discountPercent": 14,
          "stock": 50,
          "isFeatured": true
        }
      ],
      "pagination": {
        "total": 28,
        "page": 1,
        "limit": 20,
        "totalPages": 2
      }
    }
  }
  ```

---

### 4. School Bag (Cart)

Prices and calculations are **authoritative on the server**. The mobile app sends item ID and quantity; the server computes current price, stock, and total.

#### `GET /api/cart`
Returns the user's active bag items and subtotal.

- **Authentication:** `Bearer <token>`

#### `POST /api/cart/items`
Adds an item to the bag.

- **Request Body:**
  ```json
  {
    "productId": "prod_doms_brush_pens_14",
    "quantity": 1,
    "selectedVariant": "14 Shades"
  }
  ```

#### `PATCH /api/cart/items/:id`
Updates item quantity (`quantity: 0` removes the item).

---

### 5. Checkout & Coupons

#### `POST /api/coupons/validate`
Validates coupon against minimum cart requirements and returns server discount.

- **Request Body:**
  ```json
  { "code": "KIDSG50" }
  ```
- **Response:**
  ```json
  {
    "success": true,
    "data": {
      "valid": true,
      "code": "KIDSG50",
      "discountAmount": 50,
      "message": "Coupon KIDSG50 applied successfully! Saved ₹50"
    }
  }
  ```

#### `POST /api/checkout/preview`
Authoritative computation of items, subtotal, coupon discount, delivery fee, and net total.

- **Request Body:**
  ```json
  { "couponCode": "KIDSG50" }
  ```
- **Response:**
  ```json
  {
    "success": true,
    "data": {
      "subtotal": 250,
      "discount": 0,
      "couponDiscount": 50,
      "deliveryFee": 0,
      "tax": 0,
      "total": 200,
      "freeDeliveryThreshold": 199,
      "freeDeliveryUnlocked": true
    }
  }
  ```

---

### 6. Orders & Tracking

#### `POST /api/orders`
Places the order, locks snapshot prices and delivery address, and initializes tracking.

- **Request Body:**
  ```json
  {
    "addressId": "addr_dev_default",
    "paymentMethod": "UPI",
    "couponCode": "KIDSG50"
  }
  ```

#### `GET /api/orders/:id/tracking`
Returns real-time delivery milestone progression:
1. `Order Confirmed`
2. `Preparing`
3. `Ready for Pickup`
4. `Picked Up`
5. `Out for Delivery`
6. `Delivered`
