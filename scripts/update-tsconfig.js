import fs from 'fs';

const tsconfigPath = 'C:/Users/admin/Projects/kidsg-partner-app/tsconfig.app.json';
const tsconfig = {
  "compilerOptions": {
    "tsBuildInfoFile": "./node_modules/.tmp/tsconfig.app.tsbuildinfo",
    "target": "es2023",
    "lib": ["ES2023", "DOM"],
    "module": "esnext",
    "types": ["vite/client"],
    "allowArbitraryExtensions": true,
    "skipLibCheck": true,

    /* Bundler mode */
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "verbatimModuleSyntax": false,
    "moduleDetection": "force",
    "noEmit": true,
    "jsx": "react-jsx",

    /* Linting */
    "noUnusedLocals": false,
    "noUnusedParameters": false,
    "erasableSyntaxOnly": false,
    "noFallthroughCasesInSwitch": true
  },
  "include": ["src"]
};

fs.writeFileSync(tsconfigPath, JSON.stringify(tsconfig, null, 2), 'utf8');
console.log('tsconfig.app.json updated.');
