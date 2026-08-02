const { spawnSync } = require('child_process');
const path = require('path');
const os = require('os');
const fs = require('fs');

const isWin = os.platform() === 'win32';
const androidDir = path.resolve(__dirname, '..', 'android');
const localPropsFile = path.join(androidDir, 'local.properties');

// Detect JDK (Android Studio JBR or system JAVA_HOME)
let javaHome = process.env.JAVA_HOME;
if (isWin) {
  const winJbr = 'C:\\Program Files\\Android\\Android Studio\\jbr';
  if (fs.existsSync(winJbr)) {
    javaHome = winJbr;
  }
} else {
  const macJbr = '/Applications/Android Studio.app/Contents/jbr/Contents/Home';
  if (fs.existsSync(macJbr)) {
    javaHome = macJbr;
  }
}

let sdkDir = process.env.ANDROID_HOME || process.env.ANDROID_SDK_ROOT;
if (!sdkDir) {
  const home = os.homedir();
  if (isWin) {
    const winSdk = path.join(home, 'AppData', 'Local', 'Android', 'Sdk');
    if (fs.existsSync(winSdk)) sdkDir = winSdk;
  } else {
    const macSdk = path.join(home, 'Library', 'Android', 'sdk');
    if (fs.existsSync(macSdk)) sdkDir = macSdk;
  }
}

if (sdkDir) {
  let formattedPath = sdkDir.replace(/\\/g, '/');
  if (isWin && formattedPath.includes(':')) {
    formattedPath = formattedPath.replace(':', '\\:');
  }
  fs.writeFileSync(localPropsFile, `sdk.dir=${formattedPath}\n`, 'utf8');
}

const env = { ...process.env };
if (javaHome) {
  env.JAVA_HOME = javaHome;
}

const gradlewCmd = isWin
  ? path.join(androidDir, 'gradlew.bat')
  : './gradlew';

const args = process.argv.slice(2);

const res = spawnSync(gradlewCmd, args, {
  cwd: isWin ? process.cwd() : androidDir,
  stdio: 'inherit',
  shell: true,
  env
});

process.exit(res.status ?? 0);
