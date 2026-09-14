# KidsG API Contract & Ecosystem Specifications

This document defines the REST and WebSocket contracts between the **KidsG Customer App** and the backend microservices.

---

## 1. Authentication Endpoints

### Request OTP
`POST /auth/otp/request`
```json
{
  "phoneNumber": "+919148473131",
  "clientType": "CUSTOMER_APP"
}
```
**Response (200 OK):**
```json
{
  "status": "OTP_SENT",
  "retryAfterSeconds": 30,
  "maskedPhone": "+91 91484 *****"
}
```

### Verify OTP
`POST /auth/otp/verify`
```json
{
  "phoneNumber": "+919148473131",
  "otpCode": "7313"
}
```
**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOi...",
  "refreshToken": "d8a7c2...",
  "user": {
    "id": "usr_aarav",
    "name": "Aarav Sharma",
    "studentGrade": "Class 7",
    "schoolName": "National Public School"
  }
}
```

---

## 2. Catalog & Discovery

### Get Products by Intent Mode
`GET /catalog/products?mode=OOPS`
**Response (200 OK):**
```json
[
  {
    "id": "prod_camlin_gel_set",
    "name": "Camlin Kokuyo Gel Pen Set",
    "brand": "Camlin",
    "price": 180.0,
    "mrp": 200.0,
    "rating": 4.9,
    "stockQuantity": 60,
    "intentModes": ["SCHOOL", "EXAM", "OOPS"]
  }
]
```

---

## 3. Orders & Payment Abstraction

### Initiate Order Payment
`POST /orders/{orderId}/payments/initiate`
```json
{
  "amount": 270.0,
  "paymentMethod": "UPI"
}
```
**Response (200 OK):**
```json
{
  "transactionId": "TXN_KG_89218",
  "gatewayToken": "order_Hj2k9L...",
  "requiresSdkLaunch": true
}
```

### Authoritative Payment Verification
`POST /orders/{orderId}/payments/verify`
```json
{
  "transactionId": "TXN_KG_89218"
}
```
**Response (200 OK):**
```json
{
  "isSuccess": true,
  "transactionId": "TXN_KG_89218",
  "gatewayReferenceId": "REF_9812903",
  "orderStatus": "CONFIRMED"
}
```

---

## 4. Delivery State Machine

Order states follow this progression:
`CREATED` → `CONFIRMED` → `STORE_ACCEPTED` → `PREPARING` → `READY_FOR_PICKUP` → `PICKED_UP` → `OUT_FOR_DELIVERY` → `DELIVERED`.
Terminal states: `DELIVERED`, `CANCELLED`, `FAILED`, `REFUNDED`.
