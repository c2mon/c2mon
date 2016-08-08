
The C2MON documentation is written in [Markdown](https://daringfireball.net/projects/markdown/). and built using
[MkDocs](http://www.mkdocs.org/). You will need MkDocs installed before building/previewing the docs.

## Preview

```bash
$ cd docs && mkdocs serve
```

As result you should be able to navigate through your the full documentation in your browser.
By default, the URL where you would be able to access documentation is
[http://127.0.0.1:8000/](http://127.0.0.1:8000/).


## Build

```bash
$ cd docs && mkdocs build
```

This will produce a `docs/build/` directory containing all the static html, css and js files.
