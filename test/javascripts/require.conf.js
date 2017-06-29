require.config({
    baseUrl: EnvJasmine.rootDir,
    paths: {
        'specs': EnvJasmine.specsDir,
        'jquery': EnvJasmine.libDir + "jquery",
        'jqueryui': EnvJasmine.libDir + "jquery-ui",
        'file-upload': EnvJasmine.libDir + 'jquery.fileupload',
        'fancybox': EnvJasmine.libDir + 'jquery.fancybox',
        'jasmine-jquery': EnvJasmine.testDir + 'jasmine-jquery',
        'squire': EnvJasmine.testDir + "squire",
        'sinon': EnvJasmine.testDir + "sinon"
    }
});
