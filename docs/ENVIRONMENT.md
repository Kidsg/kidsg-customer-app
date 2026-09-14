# KidsG Environment Configuration & Secrets

KidsG enforces strict separation between server-side secrets and public client configurations.

---

## 1. Zero Client Secrets Rule

> [!IMPORTANT]
> The Android and iOS client applications must **NEVER** contain:
> - `SUPABASE_SERVICE_ROLE_KEY`
> - `PAYMENT_KEY_SECRET`
> - `RAZORPAY_WEBHOOK_SECRET`
> - `OTP_API_KEY`
> - Database credentials
>
> Any secret found in the mobile binary is a severe security vulnerability. All operations requiring privileged credentials execute exclusively on the Vercel API layer.

---

## 2. Environment Variables Matrix

| Variable | Environment | Provider Default | Description |
|---|---|---|---|
| `NODE_ENV` | All | `development` | Node runtime environment |
| `APP_ENV` | All | `DEV` | `DEV` \| `STAGING` \| `PRODUCTION` |
| `PORT` | Local | `3000` | Local Express development port |
| `API_BASE_URL` | Local | `http://localhost:3000` | Base endpoint for callbacks |
| `SUPABASE_URL` | All | `https://YOUR_PROJECT.supabase.co` | Supabase project API URL |
| `SUPABASE_ANON_KEY` | All | `YOUR_ANON_KEY` | Public anonymous key |
| `SUPABASE_SERVICE_ROLE_KEY` | Server Only | `YOUR_SERVICE_ROLE_KEY` | Privileged admin key for backend operations |
| `SUPABASE_JWT_SECRET` | Server Only | `kidsg_dev_jwt_secret` | JWT signing secret |
| `OTP_PROVIDER` | All | `mock` | `mock` \| `msg91` \| `twilio` \| `twofactor` |
| `OTP_API_KEY` | Server Only | - | SMS Gateway authentication key |
| `PAYMENT_PROVIDER` | All | `mock` | `mock` \| `razorpay` \| `cashfree` \| `phonepe` |
| `RAZORPAY_KEY_ID` | All | - | Public gateway merchant key |
| `RAZORPAY_KEY_SECRET` | Server Only | - | Private merchant key |
| `RAZORPAY_WEBHOOK_SECRET` | Server Only | - | Signature secret for webhook validation |
| `FREE_DELIVERY_THRESHOLD` | All | `199` | Subtotal in ₹ required for ₹0 delivery fee |
| `DEFAULT_DELIVERY_FEE` | All | `30` | Standard delivery charge in ₹ |

---

## 3. Switching Providers Seamlessly

To activate real providers, update the environment variable in `.env.local` or Vercel:

### Switch to MSG91 for SMS:
```env
OTP_PROVIDER=msg91
OTP_API_KEY=your_msg91_auth_token
```

### Switch to Razorpay for Payments:
```env
PAYMENT_PROVIDER=razorpay
RAZORPAY_KEY_ID=rzp_live_xxxxxxxx
RAZORPAY_KEY_SECRET=xxxxxxxxxxxxxxxx
RAZORPAY_WEBHOOK_SECRET=xxxxxxxxxxxxxxxx
```

**Zero code or UI changes are required**; the backend provider abstraction handles the protocol automatically.
