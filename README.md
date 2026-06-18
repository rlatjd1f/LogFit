# LogFit

LogFit is a lightweight mobile PWA workout log for rotating split routines. It runs as a static web app, stores data locally in the browser, and works offline after the first load.

## Features

- Rotating N-split routine management
- Built-in exercise master database with 50+ exercises
- Custom exercise registration
- Set-by-set workout tracking for weight, reps, and RIR
- Full-screen rest countdown timer
- Warning background and Web Audio beep cues near the end of rest
- Completed workout card state and automatic sorting
- Progressive overload analytics with Chart.js
- Volume, estimated 1RM, and average RIR trend chart
- Light and dark theme toggle
- JSON backup and restore
- PWA support with `manifest.json` and service worker caching

## Project Structure

```text
.
├── code_artifact.html  # Main app UI, styles, and JavaScript
├── manifest.json       # PWA metadata
├── sw.js               # Service worker for offline caching
├── icon.svg            # App icon
├── .gitignore
└── README.md
```

## Getting Started

Run a local static server from the project root:

```bash
python3 -m http.server 4173
```

Open the app:

```text
http://localhost:4173/code_artifact.html
```

You can also deploy the project to any static hosting service, such as GitHub Pages, Netlify, Vercel, or Cloudflare Pages.

## Data Storage

LogFit uses `localStorage` only. No backend server or account system is required.

Stored data includes:

- Theme settings
- Exercise master data
- Custom exercises
- Routine definitions
- Workout history

Use the Backup tab to export or import all app data as JSON.

## PWA Notes

The app includes:

- `manifest.json`
- `sw.js`
- SVG app icon
- Offline app shell caching

For install prompts and service worker behavior, serve the app through `http://localhost`, `https`, or a static hosting provider. Opening the HTML file directly from disk may skip some PWA behavior.

## Tech Stack

- HTML
- CSS
- Vanilla JavaScript
- Chart.js CDN
- Web Audio API
- Service Worker API

## Status

This is an early single-file PWA prototype focused on mobile workout logging and offline-first local data management.
