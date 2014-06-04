Reveal.DrawingCanvas = function() {

    const SECTION_SELECTOR = 'section';

    const SECTION_ELEMENTS_SELECTOR = '.reveal .slides ' + SECTION_SELECTOR;

    const CanvasMode = (function() {
        var CanvasMode = {
            CANVAS_MODE_ENABLED: {mode: 'CANVAS_MODE_ENABLED'},
            CANVAS_MODE_DISABLED: {mode: 'CANVAS_MODE_DISABLED'},
            CANVAS_MODE_EDIT: {mode: 'CANVAS_MODE_EDIT'}
        };

        CanvasMode.CANVAS_MODE_ENABLED.nextMode = CanvasMode.CANVAS_MODE_DISABLED;
        CanvasMode.CANVAS_MODE_DISABLED.nextMode = CanvasMode.CANVAS_MODE_EDIT;
        CanvasMode.CANVAS_MODE_EDIT.nextMode = CanvasMode.CANVAS_MODE_ENABLED;

        return CanvasMode;
    })();

    function DataManager() {
        const STORAGE_AREA = 'Reveal.Canvas';

        function loadCurves(sectionId) {
            var storage = JSON.parse(window.localStorage.getItem(STORAGE_AREA)) || {};
            return (storage['curves'] || {})[sectionId] || [];
        }

        function saveCurves(sectionId, curves) {
            var storage = JSON.parse(window.localStorage.getItem(STORAGE_AREA)) || {};
            storage['curves'] = storage['curves'] || {};
            storage['curves'][sectionId] = curves;

            window.localStorage.setItem(STORAGE_AREA, JSON.stringify(storage));
        }

        function clearCurves(sectionId) {
            saveCurves(sectionId, []);
        }

        return {
            loadCurves: loadCurves,
            saveCurves: saveCurves,
            clearCurves: clearCurves
        };
    }

    function DrawingCanvasForCanvasElement(dataManager) {

        var sectionQueryElement, sectionId;

        var options;

        function CanvasHelper() {
            var canvasQueryElement, canvasElement;

            function getZoomLevel(element) {
                var zoomLevel = 1.0;
                element.parents().each(function() {
                    var parentElement = $(this);
                    zoomLevel *= parentElement.css('zoom') ? parentElement.css('zoom') : 1.0;
                });

                return zoomLevel;
            }

            function getCanvasCoordinates(coordinates) {
                var zoomLevel = getZoomLevel(canvasQueryElement);

                return {
                    x: coordinates.x / zoomLevel / canvasQueryElement.width() * canvasElement.width,
                    y: coordinates.y / zoomLevel / canvasQueryElement.height() * canvasElement.height
                };
            }

            function getDistance(startCoordinate, endCoordinate) {
                return Math.sqrt(Math.pow((endCoordinate.x - startCoordinate.x), 2) + Math.pow((endCoordinate.y - startCoordinate.y), 2));
            }

            function initialize(_canvasQueryElement) {
                canvasQueryElement = _canvasQueryElement;
                canvasElement = _canvasQueryElement.get(0);
            }

            return {
                initialize: initialize,
                getZoomLevel: getZoomLevel,
                getCanvasCoordinates: getCanvasCoordinates,
                getDistance: getDistance
            }
        }

        function CurveEditor(canvasHelper) {

            var context;

            var lastCoordinates = null;

            var currentCurve = null;

            function initialize(_context) {
                context = _context;
            }

            function reset() {
                lastCoordinates = null;
                currentCurve = null;
            }

            function setStyleForDrawing(curve) {
                context.lineWidth = curve.width;
                context.strokeStyle = curve.color;
            }

            function setStandardStyle(highlighted) {
                context.lineWidth = options.standardStyle.lineWidth;
                if (highlighted) {
                    context.strokeStyle = options.standardStyle.highlightedLineColor;
                } else {
                    context.strokeStyle = options.standardStyle.lineColor;
                }
            }

            function startDrawingCurve(coordinates, color) {
                currentCurve = { color: color, width: 3, coordinates: [] };
                setStyleForDrawing(currentCurve);

                lastCoordinates = canvasHelper.getCanvasCoordinates(coordinates);
            }

            function drawLine(startCoordinates, endCoordinates) {
                if (lastCoordinates != null) {
                    context.beginPath();
                    context.moveTo(startCoordinates.x, startCoordinates.y);
                    context.lineTo(endCoordinates.x, endCoordinates.y);
                    context.stroke();
                }
            }

            function addCoordinatesToCurveDrawn(mouseCoordinates) {
                var coordinates = canvasHelper.getCanvasCoordinates(mouseCoordinates);

                if (canvasHelper.getDistance(lastCoordinates, coordinates) > options.minPointDistance) {
                    drawLine(lastCoordinates, coordinates);
                    currentCurve.coordinates.push(coordinates);

                    lastCoordinates = coordinates;
                }
            }

            function clearCanvas() {
                context.clearRect(0, 0, options.canvasSize.width, options.canvasSize.height);
            }

            function drawCurve(curve) {
                var i, xc, yc;

                context.beginPath();
                context.moveTo(curve.coordinates[0].x, curve.coordinates[0].y);
                for (i = 1; i < curve.coordinates.length - 2; i++) {
                    xc = (curve.coordinates[i].x + curve.coordinates[i + 1].x) / 2;
                    yc = (curve.coordinates[i].y + curve.coordinates[i + 1].y) / 2;
                    context.quadraticCurveTo(curve.coordinates[i].x, curve.coordinates[i].y, xc, yc);
                }
                context.quadraticCurveTo(curve.coordinates[i].x, curve.coordinates[i].y, curve.coordinates[i + 1].x, curve.coordinates[i + 1].y);
                context.stroke();
            }

            function drawPoints(curve) {
                context.beginPath();
                for (var i = 0; i < curve.coordinates.length; i++) {
                    context.strokeRect(curve.coordinates[i].x, curve.coordinates[i].y, 1, 1);
                }
                context.stroke();
            }

            function drawForEnabledMode(curves) {
                clearCanvas();
                for (var i = 0; i < curves.length; i++) {
                    setStyleForDrawing(curves[i]);
                    drawCurve(curves[i]);
                }
            }

            function drawForEditMode(curves, highlightedCurveIndex) {
                clearCanvas();
                for (var i = 0; i < curves.length; i++) {
                    setStandardStyle(i === highlightedCurveIndex);
                    drawCurve(curves[i]);
                }
            }

            function endDrawingCurve(previousCurves) {
                lastCoordinates = null;
                var newCurve = currentCurve;
                currentCurve = null;

                if (newCurve.coordinates.length > 2) {
                    previousCurves.push(newCurve);
                }

                drawForEnabledMode(previousCurves);
                return previousCurves;
            }

            function findNeighbouringCurveIndex(currentCurves, coordinates) {
                var i, j, currentCurve, currentCoordinates, distance, minDistanceCurveIndex = -1, minDistance = 500000;

                for (i = 0; i < currentCurves.length; i++) {
                    currentCurve = currentCurves[i];
                    for (j = 0; j < currentCurve.coordinates.length; j++) {
                        currentCoordinates = currentCurve.coordinates[j];

                        distance = canvasHelper.getDistance(coordinates, currentCoordinates);

                        if (distance < minDistance) {
                            minDistance = distance;
                            minDistanceCurveIndex = i;
                        }
                    }
                }

                return minDistance < 25 ? minDistanceCurveIndex : -1;
            }

            function getHighlightedCurveIndex(currentCurves, mouseCoordinates) {
                var markedIndex = -1;
                if (mouseCoordinates) {
                    var coordinates = canvasHelper.getCanvasCoordinates(mouseCoordinates);
                    markedIndex = findNeighbouringCurveIndex(currentCurves, coordinates);
                }
                drawForEditMode(currentCurves, markedIndex);
                return markedIndex;
            }

            return {
                initialize: initialize,
                reset: reset,
                startDrawingCurve: startDrawingCurve,
                addCoordinatesToCurveDrawn: addCoordinatesToCurveDrawn,
                endDrawingCurve: endDrawingCurve,
                draw: drawForEnabledMode,
                getHighlightedCurveIndex: getHighlightedCurveIndex
            };
        }

        function DomElementManager() {

            const BODY_ELEMENT_SELECTOR = 'body';

            const CANVAS_HTML_CODE = '<canvas style="position: absolute; left: 0; top: 0; width: 100%; height: 100%; margin: 0; padding: 0;" />';

            const BUTTON_MENU_HTML_CODE = '<div><button/><button/><button/></div>';

            var canvasElement, canvasQueryElement, context;

            var buttonMenuQueryElement, canvasModeButtonQueryElement, clearButtonQueryElement, colorButtonQueryElement;

            function createCanvas() {
                canvasQueryElement = $(CANVAS_HTML_CODE);
                canvasElement = canvasQueryElement.get(0);
                sectionQueryElement.append(canvasQueryElement);
            }

            function createButtons() {
                buttonMenuQueryElement = $(BUTTON_MENU_HTML_CODE);
                buttonMenuQueryElement.addClass(options.cssClasses.buttonMenu);

                canvasModeButtonQueryElement = $(buttonMenuQueryElement.children('button').get(0));

                clearButtonQueryElement = $(buttonMenuQueryElement.children('button').get(1));
                clearButtonQueryElement.addClass(options.cssClasses.clearButton);
                clearButtonQueryElement.html(options.texts.clearButton);

                colorButtonQueryElement = $(buttonMenuQueryElement.children('button').get(2));
                colorButtonQueryElement.addClass(options.cssClasses.colorButton + ' black');
                colorButtonQueryElement.html(options.texts.colorButton);

                sectionQueryElement.append(buttonMenuQueryElement);
            }

            function initializeContext(element) {
                context = element.getContext('2d');
                context.drawStyle = "#FF0000";
                context.webkitImageSmoothingEnabled = true;

                canvasElement.width = options.canvasSize.width;
                canvasElement.height = options.canvasSize.height;
                canvasQueryElement = $(canvasElement);
            }

            function makeCanvasTransparent() {
                context.fillStyle = 'rgba(0, 0, 0, 0)';
                context.fillRect(0, 0, canvasElement.width, canvasElement.height);
            }

            function initialize() {
                createCanvas();
                createButtons();

                initializeContext(canvasElement);
                makeCanvasTransparent();
            }

            function setCanvasModeEnabled() {
                canvasQueryElement.css('pointer-events', 'auto');

                canvasModeButtonQueryElement.addClass(options.cssClasses.canvasEnabledButton);
                canvasModeButtonQueryElement.removeClass(options.cssClasses.canvasDisabledButton + ' ' + options.cssClasses.canvasEditButton);
                canvasModeButtonQueryElement.html(options.texts.canvasEnabledButton);
            }

            function setCanvasModeDisabled() {
                canvasQueryElement.css('pointer-events', 'none');

                canvasModeButtonQueryElement.addClass(options.cssClasses.canvasDisabledButton);
                canvasModeButtonQueryElement.removeClass(options.cssClasses.canvasEnabledButton + ' ' + options.cssClasses.canvasEditButton);
                canvasModeButtonQueryElement.html(options.texts.canvasDisabledButton);
            }

            function setCanvasModeEdit() {
                canvasQueryElement.css('pointer-events', 'auto');

                canvasModeButtonQueryElement.addClass(options.cssClasses.canvasEditButton);
                canvasModeButtonQueryElement.removeClass(options.cssClasses.canvasEnabledButton + ' ' + options.cssClasses.canvasDisabledButton);
                canvasModeButtonQueryElement.html(options.texts.canvasEditButton);
            }

            function setCurrentColorIndex(index) {
                for (var i = 0; i < options.colors.length; i++) {
                    if (i != index) {
                        colorButtonQueryElement.removeClass(options.colors[i].cssClass)
                    } else {
                        colorButtonQueryElement.addClass(options.colors[i].cssClass);
                    }
                }
            }

            function addMouseDownHandler(handlerFunction) {
                canvasQueryElement.mousedown(handlerFunction);
            }

            function addMouseMoveHandler(handlerFunction) {
                canvasQueryElement.mousemove(handlerFunction);
            }

            function addMouseUpHandler(handlerFunction) {
                $(BODY_ELEMENT_SELECTOR).mouseup(handlerFunction);
            }

            function addKeyUpHandler(handlerFunction) {
                $(BODY_ELEMENT_SELECTOR).keyup(handlerFunction);
            }

            function addCanvasModeButtonClickHandler(handlerFunction) {
                canvasModeButtonQueryElement.click(handlerFunction);
            }

            function addClearButtonClickHandler(handlerFunction) {
                clearButtonQueryElement.click(handlerFunction);
            }

            function addColorButtonClickHandler(handlerFunction) {
                colorButtonQueryElement.click(handlerFunction);
            }

            function getCanvasQueryElement() {
                return canvasQueryElement;
            }

            function getContext() {
                return context;
            }

            function getButtonMenuQueryElement() {
                return buttonMenuQueryElement;
            }

            function getCanvasModeButtonQueryElement() {
                return canvasModeButtonQueryElement;
            }

            function getClearButtonQueryElement() {
                return clearButtonQueryElement;
            }

            function getColorButtonQueryElement() {
                return colorButtonQueryElement;
            }

            return {
                initialize: initialize,
                setCanvasModeEnabled: setCanvasModeEnabled,
                setCanvasModeDisabled: setCanvasModeDisabled,
                setCanvasModeEdit: setCanvasModeEdit,

                setCurrentColorIndex: setCurrentColorIndex,

                addMouseDownHandler: addMouseDownHandler,
                addMouseMoveHandler: addMouseMoveHandler,
                addMouseUpHandler: addMouseUpHandler,
                addKeyUpHandler: addKeyUpHandler,
                addCanvasModeButtonClickHandler: addCanvasModeButtonClickHandler,
                addClearButtonClickHandler: addClearButtonClickHandler,
                addColorButtonClickHandler: addColorButtonClickHandler,

                getCanvasQueryElement: getCanvasQueryElement,
                getContext: getContext,
                getButtonMenuQueryElement: getButtonMenuQueryElement,
                getCanvasModeButtonQueryElement: getCanvasModeButtonQueryElement,
                getClearButtonQueryElement: getClearButtonQueryElement,
                getColorButtonQueryElement: getColorButtonQueryElement
            };
        }

        function StateManager(domElementManager, curveEditor) {

            const DELETE_KEY_CODE = 46;

            var currentCanvasMode = CanvasMode.CANVAS_MODE_DISABLED;

            var curveActive = false;

            var highlightedCurveIndex = -1;

            var currentCurves = [];

            var currentColorIndex = 0;

            function getCoordinates(e) {
                var x = e.offsetX ? e.offsetX : e.layerX ? e.layerX : e.originalEvent.layerX;
                var y = e.offsetY ? e.offsetY : e.layerY ? e.layerY : e.originalEvent.layerY;

                return { x: x, y: y};
            }

            function onMouseDown(e) {
                var color, coordinates = getCoordinates(e);
                if (currentCanvasMode === CanvasMode.CANVAS_MODE_ENABLED) {
                    color = options.colors[currentColorIndex].color;
                    curveEditor.startDrawingCurve(coordinates, color);
                    curveActive = true;
                }
            }

            function onMouseMove(e) {
                var coordinates = getCoordinates(e);

                if (currentCanvasMode === CanvasMode.CANVAS_MODE_ENABLED && curveActive) {
                    curveEditor.addCoordinatesToCurveDrawn(coordinates);
                } else if (currentCanvasMode === CanvasMode.CANVAS_MODE_EDIT && !curveActive) {
                    highlightedCurveIndex = curveEditor.getHighlightedCurveIndex(currentCurves, coordinates);
                }
            }

            function endCurveAndSaveNewCurves() {
                currentCurves = curveEditor.endDrawingCurve(currentCurves);
                dataManager.saveCurves(sectionId, currentCurves);
            }

            function onMouseUp() {
                if (currentCanvasMode === CanvasMode.CANVAS_MODE_ENABLED && curveActive) {
                    endCurveAndSaveNewCurves();
                    curveActive = false;
                } else if (currentCanvasMode === CanvasMode.CANVAS_MODE_EDIT) {
                    if (highlightedCurveIndex != -1 && !curveActive) {
                        curveActive = true;
                    } else if (curveActive) {
                        curveActive = false;
                    }
                }
            }

            function deleteHighlightedCurve() {
                currentCurves.splice(highlightedCurveIndex, 1);
                curveActive = false;
                highlightedCurveIndex = -1;

                dataManager.saveCurves(sectionId, currentCurves);
                curveEditor.getHighlightedCurveIndex(currentCurves);
            }

            function onKeyUp(e) {
                if (currentCanvasMode === CanvasMode.CANVAS_MODE_EDIT && curveActive && e.keyCode === DELETE_KEY_CODE) {
                    deleteHighlightedCurve();
                }
            }

            function onCanvasModeButtonClick() {
                currentCanvasMode = currentCanvasMode.nextMode;
                setCanvasMode(currentCanvasMode);
            }

            function setCanvasMode(mode) {
                curveEditor.reset();
                currentCanvasMode = mode;

                if (mode === CanvasMode.CANVAS_MODE_ENABLED) {
                    domElementManager.setCanvasModeEnabled();
                    curveEditor.draw(currentCurves);
                } else if (mode === CanvasMode.CANVAS_MODE_DISABLED) {
                    domElementManager.setCanvasModeDisabled();
                    curveEditor.draw(currentCurves);
                } else if (mode === CanvasMode.CANVAS_MODE_EDIT) {
                    domElementManager.setCanvasModeEdit();
                    curveEditor.getHighlightedCurveIndex(currentCurves);
                }
            }

            function onClearButtonClick() {
                currentCurves = [];
                dataManager.clearCurves(sectionId);

                curveEditor.draw(currentCurves);
            }

            function onColorButtonClick() {
                currentColorIndex = (currentColorIndex + 1) % options.colors.length;
                domElementManager.setCurrentColorIndex(currentColorIndex);
            }

            function addEventListeners() {
                domElementManager.addMouseDownHandler(onMouseDown);
                domElementManager.addMouseMoveHandler(onMouseMove);
                domElementManager.addMouseUpHandler(onMouseUp);
                domElementManager.addKeyUpHandler(onKeyUp);

                domElementManager.addCanvasModeButtonClickHandler(onCanvasModeButtonClick);
                domElementManager.addClearButtonClickHandler(onClearButtonClick);
                domElementManager.addColorButtonClickHandler(onColorButtonClick);
            }

            function loadPreviousCurves() {
                currentCurves = dataManager.loadCurves(sectionId);
            }

            function initialize() {
                addEventListeners();
                loadPreviousCurves();
                domElementManager.setCurrentColorIndex(currentColorIndex);

                setCanvasMode(currentCanvasMode);
            }

            return {
                initialize: initialize
            };
        }

        function initializeObjects() {
            var domElementManager = DomElementManager();
            domElementManager.initialize();
            var canvasHelper = CanvasHelper();
            canvasHelper.initialize(domElementManager.getCanvasQueryElement());
            var curveEditor = CurveEditor(canvasHelper);
            curveEditor.initialize(domElementManager.getContext());
            var stateManager = StateManager(domElementManager, curveEditor);
            stateManager.initialize();

            return {
                domElementManager: domElementManager, canvasHelper: canvasHelper, curveEditor: curveEditor, stateManager: stateManager
            };
        }

        function callInitializeHandler(domElementManager) {
            var eventArgs = {
                buttonMenuElement: domElementManager.getButtonMenuQueryElement().get(0),
                canvasElement: domElementManager.getCanvasQueryElement().get(0)
            };
            options.handlers.initialize.call(this, eventArgs);
        }

        function initialize(_sectionQueryElement, _sectionId, _options) {
            sectionQueryElement = _sectionQueryElement;
            sectionId = _sectionId;
            options = _options;

            var objects = initializeObjects();
            callInitializeHandler(objects.domElementManager);
        }

        return {
            initialize: initialize
        };
    }

    function refineOptions(options) {
        options = options || {};
        options.canvasSize = options.canvasSize || {};
        options.canvasSize.width = options.canvasSize.width || 1024;
        options.canvasSize.height = options.canvasSize.height || 768;
        options.minPointDistance = options.minPointDistance || 5;
        options.cssClasses = options.cssClasses || {};
        options.cssClasses.buttonMenu = options.cssClasses.buttonMenu || 'canvas-button-menu';
        options.cssClasses.canvasEnabledButton = options.cssClasses.canvasEnabledButton || 'canvas-enabled-button';
        options.cssClasses.canvasDisabledButton = options.cssClasses.canvasDisabledButton || 'canvas-disabled-button';
        options.cssClasses.canvasEditButton = options.cssClasses.canvasEditButton || 'canvas-edit-button';
        options.cssClasses.clearButton = options.cssClasses.clearButton || 'clear-canvas-button';
        options.cssClasses.colorButton = options.cssClasses.colorButton || 'color-button';
        options.texts = options.texts || {};
        options.texts.canvasEnabledButton = options.texts.noTexts ? "" : options.texts.canvasEnabledButton || 'Drawing enabled';
        options.texts.canvasDisabledButton = options.texts.noTexts ? "" : options.texts.canvasDisabledButton || 'Drawing disabled';
        options.texts.canvasEditButton = options.texts.noTexts ? "" : options.texts.canvasEditButton || 'Edit enabled';
        options.texts.clearButton = options.texts.noTexts ? "" : options.texts.clearButton || 'Clear elements';
        options.texts.colorButton = options.texts.noTexts ? "" : options.texts.colorButton || 'Change color';
        options.handlers = options.handlers || {};
        options.handlers.initialize = options.handlers.initialize || function() {
        };
        options.standardStyle = options.standardStyle || {};
        options.standardStyle.lineWidth = options.standardStyle.lineWidth || 3;
        options.standardStyle.lineColor = options.standardStyle.lineColor || '#000000';
        options.standardStyle.highlightedLineColor = options.standardStyle.highlightedLineColor || '#FF0000';
        options.colors = options.colors || [
            { cssClass: 'black', color: '#000000' },
            { cssClass: 'red', color: '#FF0000'}
        ];

        return options;
    }

    function getSectionElementsWithoutChildSections() {
        return $(SECTION_ELEMENTS_SELECTOR).filter(function() {
            return $(this).find(SECTION_SELECTOR).size() == 0;
        });
    }

    function initialize(options) {
        var dataManager = DataManager();

        options = refineOptions(options);
        var sectionElements = getSectionElementsWithoutChildSections();

        function getSectionId(sectionIndex) {
            return window.location.pathname + "#section-" + sectionIndex;
        }

        sectionElements.each(function(sectionIndex) {
            var sectionId = getSectionId(sectionIndex);
            //noinspection JSCheckFunctionSignatures
            DrawingCanvasForCanvasElement(dataManager).initialize($(this), sectionId, options);
        });
    }

    return {
        initialize: initialize
    };
};