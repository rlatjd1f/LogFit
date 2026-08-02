const { spawnSync } = require('child_process');
const path = require('path');
const os = require('os');

const isWin = os.platform() === 'win32';
const androidDir = path.resolve(__dirname, '..', 'android');
const gradlewCmd = isWin
  ? path.join(androidDir, 'gradlew.bat')
  : './gradlew';

const args = process.argv.slice(2);

const res = spawnSync(gradlewCmd, args, {
  cwd: isWin ? process.cwd() : androidDir,
  stdio: 'inherit',
  shell: true
});

process.exit(res.status ?? 0);
