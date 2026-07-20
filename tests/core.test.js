const test = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const { escapeHtml, normalizeTimerSnapshot } = require("../logfit-core.js");

test("escapeHtml은 사용자 HTML과 속성 문자를 이스케이프한다", () => {
  assert.equal(escapeHtml(`<img src=x onerror="alert('x')">`), "&lt;img src=x onerror=&quot;alert(&#39;x&#39;)&quot;&gt;");
});

test("normalizeTimerSnapshot은 endAt을 기준으로 남은 시간을 계산한다", () => {
  assert.deepEqual(normalizeTimerSnapshot({ isRunning: true, endAt: 12_500, seconds: 99, label: "벤치" }, 10_000), {
    isRunning: true,
    endAt: 12_500,
    seconds: 3,
    label: "벤치"
  });
  assert.equal(normalizeTimerSnapshot({ isRunning: false, seconds: 10 }, 10_000), null);
});

test("웹, 패키지, Android 버전과 서비스 워커 캐시가 일치한다", () => {
  const root = path.resolve(__dirname, "..");
  const pkg = JSON.parse(fs.readFileSync(path.join(root, "package.json"), "utf8"));
  const html = fs.readFileSync(path.join(root, "index.html"), "utf8");
  const gradle = fs.readFileSync(path.join(root, "android/app/build.gradle"), "utf8");
  const sw = fs.readFileSync(path.join(root, "sw.js"), "utf8");
  assert.match(html, new RegExp(`const appVersion = "${pkg.version.replaceAll(".", "\\.")}"`));
  assert.match(gradle, new RegExp(`versionName "${pkg.version.replaceAll(".", "\\.")}"`));
  assert.match(html, /const cacheVersion = "v27"/);
  assert.match(sw, /logfit-pwa-v27/);
});
