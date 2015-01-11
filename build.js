({
    appDir: 'src/main/webapp',
    baseUrl: 'js',
    dir: 'target/webapp',
    paths: {
      'earthAsyncLoad': 'lib/gearth-loader',
      'mapsAsyncLoad': 'lib/gmaps-loader',
      'analytics': 'lib/analytics',
      'facebook': 'lib/facebook',
      'twitter': '//platform.twitter.com/widgets',
      'jquery-ui': 'lib/jquery-ui',
      'jquery-geocomplete': 'lib/jquery.geocomplete',
      'jquery-fileupload': 'lib/jquery.fileupload',
      'jquery-iframe-transport': 'lib/jquery.iframe-transport',
      'jquery-datatables': 'lib/jquery.dataTables',
      'geojs': 'lib/geo',
      'lightbox': 'lib/lightbox'
    },
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
            name: 'main-admin'
        }
    ]
})
