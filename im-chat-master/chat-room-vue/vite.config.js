import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
    // vite 相关配置
    server: {
      port: 8081,
      host: true,
      open: true,
      headers: {
        'Access-Control-Allow-Origin': '*',
      },
      proxy: {
        // https://cn.vitejs.dev/config/#server-proxy
        '/v1': {
          target: 'http://localhost:8080',
          changeOrigin: true,
        },
      },
    base : './'
  }
})
