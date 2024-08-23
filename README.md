# Timeline diagrams

This repository contains manually drawn diagrams with timelines, as well as
Kotlin code for generating similar types of diagrams. However, please note that
the code is not fully functional yet and currently produces diagrams that are
graphically less refined compared to the manually created ones.

## Manually drawn diagrams

### Symbolic execution

See the latest version in
[`symbolic-execution.svg`](diagram/symbolic-execution.svg).

![Preview](https://raw.github.com/enzet/symbolic-execution/master/diagram/symbolic-execution.png)

The [symbolic execution timeline](diagram/symbolic-execution.svg) highlights key
tools and concepts in pure symbolic execution, dynamic symbolic execution
(concolic testing), as well as related ideas of model checking, SAT/SMT solving,
black-box fuzzing, taint data tracking, and other dynamic analysis techniques.

### SAT and SMT solving

See the latest version in [`solving.svg`](diagram/solving.svg).

![Preview](https://raw.github.com/enzet/symbolic-execution/master/diagram/solving.png)

The [solving timeline](diagram/solving.svg) showcases major SAT and SMT
techniques and solvers, including those not directly related to symbolic
execution.

Additionally, there is [a temporary timeline](diagram/other.svg) for some tools
that are not displayed in the diagrams above.

### Building PNG and PDF

Please install the following fonts for correct SVG display:
  - [Roboto](https://fonts.google.com/specimen/Roboto) (regular and italic) and
  - [Fira Code](https://github.com/tonsky/FiraCode).

Use Inkscape to build PNG or PDF files. Example for the `symbolic-execution`
diagram:
  - PNG: `inkscape diagram/symbolic-execution.svg --export-png diagram/symbolic-execution.png --export-dpi 150`,
  - PDF: `inkscape diagram/symbolic-execution.svg --export-pdf diagram/symbolic-execution.pdf`.

## Design

We use colors from
[GitHub Linguist](https://github.com/github/linguist/blob/master/lib/linguist/languages.yml)
for input languages.

## Contribution to SVG files

Feel free to suggest changes or add new information. If your change is minor
(like fixing a typo), you can directly edit the source code of
[`symbolic-execution.svg`](diagram/symbolic-execution.svg). For major changes,
you're encouraged to either create a new issue or edit `symbolic-execution.svg`.
The [Inkscape](https://inkscape.org/en/) editor is strongly recommended due to
source code compatibility issues.

### Before commiting

Please use [SVGO](https://github.com/svg/svgo) for diagram optimization before
commiting (to achieve a cleaner diff):

```shell
svgo diagram/symbolic-execution.svg \
    --pretty \
    --enable=sortAttrs \
    --disable=removeEditorsNSData \
    --disable=cleanupIDs \
    --indent=2
```

## Automatic diagram generation

The repository also includes Kotlin code that automatically generates similar
diagrams using the [`tools.json`](tools/tools.json) file, which contains
descriptions of the tools, and a [`config`](diagram/config) file that guides the
generator on how to arrange and draw the elements.

### Tools structure

File [`tools.json`](tools/tools.json) contains tools JSON description. E.g.:

```json
    {
        "name": "DART",
        "since": 2005,
        "languages": ["C"],
        "uses": ["lp_solve"],
        "based": ["CIL"],
        "description": "random testing\nand direct\nexecution",
        "type": "concolic",
        "authors": ["P. Godefroid (B)", "K. Sen (I)", "N. Klarlund (B)"]
    },
```
