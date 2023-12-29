# Changelog

This file represents a prelimary changelog for unreleased changes.
Changelogs for published releases can be found on [GitHub Releases](https://github.com/Dyvil/Dyvil/releases)

---

# v0.48.0

# Library v0.48.0

## Improvements

* Improved the command-line interface of the GenSrc Runner with new `--output-dir` and `--source-dir` options. #517
* Replaced many `infix` and `postfix` methods with `final extension` methods.
* Warnings now separate row and column numbers with `:` instead of `.`.

### Bugfixes

* Fixed the `Files.antPattern` method not handling backslashes as file separators. #518
* Fixed the `Files.antPattern` method ignoring some subtleties of Ant patterns. #518
* The `DyvilLexer` correctly handles `\r` and `\r\n` line separators now. #519
  > Within double-quoted string and verbatim char literals, they are normalized to `\n`.
* The `Builtins.decorate` method correctly handles `\r` and `\r\n` line separators now. #519

## Removals

- Removed some deprecated `Strings` methods.
- Removed deprecated `MarkerList` and `Marker` methods.

# Compiler v0.48.0

## New Features

+ Added the `--no-deprecated` option to hide deprecation warnings completely.

## Bugfixes

* Postfix methods no longer produce warnings about the number of parameters.
* Extension methods now allow directly extending a type parameter (e.g. `extension func foo<T>(this: T)`).

## Removals

- Removed the `test` phase and related options. #496

# REPL v0.30.0

# GenSrc v0.12.0

## Bugfixes

* The GenSrc lexer correctly handles `\r` and `\r\n` line separators now. #519

## Removals

- The GenSrc compiler no longer runs the templates. #517
- Removed the `gensrc-dir` option. #517
