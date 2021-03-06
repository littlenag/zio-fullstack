'use strict';

const webpack = require('webpack');
const merge = require("webpack-merge");

// This file is auto generated by the bundler plugin, ./client/target/scala-2.12/scalajs-bundler/main/scalajs.webpack.fast.js
const generatedWebpackConfig = require('./scalajs.webpack.config.js');

const merged = merge(generatedWebpackConfig, {
  plugins: [
    new webpack.NoEmitOnErrorsPlugin(),
    // https://webpack.js.org/plugins/provide-plugin/
    // Auto load modules instead of importing or requiring them
    new webpack.ProvidePlugin({
      $: 'jquery',
      jQuery: 'jquery'
    })
  ],

  optimization: {
    minimize: false
  },

  module: {
    rules: [
      {
        test: /\.scala$/,
        use: [
          {
            loader: 'url-loader',
            options: {
              mimetype: 'text/plain',
              name: 'public/[path][name].[ext]',
            }
          }
        ],
      },
      {
        test: /\.css$/,
        use: ['style-loader', 'css-loader'],
      },
      {
        test: /\.(scss)$/,
        use: [{
          loader: 'style-loader',   // inject CSS to page
        }, {
          loader: 'css-loader',     // translates CSS into CommonJS modules
        }, {
          loader: 'postcss-loader', // Run postcss actions
          options: {
            plugins: function () { // postcss plugins, can be exported to postcss.config.js
              return [
                require('autoprefixer')
              ];
            }
          }
        }, {
          loader: 'sass-loader' // compiles Sass to CSS
        }]
      },
      {
        test: /\.(png|jpg|gif)$/,
        use: [
          {
            loader: 'url-loader',
            options: {
              limit: 8192,
              mimetype: 'image/png',
              name: 'public/images/[name].[ext]',
            }
          }
        ],
      },
      {
        test: /\.svg(\?v=\d+\.\d+\.\d+)?$/,
        use: [
          {
            loader: 'url-loader',
            options: {
              limit: 8192,
              mimetype: 'image/svg+xml',
              name: 'public/images/[name].[ext]',
            }
          }
        ],
      },
      {
        test: /\.eot(\?v=\d+.\d+.\d+)?$/,
        use: [
          {
            loader: 'file-loader',
            options: {
              name: 'public/fonts/[name].[ext]'
            }
          }
        ],
      },
      {
        test: /\.woff(2)?(\?v=[0-9]\.[0-9]\.[0-9])?$/,
        use: [
          {
            loader: 'url-loader',
            options: {
              limit: 8192,
              mimetype: 'application/font-woff',
              name: 'public/fonts/[name].[ext]',
            }
          }
        ],
      },
      {
        test: /\.[ot]tf(\?v=\d+.\d+.\d+)?$/,
        use: [
          {
            loader: 'url-loader',
            options: {
              limit: 8192,
              mimetype: 'application/octet-stream',
              name: 'public/fonts/[name].[ext]',
            }
          }
        ],
      },
    ]
  }
});

module.exports = merged;