# Dyvil v0.44.0

## Dyvil Library v0.44.0

- Added the `SourcePosition.FIRST_LINE` and `.FIRST_COLUMN` constants.
- Added the `SourcePosition.apply(line, column)` method.
- Fixed an infinite recursion issue caused by the `BytecodeDump` implementation.
- Improved the `SourcePosition.Base` implementation.
- Improved the `SourcePosition` implementation.
- Removed an unnecessary assertion in the `DyvilLexer.parseDoubleString` method.
- Removed the `FileUtils.java` and `MarkerList.java` classes.
- Removed the deprecated `Iterators.filtered` and `.mapped` overloads.
- Removed the deprecated `Marker.log` and `MarkerList.log` methods.
- The `SourcePosition.toPositionString` method now shows the inclusive end column.

## Dyvil Compiler v0.44.0

- Added an error diagnostic for invalid modifier combinations. #459
- Added an error diagnostic for non-private nested methods. #460
- Added support for the new marker style command-line arguments. #454
- Added support for the new marker styles. #454
- Cleaned up the `ICall` interface.
- Cleaned up the `IClassMetadata` interface.
- Cleaned up the `IClass` class.
- Cleaned up the `IParameter` interface.
- Fixed trailing space in the `ModifierUtil.accessModifiersToString` result.
- Improved the `FieldExpr.writeReceiver` implementation.
- Improved the `LambdaExpr.isType` implementation.
- Moved the `AbstractClass.getSignature` and `.getInterfaceArray` methods to `CodeClass` and made them private.
- Moved the `ICall.resolveField` method to the `AbstractFieldAccess` class.
- Moved the `IClass.getClassMetadata` method to `IClassMetadata`.
- Removed and inlined the `ICall.privateAccess` method.
- Removed the `IClass.getFunctionalMethod` method and replaced usages with `getClassMetadata().getFunctionalMethod()`.
- Removed the `IClass.getReceiverType` method.
- Removed the `IClass.getSignature` and `.getInterfaceArray` methods.
- Replaced a usage of `FileUtils` with `Files` in the `ICompilerPhase` implementation.
- Replaced usages of the deprecated `Iterators.{mapped, filtered}` overloads in the `ClassBody`, `ClassList` and `Deprecation` classes.

## Dyvil REPL v0.27.0

- Added command argument completions for the `debug`, `dump`, `help`, `library` and `load` commands. #466
- Added completions for command names and aliases. #466
- Added support for the new marker style command-line arguments. #454
- Added support for the new marker styles. #454
- Fixed backslashes being stripped from the input.
- Improved multiline input using a custom JLine `Parser`. #463
- Improved the `Colorizer` color scheme.
- Made the `Colorizer` class more robust.
- Pressing Tab at the start of a line now inserts a tab character. #463
- Syntax highlighting is now done during input. #463
- The `InputManager` implementation now uses JLine. #463

## Dyvil Property Format v0.16.1

## Dyvil GenSrc v0.9.5

# Dyvil v0.44.1

## Dyvil Library v0.44.1

* Fixed errors and warnings in Javadoc comments.

## Dyvil Compiler v0.44.1

* Fixed errors and warnings in Javadoc comments.

## Dyvil REPL v0.27.0

## Dyvil Property Format v0.16.1

## Dyvil GenSrc v0.9.5
