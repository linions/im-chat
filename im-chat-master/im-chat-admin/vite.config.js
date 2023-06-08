import vue from '@vitejs/plugin-vue'

export default {
    base: './',
    plugins: [vue()],
    optimizeDeps: {
        include: ['schart.js']
    },
    // vite 相关配置
    server: {
    port: 8082,
    host: true,
    open: true,
    proxy: {
        // https://cn.vitejs.dev/config/#server-proxy
        '/v1': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        },
    }
    }
}