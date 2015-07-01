({
    appDir: 'src/main/webapp',
    baseUrl: 'js',
    dir: 'target/webapp',
    removeCombined: true, // delete avyeyes.*.js files after combining into main.js
    preserveLicenseComments: false,
    generateSourceMaps: false,
    optimizeCss: 'standard',
    optimize: 'uglify2',
    skipDirOptimize: true, // don't uglify the lib dir!
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
            name: 'main',
            exclude: [
                "lib/Cesium/Cesium",
                "lib/jquery-ui",
                "lib/lightbox",
                "lib/jquery.fileupload",
                "lib/jquery.iframe-transport"
            ]
        },
        {
            name: "main.admin",
            exclude: [
                "lib/jquery.dataTables"
            ]
        }
    ]
})
