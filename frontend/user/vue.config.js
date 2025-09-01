const path = require('path');
const { defineConfig } = require('@vue/cli-service');

module.exports = defineConfig({
  transpileDependencies: true,
  chainWebpack: (config) => {
    config.resolve.alias
      .set('@', path.resolve(__dirname, 'src'));
  },

  // 修改 port 為 5174
  devServer: {
    port: 5174
  }
});