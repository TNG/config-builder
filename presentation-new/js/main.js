require.config({
    paths: {
        text: 'vendor/text',

        jquery: 'vendor/jquery',
        jqueryAnimateColors: 'vendor/jquery.animate.colors',
        jqueryBrowser: 'vendor/jquery.browser',
        jqueryFlot: 'vendor/jquery.flot',
        jquerySvg: 'vendor/jquery.svg',
        jqueryAnimateSvg: 'vendor/jquery.svganim',

        sylvester: 'vendor/sylvester',

        reveal: 'vendor/reveal',
        head: '../lib/js/head',
        marked: '../plugin/markdown/marked',

        highlight: 'vendor/highlight.pack',
        'socket-io': '../socket.io/socket.io'
    },

    shim: {
        jqueryBrowser: {
            exports: '$.browser'
        },
        jquerySvg: {
            exports: '$.svg'
        },
        sylvester: {
            exports: 'Sylvester'
        },
        reveal: {
            exports: 'Reveal'
        },
        highlight: {
            exports: 'hljs'
        }
    }
});


require(['head', 'jquery', 'marked', 'custom/structure.builder', 'custom/issue.fixer', 'custom/viewer/codeViewer' ],
    function(head, $, marked, StructureBuilder, IssueFixer, CodeViewer) {
        var structureBuilder = StructureBuilder();
        structureBuilder.buildRevealStructure();
        structureBuilder.highlightCode();
        structureBuilder.removeSlidesAccordingToMediaType();

        // Since we both use AMD and non-AMD plugins, we must load marked asynchronously
        if (!!document.querySelector('[data-markdown]')) {
            window.marked = marked;
        }

        require(['reveal', 'sylvester'], function(Reveal) {
            require(['jqueryAnimateColors', 'jqueryBrowser', 'jqueryFlot', 'jquerySvg'], function() {
                require(['jqueryAnimateSvg'], function() {
                    // The framework is now initialized

                    new CodeViewer();

                    var event = new CustomEvent("initialized", {});
                    document.body.dispatchEvent(event);

                    // Full list of configuration options available here:
                    // https://github.com/hakimel/reveal.js#configuration
                    Reveal.initialize({
                        controls: false,
                        progress: true,
                        history: true,
                        center: true,
                        rollingLinks: false,
                        width: 1024,
                        height: 768,

                        theme: Reveal.getQueryHash().theme, // available themes are in /css/theme
                        transition: Reveal.getQueryHash().transition || 'linear', // default/cube/page/concave/zoom/linear/fade/none

                        // Parallax scrolling
                        // parallaxBackgroundImage: 'https://s3.amazonaws.com/hakim-static/reveal-js/reveal-parallax-1.jpg',
                        // parallaxBackgroundSize: '2100px 900px',

                        // Optional libraries used to extend on reveal.js
                        dependencies: [
                            { src: 'plugin/sectiontitle/reveal.sectiontitle.js', async: false, callback: function() {
                                var sectionTitle = Reveal.SectionTitle({
                                    header: $('.i18n>.section-overview').html(),
                                    wrapper: {
                                        start: '<div class="titles-parent"><div class="titles">',
                                        end: '</div></div>'
                                    },
                                    sectionClass: 'overview',
                                    titleClass: 'title',
                                    selectedTitleClass: 'selected-title'
                                });
                                sectionTitle.createSections();
                            } },
                            { src: 'lib/js/classList.js', condition: function() {
                                return !document.body.classList;
                            } },
                            { src: 'plugin/markdown/markdown.js', condition: function() {
                                return !!document.querySelector('[data-markdown]');
                            } },
                            { src: 'plugin/highlight/highlight.js', callback: function() {
                                hljs.initHighlighting();
                            } },
                            { src: 'plugin/zoom-js/zoom.js', async: true, condition: function() {
                                return !!document.body.classList;
                            } },
                            { src: 'plugin/notes/notes.js', async: true, condition: function() {
                                return !!document.body.classList;
                            } },
                            { src: 'plugin/animate/reveal.animate.js', async: true, callback: function() {
                                Reveal.Animate({
                                    animationProviders: {
                                        'animated-svg': Reveal.Animate.Svg(),
                                        'animated-html': Reveal.Animate.Html()
                                    }
                                });
                            } },
                            { src: 'plugin/charred-trail/charred-trail.js', async: true, condition: function() {
                                return !!document.body.classList;
                            } }
                        ]
                    });

                    var issueFixer = IssueFixer();
                    issueFixer.fixIssues();
                });
            });

        });
    });
