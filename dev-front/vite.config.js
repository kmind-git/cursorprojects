import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'node:path'

const DEFAULT_API = 'http://localhost:8080'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const apiTarget = (env.API_TARGET || DEFAULT_API).replace(/\/$/, '')

  const proxy = {
    '/api': {
      target: apiTarget,
      changeOrigin: true,
    },
  }

  /** 固定 5173；若占用请先结束其它 Vite（如 frontend）。保存代码后由 Vite HMR 自动刷新，无需重启。 */
  const devPort = Number(env.DEV_FRONT_PORT || 5173)

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
      },
    },
    server: {
      proxy,
      port: devPort,
      strictPort: true,
    },
    preview: { proxy },
  }
})
