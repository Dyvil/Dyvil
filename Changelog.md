Dyvil v0.11.0
=============

- Added support for prefix and postfix `++` and `--` operators. #140
- Added support for prefixing method names with the `operator` keyword. #146
- Added support for generic Type Aliases. #147
- Closures can no longer be used as anything other than Lambda substitutes.
- Closures have multiple implicit variables now, named from `$0` to `$n` where n is the number of parameters.
- Closures can now be used anywhere a Functional Interface is required and are no longer bound to Lambda Types.

## Dyvil Library v0.11.0

- Added a basic Event API with Invariant and Covariant Dispatch support. #141
- Added the `dyvil.annotation.pure` Annotation to mark pure functions / functions without side effects.
- Added the `StringUtils.split(String, char)` method.
- Added `Predef.runnable(=> void)` for easy function → Runnable conversion.
- Added `Predef.callable(=> T)` for easy function → Callable conversion.
- Added `Predef.thread(=> void)` for easy function → Thread conversion.
- Updated `StringUtils.lineList(String)` to use proper Collection API method.
- Changed the StringUtils class from an interface to a utility class.
- Renamed `Predef.closure(=> T)` to `.function`.
- Removed `StringUtils.trimLineLength(String, int)`

## Dyvil Compiler v0.11.0

- Updated the Modifier System.
- Added a visibility check for Include Declarations. #144
- Added a callback to check if an expression has side effects.
- Simplified the CompoundCall class to work as a Factory Class for desugared expressions rather than an actual expression.
- Improved Intrinsic Operator Resolution.
- Improved Side Effect handling in Compound Operators
- Fixed a few type checking errors related to type variable parameters.
- Fixed Lambda Types being checked incorrectly in some cases.
- Fixed Interface Lists not allowing EOFs, leading to infinite loops for some class declarations.
- Fixed Variables captured as Fields (e.g. in Anonymous Classes) generating warnings about being unqualified without 'this'.
- Removed the non-typed variant of `IValue.writeExpression(MethodWriter, IType)`.
- Removed the `IValue.writeStatement(MethodWriter)` method.

## Dyvil REPL v0.6.1

- Fixed REPL Variables displaying duplicate and implicit modifiers.
- Fixed Exception Filtering not being applied for Void results.
- Fixed Exception Filtering not filtering the 'sun.misc.Unsafe.ensureClassInitialized' call.
- Fixed Exception Filtering not filtering Cause and Suppressed Exceptions.
- Fixed Type Aliases not being resolved in the REPL.

## Dyvil Property Format v0.3.0

- Added the FlatMapConverter, a DPF Visitor that converts the tree into a flat Map structure.

Dyvil v0.10.1
=============

## Dyvil Library v0.10.0

- Added the `List.subscript(Range[int])` method.
- Added the `List[T].subscript_=(Range[int], T[])` and `List[T].subscript_=(Range[int], List[T])` methods.
- Added the `Queryable.reduceOption(...)` method.
- Added the `BidiQueryable.reduceLeftOption(...)` and `.reduceRightOption(...)` methods.
- Updated the `List.fromArray` methods to take normal array parameters rather than being variadic.
- Updated the Array Classes to have more concise / idiomatic and efficient method implementations.

## Dyvil Compiler v0.10.1

- Unresolved Method Calls and Field Accesses are no longer reported as such if the receiver or any argument is not resolved.
- Fixed type mismatch JVM errors when an array of a primitive type is converted to an array of a wrapper type, which is now reported with a proper compiler error.
- Fixed If Statements with void return values used in Lambda Expressions causing Frame computation errors.
- Fixed Type Var Types generating unnecessary casts.
- Fixed Primitive Autoboxing combined with Primitive Widening Conversion causing JVM errors.
- Fixed Cast Operators generating 'Unnecessary Cast' warning markers where the cast is actually necessary.
- Fixed Import Aliases not working for methods.
- Fixed Method Receiver Match Checking using the generic class type rather than the non-generic one.
- Fixed Array and Varargs Method Match Resolution using incorrect values, leading to the wrong method being marked as the best match.
- Fixed Semicolon Inference working incorrectly with Wildcard Values.
- Fixed Nil Literals causing NullPointerExceptions during bytecode generation when they have not been properly typed.

## Dyvil REPL v0.6.0

## Dyvil Property Format v0.2.1

Dyvil v0.10.0
=============

- Added Partially Applied Functions using Wildcard Values in Methods.
- Added Qualified Types using the `package.package.TypeName` syntax.
- Number Literals with a trailing `L`, `F` or `D` only recognize these characters if they are not suceeded by another letter.
- Changed the Wildcard Value Syntax from `...` to `_`.

## Dyvil Library v0.9.0

- Added the `@UsageInfo` annotation.
- Added the `@dyvil.annotation.analysis.Contract`, `@NotNull` and `@Nullable` annotations and the `dyvil.Analysis` header.
- Added the `@DefaultValue` and `@DefaultArrayValue` annotations.
- Added the `Predef.run(=> any)`, `.run(any, any => any)`, `.use(any, any => void)` and `.with(any, any => any)` methods.
- Added mutable and immutable `TreeSet` implementations that use a backing `TreeMap`.
- Added the `Queryable.allMatch(E => boolean)` and `.exists(E => boolean)` methods.
- Added the `Map.allMatch((K, V) => boolean)` and `.exists((K, V) => boolean)` methods.
- Added several methods to find and return the first or last element matching a condition in Queryables and Maps.
- Added default implementations for `dyvil.collection.Map.containsKey(Object)` and `.containsValue(Object)`.
- Added missing `@NilConvertible` and `@ArrayConvertible` annotations to `IdentityHashMap`s and `IdentityHashSet`s.
- Converted the Utility Interfaces `dyvil.io.FileUtils`, `.WebUtils` and `dyvil.random.RandomUtils` to classes with private constructors.
- Implemented the `.toString()`, `.equals(any)` and `.hashCode()` methods for the `dyvil.util.Some` class.
- Improved the `@Deprecated` annotation by adding an optional Description.
- Renamed `dyvil/annotation/specialized.dyvil` to `Specialized.dyv`.
- Renamed `dyvil/lang/Null.dyvil` to `Null.dyv`.
- Moved `dyvil/lang/JavaUtils.dyh` to the `dyvil` package.

## Dyvil Compiler v0.10.0

- Re-added Closure (formerly Applied Statement Lists) Support.
- Overhauled the Formatting System which now uses a config file.
- Additional Marker Information is not also localized rather than being hard-coded Strings.
- Constructors that attempt to create an array of a non-reified generic type argument will now cause a compiler error.
- String Concatenation Chains that contain expressions returning a Void result will create an error marker.
- Added a warning for when the `dyvil.Lang` header cannot be resolved.
- Added Compiler Support for the `@Experimental` annotation.
- Added Compiler Support for the `@UsageInfo` annotation.
- Added the `dyvil.tools.compiler.ast.access.IReceiverAccess` class as a supertype of `dyvil.tools.compiler.ast.access.ICall`.
- Added the `IReceiverAccess.resolveReceiver()` method and implemented it in all subtypes.
- Added a callback to check if an expression has been resolved without errors, i.e. has a valid type.
- Added `IClass.getClassType()` to get a non-generic version of a classes' type'.
- Added `CastOperator.toString()` implementation.
- Added `ThisValue.toString()` implementation.
- Updated Field Assignment Resolution to mimic Field Access behaviour in regards to Setter methods and private access contexts.
- Updated / Improved Subscript Method Resolution to work in more cases and be more flexible.
- Updated Parameter Default Values to use Annotations (`@DefaultValue` and `@DefaultArrayValue`).
- Updated the MarkerMessages member. localizations.
- Improved Wildcard Literal and Nil Literal marker messages.
- Improved Lambda Type Checking.
- Improved String Builder Expression Conversion for Wildcard Values.
- Fixed Class Parameters being capturable.
- Fixed Lambda Type inference working incorrectly.
- Fixed Void Results being handled incorrectly by the `AbstractLMF` type checker.
- Fixed Semicolon Inference working incorrectly when the last token in the line is a symbol (like `.,;:`).
- Fixed Automatic Lambda Conversion working incorrectly in some cases.
- Fixed empty Statement Lists being compiled incorrectly when used as expressions.
- Fixed Intrinsics not being converted to the correct type after being compiled.
- Fixed Literal Conversion Expressions type-checking incorrectly with non-concrete types.
- Fixed Field Accesses checking the receiver type in a way that previously removed the receiver completely and added a proper error message.
- Fixed Method Override Return Type checking using non-concrete types.
- Fixed Tuple Type Variable resolution working incorrectly because of an erroneous assumption.
- Fixed Type Variable Types being type-checked incorrectly, allowing any type to be compatible with a Type Var Type in the local scope.
- Fixed Array Constructors causing compiler errors.
- Fixed Field Assignments without actual assignment values causing compiler errors.
- Fixed a parser error that was caused when unregistered operators ending with the `=` symbol were used.
- Fixed Invalid Import Statements causing Compiler Errors.
- Fixed Method Calls with Type Arguments but without Parameters ignoring the Type Arguments.
- Fixed Class Parameter indexes being set incorrectly.
- Fixed Named Type Resolution working incorrectly.
- Fixed Compiler Errors being caused when type-checking unresolved types.
- Fixed `WildcardValue.toString()` implementation for unbounded Wildcard Types.
- Fixed Field Assignments in Statement Lists being parsed incorrectly because they are treated as Variable Declarations in some cases.
- Fixed Compound Call Type Check failing because the method return type is always inferred to `void`.
- Fixed `IType.getSuperTypeDistance(IType)` causing a NPE for unresolved types.
- Refactored the `StatementList` class.
- Moved some methods from `AbstractClass` to `IClass` and implemented them in subclasses.
- Moved the Internal Annotations (Annotations used for Bytecode attributes, modifiers, special metadata, etc.) to the `dyvil.annotation._internal` package.
- Renamed `IType.equals(IType)` to `.isSameType`.
- Renamed `dyvil.tools.compiler.lang.lang.properties` to `MarkerMessages.properties`.
- Removed the `dyvil.tools.compiler.ast.expression.IValued` class.
- Moved Marker Level Properties from the MarkerMessages.properties file to a new resource and updated the `I18n` class accordingly.
- Type Structure Changes.
- AST Structure Changes.

## Dyvil REPL v0.6.0

- Added the `:complete` REPL command.
- Empty Commands and Commands starting with `:` are no longer recognized as such by the REPL.
- Improved the `:exit` (`:quit`, `:q`) REPL command.
- Improved Exception Stack Trace printing in the REPL.
- Fixed rare NPE when `CTRL-D` is inserted in the REPL.
- Fixed REPL Markers not being reported when the input was not parseable as an expression.

## Dyvil Property Format v0.2.1

Dyvil v0.9.0
============

- Added the `where` keyword.
- Added support for Literal Strings, prefixed with an `@` sign.
- Added support for negative exponents.
- Re-added Method Invocation Type Arguments.
- Trailing dots in number literals are now only interpreted as Floating Point Literals if the next character is a digit.
- Floating Point Literals without an explicit `F` or `D` suffix are now implicitly of type double.

## Dyvil Library v0.8.0

- Added the `Map.keys()` and `Map.values()` methods to simplify use in For Each Statements.
- Added the `Map.keyMapped(BiFunction)` and `.mapKeys(BiFunction)` methods.
- Added the `dyvil.annotation.Deprecated` and `.Experimental` annotations.
- Added the `dyvil.util.MarkerLevel` enum.
- Added the `closure(=> any)` method to `Predef`.
- Added the `$(char)` method to `Predef` to allow easier creation of char literals.
- Added several new `CMP` instructions.
- Added the `NULL` and `NONNULL` instructions.
- Added the `[X].subscript(Range<X>)` and `[X].subscript_=(Range<X>, X)` methods for all array types.
- Added the `String.subscript(Range<int>)` method.
- Added the `Predef.isNull(any)` and `.isNonNull(any)` methods.
- Added Apply Methods for `dyvil.util.Version`.
- Added the `StringConvertible` and `TupleConvertible` annotation to `dyvil.util.Version`.
- Added an import for the `dyvil.util.Version` class to the Lang Header.
- Added an import for the `dyvil.annotation.Intrinsic`, `.Native`, `.Transient` and `.Volatile` annotations to the Lang Header.
- Added the `ReflectUtils.getEnumConstants(Class)` method.
- Added the `ReflectUtils.newUnsafeString(char[])` method.
- Split the `ReflectUtils` class into several new classes, namely `dyvil.reflect.Caller`, `.EnumReflection`, `.MethodReflection`, `.ObjectReflection` and `.FieldReflection`.
- Updated the `Modifiers.METHOD_MODIFIERS` field.
- Updated primitive instance Intrinsics to be Infix methods.
- Updated / Fixed the Intrinsic annotations in the primitive classes.
- Made all Primitive toString() Methods Intrinsic.
- Improved the `TupleMap` and `ArrayMap` implementations by adding `.putInternal(K, V)` methods to their abstract base classes.
- Fixed `AbstractArrayMap.putNew(K, V)` causing an `ArrayIndexOutOfBoundsException`.
- Fixed `Int.previous()` returning the next integer (+ 1) instead of the previous one (- 1).
- Fixed `HalfOpenRange.last()` being off by one.
- Moved the primitive `##` (hash) methods from `Predef` to the respective primitive classes.
- Renamed `Map.mapped(BiFunction)` and `.map(BiFunction)` to `.valueMapped` and `.mapValues`.
- Renamed `ReflectUtils.unsafe` to `UNSAFE`.
- Renamed `List.apply(int, Object)` and `List.apply(int, IntFunction<Object>)` as well as their `ImmutableList` counterparts to `.repeat` and `.generate` for clarity.
- Renamed the `[X].apply(int, X)` and `[X].apply(int, => X)` methods to `repeat` and `generate`, respectively.

## Dyvil Compiler v0.9.0

- Overhauled the Intrinsic System.
- Added support for `INVOKE*`, `GET*`, and `PUT*` instructions in Intrinsic Annotations.
- Added support for `LDC`, `BIPUSH` and `SIPUSH` instructions in Intrinsic Annotations.
- Added Simple Intrinsic branch optimizations
- Added various checks for invalid modifiers and invalid combinations thereof for Classes, Fields, Methods and Properties.
- Added a cache to speed up `RootPackage.resolveInternalClass(String)`.
- Added `FieldAssign.toString()` implementation.
- Added `IfStatement.toString()` implementation.
- Added compiler support for the `dyvil.annotation.Deprecated` annotation.
- Implemented Bytecode Generation for Annotation values by introducing a new dynamic bootstrap method.
- Updated the MethodMatch / ConstructorMatch system to use dedicated MethodMatchList / ConstructorMatchList classes instead of Lists of MethodMatch and ConstructorMatch wrappers.
- Updated the Parameter Index System by separating local variable index and method signature index.
- Improved Lambda and Tuple Type inference and type conversion.
- Improved Overload Method Resolution for Varargs Methods, Constructors and Primitive Types.
- Fixed Compound Operators being parsed as if they were left-associative.
- Fixed String += Operator working incorrectly and being marked as an unresolvable method.
- Fixed `DyvilSymbols.toString(int)` working incorrectly for keywords.
- Fixed the Range Operator return type for `Rangeable` values.
- Fixed the Method Resolution System allowing for matches where the receiver type does not match the receiver value.
- Fixed the Method Receiver Type Mismatch error message.
- Fixed Annotation Parameter Access generating invalid bytecode output.
- Fixed Annotation Parameters being assignable without a compiler error.
- Fixed inner class resolution for external classes creating multiple IClass objects for the same class.
- Fixed Enum Annotation Values being decompiled incorrectly.
- Fixed NPE caused by Single-Quoted Strings in String Concatenation chains.
- Fixed empty Double-Quoted Strings being parsed incorrectly.
- Fixed Annotations with unresolved types creating two errors, one for the unchecked type and one for the type not being an annotation type.
- Fixed incorrect compilation for special / custom bytecode instructions.
- Fixed Compound Calls working incorrectly with Intrinsics.
- Fixed the generated code for `equals` and `hashCode` in Case Classes working incorrectly for doubles.
- Fixed `Util.toTime(long)` working incorrectly for long timespans.

## Dyvil REPL v0.5.0

- Added a check if a class has already been defined.
- Added the :debug command.
- Added the :variables command.
- Added the :methods command.
- Improved CTRL-D support.
- Improved method re-definition.
- Improved the REPL so that multiple REPL instances can be operated on separately.
- Fixed variables being printed with incorrect values when directly referenced from within the REPL.
- Fixed strange visibility errors in the REPL.
- Fixed If Statements without an action causing errors in the REPL.
- Fixed semantic error markers not being reported in the REPL.
- Fixed strange formatting in IDE consoles by using stdout and stderr within very short periods of time.

## Dyvil Propery Format v0.2.1

- Made commas in DPF Maps optional.

Dyvil v0.8.0
============

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
