'use strict'
const merge = require('webpack-merge')
const prodEnv = require('./prod.env')

module.exports = merge(prodEnv, {
  NODE_ENV: '"development"',
  BASE_API: '""', // 空字符串，因为开发环境使用代理
  BASE_URL: '"http://localhost:8101"', // 开发环境前端URL
  BACKEND_URL: '"http://119.91.219.153:18100"' // 开发环境后端URL
})
