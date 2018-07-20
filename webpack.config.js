const path = require('path');
const webpack = require('webpack');
const CopywebpackPlugin = require('copy-webpack-plugin');

const avyeyesSource = './app/assets/javascripts';
const cesiumSource = 'node_modules/cesium/Source';
const cesiumWorkers = '../Build/Cesium/Workers';

module.exports = {
    target: 'web',
    devtool: 'source-map',
    entry: {
      avyeyes: `${avyeyesSource}/AvyEyesClient`
    },
    output: {
      path: path.resolve(avyeyesSource, 'build'),
      filename: '[name].js',
      publicPath: '/',
      sourcePrefix: '' // Needed to compile multiline strings in Cesium
    },
    resolve: {
        extensions: ['.js', '.jsx'],
        alias: {
          cesium: path.resolve(__dirname, cesiumSource)
        }
    },
    module: {
        rules: [{
            test: /\.(js|jsx)$/,
            include: path.resolve(avyeyesSource),
            use: [{
                loader: 'babel-loader',
                options: {
                    presets: [ 'env', 'react' ]
                }
            }],
        },{
            test: /\.(scss|css)$/,
            use: ['style-loader', 'css-loader', 'sass-loader']
        },{
            test: /\.(png|gif|jpg|jpeg|svg|xml|json)$/,
            use: [ 'url-loader' ]
        }],
        unknownContextCritical: false, // Needed to avoid warnings from Cesium requires
    },
    plugins: [
        new webpack.NamedModulesPlugin(),
        new webpack.NoEmitOnErrorsPlugin(),
        new CopywebpackPlugin([ { from: path.join(cesiumSource, cesiumWorkers), to: 'Workers' } ]),
        new CopywebpackPlugin([ { from: path.join(cesiumSource, 'Assets'), to: 'Assets' } ]),
        new CopywebpackPlugin([ { from: path.join(cesiumSource, 'Widgets'), to: 'Widgets' } ]),
        new webpack.DefinePlugin({
          CESIUM_BASE_URL: JSON.stringify('assets/javascripts/build') // Define relative base path in cesium for loading assets
        }),
        new webpack.optimize.CommonsChunkPlugin({
          name: 'cesium',
          minChunks: (module) => { return module.context && module.context.indexOf('cesium') !== -1 }
        })
    ],
    amd: {
      toUrlUndefined: true // Enable webpack-friendly use of require in Cesium
    },
    node: {
      fs: 'empty' // Resolve node module use of fs, needed for Cesium
    },
};