Symbolic execution timeline
===========================

Diagram highlights some major tools and ideas of pure symbolic execution, dynamic symbolic execution (concolic) as well as related ideas of model checking, SAT/SMT solving, black-box fuzzing, taint data tracking, and other dynamic analysis techniques.

Requirements
------------

Please, install fonts for correct SVG display:
  * [Roboto](https://fonts.google.com/specimen/Roboto) (regular and italic) and
  * [Fira Code](https://github.com/tonsky/FiraCode).

Preview
-------

:warning: PNG preview could be outdated. See [`diagram.svg`](diagram.svg) for the latest version.

![Preview](https://raw.github.com/enzet/dynamic-symbolic-execution/master/diagram.png)

Contribution
------------

Feel free to suggest changes or add new information. If your change is minor (like typo), you can just edit source code of [`diagram.svg`](diagram.svg). If change is major, you are encouraged to either create new issue, or edit `diagram.svg` ([Inkscape](https://inkscape.org/en/) editor is strongly recommended due to source code issues).

### Before commiting ###

Please, use [SVGO](https://github.com/svg/svgo) for diagram optimization before commiting (to get more clean diff):

    svgo diagram.svg \
        --pretty \
        --enable=sortAttrs \
        --disable=removeEditorsNSData \
        --disable=cleanupIDs \
        --indent=2

And update preview:

    inkscape diagram.svg --export-png diagram.png --export-dpi 150
