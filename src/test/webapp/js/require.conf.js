require.config({
    baseUrl: EnvJasmine.rootDir,
    paths: {
        'specs': EnvJasmine.specsDir,
        'mocks': EnvJasmine.mocksDir,
    	'jquery': EnvJasmine.libDir + 'jquery',
        'jquery-ui': EnvJasmine.libDir + 'jquery-ui.custom.min',
        'jquery-geocomplete': EnvJasmine.libDir + 'jquery.geocomplete.min',
    	'jasmine-jquery': EnvJasmine.libDir + 'jasmine-jquery',
        'geojs': EnvJasmine.mocksDir + 'geo',
    	'geplugin': EnvJasmine.mocksDir + 'geplugin',
    	'gmaps': EnvJasmine.mocksDir + 'gmaps',
    	'gearth': EnvJasmine.mocksDir + 'gearth'
    }
});
