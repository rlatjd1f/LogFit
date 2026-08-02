const fs = require('fs');
const path = require('path');

const filesToCopy = [
  'index.html',
  'privacy.html',
  'logfit-core.js',
  'manifest.json',
  'sw.js',
  'chart.umd.min.js',
  'code_artifact.html',
  'icon.svg',
  'favicon.svg',
  'favicon-16.png',
  'favicon-32.png',
  'icon-192.png',
  'icon-512.png'
];

const rootDir = path.resolve(__dirname, '..');
const wwwDir = path.join(rootDir, 'www');

fs.rmSync(wwwDir, { recursive: true, force: true });
fs.mkdirSync(wwwDir, { recursive: true });

for (const file of filesToCopy) {
  const src = path.join(rootDir, file);
  const dst = path.join(wwwDir, file);
  if (fs.existsSync(src)) {
    fs.copyFileSync(src, dst);
  }
}
console.log('Build completed successfully.');
