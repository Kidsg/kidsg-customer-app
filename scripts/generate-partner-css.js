import fs from 'fs';
import path from 'path';

const target = 'C:/Users/admin/Projects/kidsg-partner-app/src/index.css';

const css = `
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700&display=swap');

:root {
  --primary: #2563eb;
  --primary-hover: #1d4ed8;
  --primary-light: #eff6ff;
  --primary-border: #bfdbfe;
  
  --success: #16a34a;
  --success-bg: #f0fdf4;
  --success-border: #bbf7d0;

  --warning: #d97706;
  --warning-bg: #fffbeb;
  --warning-border: #fde68a;

  --danger: #dc2626;
  --danger-bg: #fef2f2;
  --danger-border: #fecaca;

  --purple: #7c3aed;
  --purple-bg: #f5f3ff;
  --purple-border: #ddd6fe;

  --slate-900: #0f172a;
  --slate-800: #1e293b;
  --slate-700: #334155;
  --slate-600: #475569;
  --slate-500: #64748b;
  --slate-400: #94a3b8;
  --slate-200: #e2e8f0;
  --slate-100: #f1f5f9;
  --slate-50: #f8fafc;
  
  --radius-sm: 8px;
  --radius-md: 12px;
  --radius-lg: 16px;
  --radius-xl: 24px;
  --radius-full: 9999px;

  --shadow-sm: 0 1px 2px 0 rgb(0 0 0 / 0.05);
  --shadow-md: 0 4px 6px -1px rgb(0 0 0 / 0.07), 0 2px 4px -2px rgb(0 0 0 / 0.05);
  --shadow-lg: 0 10px 15px -3px rgb(0 0 0 / 0.08), 0 4px 6px -4px rgb(0 0 0 / 0.04);
}

* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
  font-family: 'Plus Jakarta Sans', 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
  -webkit-tap-highlight-color: transparent;
}

body {
  background: #0b0f19;
  color: var(--slate-900);
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 16px;
}

/* App container */
.app-viewport-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.viewport-controls {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  max-width: 440px;
  margin-bottom: 12px;
  color: #94a3b8;
  font-size: 13px;
  font-weight: 500;
}

.viewport-toggle-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #1e293b;
  color: #e2e8f0;
  border: 1px solid #334155;
  border-radius: var(--radius-full);
  padding: 6px 14px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.viewport-toggle-btn:hover {
  background: #334155;
}

/* Device frame mockup */
.phone-frame {
  width: 100%;
  max-width: 420px;
  height: 870px;
  background: #ffffff;
  border-radius: 40px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.7), 0 0 0 10px #1e293b, 0 0 0 12px #334155;
  overflow: hidden;
  position: relative;
  display: flex;
  flex-direction: column;
}

.phone-frame.full-width {
  max-width: 640px;
  height: 92vh;
  border-radius: 20px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.5);
}

/* Status Bar */
.phone-status-bar {
  height: 38px;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  font-size: 12px;
  font-weight: 600;
  color: var(--slate-900);
  z-index: 100;
  flex-shrink: 0;
  border-bottom: 1px solid rgba(0,0,0,0.03);
}

.dynamic-island {
  width: 90px;
  height: 18px;
  background: #000000;
  border-radius: 20px;
}

/* Screen Content Area */
.screen-scroll-container {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  background: var(--slate-50);
  display: flex;
  flex-direction: column;
  padding-bottom: 74px; /* for bottom nav */
}

.screen-scroll-container.no-nav {
  padding-bottom: 20px;
}

/* Bottom Nav */
.bottom-nav {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 68px;
  background: #ffffff;
  border-top: 1px solid var(--slate-200);
  display: flex;
  align-items: center;
  justify-content: space-around;
  z-index: 90;
  padding-bottom: 4px;
}

.nav-tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: var(--slate-500);
  font-size: 11px;
  font-weight: 600;
  text-decoration: none;
  cursor: pointer;
  background: none;
  border: none;
  padding: 6px 12px;
  position: relative;
  transition: all 0.2s ease;
}

.nav-tab-item.active {
  color: var(--primary);
}

.nav-tab-item svg {
  width: 20px;
  height: 20px;
}

.nav-badge {
  position: absolute;
  top: 2px;
  right: 8px;
  background: var(--danger);
  color: white;
  font-size: 10px;
  font-weight: 700;
  padding: 1px 5px;
  border-radius: 10px;
  min-width: 16px;
  text-align: center;
}

/* Header Component */
.app-header {
  background: #ffffff;
  padding: 14px 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--slate-200);
  position: sticky;
  top: 0;
  z-index: 80;
}

.header-shop-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.header-shop-name {
  font-size: 14px;
  font-weight: 700;
  color: var(--slate-900);
  display: flex;
  align-items: center;
  gap: 6px;
}

.header-greeting {
  font-size: 11px;
  color: var(--slate-500);
  font-weight: 500;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 6px;
}

.status-pill-toggle {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 5px 10px;
  border-radius: var(--radius-full);
  font-size: 11px;
  font-weight: 700;
  cursor: pointer;
  border: none;
}

.status-pill-toggle.open {
  background: var(--success-bg);
  color: var(--success);
  border: 1px solid var(--success-border);
}

.status-pill-toggle.busy {
  background: var(--warning-bg);
  color: var(--warning);
  border: 1px solid var(--warning-border);
}

.status-pill-toggle.closed {
  background: var(--danger-bg);
  color: var(--danger);
  border: 1px solid var(--danger-border);
}

.status-indicator-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: currentColor;
}

.icon-btn-wrapper {
  position: relative;
  background: var(--slate-100);
  border: none;
  border-radius: 50%;
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--slate-700);
}

/* Common cards & badges */
.card {
  background: #ffffff;
  border-radius: var(--radius-lg);
  border: 1px solid var(--slate-200);
  padding: 16px;
  box-shadow: var(--shadow-sm);
}

.badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 8px;
  border-radius: var(--radius-full);
  font-size: 11px;
  font-weight: 700;
}

.badge-new, .badge-confirmed { background: #e0f2fe; color: #0284c7; }
.badge-accepted { background: #eff6ff; color: #2563eb; }
.badge-preparing { background: #fef3c7; color: #d97706; }
.badge-ready-for-pickup { background: #f3e8ff; color: #7c3aed; }
.badge-out-for-delivery, .badge-delivery { background: #fed7aa; color: #ea580c; }
.badge-delivered { background: #dcfce7; color: #16a34a; }
.badge-cancelled { background: #fee2e2; color: #dc2626; }

/* Buttons */
.btn-primary {
  background: var(--primary);
  color: #ffffff;
  border: none;
  border-radius: var(--radius-md);
  padding: 12px 18px;
  font-size: 14px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.15s ease;
  width: 100%;
}

.btn-primary:hover {
  background: var(--primary-hover);
}

.btn-secondary {
  background: var(--slate-100);
  color: var(--slate-800);
  border: 1px solid var(--slate-200);
  border-radius: var(--radius-md);
  padding: 10px 16px;
  font-size: 13px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
}

.btn-success {
  background: var(--success);
  color: white;
  border: none;
  border-radius: var(--radius-md);
  padding: 12px 18px;
  font-size: 14px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  width: 100%;
}

.btn-danger-outline {
  background: white;
  color: var(--danger);
  border: 1.5px solid var(--danger-border);
  border-radius: var(--radius-md);
  padding: 12px 18px;
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

/* Toast Message */
.toast-banner {
  position: absolute;
  top: 48px;
  left: 16px;
  right: 16px;
  background: #0f172a;
  color: #ffffff;
  padding: 10px 16px;
  border-radius: var(--radius-md);
  font-size: 13px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: var(--shadow-lg);
  z-index: 200;
  animation: slideDown 0.25s ease;
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Modals */
.modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  backdrop-filter: blur(3px);
  z-index: 150;
  display: flex;
  align-items: flex-end;
}

.modal-sheet {
  background: #ffffff;
  width: 100%;
  border-radius: 24px 24px 0 0;
  padding: 20px;
  max-height: 88%;
  overflow-y: auto;
  box-shadow: 0 -10px 25px rgba(0,0,0,0.15);
  animation: slideUp 0.25s ease;
}

@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}
`;

fs.writeFileSync(target, css.trim(), 'utf8');
console.log('index.css updated successfully.');
