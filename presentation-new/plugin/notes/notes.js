/**
 * Handles opening of and synchronization with the reveal.js
 * notes window.
 */
var RevealNotes = (function() {
	"use strict";

	function openNotes() {
		var jsFileLocation = document.querySelector('script[src$="notes.js"]').src;  // this js file path
		jsFileLocation = jsFileLocation.replace(/notes\.js(\?.*)?$/, '');   // the js folder path
		var notesPopup = window.open( jsFileLocation + 'notes.html', 'reveal.js - Notes', 'width=1120,height=850' );
	}

	// If the there's a 'notes' query set, open directly
	if( window.location.search.match( /(\?|\&)notes/gi ) !== null && !window.location.search.match( /(\?|\&)receiver/gi )) {
		openNotes();
	}

	// Open the notes when the 's' key is hit
	document.addEventListener( 'keydown', function( event ) {
		// Disregard the event if the target is editable or a
		// modifier is present
		if ( document.querySelector( ':focus' ) !== null || event.shiftKey || event.altKey || event.ctrlKey || event.metaKey ) return;

		if( event.keyCode === 83 ) {
			event.preventDefault();
			openNotes();
		}
	}, false );

	return { open: openNotes };
})();

/**
 * Patch Reveal's configure() method to add 
 * option 'peekNextFragments: display all upcoming Fragments
 */
(function() {
	"use strict";
	// patch Reveal's configure() method
	var _configure = Reveal.configure;
	var styleNode = document.createElement('style');

	// the initial state of the fragment, just grayed out; only works nicely for default animation (opacity change)
	styleNode.innerHTML = ".reveal .slides section .fragment:not(.visible) { opacity: 0.33 !important; }";

	Reveal.configure = function(options) {
		_configure.call(Reveal, options);
		options = Reveal.getConfig();
		if(options.peekFragments === true && !document.head.contains(styleNode)) document.head.appendChild(styleNode);
		if(options.peekFragments === false && document.head.contains(styleNode)) document.head.removeChild(styleNode);
	}
	Reveal.configure({});
})();

/**
 * A Plugin to assign a time to each slide
 * Include data-timer="[+-]MMM:SS" in sections
 * 
 */
// TODO config.dependencies (reveal.js l. 228)
(function() {
	"use strict";
	var Timer = (function() {
		var time = 0, start = Math.floor((new Date()).getTime()/1000);

		function update() {
			var slides = document.querySelectorAll(".reveal .slides section.past, .reveal .slides section.present");
			slides = Array.prototype.slice.apply(slides);
			slides.forEach(function(slide) {
				var slideTime, minutes, seconds, pm;
				if(typeof slide === 'undefined') return;
				slideTime = slide.getAttribute('data-timer');
				if(!slideTime || !/^(\+|-)?\d+:[0-5]\d$/.test(slideTime)) return;
				slideTime = slideTime.match(/^((\+|-)?)(\d+):([0-5]\d)$/);
				pm = slideTime[2];
				minutes = Number(slideTime[3]);
				seconds = Number(slideTime[4]);
				switch(pm) {
					case "+": add(minutes, seconds); break;
					case "-": subtract(minutes, seconds); break;
					default: reset(minutes, seconds);
				}
				slide.removeAttribute("data-timer");
			});
		}
		function add(minutes, seconds) {
			time += (minutes?minutes:0) * 60 + (seconds?seconds:0);
		}
		function subtract(minutes, seconds) {
			time -= (minutes?minutes:0) * 60 + (seconds?seconds:0);
		}
		function reset(minutes, seconds) {
			time = (minutes?minutes:0) * 60 + (seconds?seconds:0);
		}
		// TODO start, stop, pause, resume?

		function getTime() {
			var now = Math.floor((new Date()).getTime()/1000);
			return start - now + time;
		}

		Reveal.addEventListener('slidechanged', update);
		if(Reveal.isReady()) {
			update();
		} else {
			Reveal.addEventListener('ready', update);
		}

		return {
			add: add,
			subtract: subtract,
			reset: reset,
			getTime: getTime
		};
	})();

	// patch Reveal's configure() method
	(function() {
		var _configure = Reveal.configure;
		Reveal.configure = function(options) {
			_configure.call(Reveal, options);
			options = Reveal.getConfig();
			if(options.timer === true && !Reveal.Timer) Reveal.Timer = Timer;
			if(options.timer === false && Reveal.Timer) Reveal.Timer = undefined;
		}
		Reveal.configure({});
	})();
})();
