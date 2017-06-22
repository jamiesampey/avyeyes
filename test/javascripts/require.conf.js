require.config({
    baseUrl: EnvJasmine.rootDir,
    paths: {
        'specs': EnvJasmine.specsDir,
        'jasmine-jquery': EnvJasmine.testDir + 'jasmine-jquery',
        'squire': EnvJasmine.testDir + "squire",
        'sinon': EnvJasmine.testDir + "sinon"
    }
});
