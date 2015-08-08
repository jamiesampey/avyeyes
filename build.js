({
    appDir: 'src/main/webapp',
    baseUrl: 'js',
    dir: 'target/webapp',
    removeCombined: true, // delete avyeyes.*.js files after combining into main.js
    preserveLicenseComments: false,
    generateSourceMaps: false,
    optimizeCss: 'standard',
    optimize: 'uglify2',
    uglify2: {
        output: {
            beautify: false
        },
        compress: {
            sequences: false,
            global_defs: {
                DEBUG: false
            }
        },
        warnings: false,
        mangle: true
    },
    modules: [
        {
            name: 'main'
        },
        {
            name: "main.admin"
        }
    ]
})
