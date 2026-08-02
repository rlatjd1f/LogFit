const { spawnSync } = require('child_process');
const path = require('path');
const os = require('os');
const fs = require('fs');

// 1. Install Debug APK via gradlew
const gradlewRes = spawnSync('node', [path.join(__dirname, 'gradlew.js'), '-p', 'android', 'installDebug'], {
  stdio: 'inherit',
  shell: true
});

if (gradlewRes.status !== 0) {
  process.exit(gradlewRes.status ?? 1);
}

// 2. Find adb path
const isWin = os.platform() === 'win32';
let adbCmd = 'adb';
const home = os.homedir();
if (isWin) {
  const winAdb = path.join(home, 'AppData', 'Local', 'Android', 'Sdk', 'platform-tools', 'adb.exe');
  if (fs.existsSync(winAdb)) adbCmd = winAdb;
} else {
  const macAdb = path.join(home, 'Library', 'Android', 'sdk', 'platform-tools', 'adb');
  if (fs.existsSync(macAdb)) adbCmd = macAdb;
}

// 3. Launch MainActivity on device/emulator
console.log('Launching LogFit app on connected device/emulator...');
const adbRes = spawnSync(adbCmd, ['shell', 'am', 'start', '-n', 'com.logfit.app/.MainActivity'], {
  stdio: 'inherit',
  shell: true
});

process.exit(adbRes.status ?? 0);
