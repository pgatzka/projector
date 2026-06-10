import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
// In dev, the app is served by Vite on :5173 and calls to /api are proxied to the
// Spring Boot backend on :8080. In production the backend serves the built frontend
// (wired in a later step), so the same /api paths work without a proxy.
export default defineConfig({
  plugins: [react(), tailwindcss()],
  // Build straight into the backend's classpath so `mvn package` bundles the UI into
  // the jar (Spring Boot serves it from classpath:/static/). Dev (`npm run dev`) is
  // unaffected — it serves in-memory.
  build: {
    outDir: '../target/classes/static',
    emptyOutDir: true,
  },
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
