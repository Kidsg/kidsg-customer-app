import fs from 'fs';

const appPath = 'C:/Users/admin/Projects/kidsg-partner-app/src/App.tsx';
let code = fs.readFileSync(appPath, 'utf8');

// Add deferredPrompt state and event listener
if (!code.includes('deferredPrompt')) {
  code = code.replace(
    'const [toastMessage, setToastMessage] = useState<string | null>(null);',
    `const [toastMessage, setToastMessage] = useState<string | null>(null);
  const [installPrompt, setInstallPrompt] = useState<any>(null);
  const [isStandalone, setIsStandalone] = useState(false);

  useEffect(() => {
    // Check if already installed
    if (window.matchMedia('(display-mode: standalone)').matches || (window.navigator as any).standalone) {
      setIsStandalone(true);
    }

    const handler = (e: any) => {
      e.preventDefault();
      setInstallPrompt(e);
    };
    window.addEventListener('beforeinstallprompt', handler);
    return () => window.removeEventListener('beforeinstallprompt', handler);
  }, []);

  const handleInstallClick = async () => {
    if (installPrompt) {
      installPrompt.prompt();
      const { outcome } = await installPrompt.userChoice;
      if (outcome === 'accepted') {
        setInstallPrompt(null);
        showToast('App installed successfully!');
      }
    } else {
      alert('To install on iPhone/iPad: Tap the Share button below and tap "Add to Home Screen".\\n\\nOn Android: Tap the three dots menu at top right and choose "Install app" or "Add to Home Screen".');
    }
  };`
  );

  // Add the install banner right above or below the status bar if not standalone
  code = code.replace(
    '{/* Action Toast Feedback */}',
    `{!isStandalone && (
          <div style={{
            background: 'linear-gradient(90deg, #1e3a8a, #2563eb)',
            color: 'white',
            padding: '8px 14px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            fontSize: '12px',
            fontWeight: 600,
            borderBottom: '1px solid rgba(255,255,255,0.1)'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
              <span>📲</span>
              <span>Install KidsG Partner as Phone App</span>
            </div>
            <button
              onClick={handleInstallClick}
              style={{
                background: '#ffffff',
                color: '#1d4ed8',
                border: 'none',
                padding: '4px 10px',
                borderRadius: '6px',
                fontSize: '11px',
                fontWeight: 700,
                cursor: 'pointer'
              }}
            >
              Install
            </button>
          </div>
        )}
        {/* Action Toast Feedback */}`
  );

  fs.writeFileSync(appPath, code, 'utf8');
  console.log('App.tsx updated with PWA install banner.');
} else {
  console.log('Install prompt already in App.tsx');
}
