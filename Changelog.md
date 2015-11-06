Dyvil v0.8.0
============

- Remove minus signs `-` as part of number literals.

## Dyvil Library v0.7.0
- The primitive wrapper classes are now `Serializable`.
- The Option, Some and None classes are now `Serializable`.
- The Tuple2 and Tuple3 classes are now `Serializable`.
- Made all `Collection`, `Map` and `Matrix` classes `Serializable`.
- Made the `dyvil.math.Complex` class `Serializable`.
- Added the `dyvil.util.Version` class.
- Updated `EnumMap` constructors.
- Updated `EnumMap.copy()` implementations.
- Updated the `FileUtils` class to fix a few rare errors.
- Updated the `dyvil.IO` Header.

## Dyvil Compiler v0.8.0

- Case Classes and Object Classes are now (implicitly) `Serializable`.
- String Append Chains and String Interpolation literals now use more efficient `append()` calls for String Literals.
- String Interpolation Literals now make use of precomputed String length.
- Added EOF markers for unfinished constructs.
- Updated the way `ClassMetadata` is resolved.
- Updated `-` + number literal handling in Pattern Matching.
- Improved the errors reported for invalid Import and Using Declarations.
- Fixed a compiler error being produced on malformed expressions (such as those containing invalid keywords).
- Fixed `methodName` in `LiteralConvertible` annotations not being handled correctly.
- Fixed markers for unnecessary `is` operators being errors instead of warnings.
- Fixed constant folding working incorrectly for prefix methods.
- Fixed a rare error causing the receiver of instance calls to be removed, which causes errors during bytecode generation.
- Renamed `PackageImport` to `WildcardImport`.
- Renamed `SimpleImport` to `SingleImport`.
- Renamed some methods in `dyvil.tools.compiler.transform.CaseClasses`.

## Dyvil REPL v0.4.0

- Directly Accessing a REPL variable now causes that variable to be printed, rather than a new one being generated.
- Fixed Anonymous Classes in the REPL being unaccessible.
- Fixed Result Class Dumping for statements (`void` results).
- Fixed exceptions in the `toString()` implementation of results not being caught when printing REPL variables.
- Fixed Constructor Calls causing errors in the REPL in some cases.
- Fixed constant folding not being applied in the REPL.

## Dyvil Property Format v0.2.0

- Fixed an EOF after an identifier causing the DPF Parser to fail.
- Added support for Builders in the DPF Parser and AST.
- Adjusted visibility in the DPF AST classes.
- Renamed `dyvil.tools.dpf.ast.DPFFile` to RootNode.
- Renamed `dyvil.tools.dpf.DPFParser` to Parser.

Dyvil v0.7.0
============

- Added support for String Interpolation in Double-Quoted String Literals without `@` symbols.
- Single-Quoted Char Literals can now be used as String Literals.

## Dyvil Library v0.6.0

- Added the dyvil.tools.parsing Package and moved various core classes from the Compiler.
- Fixed `LinkedList.iterator.next` not causing `NoSuchElementException`s.

## Dyvil Compiler v0.7.0

- Moved many parsing-related classes to the `dyvil.tools.parsing` package, which is now part of the Standard Library.
- Updated and moved the Semicolon Inference algorithm.
- Fixed a JVM bytecode error that was caused when certain combinations of If/Else and Return Statements where used.
- Fixed `StringIndexOutOfBoundsException`s caused by markers at the end of files.
- Fixed infinite parsing instead of proper error reporting with missing `}` to end class bodies.
- Removed the `IPattern.writeJump(MethodWriter, int, Label)` method and subclass implementations.
- Removed special handling for `\$` escape sequences in the Dyvil Lexer.
- Removed the `--pstack` compiler argument.

## Dyvil REPL v0.3.1

- Fixed NPE when the REPL encounters an EOI / End of Input.
- Fixed custom classes defined in the REPL being unavailable from REPL statements.

## Dyvil Property Format v0.1.0

- Initial Release.
- Added the `DPFParser` class for easy DPF parsing.
- Added various visitor classes for use in both AST and parser.
- Added a basic AST implementation.
- Added a basic Printer.

Dyvil v0.6.0
============

- Added support for the new infix operator behavior with compound assignments.
- Improved Apply Syntax to work without parenthesis.

## Dyvil Library v0.5.0

- Renamed the `AbstractArrayMap.ArrayEntry` class to `ArrayMapEntry`.
- Renamed the `dyvil.Utils` header to `Utilities` and made use of nested Include Declarations.
- Added default methods for Map -> Array conversions.
- Added `BigInteger` and `BigDecimal` to the `dyvil.Math` header.
- Added the abstract base-classes for `IdentityHashMap` and `IdentityHashSet`.
- Added the immutable `IdentityHashMap` and `IdentityHashSet` implementations.
- Added new classes to the `dyvil.Collections` header.
- Added some constants for default capacities in various Array-based Set, List and Map implementations.
- Added `Collection.intersects(Collection)`.
- Improved the implementation of `MutableMap.entryMapped(BiFunction)` and `.flatMapped(BiFunction)`.
- Improved the implementation of `MutableMap.mapEntries(BiFunction)` and `.flatMap(BiFunction)`.
- Improved the implementation of `AbstractArrayMap.forEach(Consumer)` by making use of the `ArrayMapEntry` class.
- Improved the implementation of `ArrayList.flatMap(BiFunction)`.
- Improved the `immutable.HashMap` constructors.
- Improved the implementation of `HashSet.toJava()`.
- Improved and fixed the implementation of `AbstractMapBasedSet.toJava`
- Improved the implementation of `LinkedList.immutable`.
- Improved the `ensureCapacity(int)` methods in the `AbstractHashMap` and `AbstractHashSet` classes.
- Made the `ReflectUtils.modifiersField` final.
- Formatted the `dyvil.Lang` header.
- Fixed HashMap, HashSet, IdentityHashMap and IdentityHashSet iterators being able to remove elements from Immutable sets / maps.
- Fixed mutable.HashSet constructors discarding the loadFactor.
- Fixed the implementation of `Set.^=`.
- Fixed the implementation of `SingletonList.flatMapped(Function)` returning an incorrect result.
- Fixed some typos in the documentation.
- Fixed `Tuple4.toString()` causing a `StackOverflowError`.

## Dyvil Compiler v0.6.0

- Added `ConstructorCall.toString()` implementation.
- Added `FieldInitializer.toString()` implementation.
- Updated the Expression IDs in the `IValue` class.
- Updated the Type IDs in the `IType` class.
- Updated the mechanism that converts annotation parameters to compile-time constants.
- Fixed tokens that are not parsed not being reported as syntax errors.
- Fixed compiler errors caused by invalid Annotations.
- Fixed parameterized this and super being parsed incorrectly.
- Fixed `ThisValue.toString()` and `SuperValue.toString()` implementations.
- Fixed nested anonymous classes generating files with incorrect file names.
- Fixed the last statement in a statement list not having access to the variables in `withType`.
- Fixed Applied Statement Lists being parsed incorrectly.
- Fixed Applied Statement Lists working incorrectly by temporarily removing their special behavior.
- Fixed variable capture over multiple levels / lambdas working incorrectly and causing JVM errors.
- Fixed certain Lambda Expressions being parsed incorrectly within statement lists, e.g. `list.flatMapped { i => [ i, i ] }`.
- Fixed tuple type checking working incorrectly in contexts where `Object` is expected.
- Fixed tuple type checking working incorrectly in contexts where `Entry` or `Cell` is expected.
- Fixed incorrect Single-Abstract-Method resolution.
- Fixed incorrect parameter name decompilation.
- Fixed For statements without variables causing compiler errors.
- Fixed empty varargs parameter lists causing compiler errors upon type checking.
- Fixed withType being called multiple times with varargs parameters, leading to errors e.g. with Ranges.
- Fixed various errors related to the Void Value `()`.

## Dyvil REPL v0.3.0

- Added the `:dump` command that allows defining a directory for dumping temporary REPL classes.
- Updated Version Information in the `:version` command.
- Improved the REPL synthetic Result Class naming scheme.
- Improved the `REPLParser` class by inheriting the `ParserManager` default implementation from the Compiler.
- Fixed inner class loading in the REPL.
- Fixed an error that was caused by semantically invalid method definitions in the REPL.
- Removed the Semicolon after REPL variables.

Dyvil v0.5.0
============

- Added special type treatment for the names `Tuple` and `Function`.
- Added prefix and postfix operator precedence.
- Added support for primitive type promotion.

## Dyvil Library v0.4.0

- Moved the MathUtils.sinTable to a holder class for lazy evaluation.
- Added the AbstractHashSet base class for Hash Set implementations.
- Added the ImmutableHashSet class for hash-based implementations of immutable sets.
- Added a JUnit test class for the Dyvil Collections Framework.
- Improved `Predef.println` implementations.
- Improved the `ImmutableArrayList.flatMapped(Function)` implementation.
- Improved the implementation of various methods in the ImmutableHashMap class by introducing helper methods to the AbstractHashMap class.
- Improved copying for HashMaps by introducing a constructor that takes an AbstractHashMap argument.
- Fixed various bugs in the Collection classes.
- Fixed `MathUtils.sqrt(int)` causing exceptions for large values.

## Dyvil Compiler v0.5.0
- Renamed `ParserUtil.isTerminator2(int)` to `isExpressionTerminator` and improved the implementation.
- Improved `ParserUtil.isTerminator(int)`.
- Improved Token toString implementations.
- Improved the declaration and implementation of `IParserManager.report`.
- Improved the way type errors are reported.
- Improved and documented `ExpressionParser`.
- Improved Operator Definition syntax with named attributes.
- Improved Operator parsing in combination with parenthesis.
- Improved `this` resolution and error handling in static contexts (including the REPL).
- Fixed Compiler Errors being produced in certain situations when the parser tries to parse a compound assignment.
- Fixed Compound Assignments working incorrectly in Lambda Expressions.
- Fixed Resolution Errors being reported twice in Statement Lists.
- Fixed the action of enhanced For statements being resolved incorrectly in a way such that the special variables in the for statement (`$iterator`, `$index`, ...) are not available.
- Fixed the `$iterator` variable in For-Iterable statements having the incorrect type `Iterable[T]` instead of `Iterator[T]`.
- Fixed Case Classes generating invalid bytecode.
- Fixed compiler error with invalid arguments in `@Retention` and `@Target` annotations.
- Fixed annotations not being pretty-printed with classes.

## Dyvil REPL v0.2.0

- Improved synthetic REPL variable names. They now have more meaningful names that are directly based on the type of the variable.
- Improved REPL result computation in anonymous classes to support `return` statements.

Dyvil v0.4.0
============

- Added support for Unicode identifiers and symbols.
- Added Map Expressions and Map Types.
- Added support for the `Option` type syntactic sugar using `Type?`.

## Dyvil Library v0.3.0

- Moved `dyvil.math.*Vector` classes to a new package.
- Added support for custom method names for all `LiteralConvertible` annotations and expressions.
- Added the `measureMillis(=> void)` and `measureNanos(=> void)` methods to `Predef`.
- Added the `repeat(int)(=> void)` curried method to `Predef`.
- Added the `dyvil.Math` header.
- Added `Rangeable` as a subclass of `Ordered` and made the `next()` and `previous()` methods abstract.
- Improved the `Ordered` operator implementations

## Dyvil Compiler v0.4.0
- Added various missing `ASTNode.toString()` implementations.
- Improved primitive types being used as generic type parameters.
- Improved operator precedence in compound assignments.
- Improved boxing and unboxing and implicit type conversions.
- Fixed incorrect Bytecode generation in some edge cases.
- Fixed Cast Operators without type declaration causing a compiler error.
- Fixed Field Assignments working incorrectly and producing JVM errors.
- Fixed Compound Assignment generating invalid bytecode and causing JVM errors.
- Fixed Variables in anonymous classes being captured incorrectly.
- Fixed automatic Lambda Conversion working incorrectly with generics.
- Fixed Return Statements being compiled incorrectly in some cases.
- Fixed method signatures not being generated in all cases where they are required.
- Fixed Tuples being compiled incorrectly when used in a context where `Object` or a super type of the Tuple class is required.

## Dyvil REPL v0.1.2

- Added support for recursive method definitions.
- Added a debug output for the launch time.
- Improved the version information that is printed on launch.
- Improved the output for defined classes, methods and import declarations.
- Fixed REPL Variable assignment working incorrectly.
- Fixed Anonymous Classes with unresolved constructors being reported as such twice within the REPL.

Dyvil v0.3.0
============

## Dyvil Library v0.2.0

- Added Half-Open Ranges.
- Added missing FloatArray.range method.
- Added missing ShortArray.range method.
- Added Boolean.compareTo and made it implement Comparable.
- Added ImmutableList.apply(count, repeatedValue) and .apply(count, generator).
- Inlined the implementations of Ordered.compareTo for all Number classes.
- Fixed ObjectArray.range generating an invalid output.
- Fixed Ordered.next and .previous for Number subclasses being implemented incorrectly.
- Removed String Ranges.

## Dyvil Compiler v0.3.0

- Added argument-based return type inference for Constructors.
- Added support for Half-Open Ranges using the '..<' operator.
- Improved Method Overload resolution system for Generic Types.
- Improved Array and String ForEach bytecode output.
- Fixed the action blocks of ForEach statements being discarded.
- Fixed ForEach statements over strings using incorrect variable names.
- Fixed Direct Reference Lambdas generating invalid bytecode in some cases.
- Fixed withType not being called on Method Call / Field Access receivers.
- Fixed Intrinsic Primitive method calls being compiled incorrectly.
- Fixed Type Match calculation for Lambda Expressions with Object working incorrectly.
- Fixed missing parameter values in annotations not being reported as errors.
- Fixed parameter values in annotations with incompatible types being reported incorrectly.
- Fixed Match Case Conditions not being type-checked properly.
- Fixed classes with multiple abstract methods being usable as FunctionalInterface SAM types.
- Fixed Lambda Expressions causing Compiler errors in some edge cases.

## Dyvil REPL v0.1.1


Dyvil v0.2.0
============

- Added support for Type (Use) Annotations.

## Dyvil Library v0.1.1
- Added the List.removeFirst and .removeLast methods.
- Added the EmptyRange class.
- Updated the dyvil.collection.JavaCollections and dyvil.collection.JavaMaps classes.
- Moved Basic Operators to the Lang Header.

## Dyvil Compiler v0.2.0
- Added support for custom method names in the NilConvertible annotation.
- Added support for interfaces as anonymous class bases.
- Updated the abstract / override method resolution system.
- Improved Windows Compatibility.
- Improved Lambda compilation for direct method references.
- Improved the way Symbol / Dot Identifiers work.
- Miscellaneous improvements to the Type System, including raw types.
- Fixed 'this' reference capture in nested lambda expressions causing JVM errors.
- Fixed various Lambda-related bugs.
- Fixed Header Files being generated for all compilation units.
- Fixed invalid bridge methods being generated in interfaces.
- Miscellaneous improvements, bugfixes and changes.

## Dyvil REPL v0.1.1
- Added support for multi-line input.
- Fixed commands being handled incorrectly.

Dyvil v0.1.0-ALPHA
==================

- Alpha Test Release

## Dyvil Library v0.1.0

## Dyvil Compiler v0.1.0

## Dyvil Compiler v0.1.0
