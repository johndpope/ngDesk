const path = require('path');
const fs = require('fs');
const util = require('util');

// get application version from package.json
const appName = require('../package.json').name;

// promisify core API's
const readDir = util.promisify(fs.readdir);
const writeFile = util.promisify(fs.writeFile);
const readFile = util.promisify(fs.readFile);

// some usefull functions
function escapeRegExp(string) {
    return string.replace(/[.*+\-?^${}()|[\]\\]/g, '\\$&'); // $& means the whole matched string
}
function replaceAll(str, find, replace) {
    return str.replace(new RegExp(escapeRegExp(find), 'g'), replace);
}


// STARTS HERE
console.log('\nRunning post-build tasks');

// Local variables
// current timestamp : will be put in application javascript files and in application metadata json file
let now = Date.now();
// path to application json file which will contains application metadata
const applicationFilePath = path.join(__dirname + '/../dist/ngDesk-Angular/software.json');
let timestampPlaceholder = '{{POST_BUILD_ENTERS_TIMESTAMP_HERE}}'

// Writing application metadata json file
console.log(`Writing ${applicationFilePath}`);
const src = `{"software_name": "${appName}", "timestamp": "${now}"}`;
writeFile(applicationFilePath, src);

// replacement in javascript files in dist directory
readDir(path.join(__dirname, '../dist/ngDesk-Angular/'))
    .then(files => {
        // find jasvascript files
        let filesToReplaceRegexp = /^.*.js$/;
        let filesToReplace = files.filter(f => filesToReplaceRegexp.test(f));

        // dev build?
        if (!filesToReplace) {
            return;
        }

        // replace timestamp placeholder in all javascript files
        for (var fileToReplace of filesToReplace) {
            console.log(`Replacing ${timestampPlaceholder} in the ${fileToReplace} with ${now}`);
            const fileToReplacePath = path.join(__dirname, '../dist/ngDesk-Angular/', fileToReplace);
            readFile(fileToReplacePath, 'utf8')
                .then(fileData => {
                    const replacedFile = replaceAll(fileData, timestampPlaceholder, now);
                    return writeFile(fileToReplacePath, replacedFile);
                });
        }
    }).catch(err => {
        console.log('Error with post build:', err);
    });