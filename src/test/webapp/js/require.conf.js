require.config({
    baseUrl: EnvJasmine.rootDir,
    paths: {
        'specs': EnvJasmine.specsDir,
        'mocks': EnvJasmine.mocksDir,
    	'jasmine-jquery': EnvJasmine.libDir + 'jasmine-jquery',
        'lib/geo': EnvJasmine.mocksDir + 'geo',
    	'geplugin': EnvJasmine.mocksDir + 'geplugin',
    	'gmaps': EnvJasmine.mocksDir + 'gmaps',
    	'gearth': EnvJasmine.mocksDir + 'gearth'
    }
});
