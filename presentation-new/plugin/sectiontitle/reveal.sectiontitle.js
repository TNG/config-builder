Reveal.SectionTitle = function(options) {

    var TITLE_ATTRIBUTE = 'data-title';

    var TOP_LEVEL_SECTION_SELECTOR = '.reveal .slides>section';

    var OVERALL_SECTION_DEFINITION_ATTRIBUTE_SELECTOR = '[data-before-overview]';

    var OVERALL_SECTION_DEFINITION_SELECTOR = TOP_LEVEL_SECTION_SELECTOR + OVERALL_SECTION_DEFINITION_ATTRIBUTE_SELECTOR;

    (function construct() {
        options = options || {};
        options.header = options.header || "";
        options.wrapper = options.wrapper || {};
        options.wrapper.start = options.wrapper.start || "";
        options.wrapper.end = options.wrapper.end || "";
        options.sectionClass = options.sectionClass || null;
        options.titleClass = options.titleClass || "title";
        options.selectedTitleClass = options.selectedTitleClass || "selected-title";
    })();

    function getTopLevelSectionsWithSubSections() {
        return $(TOP_LEVEL_SECTION_SELECTOR).filter(function() {
            var element = $(this);
            return element.find("section").length != 0;
        });
    }


    function getOverallSectionDefinitionElements() {
        return $(OVERALL_SECTION_DEFINITION_SELECTOR);
    }

    function getNumberOfTopLevelSlidesBefore(section) {
        return section.index(TOP_LEVEL_SECTION_SELECTOR);
    }

    function getNumberOfOverallSectionDefintionElementsBefore(section) {
        return section.prevAll().filter(OVERALL_SECTION_DEFINITION_ATTRIBUTE_SELECTOR).size();
    }

    function getSlideDataFor(sections) {
        var slideData = [];

        sections.each(function(sectionCount) {
            var section = $(this);

            var title = section.attr(TITLE_ATTRIBUTE);
            var slideIndex = getNumberOfTopLevelSlidesBefore(section) + getNumberOfOverallSectionDefintionElementsBefore(section);

            if (title) {
                slideData[sectionCount] = { title: title, slideIndex: slideIndex };
            }
        });
        return slideData;
    }

    function getSectionText(slideData, currentIndex) {
        var sectionText, i, titleClass, titleEntry;

        sectionText = "";
        for (i = 0; i < slideData.length; i++) {
            if (!slideData[i]) {
                continue;
            }

            titleClass = (i === currentIndex ? options.selectedTitleClass : options.titleClass);
            titleEntry = '<a href="#/' + slideData[i].slideIndex + '"><div class="' + titleClass + '">' + slideData[i].title + '</div></a>';

            sectionText += titleEntry;
        }
        sectionText = options.header + options.wrapper.start + sectionText + options.wrapper.end;
        return '<section' + (options.sectionClass ? ' class="' + options.sectionClass + '"' : "") + '>' + sectionText + '</section>';
    }

    function createNewTopLevelOverviewSections(sections, slideData) {
        sections.each(function(currentIndex) {
            var section, sectionText;

            section = $(this);
            if (!section.attr(TITLE_ATTRIBUTE)) {
                return;
            }

            sectionText = getSectionText(slideData, currentIndex);
            section.prepend(sectionText);
        });
    }

    function createOverallOverviewSections(overallSectionDefinitionElements, slideData) {
        var sectionText = getSectionText(slideData, -1);
        $(sectionText).insertAfter(overallSectionDefinitionElements);
    }

    function createSections() {
        var sections = getTopLevelSectionsWithSubSections();
        var overallSectionDefinitionElements = getOverallSectionDefinitionElements();
        var slideData = getSlideDataFor(sections, overallSectionDefinitionElements);

        createNewTopLevelOverviewSections(sections, slideData);
        createOverallOverviewSections(overallSectionDefinitionElements, slideData);
    }

    return {
        createSections: createSections
    };
};