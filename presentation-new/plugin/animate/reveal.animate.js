(function($) {
    Reveal.Animate = function(options) {

        var SLIDES_SELECTOR = ".reveal .slides section";
        var PAST_SLIDES_SELECTOR = ".reveal .slides section.past, .reveal .slides section.past section";
        var FUTURE_SLIDES_SELECTOR = ".reveal .slides section.future, .reveal .slides section.future section";

        var INITIAL_STATE_ELEMENT_NAME = 'initial';
        var STATE_ELEMENT_NAME = 'state';

        var ANIMATION_ELEMENT_NAME = 'animation';
        var ANIMATE_ELEMENT_NAME = 'animate';

        var ANIMATION_ID_ATTRIBUTE = 'data-animation-id';

        var CURRENT_ANIMATION_NUMBER_ATTRIBUTE = 'data-current-animation';

        var DEFAULT_DURATION = 1000;

        var SHOWN = "SHOWN";
        var NOT_SHOWN = "NOT_SHOWN";

        var FRAGMENT_CLASSNAME = 'fragment';

        var FRAGMENT_SHOWN_CLASSNAME = 'visible';

        var animationProviders = {};

        var animationProvidersToLoad = 0;

        var forwardAnimationsPerElement = {};

        var backwardAnimationsPerElement = {};

        var currentAnimationNumber = 1;

        function getCurrentSlide() {
            return $(Reveal.getCurrentSlide());
        }

        function isAnimatedSlide() {
            return getCurrentAnimationProvider() != null;
        }

        function currentSlideHasClassName(className) {
            return getCurrentSlide().has("." + className).length == 1 || getCurrentSlide().hasClass(className);
        }

        function getCurrentAnimationProvider() {
            for (var className in animationProviders) {
                var animationProvider = animationProviders[className];

                if (currentSlideHasClassName(className)) {
                    return animationProvider;
                }
            }

            return null;
        }

        function getCurrentAnimationProviderClassName() {
            for (var className in animationProviders) {
                if (currentSlideHasClassName(className)) {
                    return className;
                }
            }
            return null;
        }

        function getCurrentAnimationElement() {
            var animationClassName = getCurrentAnimationProviderClassName();
            var currentSlide = getCurrentSlide();

            return currentSlide.hasClass(animationClassName) ? currentSlide : $("." + animationClassName, currentSlide);
        }

        function prepareAnimationElement(animationElement) {
            if (!animationElement.attr(ANIMATION_ID_ATTRIBUTE)) {
                animationElement.attr(ANIMATION_ID_ATTRIBUTE, currentAnimationNumber++);
            }
        }

        function getCurrentAnimationNumber(animationElement) {
            animationElement = animationElement || getCurrentAnimationElement();

            var currentAnimationNumber = animationElement.attr(CURRENT_ANIMATION_NUMBER_ATTRIBUTE);
            return !currentAnimationNumber ? 0 : parseInt(currentAnimationNumber);
        }

        function saveNewAnimationNumber(direction, animationElement) {
            animationElement = animationElement || getCurrentAnimationElement();
            animationElement.attr(CURRENT_ANIMATION_NUMBER_ATTRIBUTE, getCurrentAnimationNumber(animationElement) + direction);
        }

        function fragmentShownListener(event) {
            tryAnimateForEvent(event, 1);
        }

        function fragmentHiddenListener(event) {
            tryAnimateForEvent(event, -1);
        }

        function tryAnimateForEvent(event, direction) {
            if (!isFragmentEventForAnimationElement(event) || !isAnimatedSlide()) {
                return;
            }

            if (tryAnimate(getCurrentAnimationNumber(), direction)) {
                saveNewAnimationNumber(direction);
                event.stopPropagation();
            }
        }

        function isFragmentEventForAnimationElement(event) {
            return event.fragment.tagName.toLowerCase() === ANIMATION_ELEMENT_NAME;
        }

        function getInitialValues(animationProvider, animationElement, animations) {
            var initialAnimationStepValues, animationSteps, i, elementId, property;

            initialAnimationStepValues = {};
            for (i = 0; i < animations.length; i++) {
                animationSteps = animations[i].steps;

                for (elementId in animationSteps) {
                    initialAnimationStepValues[elementId] = initialAnimationStepValues[elementId] || {};
                    for (property in animationSteps[elementId]) {
                        initialAnimationStepValues[elementId][property] = animationProvider.getValue(animationElement, elementId, property);
                    }
                }
            }

            return {
                duration: 0,
                steps: initialAnimationStepValues
            };
        }

        function determineAnimation(animationElement) {
            var animationSteps = {}, animateStep, id, property, value, duration;

            duration = parseInt(animationElement.attr('data-duration')) || DEFAULT_DURATION;

            $(ANIMATE_ELEMENT_NAME, animationElement).each(function() {
                animateStep = $(this);
                id = animateStep.attr('data-id');
                property = animateStep.attr('data-property');
                value = animateStep.attr('data-value');

                animationSteps[id] = animationSteps[id] || {};
                animationSteps[id][property] = value;
            });

            return {
                duration: duration,
                steps: animationSteps
            };
        }

        function getPropertyInAnimations(elementId, property, forwardAnimations) {
            var i, forwardAnimation;

            for (i = 0; i < forwardAnimations.length; i++) {
                forwardAnimation = forwardAnimations[i];
                if (forwardAnimation.steps[elementId] && forwardAnimation.steps[elementId][property]) {
                    return forwardAnimation.steps[elementId][property];
                }
            }

            return null;
        }

        function postProcessAnimations(animationProvider, animations) {
            var i, animationsBefore, steps, elementId, property, currentValue, previousValue;

            for (i = 1; i < animations.length; i++) {
                steps = animations[i].steps;
                animationsBefore = animations.slice(0, i).reverse();

                for (elementId in steps) {
                    var values = steps[elementId];

                    for (property in values) {
                        currentValue = values[property];
                        previousValue = getPropertyInAnimations(elementId, property, animationsBefore);

                        values[property] = animationProvider.postProcess(elementId, property, previousValue, currentValue);
                    }
                }
            }
        }

        function determineForwardAnimations(animationProvider, currentAnimationElement) {
            var animations = [], animationElement;

            $(ANIMATION_ELEMENT_NAME, currentAnimationElement).each(function() {
                animationElement = $(this);
                animations.push(determineAnimation(animationElement));
            });
            animations.unshift(getInitialValues(animationProvider, currentAnimationElement, animations));

            postProcessAnimations(animationProvider, animations);

            return animations;
        }

        function determineBackwardAnimation(forwardAnimation, forwardAnimations) {
            var elementId, property, animation;

            animation = {
                steps: {},
                duration: forwardAnimation.duration
            };

            for (elementId in forwardAnimation.steps) {
                animation.steps[elementId] = animation.steps[elementId] || {};
                for (property in forwardAnimation.steps[elementId]) {
                    animation.steps[elementId][property] = getPropertyInAnimations(elementId, property, forwardAnimations);
                }
            }

            return animation;
        }

        function determineBackwardAnimations(forwardAnimations) {
            var animations = [], i;

            for (i = 1; i < forwardAnimations.length; i++) {
                animations.push(determineBackwardAnimation(forwardAnimations[i], forwardAnimations.slice(0, i).reverse()));
            }

            return animations;
        }

        function getAnimations(animationProvider, animationElement) {
            var animationElementAnimationId;

            animationElement = animationElement || getCurrentAnimationElement();
            prepareAnimationElement(animationElement);
            animationProvider = animationProvider || getCurrentAnimationProvider();

            animationElementAnimationId = animationElement.attr(ANIMATION_ID_ATTRIBUTE);

            if (!forwardAnimationsPerElement[animationElementAnimationId]) {
                forwardAnimationsPerElement[animationElementAnimationId] = determineForwardAnimations(animationProvider, animationElement);
                backwardAnimationsPerElement[animationElementAnimationId] = determineBackwardAnimations(forwardAnimationsPerElement[animationElementAnimationId]);
            }

            return {
                'forward': forwardAnimationsPerElement[animationElementAnimationId],
                'backward': backwardAnimationsPerElement[animationElementAnimationId]
            };
        }

        function tryAnimate(previousAnimationNumber, moveDirection) {
            var element, animations, animationNumber, animation;

            if (isPrintMode()) {
                return false;
            }

            element = getCurrentAnimationElement();
            animations = getAnimations();
            animationNumber = previousAnimationNumber + moveDirection;

            if (animationNumber < 0 || animationNumber >= animations.forward.length) {
                return false;
            }

            animation = moveDirection == 1 ? animations.forward[animationNumber] : animations.backward[animationNumber];

            if (!animation.steps) {
                return false;
            }

            getCurrentAnimationProvider().animate(element, animation);
            return true;
        }

        function isPrintMode() {
            // TODO find a better way to do this
            return (/print-pdf/gi).test(window.location.search);
        }

        function addFragmentClassToAnimationElements(sections) {
            var className, animationProvider, animatedElements, animatedElement, animationElements;

            for (className in animationProviders) {
                animationProvider = animationProviders[className];
                animatedElements = sections.filter("." + className).add(sections.children("." + className));

                animatedElements.each(function() {
                    animatedElement = $(this);
                    animationElements = $(ANIMATION_ELEMENT_NAME, animatedElement);

                    animationElements.addClass(FRAGMENT_CLASSNAME);
                });
            }
        }

        function getInitialStateSetInAnimationElement(animationElement) {
            var initialStateValues, stateEntry, id, property, value;

            initialStateValues = {};
            $(INITIAL_STATE_ELEMENT_NAME + " " + STATE_ELEMENT_NAME, animationElement).each(function() {
                stateEntry = $(this);
                id = stateEntry.attr('data-id');
                property = stateEntry.attr('data-property');
                value = stateEntry.attr('data-value');

                initialStateValues[id] = initialStateValues[id] || {};
                initialStateValues[id][property] = value;
            });

            return initialStateValues;
        }

        function setInitialStatesSetByAnimationElement(animationProvider, animationElement, initialStates) {
            var elementId, property, value;

            for (elementId in initialStates) {
                for (property in initialStates[elementId]) {
                    value = initialStates[elementId][property];
                    animationProvider.setValue(animationElement, elementId, property, value);
                }
            }
        }

        function initializeSlides(sections) {
            var className, animationProvider, elements, animationElement, initialStates;

            for (className in animationProviders) {
                animationProvider = animationProviders[className];
                elements = sections.filter("." + className).add(sections.children("." + className));

                elements.each(function() {
                    animationElement = $(this);
                    initialStates = getInitialStateSetInAnimationElement(animationElement);
                    setInitialStatesSetByAnimationElement(animationProvider, animationElement, initialStates);
                });
            }
        }

        function initialize() {
            var slides = $(SLIDES_SELECTOR);

            addFragmentClassToAnimationElements(slides);
            initializeSlides(slides);

            Reveal.addEventListener('slidechanged', slideChangedListener);
            Reveal.addEventListener('fragmentshown', fragmentShownListener);
            Reveal.addEventListener('fragmenthidden', fragmentHiddenListener);
        }

        function performAnimationStepImmediately(animationToPerform, animationProvider, animationElement, desiredState) {
            var immediateAnimation = {
                steps: animationToPerform.steps,
                duration: 0
            };

            animationProvider.animate(animationElement, immediateAnimation);
            saveNewAnimationNumber(desiredState == SHOWN ? 1 : -1, animationElement);
            return {animation: animationToPerform, immediateAnimation: immediateAnimation};
        }

        function animateImmediately(animationProvider, animationElement, desiredState) {
            var i, animations, animationsToPerform, currentAnimationNumber;
            animations = getAnimations(animationProvider, animationElement);

            currentAnimationNumber = getCurrentAnimationNumber(animationElement);
            if (desiredState == SHOWN) {
                animationsToPerform = animations.forward;
                for (i = currentAnimationNumber + 1; i < animationsToPerform.length; i++) {
                    performAnimationStepImmediately(animationsToPerform[i], animationProvider, animationElement, desiredState);
                }

            } else {
                animationsToPerform = animations.backward;
                for (i = currentAnimationNumber - 1; i >= 0; i--) {
                    performAnimationStepImmediately(animationsToPerform[i], animationProvider, animationElement, desiredState);
                }
            }
        }

        function setFragmentsToDesiredState(animationElement, desiredState) {
            var animationStepElements = $('.' + FRAGMENT_CLASSNAME, animationElement);

            if (desiredState == SHOWN) {
                animationStepElements.addClass(FRAGMENT_SHOWN_CLASSNAME);
            } else {
                animationStepElements.removeClass(FRAGMENT_SHOWN_CLASSNAME);
            }
        }

        function setSectionAnimationElementsToDesiredState(sections, desiredState) {
            var className, animationProvider, elements;

            for (className in animationProviders) {
                animationProvider = animationProviders[className];
                elements = sections.filter("." + className).add(sections.children("." + className));

                elements.each(function() {
                    var element = $(this);
                    animateImmediately(animationProvider, element, desiredState);
                    setFragmentsToDesiredState(element, desiredState);
                });
            }
        }

        function animateSlidesImmediately() {
            var sections = $(SLIDES_SELECTOR);

            if (isPrintMode()) {
                setSectionAnimationElementsToDesiredState(sections, SHOWN);
            } else {
                // The sequence is important here since past subslides keep the future class,
                // even if their super section is in the past
                setSectionAnimationElementsToDesiredState($(FUTURE_SLIDES_SELECTOR), NOT_SHOWN);
                setSectionAnimationElementsToDesiredState($(PAST_SLIDES_SELECTOR), SHOWN);
            }
        }

        function slideChangedListener() {
            animateSlidesImmediately();
        }

        function finishInitialization() {
            initialize();
            animateSlidesImmediately();
        }

        function animationProviderInitialized() {
            animationProvidersToLoad--;

            if (animationProvidersToLoad == 0) {
                finishInitialization();
            }
        }

        function addAnimationProvider(className, animationProvider) {
            animationProviders[className] = animationProvider;
            animationProvider.initialize(animationProviderInitialized);
        }

        function addAnimationProviders(animationProviders) {
            var className;

            for (className in animationProviders) {
                animationProvidersToLoad++;
            }
            for (className in animationProviders) {
                addAnimationProvider(className, animationProviders[className]);
            }
        }

        (function construct() {
            options = options || {};
            options.animationProviders = options.animationProviders || {};

            addAnimationProviders(options.animationProviders);
        })();

        return {
        };
    };

    Reveal.Animate.Svg = function() {

        var ID_SUFFIX_ATTRIBUTE = 'data-id-suffix';

        var elementsToLoad = -1, elements, element;

        var transformMath;

        var finishedCallback;

        function getIdSuffix(element) {
            return element.attr(ID_SUFFIX_ATTRIBUTE);
        }

        function setIdSuffix(element, suffix) {
            return element.attr(ID_SUFFIX_ATTRIBUTE, suffix);
        }

        function initialize(callback) {
            finishedCallback = callback;
            transformMath = Reveal.Animate.Svg.TransformMath();

            elements = $('div.svg');
            elementsToLoad = elements.length;
            elements.each(function(number) {
                element = $(this);
                setIdSuffix(element, "-svg-" + number);
                element.svg({ onLoad: function(svg) {
                    svgCanvasCreatedEventHandler(element, svg);
                }});
            });
            checkIfLoaded();
        }

        function svgCanvasCreatedEventHandler(element, svg) {
            var imgSrc = element.attr('src');
            svg.load(imgSrc, {addTo: true, changeSize: false, onLoad: function() {
                svgImageLoadedEventHandler(element);
            }});
        }

        function svgImageLoadedEventHandler(element) {
            var idSuffix = getIdSuffix(element);

            $("*", element).not('defs').not('defs *').each(function() {
                var element = $(this);
                var id = element.attr('id');

                if (id) {
                    element.attr('id', id + idSuffix);
                }
            });

            elementsToLoad--;
            checkIfLoaded()
        }

        function checkIfLoaded() {
            if (elementsToLoad == 0) {
                finishedCallback.call();
            }
        }

        function getMatrixTransform(transformsText) {
            return transformMath.getMatrixTransform(transformsText);
        }

        function getAndRecalculateTransformAttribute(element) {
            var value = getMatrixTransform(element.attr('transform'));
            element.attr('transform', value);

            return value;
        }

        function getElementById(animationElement, elementId) {
            var svgCanvas = animationElement.svg('get');
            var idSuffix = getIdSuffix(animationElement);
            var realId = elementId + idSuffix;
            return svgCanvas.getElementById(realId);
        }

        function getValue(animationElement, elementId, property) {
            var value, element;
            element = getElementById(animationElement, elementId);

            if (['x', 'y', 'width', 'height'].indexOf(property) > -1) {
                value = element[property].baseVal.value;
            } else if (property === 'transform') {
                value = getAndRecalculateTransformAttribute($(element));
            } else {
                value = element.style[property];
            }

            if (!value) {
                // TODO get computed value instead
                value = getDefaultValue(property);
            }
            return value;
        }

        function getDefaultValue(property) {
            switch (property) {
                case 'fill':
                    return '#FFFFFF';
                case 'stroke':
                    return '#000000';
                case 'opacity':
                    return 1;
                default:
                    return 0;
            }
        }

        function setValue(animationElement, elementId, property, value) {
            var element = getElementById(animationElement, elementId);

            if (['x', 'y', 'width', 'height'].indexOf(property) > -1) {
                element[property].baseVal.value = value;
            } else if (property === 'transform') {
                $(element).attr('transform', value);
            } else {
                element.style[property] = value;
            }
        }

        //noinspection JSUnusedLocalSymbols
        function postProcess(elementId, property, previousValue, currentValue) {
            if (property === 'transform') {
                return getMatrixTransform(currentValue + " " + previousValue);
            }

            return currentValue;
        }

        function transformValues(values) {
            var transformedValues = {}, key, transformedKey, value;

            for (key in values) {
                if (!values.hasOwnProperty(key)) {
                    continue;
                }

                value = values[key];
                transformedKey = 'svg-' + key;
                transformedValues[transformedKey] = value;
            }

            return transformedValues;
        }

        function animate(animationElement, animation) {
            var elementId, svgCanvas, values;

            checkIfLoaded();

            svgCanvas = animationElement.svg('get');
            var idSuffix = getIdSuffix(animationElement);

            for (elementId in animation.steps) {
                if (!animation.steps.hasOwnProperty(elementId)) {
                    continue;
                }

                var realId = elementId + idSuffix;
                values = transformValues(animation.steps[elementId]);
                $(svgCanvas.getElementById(realId)).animate(values, animation.duration);
            }
        }

        return {
            initialize: initialize,
            getValue: getValue,
            setValue: setValue,
            postProcess: postProcess,
            animate: animate
        };
    };

    Reveal.Animate.Svg.TransformMath = function() {
        var Matrix = window.Matrix;
        var transformPattern = /([a-zA-Z]+)\(\s*([+-]?[\d\.]+)\s*(?:[\s,]\s*([+-]?[\d\.]+)\s*(?:[\s,]\s*([+-]?[\d\.]+)\s*(?:[\s,]\s*([+-]?[\d\.]+)\s*[\s,]\s*([+-]?[\d\.]+)\s*[\s,]\s*([+-]?[\d\.]+)\s*)?)?)?\)/g;

        function getRadians(degrees) {
            return degrees * Math.PI / 180;
        }

        function getAllTransforms(transformsText) {
            var transform = transformPattern.exec(transformsText);
            var transforms = [];
            while (transform) {
                var transformType = transform[1];
                var values = transform.slice(2, transform.length);

                transforms.unshift({
                    type: transformType,
                    values: values
                });
                transform = transformPattern.exec(transformsText);
            }

            return transforms;
        }

        function getTranslationMatrix(x, y) {
            return Matrix.create([
                [1, 0, x],
                [0, 1, y],
                [0, 0, 1]
            ]);
        }

        function getTranslation(values) {
            var x = values[0] ? parseFloat(values[0]) : 0;
            var y = values[1] ? parseFloat(values[1]) : 0;

            return getTranslationMatrix(x, y);
        }

        function getRotation(values) {
            var w = values[0] ? parseFloat(values[0]) : 0;
            var x = values[1] ? parseFloat(values[1]) : 0;
            var y = values[2] ? parseFloat(values[2]) : 0;

            return getTranslationMatrix(x, y).multiply(Matrix.RotationZ(getRadians(w))).multiply(getTranslationMatrix(-x, -y));
        }

        function getScale(values) {
            var x = values[0] ? parseFloat(values[0]) : 1;
            var y = values[1] ? parseFloat(values[1]) : 1;

            return Matrix.create([
                [x, 0, 0],
                [0, y, 0],
                [0, 0, 1]
            ]);
        }

        function getSkewX(values) {
            var u = values[0] ? parseFloat(values[0]) : 0;
            var uValue = Math.tan(getRadians(u));

            return Matrix.create([
                [1, uValue, 0],
                [0, 1, 0],
                [0, 0, 1]
            ]);
        }

        function getSkewY(values) {
            var v = values[0] ? parseFloat(values[0]) : 0;
            var vValue = Math.tan(getRadians(v));

            return Matrix.create([
                [1, 0, 0],
                [vValue, 1, 0],
                [0, 0, 1]
            ]);
        }

        function getMatrix(result) {
            var a = result[0] ? parseFloat(result[0]) : 1;
            var b = result[1] ? parseFloat(result[1]) : 0;
            var c = result[2] ? parseFloat(result[2]) : 0;
            var d = result[3] ? parseFloat(result[3]) : 1;
            var e = result[4] ? parseFloat(result[4]) : 0;
            var f = result[5] ? parseFloat(result[5]) : 0;

            return Matrix.create([
                [a, c, e],
                [b, d, f],
                [0, 0, 1]
            ]);
        }

        function getMatrixValue(transforms) {
            var transform, matrix, i;

            matrix = Matrix.I(3);
            for (i = 0; i < transforms.length; i++) {
                transform = transforms[i];

                switch (transform.type) {
                    case 'translate':
                        matrix = matrix.multiply(getTranslation(transform.values));
                        break;
                    case 'scale':
                        matrix = matrix.multiply(getScale(transform.values));
                        break;
                    case 'rotate':
                        matrix = matrix.multiply(getRotation(transform.values));
                        break;
                    case 'skewX':
                        matrix = matrix.multiply(getSkewX(transform.values));
                        break;
                    case 'skewY':
                        matrix = matrix.multiply(getSkewY(transform.values));
                        break;
                    case 'matrix':
                        matrix = matrix.multiply(getMatrix(transform.values));
                        break;
                }
            }

            return matrix;
        }

        function getSvgTransform(matrix) {
            return "matrix(" + matrix.e(1, 1) + ", " + matrix.e(2, 1) +
                ", " + matrix.e(1, 2) + ", " + matrix.e(2, 2) +
                ", " + matrix.e(1, 3) + ", " + matrix.e(2, 3) + ")";
        }

        function getMatrixTransform(transforms) {
            return getSvgTransform(getMatrixValue(getAllTransforms(transforms)));
        }

        return {
            getMatrixTransform: getMatrixTransform
        };
    };

    Reveal.Animate.Html = function() {
        function initialize(finishedCallback) {
            finishedCallback.call();
        }

        function getValue(animationElement, elementId, property) {
            var element = $('#' + elementId, animationElement);

            return element.css(property);
        }

        function setValue(animationElement, elementId, property, value) {
            var element = $('#' + elementId, animationElement);

            return element.css(property, value);
        }

        //noinspection JSUnusedLocalSymbols
        function postProcess(elementId, property, previousValue, currentValue) {
            return currentValue;
        }

        function animate(animationElement, animation) {


            var elementId, element, values;

            for (elementId in animation.steps) {
                if (!animation.steps.hasOwnProperty(elementId)) {
                    continue;
                }

                values = animation.steps[elementId];
                element = $('#' + elementId, animationElement).animate(values, { duration: animation.duration, easing: 'linear' });
            }
        }

        return {
            initialize: initialize,
            getValue: getValue,
            setValue: setValue,
            postProcess: postProcess,
            animate: animate
        };
    };

})(jQuery);