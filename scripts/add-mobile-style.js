import fs from 'fs';

const cssPath = 'C:/Users/admin/Projects/kidsg-partner-app/src/index.css';
let css = fs.readFileSync(cssPath, 'utf8');

const mobileCSS = `
/* Responsive adjustments for real mobile phones */
@media (max-width: 640px) {
  body {
    padding: 0 !important;
    background: #ffffff !important;
  }

  .viewport-controls {
    display: none !important;
  }

  .phone-frame {
    max-width: 100% !important;
    width: 100vw !important;
    height: 100dvh !important;
    border-radius: 0 !important;
    box-shadow: none !important;
    border: none !important;
  }

  .phone-status-bar {
    display: none !important;
  }
}
`;

if (!css.includes('@media (max-width: 640px)')) {
  fs.writeFileSync(cssPath, css + mobileCSS, 'utf8');
  console.log('Mobile styles appended.');
}
