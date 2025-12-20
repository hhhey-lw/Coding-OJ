import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import monacoEditorPlugin from 'vite-plugin-monaco-editor'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  base: './',
  plugins: [
    vue(),
    monacoEditorPlugin({
        // 默认配置即可，如果有特殊需求可以在这里配置
    })
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    },
    extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.vue']
  },
  server: {
    port: 8080, // 保持与 Vue CLI 默认端口一致，或者不设置让其自动分配
    proxy: {
      '/api': {
        target: 'http://localhost:8101',
        changeOrigin: true,
      }
    }
  }
})
