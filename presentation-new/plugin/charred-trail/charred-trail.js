/**
 * Implements the "Charred Trail" pattern for fragments, see the book
 * "Presentation Patterns" by Neal Ford, Matthew McCullough, and Nate Schutta
 * (http://presentationpatterns.com/).
 *
 * See README.md for more information.
 *
 * MIT Licensed
 * See LICENSE for more information.
 */
(function () {
  function toArray(list) {
    if (!list) return [];
    return Array.prototype.slice.call(list);
  }

  function getIndex(fragment) {
    return parseInt(fragment.getAttribute('data-fragment-index'));
  }

  function makeEventHandler(classActionCurrentFragment, compareIndexFunction, classActionIfMatch) {
    return function(e) {
      var currentIndex = getIndex(e.fragment);
      var fragments = Reveal.getCurrentSlide().querySelectorAll('.fragment');
      toArray(fragments).forEach(function(fragment) {
        if (compareIndexFunction(getIndex(fragment), currentIndex)) {
          fragment.classList[classActionIfMatch]('focus');
        }
      });
      e.fragment.classList[classActionCurrentFragment]('focus');
    }
  }

  var NOT_CURRENT = function(f, i) { return f !== i };
  var IS_PREVIOUS = function(f, i) { return f === i - 1 };

  Reveal.addEventListener('fragmentshown',  makeEventHandler('add',    NOT_CURRENT, 'remove'));
  Reveal.addEventListener('fragmenthidden', makeEventHandler('remove', IS_PREVIOUS, 'add'));
}());

