import fs from 'fs';
import path from 'path';

const partnerAppDir = 'C:/Users/admin/Projects/kidsg-partner-app';
const srcDir = path.join(partnerAppDir, 'src');
const componentsDir = path.join(srcDir, 'components');
const servicesDir = path.join(srcDir, 'services');

fs.mkdirSync(componentsDir, { recursive: true });
fs.mkdirSync(servicesDir, { recursive: true });

function write(relPath, content) {
  const fullPath = path.join(partnerAppDir, relPath);
  fs.mkdirSync(path.dirname(fullPath), { recursive: true });
  fs.writeFileSync(fullPath, content.trim(), 'utf8');
  console.log(`Wrote: ${relPath}`);
}

// 1. Header.tsx
write('src/components/Header.tsx', `
import React from 'react';
import { Bell, Store, Smartphone, Monitor, Bike } from 'lucide-react';
import { ShopPartnerProfile } from '../types';

interface HeaderProps {
  profile: ShopPartnerProfile | null;
  unreadCount: number;
  onOpenNotifications: () => void;
  onToggleStatus: () => void;
  isFullWidth: boolean;
  onToggleWidth: () => void;
  onSwitchRole: () => void;
}

export const Header: React.FC<HeaderProps> = ({
  profile,
  unreadCount,
  onOpenNotifications,
  onToggleStatus,
  isFullWidth,
  onToggleWidth,
  onSwitchRole,
}) => {
  const isDelivery = profile?.role === 'DELIVERY_PERSON';

  return (
    <header className="app-header">
      <div className="header-shop-info">
        <div className="header-shop-name">
          {isDelivery ? (
            <Bike size={18} className="text-primary" />
          ) : (
            <Store size={18} className="text-primary" />
          )}
          <span>{isDelivery ? 'Delivery Partner' : profile?.shopName || 'Vidya Stationery'}</span>
        </div>
        <span className="header-greeting">
          {isDelivery ? \`Rider: \${profile?.ownerName || 'Venkatesh R'}\` : \`Hello, \${profile?.ownerName || 'Ramesh Kumar'}\`}
        </span>
      </div>

      <div className="header-actions">
        <button
          className={\`status-pill-toggle \${(profile?.shopStatus || 'OPEN').toLowerCase()}\`}
          onClick={onToggleStatus}
          title="Click to toggle store status"
        >
          <span className="status-indicator-dot" />
          <span>{profile?.shopStatus || 'OPEN'}</span>
        </button>

        <button 
          className="icon-btn-wrapper"
          onClick={onSwitchRole}
          title="Switch between Shop Owner and Delivery Partner"
        >
          {isDelivery ? <Store size={17} /> : <Bike size={17} />}
        </button>

        <button 
          className="icon-btn-wrapper"
          onClick={onOpenNotifications}
          title="Notifications"
        >
          <Bell size={18} />
          {unreadCount > 0 && <span className="nav-badge" style={{ top: 2, right: 2 }}>{unreadCount}</span>}
        </button>

        <button 
          className="icon-btn-wrapper"
          onClick={onToggleWidth}
          title={isFullWidth ? "Switch to Phone Frame" : "Switch to Full Screen"}
        >
          {isFullWidth ? <Smartphone size={17} /> : <Monitor size={17} />}
        </button>
      </div>
    </header>
  );
};
`);

// 2. BottomNav.tsx
write('src/components/BottomNav.tsx', `
import React from 'react';
import { LayoutDashboard, ShoppingBag, Package, Bike, IndianRupee } from 'lucide-react';

interface BottomNavProps {
  activeTab: 'dashboard' | 'orders' | 'products' | 'delivery' | 'earnings';
  onChangeTab: (tab: 'dashboard' | 'orders' | 'products' | 'delivery' | 'earnings') => void;
  pendingOrdersCount: number;
  availablePickupsCount: number;
}

export const BottomNav: React.FC<BottomNavProps> = ({
  activeTab,
  onChangeTab,
  pendingOrdersCount,
  availablePickupsCount,
}) => {
  return (
    <nav className="bottom-nav">
      <button
        className={\`nav-tab-item \${activeTab === 'dashboard' ? 'active' : ''}\`}
        onClick={() => onChangeTab('dashboard')}
      >
        <LayoutDashboard />
        <span>Home</span>
      </button>

      <button
        className={\`nav-tab-item \${activeTab === 'orders' ? 'active' : ''}\`}
        onClick={() => onChangeTab('orders')}
      >
        <ShoppingBag />
        <span>Orders</span>
        {pendingOrdersCount > 0 && <span className="nav-badge">{pendingOrdersCount}</span>}
      </button>

      <button
        className={\`nav-tab-item \${activeTab === 'products' ? 'active' : ''}\`}
        onClick={() => onChangeTab('products')}
      >
        <Package />
        <span>Inventory</span>
      </button>

      <button
        className={\`nav-tab-item \${activeTab === 'delivery' ? 'active' : ''}\`}
        onClick={() => onChangeTab('delivery')}
      >
        <Bike />
        <span>Delivery</span>
        {availablePickupsCount > 0 && <span className="nav-badge">{availablePickupsCount}</span>}
      </button>

      <button
        className={\`nav-tab-item \${activeTab === 'earnings' ? 'active' : ''}\`}
        onClick={() => onChangeTab('earnings')}
      >
        <IndianRupee />
        <span>Earnings</span>
      </button>
    </nav>
  );
};
`);

// 3. WelcomeView.tsx
write('src/components/WelcomeView.tsx', `
import React from 'react';
import { Store, Bike, ArrowRight, ShieldCheck, Zap, Sparkles } from 'lucide-react';

interface WelcomeViewProps {
  onGetStarted: () => void;
  onLogin: () => void;
  onQuickLogin: (role: 'SHOP_OWNER' | 'DELIVERY_PERSON') => void;
}

export const WelcomeView: React.FC<WelcomeViewProps> = ({
  onGetStarted,
  onLogin,
  onQuickLogin,
}) => {
  return (
    <div style={{ padding: '28px 20px', display: 'flex', flexDirection: 'column', height: '100%', justifyContent: 'space-between' }}>
      <div style={{ textAlign: 'center', marginTop: 16 }}>
        <div style={{
          width: 80,
          height: 80,
          borderRadius: 24,
          background: 'linear-gradient(135deg, #2563eb, #1d4ed8)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          margin: '0 auto 20px',
          boxShadow: '0 12px 24px -6px rgba(37, 99, 235, 0.4)',
          color: 'white'
        }}>
          <Store size={40} />
        </div>

        <div style={{ display: 'inline-flex', alignItems: 'center', gap: 6, background: '#eff6ff', color: '#2563eb', padding: '4px 12px', borderRadius: 999, fontSize: 12, fontWeight: 700, marginBottom: 12 }}>
          <Sparkles size={14} /> KIDSG PARTNER PORTAL
        </div>

        <h1 style={{ fontSize: 28, fontWeight: 800, color: '#0f172a', lineHeight: 1.2, marginBottom: 8 }}>
          KidsG Shop & Delivery Partner
        </h1>
        <p style={{ fontSize: 14, color: '#64748b', lineHeight: 1.5, maxWidth: 320, margin: '0 auto' }}>
          Accept orders from local school parents, manage your stationery catalog, and coordinate speedy 10-minute deliveries.
        </p>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: 12, margin: '24px 0' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12, background: 'white', padding: '12px 16px', borderRadius: 14, border: '1px solid #e2e8f0' }}>
          <div style={{ background: '#f0fdf4', color: '#16a34a', padding: 8, borderRadius: 10 }}>
            <Zap size={20} />
          </div>
          <div>
            <h4 style={{ fontSize: 13, fontWeight: 700 }}>Instant Order Dispatch</h4>
            <p style={{ fontSize: 12, color: '#64748b' }}>Real-time sync with KidsG Customer app</p>
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: 12, background: 'white', padding: '12px 16px', borderRadius: 14, border: '1px solid #e2e8f0' }}>
          <div style={{ background: '#f5f3ff', color: '#7c3aed', padding: 8, borderRadius: 10 }}>
            <Bike size={20} />
          </div>
          <div>
            <h4 style={{ fontSize: 13, fontWeight: 700 }}>Integrated Delivery Fleet</h4>
            <p style={{ fontSize: 12, color: '#64748b' }}>Pickups, turn-by-turn steps & proof of delivery</p>
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: 12, background: 'white', padding: '12px 16px', borderRadius: 14, border: '1px solid #e2e8f0' }}>
          <div style={{ background: '#fef3c7', color: '#d97706', padding: 8, borderRadius: 10 }}>
            <ShieldCheck size={20} />
          </div>
          <div>
            <h4 style={{ fontSize: 13, fontWeight: 700 }}>Daily Direct Settlements</h4>
            <p style={{ fontSize: 12, color: '#64748b' }}>Automated payouts directly to your UPI/bank</p>
          </div>
        </div>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
        <button className="btn-primary" onClick={onGetStarted}>
          <span>Register New Store</span>
          <ArrowRight size={16} />
        </button>
        <button className="btn-secondary" onClick={onLogin}>
          <span>Login with Existing Account</span>
        </button>

        <div style={{ textAlign: 'center', margin: '10px 0 4px', fontSize: 11, fontWeight: 700, color: '#94a3b8' }}>
          ── OR QUICK DEMO LOGIN ──
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8 }}>
          <button 
            onClick={() => onQuickLogin('SHOP_OWNER')}
            style={{
              padding: '10px',
              borderRadius: 12,
              background: '#eff6ff',
              border: '1px solid #bfdbfe',
              color: '#1e40af',
              fontSize: 12,
              fontWeight: 700,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: 6,
              cursor: 'pointer'
            }}
          >
            <Store size={15} /> Shop Owner
          </button>
          <button 
            onClick={() => onQuickLogin('DELIVERY_PERSON')}
            style={{
              padding: '10px',
              borderRadius: 12,
              background: '#f0fdf4',
              border: '1px solid #bbf7d0',
              color: '#15803d',
              fontSize: 12,
              fontWeight: 700,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: 6,
              cursor: 'pointer'
            }}
          >
            <Bike size={15} /> Delivery Rider
          </button>
        </div>
      </div>
    </div>
  );
};
`);

// 4. LoginView.tsx
write('src/components/LoginView.tsx', `
import React, { useState } from 'react';
import { ArrowLeft, Mail, Lock, Phone, ArrowRight, Store, Bike } from 'lucide-react';

interface LoginViewProps {
  onBack: () => void;
  onSuccess: (email: string, pass: string, role: 'SHOP_OWNER' | 'DELIVERY_PERSON') => void;
  onGoToRegister: () => void;
}

export const LoginView: React.FC<LoginViewProps> = ({
  onBack,
  onSuccess,
  onGoToRegister,
}) => {
  const [tab, setTab] = useState<'password' | 'otp'>('password');
  const [email, setEmail] = useState('shop@kidsg.in');
  const [password, setPassword] = useState('Partner123!');
  const [phone, setPhone] = useState('9845012345');
  const [otp, setOtp] = useState('');
  const [otpSent, setOtpSent] = useState(false);
  const [role, setRole] = useState<'SHOP_OWNER' | 'DELIVERY_PERSON'>('SHOP_OWNER');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSuccess(email, password, role);
  };

  const handleQuickFill = (targetRole: 'SHOP_OWNER' | 'DELIVERY_PERSON') => {
    setRole(targetRole);
    if (targetRole === 'SHOP_OWNER') {
      setEmail('shop@kidsg.in');
      setPassword('Partner123!');
      setPhone('9845012345');
    } else {
      setEmail('rider@kidsg.in');
      setPassword('Rider123!');
      setPhone('9880067890');
    }
  };

  return (
    <div style={{ padding: '20px', display: 'flex', flexDirection: 'column', height: '100%' }}>
      <button 
        onClick={onBack}
        style={{ background: 'none', border: 'none', display: 'flex', alignItems: 'center', gap: 6, color: '#64748b', fontSize: 13, fontWeight: 600, cursor: 'pointer', marginBottom: 20 }}
      >
        <ArrowLeft size={16} /> Back
      </button>

      <div style={{ marginBottom: 24 }}>
        <h2 style={{ fontSize: 24, fontWeight: 800, color: '#0f172a', marginBottom: 6 }}>
          Welcome Partner!
        </h2>
        <p style={{ fontSize: 13, color: '#64748b' }}>
          Login to access your store orders and delivery operations
        </p>
      </div>

      {/* Role Toggle */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', background: '#f1f5f9', padding: 4, borderRadius: 12, marginBottom: 16 }}>
        <button
          type="button"
          onClick={() => handleQuickFill('SHOP_OWNER')}
          style={{
            padding: '8px 12px',
            border: 'none',
            borderRadius: 8,
            fontSize: 13,
            fontWeight: 700,
            cursor: 'pointer',
            background: role === 'SHOP_OWNER' ? 'white' : 'transparent',
            color: role === 'SHOP_OWNER' ? '#2563eb' : '#64748b',
            boxShadow: role === 'SHOP_OWNER' ? '0 1px 3px rgba(0,0,0,0.1)' : 'none',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 6
          }}
        >
          <Store size={15} /> Shop Owner
        </button>
        <button
          type="button"
          onClick={() => handleQuickFill('DELIVERY_PERSON')}
          style={{
            padding: '8px 12px',
            border: 'none',
            borderRadius: 8,
            fontSize: 13,
            fontWeight: 700,
            cursor: 'pointer',
            background: role === 'DELIVERY_PERSON' ? 'white' : 'transparent',
            color: role === 'DELIVERY_PERSON' ? '#16a34a' : '#64748b',
            boxShadow: role === 'DELIVERY_PERSON' ? '0 1px 3px rgba(0,0,0,0.1)' : 'none',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 6
          }}
        >
          <Bike size={15} /> Delivery Rider
        </button>
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', borderBottom: '1px solid #e2e8f0', marginBottom: 20 }}>
        <button
          type="button"
          onClick={() => setTab('password')}
          style={{
            flex: 1,
            padding: '10px 0',
            border: 'none',
            background: 'none',
            fontSize: 13,
            fontWeight: 700,
            color: tab === 'password' ? '#2563eb' : '#64748b',
            borderBottom: tab === 'password' ? '2px solid #2563eb' : 'none',
            cursor: 'pointer'
          }}
        >
          Password Login
        </button>
        <button
          type="button"
          onClick={() => setTab('otp')}
          style={{
            flex: 1,
            padding: '10px 0',
            border: 'none',
            background: 'none',
            fontSize: 13,
            fontWeight: 700,
            color: tab === 'otp' ? '#2563eb' : '#64748b',
            borderBottom: tab === 'otp' ? '2px solid #2563eb' : 'none',
            cursor: 'pointer'
          }}
        >
          Phone OTP
        </button>
      </div>

      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
        {tab === 'password' ? (
          <>
            <div>
              <label style={{ fontSize: 12, fontWeight: 700, color: '#334155', display: 'block', marginBottom: 6 }}>
                Email Address
              </label>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8, background: 'white', border: '1px solid #cbd5e1', borderRadius: 10, padding: '10px 14px' }}>
                <Mail size={17} color="#94a3b8" />
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="e.g. shop@kidsg.in"
                  required
                  style={{ border: 'none', outline: 'none', width: '100%', fontSize: 14 }}
                />
              </div>
            </div>

            <div>
              <label style={{ fontSize: 12, fontWeight: 700, color: '#334155', display: 'block', marginBottom: 6 }}>
                Password
              </label>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8, background: 'white', border: '1px solid #cbd5e1', borderRadius: 10, padding: '10px 14px' }}>
                <Lock size={17} color="#94a3b8" />
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  required
                  style={{ border: 'none', outline: 'none', width: '100%', fontSize: 14 }}
                />
              </div>
            </div>
          </>
        ) : (
          <>
            <div>
              <label style={{ fontSize: 12, fontWeight: 700, color: '#334155', display: 'block', marginBottom: 6 }}>
                Registered Mobile Number
              </label>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8, background: 'white', border: '1px solid #cbd5e1', borderRadius: 10, padding: '10px 14px' }}>
                <Phone size={17} color="#94a3b8" />
                <span style={{ fontSize: 14, fontWeight: 600, color: '#64748b' }}>+91</span>
                <input
                  type="tel"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  placeholder="9845012345"
                  maxLength={10}
                  required
                  style={{ border: 'none', outline: 'none', width: '100%', fontSize: 14 }}
                />
              </div>
            </div>

            {otpSent && (
              <div>
                <label style={{ fontSize: 12, fontWeight: 700, color: '#334155', display: 'block', marginBottom: 6 }}>
                  Enter 6-Digit OTP (Demo: 123456)
                </label>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, background: 'white', border: '1px solid #cbd5e1', borderRadius: 10, padding: '10px 14px' }}>
                  <input
                    type="text"
                    value={otp}
                    onChange={(e) => setOtp(e.target.value)}
                    placeholder="123456"
                    maxLength={6}
                    required
                    style={{ border: 'none', outline: 'none', width: '100%', fontSize: 16, letterSpacing: 4, textAlign: 'center' }}
                  />
                </div>
              </div>
            )}

            {!otpSent && (
              <button
                type="button"
                className="btn-secondary"
                onClick={() => setOtpSent(true)}
              >
                Send Verification OTP
              </button>
            )}
          </>
        )}

        <button type="submit" className="btn-primary" style={{ marginTop: 8 }}>
          <span>Login to Dashboard</span>
          <ArrowRight size={16} />
        </button>
      </form>

      <div style={{ marginTop: 'auto', paddingTop: 20, textAlign: 'center' }}>
        <p style={{ fontSize: 13, color: '#64748b' }}>
          Don't have a partner account?{' '}
          <button
            onClick={onGoToRegister}
            style={{ background: 'none', border: 'none', color: '#2563eb', fontWeight: 700, cursor: 'pointer' }}
          >
            Register Your Shop
          </button>
        </p>
      </div>
    </div>
  );
};
`);

// 5. RegisterView.tsx
write('src/components/RegisterView.tsx', `
import React, { useState } from 'react';
import { ArrowLeft, ArrowRight, CheckCircle2 } from 'lucide-react';

interface RegisterViewProps {
  onBack: () => void;
  onSuccess: () => void;
}

export const RegisterView: React.FC<RegisterViewProps> = ({
  onBack,
  onSuccess,
}) => {
  const [step, setStep] = useState(1);
  const [formData, setFormData] = useState({
    ownerName: '',
    phone: '',
    email: '',
    password: '',
    shopName: '',
    shopCategory: 'Books & Stationery',
    openTime: '08:00 AM',
    closeTime: '09:00 PM',
    address: '',
    area: 'Indiranagar',
    city: 'Bengaluru',
    pincode: '560038',
    upiId: ''
  });

  const nextStep = () => setStep(s => Math.min(s + 1, 3));
  const prevStep = () => setStep(s => Math.max(s - 1, 1));

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSuccess();
  };

  return (
    <div style={{ padding: '20px', display: 'flex', flexDirection: 'column', height: '100%' }}>
      <button 
        onClick={step === 1 ? onBack : prevStep}
        style={{ background: 'none', border: 'none', display: 'flex', alignItems: 'center', gap: 6, color: '#64748b', fontSize: 13, fontWeight: 600, cursor: 'pointer', marginBottom: 16 }}
      >
        <ArrowLeft size={16} /> {step === 1 ? 'Back to Login' : 'Previous Step'}
      </button>

      <div style={{ marginBottom: 20 }}>
        <h2 style={{ fontSize: 22, fontWeight: 800, color: '#0f172a' }}>Register Your Shop</h2>
        <p style={{ fontSize: 13, color: '#64748b' }}>Step {step} of 3 • Partner Onboarding</p>

        {/* Progress Bar */}
        <div style={{ display: 'flex', gap: 6, marginTop: 12 }}>
          <div style={{ flex: 1, height: 4, borderRadius: 2, background: step >= 1 ? '#2563eb' : '#e2e8f0' }} />
          <div style={{ flex: 1, height: 4, borderRadius: 2, background: step >= 2 ? '#2563eb' : '#e2e8f0' }} />
          <div style={{ flex: 1, height: 4, borderRadius: 2, background: step >= 3 ? '#2563eb' : '#e2e8f0' }} />
        </div>
      </div>

      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 14, flex: 1 }}>
        {step === 1 && (
          <>
            <div>
              <label style={{ fontSize: 12, fontWeight: 700, color: '#334155', display: 'block', marginBottom: 4 }}>
                Owner Full Name *
              </label>
              <input
                type="text"
                value={formData.ownerName}
                onChange={e => setFormData({ ...formData, ownerName: e.target.value })}
                placeholder="e.g. Ramesh Kumar"
                required
                style={{ width: '100%', padding: '10px 14px', borderRadius: 10, border: '1px solid #cbd5e1', fontSize: 14 }}
              />
            </div>

            <div>
              <label style={{ fontSize: 12, fontWeight: 700, color: '#334155', display: 'block', marginBottom: 4 }}>
                Mobile Number *
              </label>
              <input
                type="tel"
                value={formData.phone}
                onChange={e => setFormData({ ...formData, phone: e.target.value })}
                placeholder="10-digit mobile number"
                required
                style={{ width: '100%', padding: '10px 14px', borderRadius: 10, border: '1px solid #cbd5e1', fontSize: 14 }}
              />
            </div>

            <div>
              <label style={{ fontSize: 12, fontWeight: 700, color: '#334155', display: 'block', marginBottom: 4 }}>
                Email Address *
              </label>
              <input
                type="email"
                value={formData.email}
                onChange={e => setFormData({ ...formData, email: e.target.value })}
                placeholder="shop@example.com"
                required
                style={{ width: '100%', padding: '10px 14px', borderRadius: 10, border: '1px solid #cbd5e1', fontSize: 14 }}
              />
            </div>

            <div>
              <label style={{ fontSize: 12, fontWeight: 700, color: '#334155', display: 'block', marginBottom: 4 }}>
                Create Password *
              </label>
              <input
                type="password"
                value={formData.password}
                onChange={e => setFormData({ ...formData, password: e.target.value })}
                placeholder="Min. 8 characters"
                required
                style={{ width: '100%', padding: '10px 14px', borderRadius: 10, border: '1px solid #cbd5e1', fontSize: 14 }}
              />
            </div>

            <button type="button" className="btn-primary" onClick={nextStep} style={{ marginTop: 'auto' }}>
              <span>Next: Shop Details</span>
              <ArrowRight size={16} />
            </button>
          </>
        )}

        {step === 2 && (
          <>
            <div>
              <label style={{ fontSize: 12, fontWeight: 700, color: '#334155', display: 'block', marginBottom: 4 }}>
                Store / Shop Name *
              </label>
              <input
                type="text"
                value={formData.shopName}
                onChange={e => setFormData({ ...formData, shopName: e.target.value })}
                placeholder="e.g. Vidya Book & Stationery Depot"
                required
                style={{ width: '100%', padding: '10px 14px', borderRadius: 10, border: '1px solid #cbd5e1', fontSize: 14 }}
              />
            </div>

            <div>
              <label style={{ fontSize: 12, fontWeight: 700, color: '#334155', display: 'block', marginBottom: 4 }}>
                Primary Category
              </label>
              <select
                value={formData.shopCategory}
                onChange={e => setFormData({ ...formData, shopCategory: e.target.value })}
                style={{ width: '100%', padding: '10px 14px', borderRadius: 10, border: '1px solid #cbd5e1', fontSize: 14, background: 'white' }}
              >
                <option>Books & School Stationery</option>
                <option>Art & Craft Supplies</option>
                <option>Uniforms & Bags</option>
                <option>General All-in-One Stationery</option>
              </select>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
              <div>
                <label style={{ fontSize: 12, fontWeight: 700, color: '#334155', display: 'block', marginBottom: 4 }}>
                  Opening Time
                </label>
                <input
                  type="text"
                  value={formData.openTime}
                  onChange={e => setFormData({ ...formData, openTime: e.target.value })}
                  style={{ width: '100%', padding: '10px 14px', borderRadius: 10, border: '1px solid #cbd5e1', fontSize: 14 }}
                />
              </div>
              <div>
                <label style={{ fontSize: 12, fontWeight: 700, color: '#334155', display: 'block', marginBottom: 4 }}>
                  Closing Time
                </label>
                <input
                  type="text"
                  value={formData.closeTime}
                  onChange={e => setFormData({ ...formData, closeTime: e.target.value })}
                  style={{ width: '100%', padding: '10px 14px', borderRadius: 10, border: '1px solid #cbd5e1', fontSize: 14 }}
                />
              </div>
            </div>

            <button type="button" className="btn-primary" onClick={nextStep} style={{ marginTop: 'auto' }}>
              <span>Next: Location & Payout</span>
              <ArrowRight size={16} />
            </button>
          </>
        )}

        {step === 3 && (
          <>
            <div>
              <label style={{ fontSize: 12, fontWeight: 700, color: '#334155', display: 'block', marginBottom: 4 }}>
                Shop Physical Address *
              </label>
              <textarea
                value={formData.address}
                onChange={e => setFormData({ ...formData, address: e.target.value })}
                placeholder="Shop number, street, near landmark"
                required
                rows={3}
                style={{ width: '100%', padding: '10px 14px', borderRadius: 10, border: '1px solid #cbd5e1', fontSize: 14 }}
              />
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
              <div>
                <label style={{ fontSize: 12, fontWeight: 700, color: '#334155', display: 'block', marginBottom: 4 }}>
                  Area
                </label>
                <input
                  type="text"
                  value={formData.area}
                  onChange={e => setFormData({ ...formData, area: e.target.value })}
                  style={{ width: '100%', padding: '10px 14px', borderRadius: 10, border: '1px solid #cbd5e1', fontSize: 14 }}
                />
              </div>
              <div>
                <label style={{ fontSize: 12, fontWeight: 700, color: '#334155', display: 'block', marginBottom: 4 }}>
                  Pincode
                </label>
                <input
                  type="text"
                  value={formData.pincode}
                  onChange={e => setFormData({ ...formData, pincode: e.target.value })}
                  style={{ width: '100%', padding: '10px 14px', borderRadius: 10, border: '1px solid #cbd5e1', fontSize: 14 }}
                />
              </div>
            </div>

            <div>
              <label style={{ fontSize: 12, fontWeight: 700, color: '#334155', display: 'block', marginBottom: 4 }}>
                UPI ID for Instant Payouts *
              </label>
              <input
                type="text"
                value={formData.upiId}
                onChange={e => setFormData({ ...formData, upiId: e.target.value })}
                placeholder="e.g. vidya.stationery@okhdfcbank"
                required
                style={{ width: '100%', padding: '10px 14px', borderRadius: 10, border: '1px solid #cbd5e1', fontSize: 14 }}
              />
            </div>

            <button type="submit" className="btn-success" style={{ marginTop: 'auto' }}>
              <CheckCircle2 size={18} />
              <span>Complete Shop Registration</span>
            </button>
          </>
        )}
      </form>
    </div>
  );
};
`);

console.log('Header, BottomNav, Welcome, Login, Register views created.');
