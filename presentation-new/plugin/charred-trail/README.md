# charred-trail

A plugin for [reveal.js](https://github.com/hakimel/reveal.js/) that implements
the "Charred Trail" Pattern from the book
[Presentation Patterns](http://presentationpatterns.com/) by Neal Ford,
Matthew McCullough,
and Nate Schutta.

[View the demo!](http://ericweikl.github.io/revealjs-charred)


## Installation

Copy charred-trail.js into your reveal.js installation under
`plugin/charred-trail/`, or add it as a git submodule.

Then add the following to your dependencies in `Reveal.initialize()`:
```js
  {
    src: 'plugin/charred-trail/charred-trail.js',
    async: true,
    condition: function() { return !!document.body.classList; }
  }
```

## Styling

The newest displayed fragment is marked with the CSS class "focus". Using this,
you can highlight the current fragment and/or gray out previous fragments.

```css
.reveal .slides section .fragment.visible { opacity: 0.3; }
.reveal .slides section .fragment.visible.focus { opacity: 1; }
```

To protect fragments from fading out, you can mark them with a separate class,
e.g.

```css
.reveal .slides section .fragment.visible.no-burn { opacity: 1; }
```

Or, just include the supplied `charred-trail.css`.

