require.config({
    baseUrl: EnvJasmine.rootDir,
    paths: {
        'specs': EnvJasmine.specsDir,
        'mocks': EnvJasmine.mocksDir,
        'jquery-ui': EnvJasmine.libDir + 'jquery-ui',
        'jquery-geocomplete': EnvJasmine.libDir + 'jquery.geocomplete.min',
        'jquery-fileupload': EnvJasmine.libDir + 'jquery.fileupload',
        'jquery-iframe-transport': EnvJasmine.libDir + 'jquery.iframe-transport',
        'jquery-datatables': EnvJasmine.libDir + 'jquery.datatables.min',
        'lightbox': EnvJasmine.libDir + 'lightbox.min',
    	'jasmine-jquery': EnvJasmine.libDir + 'jasmine-jquery',
        'geojs': EnvJasmine.mocksDir + 'geo',
    	'geplugin': EnvJasmine.mocksDir + 'geplugin',
    	'gmaps': EnvJasmine.mocksDir + 'gmaps',
    	'gearth': EnvJasmine.mocksDir + 'gearth'
    }
});
