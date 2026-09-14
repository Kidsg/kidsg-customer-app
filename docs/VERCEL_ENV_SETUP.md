# KidsG Vercel Environment Variables Setup Guide

This document lists the exact environment variables required for the KidsG Production API on Vercel.

**Project Link:** [https://vercel.com/kids-g/kidsg-customer-app](https://vercel.com/kids-g/kidsg-customer-app)

---

## Instructions for the User

1. Open your Vercel Project Dashboard: **KidsG Customer App**
2. Navigate to: **Settings** > **Environment Variables**
3. Select Environment: **Production** (and optionally Preview)
4. Add the following variables:

| Variable Name | Required / Optional | Example / Guidance |
|---|---|---|
| `NODE_ENV` | Required | `production` |
| `SUPABASE_URL` | Required | `https://ogglmiewsswgkyjajnrg.supabase.co` |
| `SUPABASE_SECRET_KEY` | Required | Your Supabase secret/service_role key |
| `EMAIL_PROVIDER` | Required | `resend` |
| `RESEND_API_KEY` | Required | Your Resend API key (`re_...`) |
| `RESEND_FROM_EMAIL` | Required | Your verified domain sender (e.g. `auth@yourdomain.com`) |
| `RESEND_FROM_NAME` | Optional | `KidsG` (Default) |
| `OTP_PROVIDER` | Required | `supabase` (or `mock` for testing) |
| `PAYMENT_PROVIDER` | Required | `mock` (or `razorpay` when live credentials ready) |
| `DELIVERY_PROVIDER` | Required | `mock` |
| `MAPS_PROVIDER` | Required | `mock` |
| `NOTIFICATION_PROVIDER` | Required | `mock` |
| `FREE_DELIVERY_THRESHOLD` | Required | `199` |
| `DEFAULT_DELIVERY_FEE` | Required | `30` |
| `MINIMUM_ORDER_VALUE` | Required | `0` |
| `JWT_SECRET` | Required | A secure 64-character hex string generated for token signing |

---

> [!IMPORTANT]
> **Verified Sender Requirement:**
> Before production emails can be dispatched through Resend, configure and verify your sending domain at [resend.com/domains](https://resend.com/domains).
> Once verified, set `RESEND_FROM_EMAIL` to an address using that domain.
