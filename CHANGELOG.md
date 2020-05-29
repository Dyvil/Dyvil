# Dyvil v0.45.0

+ Added support for properties in extensions. #478

## Dyvil Library v0.45.0

+ Added more extension methods to the `Lists` and `Collections` classes.
+ Added the `contains`, `count` and `split` methods to the `StringCodePoints` extension.
+ Added the `count(character:from:to:)` method to the `StringChars` extension.
+ Added the `count(codePoint:from:to:)` method to the `StringCodePoints` extensions.
+ Added the `d.s.CharArrayView` class.
+ Added the `first`, `last` and `indices` methods to the `CharSequences` extension.
+ Added the `Modifiers.PREFIX_FLAG` and `.PREFIX` constants. #475
+ Added the `String.charGroups(width:)` method to the `StringChars` extension.
+ Added the `String.count(substring:)` and `String.count(substring:from:to:)` methods to the `Strings` extension.
+ Added the `String.split(by:String)` method to the `Strings` extension.
+ Added the `String.styled(with:)` extension method to the `Console` class.
+ Added the `StringBuilder.append(with:body:)` extension method to the `Console` class.
+ Added the `StringBuilder.append(with:text:)` extension method to the `Console` class.
+ Added the `StringBuilders` extension.
+ Added the `StringCodePoints` extension.
* Cleaned up some classes in the `dyvil.lang` package.
* Cleaned up the `Console` class.
* Deprecated all collection interfaces except `Matrix`.
* Deprecated the `CharUtils` class.
* Deprecated the `String.size` method.
* Deprecated the subclasses of `dx.t.p.m.Marker`.
* Fixed several problems with the `String.split(by:char)` in the `StringChars` extension.
* Moved character-based String extension methods from `Strings` to the `StringChars` extension.
* Moved the `|_|` operator methods to the `CharSequences` extension.
* Moved unsafe String extension methods from `Strings` to the `UnsafeStrings` extension.
* Renamed the `UsageInfo.markerLevel` parameter to `level`. #483
* Replaced usages of the `Marker` subclasses with `Marker` constructors.
* The `DynamicLinker` now uses the fallback method if an `IllegalAccessException` occurs.

## Dyvil Compiler v0.45.0

+ Added a deprecation warning diagnostic for infix functions with more or less than two parameters. #477
+ Added a deprecation warning diagnostic for non-symbolic prefix, infix and postfix functions. #474
+ Added a deprecation warning diagnostic for postfix functions with more or less than one parameter. #477
+ Added a deprecation warning diagnostic for prefix functions with more or less than one parameter. #477
+ Added a syntax warning diagnostic for single-statement for loops. #468
+ Added a syntax warning diagnostic for single-statement if and else branches. #468
+ Added a syntax warning diagnostic for single-statement repeat loops. #468
+ Added a syntax warning diagnostic for single-statement synchronized blocks. #468
+ Added a syntax warning diagnostic for single-statement try, catch and finally blocks. #468
+ Added a syntax warning diagnostic for single-statement while loops. #468
+ Added support for array type extensions. #476
+ Added the `ClassFormat.packageToExtended` method.
* `ExtensionMetadata.mangleName` now supports array types.
* `ExtensionMetadata` now performs cleanup earlier to avoid some warnings and errors.
* `ThisExpr` no longer type-checks using super type semantics.
* Cleaned up some compiler messages. #469
* Deprecated method return type inference for non-void methods. #471
* Fixed an issue where fenced arguments would not count as named and cause resolution to fail. #470
* Fixed function types being rendered incorrectly in diagnostic messages. #481
* Fixed incorrect translation of immutability error diagnostic messages. #480
* Fixed the `AbstractMember.getInternalName` method throwing an NPE when the name is `null`.
* Fixed the `ParameterList.removeFirst` method not updating indices.
* Fixed wildcard placeholders producing diagnostic markers with incorrect/null position. #482
* Improved compiler messages. #469
* Improved some syntax diagnostic message wording. #469
* Improved the `ExternalMethod.resolveParameters` implementation.
* Made all semantic diagnostic messages lowercase. #469
* Made all syntax diagnostic messages lowercase. #469
* Made the diagnostic summary more compact for the Dyvil marker style. #469
* Properties are now processed like methods in extensions. #478
* Renamed the `LambdaType` class to `FunctionType`.
* Replaced usages of the `Marker` subclasses with `Marker` constructors.
* Restructured the `ClassFormat` class.
* Rewrote the type and signature parser in `ClassFormat`.
* Simplified the `ClassFormat.extendedToPackage` implementation.
* Simplified the `ClassFormat.userToExtended` implementation.
* The `prefix` modifier is now reified with a flag in `DyvilModifiers` annotations. #475
* The diagnostic summary is no longer printed for the GCC and Machine marker styles. #469
* Variables in For Loops, Binding Ifs, Binding Patterns and Catch Blocks can now have the name `_`. #343
- Removed error diagnostic for properties in extensions. #478
- Removed some unnecessary syntax diagnostics. #469
- Removed unnecessary casts in the `ClassList`, `ClassBody` and `Deprecation` classes.

## Dyvil REPL v0.27.0

## Dyvil Property Format v0.16.1

## Dyvil GenSrc v0.9.5

# Dyvil v0.45.1

## Dyvil Library v0.45.0

## Dyvil Compiler v0.45.0

## Dyvil REPL v0.27.1

* Bumped version number.

## Dyvil Property Format v0.16.2

* Bumped version number.

## Dyvil GenSrc v0.9.6

* Bumped version number.

# Dyvil v0.46.0

## Dyvil Library v0.46.0

- Removed the deprecated `CharUtils` class.
- Removed the deprecated subclasses of `Marker`.

## Dyvil Compiler v0.46.0

+ Added a more standardized command-line option format. #489
* Binding if statements no longer require parentheses when using a condition. #486
* Deprecated parentheses in if statements, for loops and catch blocks. #488
* Deprecated the old command-line option format. #489
* Fixed a '%s' instead of the package name in the `package_declaration.default_package` message.
* Fixed a possible NPE in `Library.unloadLibrary`.
* Fixed incorrect marker formatting for `Deprecated.forRemoval` values.
* Informational output is now omitted without the `--debug` option. #489
* The `else if` idiom no longer produces a single-statement else warning. #485

## Dyvil REPL v0.28.0

+ Added a more standardized command-line option format. #489
* Deprecated the old command-line option format. #489
* Informational output is now omitted without the `--debug` option. #489

## Dyvil GenSrc v0.10.0

+ Added a more standardized command-line option format. #489
* Deprecated the old command-line option format. #489
* Informational output is now omitted without the `--debug` option. #489

# Dyvil v0.46.1

## Dyvil Library v0.46.0

## Dyvil Compiler v0.46.1

+ Added the `--main-type`, `--main-args` and `--test-dir` options. #493
+ Added the `--jar-name`, `--jar-version`, `--jar-vendor` and `--jar-name-format` options. #493
* Deprecated the `jar` phase and related options. #492
* Deprecated the `test` phase and related options. #494
* Fixed markers not being printed.
* Fixed weird format of the "'x' was not compiled due to errors in the compilation unit" message.
* Fixed zero exit code when an option is unrecognized.

## Dyvil REPL v0.28.1

+ Added the `--dump-dir` option. #493

## Dyvil GenSrc v0.10.1

+ Added the `-g`/`--gensrc-dir` options. #493

# Dyvil v0.46.2

## Dyvil Library v0.46.0

## Dyvil Compiler v0.46.2

* Fixed name clash with the `-o` option.

## Dyvil REPL v0.28.1

## Dyvil GenSrc v0.10.1

# Dyvil v0.46.3

## Dyvil Library v0.46.0

## Dyvil Compiler v0.46.3

* Standardized the way include/exclude filtering is performed. #497
* Deprecated the `-L`/`--log-file` option. #500
* Fixed an issue causing the `--include-patterns` and `--exclude-patterns` options to be parsed incorrectly. #498

## Dyvil REPL v0.28.1

## Dyvil GenSrc v0.10.1

# Dyvil 0.46.4

## Dyvil Library v0.46.1

+ Added the `Modifiers.POSTFIX_FLAG` and `.POSTFIX` constants. #508
* Fixed the `MarkerList.warnings` property counting errors instead of warnings.

## Dyvil Compiler v0.46.4

+ The `postfix` modifier is now reified with a flag in `DyvilModifiers` annotations. #508
* Fixed `postfix` functions causing syntax warnings. #501
* Fixed operator functions with dot identifiers causing syntax warnings. #502
* Fixed operator functions with implicit parameters causing syntax warnings. #503

## Dyvil REPL v0.28.1

## Dyvil GenSrc v0.10.1

# Dyvil v0.46.5

## Dyvil Library v0.46.1

## Dyvil Compiler v0.46.5

+ Try/Catch Blocks can now be used as expressions. #33
+ Functional methods can now be inherited from super-interfaces. #504
* Fixed array constructors working incorrectly as subexpressions. #505
* Fixed error when using Try/Catch Blocks as the last statement. #509
* Fixed finally blocks not being executed before return statements in try or catch blocks. #511
* Fixed finally blocks not rethrowing exceptions. #512
* Fixed incorrect bytecode generation for synchronized blocks. #510
* Fixed incorrect position in marker for failed implicit reference type conversions.
* Fixed synchronized blocks being parsed incorrectly. #510
* Improved finally block compilation.
* Improved type inference for lambda expressions.

## Dyvil REPL v0.28.2

* Bumped version number due to compiler API changes.

## Dyvil GenSrc v0.10.2

* Bumped version number due to compiler API changes.

# Dyvil v0.47.0

* The main artifact (`org.dyvil:dyvil`) no longer includes all dependencies' files.

## Dyvil Library v0.47.0

+ Added the `javac` marker style.
+ Added the `UnsafeAccess` class with the `UNSAFE` constant.
+ Added the `LookupAccess` class with the `LOOKUP` constant.
* The `Name` cache now works correctly when accessed concurrently.
* Deprecated most `dyvil.reflect` classes.
- Removed the `ReflectUtils.JAVA_LANG_ACCESS` field because it did not work in some JDKs.

## Dyvil Compiler v0.47.0

+ Added support for the `javac` marker style.
+ Added the `-o` option as a short form for `--output-dir`.
* Dropped support for `key=value` command-line arguments. #490
* Dropped support for the `jar` phase and related options. #495
* Fixed a typo in the `throw.type` error message.
* Fixed an exception caused by implicit lambda conversion. #514
- Dropped support for legacy `marker-style` values `m` and `g`. #491
- Dropped support for legacy options `-o`, `--machine-markers` and `--gcc-markers`. #491
- Dropped support for the `--log-file` option. #491

# Dyvil REPL v0.29.0

+ Added a persistent history, saved in `~/.dyvil/repl-history`. #515
- Dropped support for `key=value` command-line arguments. #490

# Dyvil GenSrc v0.11.0

- Dropped support for `key=value` command-line arguments. #490
