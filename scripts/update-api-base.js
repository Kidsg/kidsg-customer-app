import fs from 'fs';

const apiTsPath = 'C:/Users/admin/Projects/kidsg-partner-app/src/services/api.ts';
let code = fs.readFileSync(apiTsPath, 'utf8');

code = code.replace(
  "const API_BASE = 'https://kidsg-customer-app.vercel.app/api';",
  "const API_BASE = typeof window !== 'undefined' && window.location.origin.includes('kidsg-partner-app') ? '/api' : 'https://kidsg-partner-app.vercel.app/api';"
);

fs.writeFileSync(apiTsPath, code, 'utf8');
console.log('Updated API_BASE in api.ts');
