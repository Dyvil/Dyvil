# Dyvil v0.43.0

- Field access and assignment are now compatible with wildcard lambda expressions. #452
- Final extension methods now use static dispatch. #455
- Removed C-style for loops. #447

## Dyvil Library v0.43.0

- Added extensions for common `List` implementations. #448
- Added extensions for common `Set` implementations. #448
- Added the `--` operator declaration to the Lang header.
- Added the `Collections` extension. #448
- Added the `dyvil.io.StringBuilderWriter` class.
- Added the `FlatMapIterator` class. #445
- Added the `I18n.identity` constant.
- Added the `isError`, `isWarning` and `color` properties to the `MarkerLevel` class.
- Added the `Iterables` extension.
- Added the `Lists`, `Maps` and `Sets` extensions. #448
- Added the `MarkerPrinter` class. #451
- Added the `MarkerStyle.GCC` enum constant.
- Added the `MarkerStyle.MACHINE` enum constant.
- Added the `MarkerStyle` enum. #451
- Added the `Queues` extensions. #448
- Added the `Source.filePath` property.
- Added the `SourcePosition.toPositionString` method.
- Changed the `String.build` methods in the `Strings` extension to `apply(builtBy:)`.
- Cleaned up classes in the `dyvil.function` package.
- Cleaned up the `FieldReflection` class.
- Cleaned up the `Ref` interface and its implementation templates.
- Cleaned up the `Source` class and implementations.
- Cleaned up the `SourcePosition` class.
- Cleaned up the `Strings` extension.
- Converted the `Marker` and `MarkerList` classes to Dyvil.
- Deprecated `dyvil.io.FileUtils`. #446
- Deprecated the `MarkerLevel.IGNORE` constant.
- Fixed the Java name of `Files.tryWrite(text:)` being `writeText` instead of `tryWriteText`.
- Implemented the `GCC` marker style in the `MarkerPrinter` class.
- Implemented the `MACHINE` marker style in the `MarkerPrinter` class.
- Improved `SourcePosition.apply` method signatures.
- Made methods in the `Files`, `Primitives`, `BigIntegers` and `BigDecimals` extensions `final`.
- Made most methods of the `Strings` extensions `final`.
- Made the `Source` class `Iterable` and added the `SourceIterator` class.
- Removed the `AutoPrinter` and `CppIO` classes from the `dyvil.syntax` package.
- Replaced the `MappingIterator` class with the `MapIterator` class. #445
- Replaced usages of `dyvil.io.FileUtils` with `dyvil.io.Files`. #446
- The `dyvil.collection.Iterables` and related extensions are now available through the Lang header.
- The `MarkerList` class now extends `java.util.ArrayList`.
- The `Ref` and related templates are now standalone, containing all classes.
- Updated the `FieldReflection` implementation to use `java.util.List`.
- Updated the `FilterIterator` class. #445
- Updated the `Iterators` extension. #445

## Dyvil Compiler v0.43.0

- Added the `LambdaExpr(SourcePosition, ParameterList)` constructor.
- Added the `WildcardLambdaAware` class. #452
- Cleaned up the `AbstractFieldAccess`, `FieldAccess` and `FieldAssignment` classes.
- Cleaned up the `Candidate.compareTo` method.
- Cleaned up warnings in the `AbstractClass` class.
- Dropped support for C-style for loops. #447
- Final extension methods now generate `invokestatic` instead of `invokedynamic` instructions. #455
- Fixed method ambiguity errors not showing all candidates in some cases.
- Made the `AbstractFieldAccess` and `FieldAssignment` classes `WildcardLambdaAware`. #452
- Nested methods are now `private` when no visibility modifier is specified. #461
- Removed the `ForStatement` class. #447
- Removed the `WildcardAccess` class.
- Replaced usages of `dyvil.io.FileUtils` with `dyvil.io.Files`. #446
- Updated the `ClassList.objectClasses` and `.objectClassInstanceFields` implementations.
- Updated the `ForEachStatement` class. #447
- Updated the `ForStatementParser` class. #447
- Updated the `ICall` class to use `WildcardLambdaAware`. #452

## Dyvil REPL v0.26.2

- Replaced usages of `dyvil.io.FileUtils` with `dyvil.io.Files`. #446

## Dyvil Property Format v0.16.1

- Replaced usages of `dyvil.io.FileUtils` with `dyvil.io.Files`. #446

## Dyvil GenSrc v0.9.5

- Replaced usages of `dyvil.io.FileUtils` with `dyvil.io.Files`. #446
