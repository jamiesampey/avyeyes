const path = require('path');
const webpack = require('webpack');
const jsSrcDir = './app/assets/javascripts';
const cssSrcDir = './app/assets/stylesheets';

module.exports = {
    target: 'web',
    devtool: 'source-map',
    entry: `${jsSrcDir}/AvyEyesClient`,
    resolve: {
        extensions: ['.js', '.jsx'],
    },
    module: {
        rules: [{
            test: /\.(js|jsx)$/,
            include: path.resolve(jsSrcDir),
            use: [{
                loader: 'babel-loader',
                options: {
                    presets: [ 'env', 'react' ]
                }
            }],
        },{
            test: /\.(scss|css)$/,
            include: [
                path.resolve(cssSrcDir)
            ],
            use: ['style-loader', 'css-loader', 'sass-loader']
        }],
    },
    plugins: [
        new webpack.NamedModulesPlugin(),
        new webpack.NoEmitOnErrorsPlugin(),
    ],
    output: {
        path: path.resolve(jsSrcDir, 'build'),
        filename: 'client.js',
        publicPath: '/',
    },
};