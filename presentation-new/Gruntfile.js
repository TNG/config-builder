/* global module:false */
module.exports = function(grunt) {
    var port = grunt.option('port') || 8000;

    // Project configuration
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),

        sass: {
            main: {
                files: {
                    'css/theme/default.css': 'css/theme/source/default.scss',
                    'css/theme/tng.css': 'css/theme/source/tng.scss',
                    'css/theme/beige.css': 'css/theme/source/beige.scss',
                    'css/theme/night.css': 'css/theme/source/night.scss',
                    'css/theme/serif.css': 'css/theme/source/serif.scss',
                    'css/theme/simple.css': 'css/theme/source/simple.scss',
                    'css/theme/sky.css': 'css/theme/source/sky.scss',
                    'css/theme/moon.css': 'css/theme/source/moon.scss',
                    'css/theme/solarized.css': 'css/theme/source/solarized.scss',
                    'css/theme/blood.css': 'css/theme/source/blood.scss'
                }
            }
        },

        connect: {
            server: {
                options: {
                    port: port,
                    base: '.'
                }
            }
        },

        watch: {
            main: {
                files: [ 'Gruntfile.js', 'js/vendor/reveal.js', 'css/reveal.css' ],
                tasks: 'default'
            },
            theme: {
                files: [ 'css/theme/source/*.scss', 'css/theme/template/*.scss' ],
                tasks: 'themes'
            }
        },

        run: {
            options: {},
            your_target: {
                cmd: 'node',
                args: [ 'server.js', '--port', port ]
            }
        }
    });

    // Dependencies
    grunt.loadNpmTasks('grunt-contrib-watch');
    grunt.loadNpmTasks('grunt-contrib-sass');
    grunt.loadNpmTasks('grunt-contrib-connect');
    grunt.loadNpmTasks('grunt-run');

    // Theme task
    grunt.registerTask('themes', [ 'sass' ]);
    grunt.registerTask('start', [ 'run' ]);
};