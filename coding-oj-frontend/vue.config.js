const { defineConfig } = require("@vue/cli-service");
const MonacoWebpackPlugin = require("monaco-editor-webpack-plugin");

module.exports = defineConfig({
  publicPath: "./",
  transpileDependencies: true,
  productionSourceMap: false,
  lintOnSave: false,
  chainWebpack(config) {
    config.plugin("monaco").use(new MonacoWebpackPlugin());
  },
  devServer: {
    proxy: {
      '/api': {
        target: 'http://longcoding.top:8101',
        changeOrigin: true, // 解决跨域问题
      }
    },
    client: {
      overlay: false
    },
    // historyApiFallback: true // 使用了history默认会把#去掉，刷新还是原页面
  },
});
