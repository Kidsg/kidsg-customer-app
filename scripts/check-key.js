import fs from 'fs';
const content = fs.readFileSync('c:/Users/admin/Projects/kidsg-app/backend/api/index.js', 'utf8');
const matches = content.match(/eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9\.[a-zA-Z0-9_-]+\.[a-zA-Z0-9_-]+/g);
if (matches) {
  for (const m of matches) {
    try {
      const payload = JSON.parse(Buffer.from(m.split('.')[1], 'base64').toString());
      console.log('Role:', payload.role, 'JWT length:', m.length);
      if (payload.role === 'service_role') {
        console.log('SERVICE_ROLE_KEY:', m);
      }
    } catch {}
  }
} else {
  console.log('No matches found.');
}
