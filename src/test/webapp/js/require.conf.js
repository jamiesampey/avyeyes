require.config({
    baseUrl: EnvJasmine.rootDir,
    paths: {
        'specs': EnvJasmine.specsDir,
        'mocks': EnvJasmine.mocksDir,
    	'jquery': EnvJasmine.libDir + 'jquery',
        'jquery-ui': EnvJasmine.libDir + 'jquery-ui.custom.min',
        'jquery-geocomplete': EnvJasmine.libDir + 'jquery.geocomplete.min',
        'geojs': EnvJasmine.libDir + 'geo.min',
    	'gmaps': EnvJasmine.mocksDir + 'gmaps',
    	'gearth': EnvJasmine.mocksDir + 'gearth'
    }
});
