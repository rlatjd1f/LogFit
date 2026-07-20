(function (globalScope) {
  "use strict";

  function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>'"]/g, (character) => ({
      "&": "&amp;",
      "<": "&lt;",
      ">": "&gt;",
      "'": "&#39;",
      '"': "&quot;"
    })[character]);
  }

  function normalizeTimerSnapshot(snapshot, now = Date.now()) {
    if (!snapshot?.isRunning) return null;
    const endAt = Number(snapshot.endAt) || 0;
    const reportedSeconds = Math.max(0, Number(snapshot.seconds) || 0);
    const seconds = endAt > now ? Math.ceil((endAt - now) / 1000) : reportedSeconds;
    if (seconds <= 0) return null;
    return {
      isRunning: true,
      endAt: endAt > now ? endAt : now + seconds * 1000,
      seconds,
      label: String(snapshot.label || "")
    };
  }

  const api = { escapeHtml, normalizeTimerSnapshot };
  if (typeof module !== "undefined" && module.exports) module.exports = api;
  globalScope.LogFitCore = api;
})(typeof globalThis !== "undefined" ? globalThis : window);
