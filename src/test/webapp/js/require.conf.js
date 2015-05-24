require.config({
    baseUrl: EnvJasmine.rootDir,
    paths: {
        'specs': EnvJasmine.specsDir,
        'mocks': EnvJasmine.mocksDir,
    	'jasmine-jquery': EnvJasmine.libDir + 'jasmine-jquery',
    	'gmaps': EnvJasmine.mocksDir + 'gmaps',
    }
});
