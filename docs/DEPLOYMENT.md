# KidsG Deployment Guide

This guide outlines how to deploy the KidsG backend to **Vercel** and configure the production **Supabase PostgreSQL** database.

---

## 1. Supabase Project Setup

1. Sign in to [supabase.com](https://supabase.com) and create a new project: `kidsg-production`.
2. Retrieve your project credentials from **Project Settings > API**:
   - `Project URL`
   - `anon public key`
   - `service_role secret key`
3. Execute the database migrations in sequential order via the **SQL Editor**:
   - `backend/supabase/migrations/001_initial_schema.sql` (Creates all 19 tables, indexes, constraints)
   - `backend/supabase/migrations/002_rls_policies.sql` (Enables Row Level Security and customer isolation)
4. (Optional) Run the seed script to populate initial categories and partner stationery items:
   ```bash
   cd backend
   npm run seed
   ```

---

## 2. Vercel Backend Deployment

1. Install the Vercel CLI:
   ```bash
   npm install -g vercel
   ```
2. Navigate to the backend directory:
   ```bash
   cd backend
   ```
3. Link and configure your project:
   ```bash
   vercel
   ```
4. Set the required production environment variables in the Vercel Dashboard (**Settings > Environment Variables**):
   - `NODE_ENV=production`
   - `APP_ENV=PRODUCTION`
   - `SUPABASE_URL=https://<your-project-id>.supabase.co`
   - `SUPABASE_ANON_KEY=<your-anon-key>`
   - `SUPABASE_SERVICE_ROLE_KEY=<your-service-role-key>`
   - `SUPABASE_JWT_SECRET=<your-jwt-secret>`
   - `OTP_PROVIDER=msg91` (or `twilio`)
   - `OTP_API_KEY=<your-sms-api-key>`
   - `PAYMENT_PROVIDER=razorpay`
   - `RAZORPAY_KEY_ID=<your-razorpay-key-id>`
   - `RAZORPAY_KEY_SECRET=<your-razorpay-key-secret>`
   - `RAZORPAY_WEBHOOK_SECRET=<your-webhook-secret>`
   - `FREE_DELIVERY_THRESHOLD=199`
   - `DEFAULT_DELIVERY_FEE=30`
5. Deploy to Production:
   ```bash
   vercel --prod
   ```
6. Verify deployment by querying the health endpoint:
   ```bash
   curl https://<your-vercel-domain>/api/health
   ```

---

## 3. Mobile App Release Build (Android)

1. Switch `ApiConfig` in `shared/src/commonMain/kotlin/com/kidsg/core/config/ApiConfig.kt`:
   ```kotlin
   ApiConfig.currentEnvironment = AppEnvironment.PRODUCTION
   ApiConfig.isMockBackend = false
   ```
2. Build the signed release APK or Android App Bundle (AAB):
   ```bash
   ./gradlew :androidApp:bundleRelease
   ```
3. The generated bundle is located at:
   `androidApp/build/outputs/bundle/release/androidApp-release.aab`
