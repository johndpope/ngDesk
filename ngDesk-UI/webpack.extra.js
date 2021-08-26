const webpack = require('webpack');
const UglifyJsPlugin = require('uglifyjs-webpack-plugin');
module.exports = {
  plugins: [
    new webpack.IgnorePlugin(/^\.\/locale$/, /moment$/)
  ],
  optimization: {
    splitChunks: {
      chunks: 'all',
      minSize: 30000,
      maxSize: 0,
      minChunks: 1,
      maxAsyncRequests: 5,
      maxInitialRequests: 3,
      automaticNameDelimiter: '~',
      name: 'vendor',
      cacheGroups: {
        vendors: {
          test: /[\\/]node_modules[\\/]/,
          priority: -10
        },
        default: {
          minChunks: 2,
          priority: -20,
          reuseExistingChunk: true
        }
      }
    },
    minimizer: [
      new UglifyJsPlugin({
        uglifyOptions: {
          mangle: true,
          compress: {
            warnings: false, // Suppress uglification warnings
            sequences: true,
            dead_code: true,
            conditionals: true,
            booleans: true,
            unused: true,
            if_return: true,
            join_vars: true,
            drop_console: true
          },
          output: {
            comments: false,
            "ascii_only": true
          },
          exclude: [/\.min\.js$/gi] // skip pre-minified libs
        }
      }),
    ],
  }
}
