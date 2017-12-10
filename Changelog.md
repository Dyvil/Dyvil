Dyvil v0.38.0
=============

## Dyvil Library v0.38.0

- Added covariance annotations for the `T` type parameter of the `Option` and `Some` classes.
- Added group-based generic `MathUtils.sum` and `.product` methods.
- Added spec memoization in the `dyvilx.tools.gensrc.Builtins` class.
- Added the `dyvil.math.group.Group*` and `.Ring*` classes.
- Added the `dyvil.math.group.GroupImplicits` class.
- Added the `dyvilx.tools.gensrc` package and classes.
- Fixed an `AbstractArrayList.addAll` implementation bug.
- Made the `dyvilx.tools.gensrc.Specialization.subscript` method return type optional.
- Removed the `dyvil.Strings` and `dyvil.Utilities` headers.
- Renamed the `dyvil.JavaUtils` header to `JavaCollections`.
- Updated all templates to new GenSrc syntax.
- Updated the `BigIntegerOperators` and `BigDecimalOperators` classes.
- Updated the `Builtins`, `Runner` and `Template` classes to allow relative file resolution to source dirs.
- Updated the `dyvil.lang.internal.None` and `.Null` classes to remove redundant `final` modifier.
- Updated the `dyvilx.tools.gensrc.Template` class to change the meaning of the `name` field.

## Dyvil Compiler v0.38.0

- Added an error diagnostic when Nested Methods are used before their declaration.
- Added the `DataMemberParser.PARSE_VALUE` flag.
- Added the `IMethodConsumer` interface.
- Added the `SourceHeader.setNameFromFile` method.
- Added the `test_dir` config option for the working directory of the `test` command.
- Fixed Import Parser producing syntax errors when imports are placed withing parentheses (possible in GenSrc).
- Fixed Imports not supporting Implicit Value Resolution. #384
- Fixed Inner Class resolution for External Classes.
- Fixed `StringInterpolationExpr` constructor without arguments.
- Fixed `abstract` modifiers being inferred too early, causing issues with Field Properties.
- Fixed `include` and `exclude` options being ignored without trailing `s`.
- Fixed a false positive Type Error in Super Expressions by allowing generic types as the target type of the super expression.
- Fixed a possible `IllegalArgumentException` in the `AnnotationUtil` method.
- Fixed an issue that caused Nested Methods to be compiled incorrectly in non-static contexts.
- Fixed an issue that caused a verify errors for lambda expressions targeting methods with reified type parameters. #379
- Fixed an issue that caused external type parameters referencing other type parameters to be resolved incorrectly.
- Fixed an issue that disabled Error Diagnostics for unresolved Import Declarations.
- Fixed false positive Invalid Instance Check warning.
- Fixed incorrect type checking for Implicit Class Parameter resolution.
- Fixed incorrect type checking of generic types against non-generic (raw) types.
- Generic Arguments are now locked before implicit resolution.
- Implicit Parameters are now available for Implicit Resolution in the method body. #380
- Improved Error Diagnostics for missing arguments in method calls.
- Improved `DyvilFileType` API.
- Improved allowed modifiers for different class kinds.
- Improved and fixed an issue in the `ArgumentList.resolveMissing` method implementation.
- Made the `ImportDeclaration` class implement `IImportConsumer`.
- Made the `TypeParameterList` class implement `Iterable<ITypeParameter>`.
- Renamed the `IContext.hasStaticAccess` method to `isStaticOnly`.
- Split the `MemberKind.CLASS` constant into one member kind for each kind of class.
- Statement Lists no longer produce Variable Shadowing Warnings for variables with the GENERATED modifier.
- Subclasses of `DyvilCompiler` can now create their own `CompilerConfig`.
- The `instance` field of an Object Class with the `implicit` modifier now also has that modifier.
- The test runner now spawns a new process to run the test.
- Unqualified access to nested methods no longer produces a warning.
- Updated APIs in the `DyvilCompiler` and `CompileConfig` classes.
- Updated Implicit Resolution in class bodies to support Implicit Objects. #382
- Updated the `CompilerConfig` and the `ConfigParser` system to allow `list+=value` arguments.
- Updated the `MethodParser` API.

## Dyvil REPL v0.24.0

- Updated Implicit Resolution in the `REPLContext` class to support Implicit Objects. #382
- Updated the `LibraryCommand` class to adapt to `CompilerConfig` API changes.
- Updated the `REPLContext` class to support all class member kinds.

## Dyvil Property Format v0.15.2

- Removed redundant `final` modifier from the `IgnoreVisitor` class.
- Updated all templates to new GenSrc syntax.

## Dyvil GenSrc v0.9.0

- Rewrote GenSrc to use the compiler API and compiled templates for source generation. #352

Dyvil v0.37.0
=============

- Added the Optional Cast Operator `as?`. #360

## Dyvil Library v0.37.0

- Added deprecated null coalescing overload to the `Optionals` class.
- Added missing method return types in the `Queryable` and `Polynomial` classes.
- Added the `@SwitchOptimized` annotation.
- Added the `Opcodes.AUTO_LOAD` and `Opcodes.AUTO_STORE` constants.
- Converted the `dyvil.source.*Source` classes to Dyvil.
- Deprecated the `\` (int div) operator and replaced it with the `_/`(read floordiv) operator.
- Made the Primitive Wrapper `apply` methods `implicit`.
- Minor changes to the `Big*Operators`, `Random` and `Qualifier` classes.
- Renamed the `Source.getText` method to `text` and the `Source.getLine` method to `line`.
- Renamed the specialized `*Array.flatMapped` method for primitives to `flatMappedTo*`.
- Renamed the specialized `*Array.mapped` method for primitives to `mappedTo*`.
- Updated some Dyvil files to use the Optional Cast Operator.
- Updated the `Console` class to add more ANSI escape sequences.

## Dyvil Compiler v0.37.0

- Added a bytecode optimization for Enum Patterns. #378
- Added an optimization for Optional Casts in Binding If Statements.
- Added automatic conversion from Field Patterns to Enum Patterns where possible. #378
- Added bytecode optimization for Unapply Patterns using the `@SwitchOptimized` annotation. #378
- Added compiler support for Optional Cast Operators. #360
- Added support for the `AUTO_LOAD` and `AUTO_STORE` opcodes.
- Added the `IMethodConsumer` interface.
- Added the `PrimitiveType.fromFrameType` method.
- Cleaned up some comments in `toCompoundAssignment` methods.
- Cleaned up the `DyvilFileType` class.
- Cleaned up the `InstanceOfOperator` class and associated diagnostic messages.
- Cleaned up the `SideEffectHelper` class.
- Fixed Method Resolution on Receivers with type `any`.
- Fixed Variable Formatting for For Each Statements.
- Fixed `ForEachStatement.withType` implementation not accepting `void` correctly.
- Fixed a `StackOverflowError` during compilation caused by merging `StringInterpolationExpr`s incorrectly.
- Fixed an issue that caused Match Expressions to ignore the type from `withType`.
- Fixed code class compilation using the legacy extension `.dyvil` for debug info.
- Implicit Resolution now allows builtin type-based conversions (e.g. implicit nullable unwrapping).
- Improved Parsing and Warnings for Compiler Arguments.
- Improved diagnostic errors for method calls with unresolved types.
- Improved the Cast Operator Implementation and cleaned up the file.
- Improved the `Pattern.min` and `.max` default implementations.
- Match Expressions now check for cases with duplicate values. #377
- Optimized Access to Final Variables where an additional variable is required.
- Optimized String Pattern Compilation by removing unnecessary type-checks. #378
- Refactored Pattern Compilation by renaming the `Pattern.writeInvJump` method to `writeJumpOnMismatch` and removing the `matchedType: IType` parameter. #378
- Refactored the `IType.convert` system.
- Renamed the `IFileType` class to `FileType`.
- Renamed the `IPatternList` class to `PatternList`.
- Renamed the `IPattern` class to `Pattern`.
- Renamed the `Pattern` class to `AbstractPattern`.
- Updated primitive default constants.
- Updated the `ICompilationUnit` and `SourceHeader` classes to improve diagnostic output.
- Updated the `MethodParser` API.
- Updated type checking for Literal Values and Literal Patterns to improve primitive conversion support.
- Updated usages of `Source` to adapt to library API changes.

## Dyvil REPL v0.23.0

- Added support for the `load` command line argument. #374
- Added the `:load` command for loading REPL input from a file. #374
- Improved output for the `:help` command.
- Updated and improved the `Colorizer` class.
- Updated usages of `Source` to adapt to library API changes.

## Dyvil Property Format v0.15.1

- Added missing method return type in the `BinaryReader` class.
- Updated some Dyvil files to use the Optional Cast Operator.

## Dyvil GenSrc v0.8.0

Dyvil v0.36.0
=============

- Added support for Default Constructor Attributes. #365
- Added support for `override` class parameters. #366
- Keywords that are not modifiers can now be used as Parameter Labels. #361
- Slightly altered Semicolon Inference behaviour.

## Dyvil Library v0.36.0

- Added Type Parameters to the Equality Operators in the `Objects` class.
- Added a deprecated overload for optional operators with non-optional operands. #372
- Added a field for the initial seed to the `JavaRandom` class.
- Added the `/%` (divmod) operator for ints and longs.
- Added the `ACC_VARARGS` flag to the `Modifiers.CONSTRUCTOR_MODIFIERS` bitmask.
- Added the `Function.Of1. **` operator.
- Added the `Modifiers.INTRINSIC` field.
- Added the `SourcePosition.isNeighboring` and `Positioned.expandPosition` methods.
- Added the `dyvil.annotation.AutoMangled` annotation. #368
- Added the `dyvil.lang.Representable` class.
- Added the `dyvil.math.Polynomial` class to model polynomials of doubles.
- Added the `dyvil.math.Rational` class.
- Added the `dyvil.util.Timing` and `.Blackhole` classes.
- All internal type annotations are now runtime-reifiable.
- Cleaned up the `Tokens` and `BaseSymbols` classes.
- Converted the `Name` class to Dyvil.
- Converted the `dyvil.random.MinRandom` and `.MaxRandom` classes to Dyvil.
- Converted the `dyvil.util.Qualifier` class to Dyvil.
- Converted the remaining Annotation Classes from the `dyvil.annotation.internal` package to Dyvil.
- Fixed an implementation bug in the `dyvil.random.Random.nextLong()` method.
- Fixed the `Array.copy(int)` method implementations causing AIOOB errors.
- Fixed unnecessary force-unwrapping in the `dyvilx.event.CovariantEventBus` class.
- Improved Marker Level formatting.
- Improved the `dyvil.math.MathUtils.sum`, `.product` and `.!` (factorial) methods.
- Improved the `dyvil.util.Qualifier` implementation and API.
- Made the `ObjectArray.toString`, `.hashCode` and `.equals` methods 'deep' by default.
- Markers are now sorted by position, then by level and then by message.
- Removed extra semicolons from the `JavaRandom` and `InvariantEventBus` classes.
- Removed the `AgentUtil` class.
- Removed the `String.apply(reflecting:)` method from the `dyvil.lang.Strings` class.
- Removed the `dyvil.lang.Misc` class.
- Removed the `dyvil.random.JavaRandoms` class.
- Renamed the `Name.startWith` method to `startsWith`.
- Replaced the `Marker.getMarkerType(): String` method with `getLevel(): MarkerLevel`.
- Split the `LexerUtil` class into `StringLiterals` and `CharacterTypes`.
- Updated Array classes to use the correct apply methods.
- Updated the `Lang` header and removed the `Tuple` alias for `Tuple.Of1`.
- Updated the `Map` and `ObjectRange` classes.
- Updated the `Modifiers` class to allow `override` on class parameters.
- Updated the `Primitives` class to provide `apply` methods for primitive wrapper classes.
- Updated the `dyvil.lang.Primitives` template to match Dyvil code style.

## Dyvil Compiler v0.36.0

- Added an error diagnostic for class names where the qualified name contains '$'.
- Added error diagnostics when accessing a field or method with an unresolved type.
- Added support for Default Constructor Attributes. #365
- Added support for `override` class parameters. #366
- Added support for the `AutoMangled` annotation. #368
- Added the `AbstractMemberParser.parseAttribute(...)` method.
- Added the `BracketMatcher` class.
- Added the `ThisExpr(IClass)` constructor.
- Ambiguous calls involving implicit conversion on the receiver will now be reported properly.
- Annotations and Modifiers are now both stored in the `AttributeList` class.
- Apply Assignments now use the name `apply_=` instead of `update` (affects resolution for this sugar).
- Case Classes now fully support Override Class Parameters. #366
- Dropped support for Automatic Method Name Mangling. #368
- Fixed Integer Overflow resulting from TABLESWITCH calculations.
- Fixed Optional Unwrap Operators working incorrectly with Optionals of Primitives.
- Fixed Parameter Order in a `ThisExpr` constructor.
- Fixed String Pattern Matching working incorrectly with Single-Quoted Strings.
- Fixed a few compiler errors caused by Unapply Patterns with unresolved types.
- Fixed an error that allowed `null` values to be assigned to non-nullable types.
- Fixed an issue that caused Overload Resolution to depend on Import Order.
- Fixed an issue that caused the constructor call parser to pick up field properties as anonymous classes.
- Fixed an issue that caused virtual calls to be generated which access invisible classes. #370
- Fixed an issue where `InternalType`s pointing to classes available as source would fail resolution.
- Fixed an issue where properties and field properties would not be considered for method duplicate checking.
- Fixed compiler error caused by `Deprecated` annotations using single-quoted string literals.
- Fixed compiler error for annotated types where the annotation type is unresolved.
- Fixed many issues related to (Type) Annotations and Modifiers.
- Implementations of the `Parser.reportErrors()` method now consider the parent parser.
- Improved Ambiguous Method Call error diagnostic information.
- Improved Marker Level property reading.
- Improved Override Behaviour for Methods with Custom Bytecode Names. #369
- Improved Semicolon Inference Rules.
- Improved String Pattern Matching with nullable values.
- Improved Tuple Type Formatting for `Tuple.Of1`.
- Improved diagnostic error message for unresolved methods.
- Lifted a limitation on Wildcard Imports that would only allow them to be used on packages with an `import` declaration.
- Merged the `DyvilUnitParser` and `DyvilHeaderParser` classes into the `SourceFileParser` class and cleaned up the implementation.
- Moved and renamed the `AnnotationList` class to `d.t.c.ast.attribute.AttributeList`.
- Moved and renamed the `IAnnotated` interface to `d.t.c.ast.attribute.Attributable`.
- Moved old but remaining annotation / modifier classes to the `d.t.c.ast.attribute` package.
- Moved the `DyvilKeywords`, `DyvilSymbols` and `SemicolonInference` classes to the `parser` package.
- Non-static Access to static Methods or Fields now produces an error diagnostic. #371
- Overhauled the Annotation / Modifier System.
- Primitive Boxing and Unboxing is now done using the default methods from the wrapper classes.
- Refactored method override parameter label checking.
- Removed some methods from the `Util` class.
- Removed the `ClassParameterSetter` class.
- Removed the `ParserUtil` class by moving methods to other classes.
- Renamed the `ApplyMethodCall` class to `ApplyAccess` and the `UpdateMethodCall` class to `ApplyAssignment`.
- The Header / Source File Parser now requires declarations to be separated by semicolons.
- The `OperatorParser` and `PackageParser` classes no longer consume the trailing semicolon.
- Updated many APIs related to annotations / modifiers.
- Updated the `CaseClasses` class to use correct `ObjectArray` methods.
- Updated the `ClassMetadata` class.

## Dyvil REPL v0.22.0

- Fix Header Declaration Parsing bug #367.
- Updated REPL classes to adapt to compiler API changes.
- Updated the `CompleteCommand` class to adapt to library API changes.
- Updated the `REPLVariable` class to adapt to library API changes.

## Dyvil Property Format v0.15.0

- Fixed Compilation Errors in the `StringInterpolation` and `StringValue` classes.
- Updated the `RootNode` and `d.t.d.a.value.Value` classes to adapt to `Name` API changes.
- Updated the `d.t.dpf.converter.string.Parser` class to match Dyvil code style.

## Dyvil GenSrc v0.8.0

- Updated the `StringValue` and `GenSrcLexer` classes to adapt to library API changes.

Dyvil v0.35.0
=============

- Added Multi-Binding If Statements. #358

## Dyvil Library v0.35.0

- Added the `Collection.iteratorContains` method.
- Converted the `Map` class to Dyvil.
- Fixed missing nullability annotations on `Map.get` and `.subscript`.
- Moved the `d.c.impl.MapKeys` and `MapValues` classes to `d.c.view`.
- Removed the `AUTO_POP2` constant from the `Opcodes` class.

## Dyvil Compilter v0.35.0

- Added support for Multi-Binding If Statements. #358
- Added type cache invalidation for `IfStatement`s.
- Binding If Statements now check for non-nullable values.
- Binding If Statements now support variables of primitive types.
- Cleaned up `OptionalChainAware` code. #359
- Field Access and Assignment are now Optional Chain Aware. #359
- Fixed an error during `MapType` class initialization.
- Fixed an issue that caused field access inlining to work incorrectly in some cases.
- Fixed an issue that made doubly-Nullable Types (Nullable Types of Nullable Types) possible.
- Fixed an issue where re-assigning a variable could cause invalid stack frames.
- Fixed an issue with Primitive left-hand operands in Null Coalescing Operators. #357
- Fixed optionals of primitive types being compiled incorrectly.
- Improved the `OptionalUnwrapOperator` class. #359
- Overhauled Optional Chain handling using AST transformations and Multi-Binding If Statements. #357 #359
- Removed support for the `AUTO_POP2` opcode.
- Removed the `Frame.popAndGet` method.
- Removed the `NullCoalescingOperator` class. #359

## Dyvil REPL v0.21.1

- Improved welcome message.
- Updated the autogenerated class name prefixes and packages.

## Dyvil Property Format v0.14.1

- Updated some method implementations using Optional Chaining or Null Coalescing.

## Dyvil GenSrc v0.7.0

Dyvil v0.34.0
=============

- Added support for implicit parameter values. #353
- Added support for Optional Binding in If Statements. #356

## Dyvil Library v0.34.0

- Added the `Objects.implicitly<T>(implicit T)` method.
- Converted the `Set` class to Dyvil.
- Fixed an issue that caused the `**` operator for Longs to work incorrectly for large values.
- Moved the `dyvil.tools.parsing.Name` class to `dyvil.lang`.
- Moved the `dyvil.tools.parsing.name.Qualified` class to `dyvil.util`.
- Removed deprecated Array class methods.
- Removed deprecated concat methods from the `Strings` class.
- Removed redundant `abstract` modifiers in the `Collection` class.
- Renamed the `dyvil.tools` package to `dyvilx.tools`.
- The Power Operators `**` for Ints and Floats no longer return the respective extended type, but the operand type itself.
- Updated the `Array.dyv.dgt` template.
- Updated the `Tuple.dyv.dgt` file.

## Dyvil Compiler v0.34.0

- Added an error diagnostic for Binding If Statements with Variables of primitive types.
- Added parser support for binding If Statements. #356
- Added support for Implicit Parameters in the Method Matching System. #353
- Added support for Implicit Variable Resolution for Statement Lists.
- Added support for `implicit` instance and static fields.
- Added the `AbstractFieldAccess` class as a superclass for `FieldAccess` and `FieldAssignment`.
- Class Parameters can now be `implicit` or `explicit`. #353
- Cleaned up the If Statement Parser.
- Cleaned up the `IfStatement` class.
- Dropped support for the Colon Statement Syntax.
- Dropped support for the old Initializer Call syntax.
- Fixed an issue that caused Enums to generate bytecode that could not be read by some decompilers.
- Fixed an issue that caused Implicit, Varargs and Default Values to be unavailable for Postfix Calls and Constructor Calls without argument lists.
- Fixed an issue that caused Method Type Parameters to be resolved incorrectly.
- Fixed formatting for super-interfaces of interfaces using `implements` instead of `extends`.
- Fixed wildcard types being formatted as `+any!` when they should be `_`.
- Implemented bytecode generation for Binding If Statements. #356
- Minor improvements to the Method Match system.
- Moved some `is<MODIFIER>` methods to the `IModified` class.
- Moved the `dyvil.tools` package to `dyvilx.tools`.
- Removed unnecessary `final` modifiers of some AST classes.
- Removed unused Initializer Call diagnostics.
- Replaced the `IContext.getImplicit()` method with `resolveImplicit(IType)`.
- The `implicit` Modifier can now by applied to Variables, Fields and Method Parameters. #353
- Updated the `AndPattern` class.
- Updated the `ClassDeclarationParser` to allow a line break between the class name and the generic argument list.
- Updated the `DummyValue` class.

## Dyvil REPL v0.21.0

- Added support for Implicit Variable Resolution.
- Renamed the `dyvil.tools` package to `dyvilx.tools`.

## Dyvil Property Format v0.14.0

- Updated references to tools classes.

## Dyvil GenSrc v0.7.0

- Renamed the `dyvil.tools` package to `dyvilx.tools`.

Dyvil v0.33.0
=============

- Added support for Enums. #86
- Added support for External Parameter Labels. #349

## Dyvil Library v0.33.0

- Added the `ConcatIterator` and `MappingIterator` classes.
- Added the `MathUtils.sum` and `.product` methods.
- Added the `strictfp` annotation.
- Added two overloads for the `+` operator for concatenating Strings with a single Character.
- Converted some `d.annotation.internal` classes to Dyvil.
- Converted the `CharUtils` and `StringConversions` classes to Dyvil.
- Converted the `Deprecated`, `Experimental`, `Reified` and `MarkerLevel` classes to Dyvil.
- Converted the `ImmutableCollection` and `MutableCollection` classes to Dyvil.
- Converted the `UsageInfo` class to Dyvil.
- Converted the `Version` class to Dyvil.
- Fixed an error that caused the `Strings.repeated` method to fail with inputs > 2.
- Fixed an issue that caused Hex Number Literals containing uppercase A, B or C to be parsed incorrectly.
- Fixed an issue with the `SourcePosition.toString` method.
- Updated Dyvil files to use new Initializer Call Syntax. #351
- Updated annotation classes.
- Updated the `Array.dyv.dgt` file to make use of External Parameter Labels.
- Updated the `Function` and `PartialFunction` classes.
- Updated the `Modifiers` class and added new constants.
- Updated the `Modifiers` class.
- Updated the `ScalarMath`, `SourcePosition` and `Tuple` classes.

## Dyvil Compiler v0.33.0

- Added a diagnostic warning for inner classes without an explicit `static` modifier.
- Added an error for lambda expressions whose return type cannot be inferred.
- Added diagnostics and formatting for the new Initializer Calls. #351
- Added parser support for the new constructor initializer call syntax. #351
- Added support for  a `$VALUES` field and a `values` method in enums. #86
- Added support for Enum Constant Declarations. #86
- Added support for Enum Constants. #350
- Added support for Enum Patterns in Pattern Matching. #86
- Added support for External Parameter Label (De)Compilation.
- Added support for External Parameter Labels. #349
- Added support for Tuple Expressions with Argument Labels. #252
- Added support for `_` as external and internal Parameter Labels. #349
- Added support for constructor transformation and generation in enums. #86
- Added the `ArgumentList.addAll` method.
- Added the `ArgumentList.concat` and `.insert` methods.
- Added the `EnumClassMetadata` class. #86
- Added the `EnumConstant` class.
- Added the `IClassMetadata.writeStaticInitPost` and `.writeClassInitPost` methods.
- Added the `NullableType.isNullable` method.
- Added the `ParameterList.insert` method.
- Deprecated the old initializer call syntax.
- Dropped support for C-Style (Type then Name) Declarations. #348
- Dropped support for C-Style member formatting in `toString` methods.
- Dropped support for `enum const` declarations. #350
- Enum Classes now generate `from`/`valueOf` methods for ordinal and name. #86
- Fixed If statements without an `else` body being usable as expressions.
- Fixed a potential NPE when parsing `Retention` annotations.
- Fixed an issue that caused Boolean operators to work incorrectly in some cases.
- Fixed an issue that caused Enum Values to work incorrectly in some contexts.
- Fixed an issue that caused Match and If Expressions to be compiled incorrectly when containing `return` or `throw` statements.
- Fixed an issue that caused `MatchExpr.isResolved` to work incorrectly.
- Fixed an issue that caused inner classes to not have a `static` modifier when compiled.
- Fixed an issue that caused transient Class parameters to be treated as variadic.
- Fixed an issue with the `ArgumentList.insert` method.
- Fixed compiler error caused by `@Target` decompilation.
- Fixed incorrect Field Pattern compilation.
- Improved Constructor Initializer Call resolution.
- Improved Functional Method caching.
- Moved some compilation code to the `ClassBody` class.
- Optional Unwrapping Operators no longer cause an error when used as annotation constants.
- Overhauled the `StringInterpolationExpr` class to allow `StringConcatExpr` behaviour. #347
- Properties and Field Properties are now available for Functional Method discovery.
- Refactored intrinsic operator AST classes.
- Refactored the `MemberParser` class into the `FieldParser`, `MethodParser` and `ConstructorParser` classes.
- Refactored various methods dealing with Type or Argument List formatting.
- Removed the `IClassBody` interface and replaced it with `ClassBody`.
- Removed the `StringConcatExpr` class. #347
- Removed the error message for Operators without Operands.
- Removed warnings for nested `object`, `interface` and `trait` classes without a `static` modifier.
- Renamed the `PropertyParser` class `PropertyBodyParser`.
- String Interpolation Expressions can now merge adjacent constants. #347
- String Interpolation Expressions now generate calls to `String.concat` if there are two `String` arguments. #347
- The compiler now uses the new `Modifiers.*_CLASS` constants.
- Updated and fixed Equality Comparisons for Field Patterns and Case Classes.
- Updated references to the substitute modifier annotations `transient`, `strictfp`, `volatile` and `native`.
- Updated the Override Checking and Name Mangling code to use External Parameter Labels. #349
- Updated the `ArgumentList.get` method signatures.
- Updated the `ClassMetadata` class.
- Updated the reference class for the Dyvil library.

## Dyvil REPL v0.

- Improved Completions for Types and Extension methods.
- Updated the `REPLParser` class.

## Dyvil Property Format v0.13.1

- Updated Dyvil files to use new Initializer Call Syntax. #351
- Updated usages of the deprecated `+` operator.

## GenSrc v0.6.1

- Added missing Localizations.
- Fixed an error caused by unresolved specs in Import directives.

Dyvil v0.32.0
=============

## Dyvil Library v0.32.0

- Added Nullability Annotations for all Token Classes.
- Added custom `unapply` methods for all Tuple types.
- Added the Function Composition `*` operator overload.
- Added the `++` String Concatenation Operator.
- Added the `Modifiers.GENERATED` field.
- Added the `Some.unapply(Option<T>!)` method.
- Added the `_` and `#` symbols to the `BaseSymbols` class.
- Changed the `Collection.toArray(Class)` signature.
- Changed the `reducer` calling convention for `BidiQueryable.foldRight`, `.reduceRight` and `.reduceRightOption` methods.
- Converted the `BidiQueryable` class to Dyvil.
- Converted the `Collection` class to Dyvil.
- Converted the `Queryable` and `SizedIterable` classes to Dyvil.
- Converted the `Stack`, `Queue` and `Deque` classes to Dyvil.
- Converted various `parsing` classes to Dyvil and moved and renamed them.
- Deprecated the `+` Operators for String Concatenation.
- Extracted the `Lexer` base class from the `DyvilLexer` class.
- Extracted the `TokenList` class from the `TokenIterator` class.
- Fixed an issue that caused the `!=` operator to work incorrectly for nullable lhs.
- Removed deprecated methods from the `Assert` and `Output` classes.
- Removed the `DateExtensions` class.
- Removed the `IToken.hasNext` and `.hasPrev` methods.
- Removed the `UnknownType` class.
- Updated Dyvil files to use the latest style recommendations.
- Updated all `.dgt` files to the new GenSrc syntax.
- Updated parameter labels in collection class methods.
- Updated the `Range` class and subclasses.
- Updated the `TokenIterator` class.

## Dyvil Compiler v0.32.0

- Added a syntax deprecation warning for C-style declarations (type before name).
- Added support for Property Getter and Setter annotations. #345
- Added the `AnnotationList.addAll` method.
- Added the `AnnotationList.toInlineString` method.
- Added the `IAnnotated.setAnnotations` method.
- Case Classes now generate two static unapply methods. #167
- Cleaned up the AST classes for jump statements (`goto`, `break`, `continue`).
- Dropped support for the `auto` Type. #209
- Fixed an NPE in the `TypeChecker` class.
- Fixed an issue that caused Closure Lambdas with type ascripted Parameters to parse incorrectly.
- Fixed an issue that caused Deprecation Warnings to be omitted for compiler intrinsic methods.
- Fixed an issue that prevented Wildcard Types from correctly compiling `@NonNull` annotations.
- Implemented Override Caching for External Methods.
- Method override warnings are now suppressed for auto-generated Class Parameter Properties.
- Reduced duplicate compilation errors for compiler-generated members like class parameter properties or property accessor methods.
- Removed some unused `IHeaderUnit` methods.
- Removed the `IType.asReturnType` method and fixed an issue with the `WildcardType.asParameterType` method.
- Unapply Patterns now use the `unapply` methods of case classes and custom classes. #167
- Updated path resolution rules for Config Files.
- Updated the `DummyValue` class.
- Updated the `TupleType.MAX_ARITY` constant.
- Updated the compiler for parsing API changes.
- Updated usages of `TokenIterator`.

## Dyvil REPL v0.20.0

- Updated the REPL for parsing API changes.
- Updated the `Colorizer` class to adapt to parsing API changes.
- Updated usages of `TokenIterator`.

## Dyvil Property Format v0.13.0

- Updated Dyvil files to use the latest style recommendations.
- Updated `PrimitiveValue.dyv.dgt` to new GenSrc syntax.
- Updated the `converter.string.Parser` class.

## Dyvil GenSrc v0.6.0

- Added `<-` as a reserved symbol.
- Added an error for `delete`/`undefine` directives with a body.
- Added support for List Expressions using `[]`.
- Added support for the Range operator in For Directive Argument Lists.
- Added support for the `define`, `local`, `delete`, `undefine` and `name` directives.
- Added the `BasicDirective` class.
- Added the `BlockParser` class.
- Added the `DirectiveParser` class.
- Added the `ExpressionListParser` class.
- Added the `ExpressionList` class.
- Added the `Expression` and `ExpressionParser` classes.
- Added the `ForDirectiveParser` class.
- Added the `GenSrcSymbols` class.
- Added the `LongValue`, `DoubleValue`, `StringValue` and `VarAccess` classes.
- Added the `NamedDirective` class.
- Added the new `GenSrcLexer` class.
- Implemented basic functionality of the `ExpressionParser` class.
- Removed the `Parser` class.
- Removed unused `Util` methods.
- The `Scope` class now operates on `Directive`s instead of raw Strings.
- Updated GenSrc for parsing API changes.
- Updated the `DirectiveList` class.
- Updated the `Directive` class.
- Updated the `ForDirective` class and removed the `ForEachDirective` class.
- Updated the `IfDirective` class and added the `IfDirectiveParser` class.
- Updated the `ImportDirective`, `IncludeDirective` and `ScopeDirective` classes.
- Updated the `LiteralText` and `ProcessedText` classes.
- Updated the `Template` class to use the new parser.

Dyvil v0.31.0
=============

- Dropped support for the `nil` literal. #337
- Fixed an issue that made prefix increment and decrement operators unusable. #339

## Dyvil Library v0.31.0

- Added a system property for the `BytecodeDump.dumpDirectory` path.
- Added documentation for the `Deprecated`, `Experimental` and `UsageInfo` classes.
- Added more `@NonNull` annotations in Map classes.
- Added the `Deprecated.forRemoval: String` field.
- Converted the `InvalidReferenceException`, `ImmutableException` and `MatchError` classes to Dyvil.
- Converted the `MathUtils` class to Dyvil.
- Fixed an issue in the Dyvil Lexer that caused non-decimal number literals to be parsed incorrectly.
- Fixed incorrect `inline` modifiers in the `Strings` class.
- Removed all `@LiteralConvertible.FromColonOperator` annotations from Map classes.
- Removed the Shift Operators with two `long` parameters, deprecated in v0.30.0.
- Removed the `Deprecated.Reason.UNSPECIFIED` enum constant.
- Removed the `LiteralConvertible.FromNil` annotation.
- Removed the `StringUtils` class.
- Updated dependants of `MathUtils`.
- Updated several library classes to adapt to language changes.
- Updated the `AnnotationProxyFactory` and `AnonymousClassLMF` classes.
- Updated the `FunctionConversions` and `PartialFunction` classes.
- Updated the `Strings` class.
- Updated the `Types` class to fix a type error.

## Dyvil Compiler v0.31.0

- Added a bytecode optimization for redundant CHECKCAST instructions.
- Added an Intrinsic for the Optional Unwrap and Force Unwrap operators.
- Added colon-based Explicit This Type syntax (`this: Type`).
- Added i18n support for `Deprecated.Reason`s and `Experimental.Stage`s.
- Added support for Casts in combination with Wildcard → Lambda Conversions.
- Added support for Type Alias Overloading. #334
- Added the `IOverloadable` interface and updated the `MatchList` and `Candidate` classes.
- Default Parameter Values no longer have to be annotation constants and can now be arbitrary expressions.
- Default Parameters now use a special modifier and are compiled to synthetic methods. #342
- Dropped support for `nil` expressions. #337
- Explicit This Types are now properly formatted.
- Fixed an issue that caused Type Signatures to be missing from bytecode for generic array types.
- Fixed an issue that caused external annotations to be resolved incorrectly.
- Fixed an issue that caused non-static methods to type-check receivers incorrectly.
- Fixed decompilation for the `Primitive` annotation working incorrectly with nullable types.
- Fixed some issues with the type system that caused various problems.
- Fixed undeterministic Union and Intersection Type Erasure.
- Improved Static and Implicit Method resolution for Union Types.
- Improved the `ArgumentList` API.
- Map Expressions now check for keys with side effects and produce warnings.
- Map Expressions now write expressions in the order in which they appear in source (alternating keys and values).
- Null checking is now done via a call to `Object.getClass` instead of a custom method.
- Refactored Wildcard → Lambda Conversion.
- Refactored many array+size fields into proper `TypeList` and `TypeParameterList` classes. #335
- Refactored the `AnnotationList` class. #335
- Refactored the `ArrayExpr`, `TupleExpr` and `MapExpr` classes to use `ArgumentList`s instead of raw arrays. #335
- Refactored the `IAnnotated` class.
- Refactored the `WildcardType` class to extend `TypeDelegate`.
- Removed many usages of `IType.asReturnType()`.
- Removed the `IArguments` interface in favor of `ArgumentList`.
- Removed the `ICase` interface in favor of the `MatchCase` subclass.
- Removed the `IParameterList` interface and replaced it with the `ParameterList` subclass.
- Static Methods can now use explicit this types that differ from the enclosing class. #338
- The `AbstractClass` class now uses a `TypeList` instance for interfaces instead of a raw array.
- Updated and improved Type Inference for Lambda Expressions.
- Updated dependants of `dyvil.math.MathUtils`.
- Updated method resolution to disallow unqualified access to Infix Methods. #339
- Updated the `Deprecation` class.
- Updated the `IContext.resolveTypeAlias` method.
- Updated the `UnionType` and `IntersectionType` classes and added a common superclass.
- `LiteralConvertible.*` annotations can now specify parameter labels with their `methodName` parameter. #336

## Dyvil REPL v0.19.0

- Fixed an issue that caused the Complete Command to not include `import` declarations in the resolution scope.
- Removed some unused code for constant handling in the `REPLVariable` class.
- Updated the REPL to adapt to compiler API changes.

## Dyvil Property Format v0.12.0

- Added the `dyvil.DPF` header.
- Templatized the primitive `Value` subclasses.
- Updated the `DPFSyntax` class.
- Updated the `Expandable` class.
- Updated the `ListValue` and `MapValue` classes.
- Updated the `Value` class to improve Array and Map Literal support.

## Dyvil GenSrc v0.5.0

- It is now possible to register and resolve from multiple source roots.

Dyvil v0.30.0
=============

- Added Enum Access Expressions. #329
- Added support for the Optional Chaining operators `?.` and `??`. #328
- Added syntax for Varargs Parameters with non-array types. #333

## Dyvil Library v0.30.0

- Added an `Object`-only `Types.arrayType<T>(Class<T>)` method.
- Added the `Tuple.OfN` class for tuples of arbitrary size.
- Added the `dyvil.math.ScalarMath` class.
- Converted the `dyvil.collection.range.{ halfopen, closed }.ObjectRange` classes to Dyvil (generated).
- Deprecated Shift Operators with two `long` operands.
- Fixed an issue that caused a string index error when a marker is on the last line of a `TextSource`.
- Fixed an issue where marker printing would sometimes cause errors.
- Fixed incorrect `float` `**` operator behaviour.
- Greatly improved Marker Printing by using a new modern format.
- Made Wrapper → Primitive conversion functions `implicit`.
- Renamed `dyvil.lang.*Extensions` classes to `*s`.
- The `Simple*Ref` classes are no longer `case` classes because their equality and hashcode should be identity-based.
- Updated several `dyvil.lang` class to use new Dyvil code conventions.
- Updated some classes to adapt to language changes.
- Updated the `AbstractBitSet`, `EmptyRange`, `Range`, `None`, `Some`, `Option` and `IASTNode` classes.
- Updated the `Complex` class.
- Updated the `Function` and `Tuple` classes.
- Updated the `Intrinsic` annotation, the `Optionals` class and the `Lang` header for upcoming optional chaining.
- Updated the `Optionals` class.
- Updated the `Rangeable` class.

## Dyvil Compiler v0.30.0

- Added AST nodes for Optional Chain `?.` and Null Coalescing `??` operators. #328
- Added special handling for poly expressions in the type inference algorithm.
- Added support and diagnostics for Array Literal Convertible Varargs Parameter Types. #333
- Added support for Enum Access Expressions. #329
- Added the `IArguments.convertValue(...)` method.
- Added the `IValue.isPolyExpression` method and implemented it in `LambdaExpr` and `Closure`.
- Cleaned up the `Closure` class.
- Cleaned up the `IArguments` implementations.
- Cleaned up the `ITypeList` and `ITypeParametric` interfaces and subtypes.
- Deprecated Method Type Ascription with `:` in favor of `->`.
- Duplicate and Override Checking for Methods is now performed in `CHECK_TYPES`.
- Fixed Class Expressions working incorrectly for Array Types of Type Parameters.
- Fixed a compiler error that was caused by recursive type parameter definitions. #319
- Fixed an NPE cause by `CodeMethod`s accessing the `thisType` field.
- Fixed an issue that caused Case Class' Apply Methods to be compiled incorrectly.
- Fixed an issue that caused Implicit Conversion Search for Method Resolution to use incorrect scoping rules.
- Fixed an issue that caused inline methods with `long` or `double` parameters to produce compiler errors.
- Fixed an issue that caused operator resolution for ternary operators to fail in some cases.
- Fixed an issue that caused some methods with the same name to be skipped from override checking.
- Fixed an issue that caused subtyping rules for type parameters to be incorrect.
- Fixed an issue that caused type checking to fail in some cases involving double optional types.
- Fixed an issue that disabled implicit type conversions for type parameters in some cases.
- Fixed an issue that would incorrectly report an error for Type Parameters in certain contexts.
- Fixed two related issues that caused `this` and `super` expressions to have incomplete types.
- Implicit Conversions can now be applied to subexpressions like Array Literal Elements, Tuple Elements, etc..
- Improved Formatting for Class and Type Operators.
- Improved Method signature formatting for parameters without a name.
- Improved Type Inference and Type Checking for Tuple and Colon Expressions and Tuple Patterns.
- Improved field, property and parameter formatting.
- Improved the conversion for Method Call → Class Access for generic method calls without an argument list.
- Made the `ArgumentList` and `NamedArgumentList` classes ready for upcoming non-array varargs types.
- Method Override Checking now warns about mismatching parameter labels.
- Moved some packages into the `ast.expression` package.
- Parameter Names that cannot be reified from bytecode are now null instead of a generated name with `par#`.
- Parameter References are no longer reported as a compile-time error. #331
- Parameters now internally support a missing / `null` Name.
- Refactored most Data Member (Variable, Field, Parameter, ...) classes.
- Reified Type Parameter synthetic parameters can now be captured.
- Removed the `SingleArgument` and `EmptyArguments` classes.
- Renamed the `FieldInitializer` class to `VariableStatement`.
- Renamed the `IMethod.checkOverride(...)` method to `overrides`.
- Updated Marker Printing.
- Updated Parameter and other Data Member classes.
- Updated formatting methods to use `->` instead of `:`.
- Updated the `EnumValue` class. #329
- Updated the `FieldAccess` and `ClassAccess` classes.
- Updated the compiler for library name changes.

## Dyvil REPL v0.18.0

- The REPL now stylizes definition output with syntax highlighting.
- Updated Marker Printing.

## Dyvil Property Format v0.11.0

- Added model interfaces for all Node Elements, `Value` and `Builder`.
- Updated misc. DPF classes to use correct parameter labels.
- Updated some classes to make use of Optional Chaining.

## Dyvil GenSrc v0.4.1

- Fixed a compilation error in the `Template` class.

Dyvil v0.29.0
=============

- Added the `explicit` keyword and parameter modifier. #325
- Added the `private protected` visibility modifier. #145
- Combining the `abstract` and `static` modifiers no longer results in an error. #317
- Removed support for the `dynamic` type. #323

## Dyvil Library v0.29.0

- Added method definitions for concatenation operators in `StringExtensions`.
- Added method definitions for increment, decrement and reference operators in the `dyvil.lang.References` class.
- Added method definitions for shortcut AND and OR boolean operators `&&` and `||` in `BooleanExtensions`.
- Added method definitions for the varargs spread operator `...` in `Array` classes.
- Added special equality and inequality operators with `null` arguments.
- Added the `EXPLICIT` modifier flag. #325
- Added the `Intrinsic.compileCode` field and constants.
- Added the `Optionals.unsafeInit<T>: T` method.
- Added the `Tuple.Of1` class.
- Added the `dyvil.lang.ClassExtensions` class for improved type reflection.
- Added the `dyvil.tools.parsing.source.Source` classes.
- Added the `dyvilx.tools.BasicTool` class.
- Cleaned up the `token` classes.
- Converted all `dyvil.event` classes to Dyvil.
- Converted the `dyvil.collection.range.halfopen.*` and `.closed.*` classes to Dyvil / DGT.
- Converted the `dyvil.io.Console` class to Dyvil.
- Deprecated the Boolean `^` operator as it is semantically equilvalent to the `!=` operator.
- Fixed an issue that caused markers to log incorrectly or cause runtime errors in some cases.
- Fixed invalid template syntax in the `dyvil.collection.Range` template.
- Increased the max arity for Tuples and Functions to `20`.
- Merged the `ObjectArray.java` class into the `Array.dyv.dgt` class.
- Moved the `dyvil.event` classes to `dyvilx.event`.
- Moved the classes from `dyvilx.lang.model.type` to `dyvil.reflect.types`.
- Moved tuple type aliases to the `Lang` header.
- Removed the `dyvil.Tuples` header.
- Removed the `dyvil.lang.ObjectExtensions.classOf` and `typeOf` methods.
- Renamed the `NULL` and `NONNULL` opcodes to `IS_NULL` and `IS_NONNULL`.
- Replaced the `dyvil.Reflection` header with `dyvil.Reflect`.
- The `*Array.equals`, `.hashCode` and `.toString` and the corresponding `ObjectArray.deep*` methods now accept nullable parameters.
- Updated Nullity Annotations in some `Map` subclasses.
- Updated `Marker` logging.
- Updated some `Modifiers` flags.
- Updated the `BigIntegerOperators` and `BigDecimalOperators` classes.
- Updated the `ClassExtensions` class to intrinsify `dynamicClass` and fix and issue with the `arrayType` methods.
- Updated the `Complex` class and added additional operator overloads.
- Updated the `DyvilLexer` class to use columns.
- Updated the `ICodePosition` interface to use columns instead of absolute indices.
- Updated the `Output.dyv.dgt` file to make use of `explicit`.
- Updated the `dyvil.Math` header.
- Updated the value of `Modifiers.PACKAGE`.
- Updated various modifier flags.

## Dyvil Compiler v0.29.0

- Added compiler support for the `Intrinsic.compilerCode` parameter.
- Added compiler support for the `private protected` visibility modifier. #145
- Added support for improved qualified package access. #327
- Added support for the `explicit` keyword. #325
- Added the `ModifierParser` class and moved some methods from the `ModifierUtil` class.
- Argument Lists now check for `explicit` parameters. #325
- Cleaned up Method Call Resolution by removing the `ICall.checkArguments` method.
- Cleaned up the `Library` classes.
- Cleaned up the `ReferenceOperator` class.
- Cleaned up the `ReferenceType` classes.
- Cleaned up the `StringConcatExpr` class.
- Default Modifiers are now dependent to the kind of member. #318
- Diagnostic Error and Warning Markers now display the associated key to indicate the kind.
- Disabled Override Checking for Static Final Methods.
- Final Methods in Interfaces now generate valid bytecode.
- Fixed Lambda Expressions annotating their generated methods as `synthetic`.
- Fixed a minor operator resolution issue.
- Fixed an error in the statement list parser that caused lambda headers to be parsed when there is a function declaration in the first line.
- Fixed an issue that caused Case Class Patterns to throw errors during bytecode generation.
- Fixed an issue that caused Class Operators to be incorrectly treated as constant values. #321
- Fixed an issue that caused Union and Intersection Types to work incorrectly with primitives.
- Fixed an issue that caused `char` to be inferred for Single-Quoted Literals in some contexts (mainly in place of type variables).
- Fixed an issue that caused annotations on implicitly unwrapped nullable types to be decompiled incorrectly.
- Fixed an issue that caused binding patterns to generate invalid bytecode when a `*SWITCH` instruction is used.
- Fixed an issue that caused bytecode generation to fail with an error in some cases involving `CharValue`s.
- Fixed an issue that caused implicitly unwrapped optionals to work incorrectly with type inference in some cases.
- Fixed an issue that caused invalid Stack Map Frames to be generated between `NEW` and `INVOKESPECIAL <init>` instructions.
- Fixed an issue that caused super type members to be inaccessible without an explicit `this.` expression.
- Fixed an issue that caused the argument index finder for named argument lists to return incorrect results.
- Fixed an issue that caused verification errors when assigning the result of an expression with type `null` to a Nullable reference.
- Fixed the `CodeClass.getFullName` implementation using `$` instead of `.`.
- Fixed the subtyping relation for `null` and Nullable Types being slightly incorrect in regards to Union and Intersection Types.
- Fixed type alias and decompiled generic type resolution issues.
- Fixed type checking with explicit receiver / `this` types being broken.
- Fixed variables leaking out of `match` Expression `case`s with binding patterns.
- Fixed various issues related to the reference operator `&`.
- Improved Method Overloading to support Overload-By-Receiver for methods with explicit Receiver Type.
- Improved checks for disallowed modifiers.
- Improved the error message for invisible member access.
- Members without any visibility modifiers now automatically have the `Modifiers.PACKAGE` modifier flag.
- Method Resolution no longer falls back to field resolution if the list of candidates is non-empty but ambigous.
- Modifier Decompilation is now checked by the `ClassFormat.readModifiers(int)` method.
- Moved the code for method override visibility checking to the `ModifierUtil` class.
- Override Checking now also includes Static Methods. #317
- Refactored Modifier stringification for decompiled flags.
- Refactored the Modifier Compilation system.
- Removed a special case where a Subscript Access would be converted to an Array automatically.
- Removed support for the `dynamic` type. #323
- Removed support for the `package protected` Modifier.
- Removed support for the `sealed` keyword and modifier.
- Removed the `IValue.resolveOperator(...)` method and invocations.
- Removed the `NullCheckOperator` class.
- Renamed the `ModifierSet.check(...)` method to `resolveTypes` and updated callees.
- Statement Lists are no longer parsed as a closure when the first line contains `let` or `var` keywords.
- Static Interface Methods without an implementation now automatically infer the `abstract` modifier.
- The default type parameter bound is now `Object!`.
- Unresolved Import Declarations now produce an error.
- Updated `*Parameter` constructors to take `ICallableMember callable` arguments for the surrounding method or constructor.
- Updated `ICompilationUnit` subclasses to use `FileSource`s.
- Updated `ModifierUtil` methods and callees.
- Updated checks for visibility and access modifiers in traits and interfaces. #318
- Updated code generation for `abstract static` methods. #317
- Updated compiler references to reflection Type classes.
- Updated the `DyvilCompiler` class to use the `BasicTool` class.
- Updated the `IField.writeAnnotations` method.
- Updated the `IntrinsicData` interface.
- Updated the `ModifierList.checkMethodModifiers` method and moved it to the `ModifierUtil` class.
- Updated the `ParameterListParser` class to allow varargs ellipsis after Type Ascriptions.
- Wildcard Types no longer generate useless `@NonNull` annotations.

## Dyvil Property Format v0.10.0

- Fixed compilation errors in the `Expandable` and `MapValue` classes.
- Updated the `ListValue`, `MapValue` and `StringInterpolation` classes implementation-wise.
- Updated the `Value`, `Builder` and `DPFSyntax` classes.
- Updated the `Value`, `Builder` and `DPFSyntax` classes.

## Dyvil REPL v0.17.4

- Fixed an issue that caused a runtime error when attempting to use the `complete` command with a package type.
- Fixed an issue that caused method definitions in the REPL to override previous definitions incorrectly.
- Fixed the `Extension Methods` section of the `:complete` command output containing `static` but non-`infix` methods.
- Updated the `REPLClassLoader` to use correct runtime class names.

## Dyvil GenSrc v0.4.0

- Added support for specialization-specific template markers and errors. #326
- Added support for syntax errors and warnings when parsing template files. #326
- Added support for the `#include` directive. #324
- Added support for the `#scope` and `#endscope` directives. #324
- Moved the code for default replacements `GEN_NOTICE` and `TIME_STAMP` to the `TemplateScope` class.
- Overhauled the Template / Specialization system to use a cached AST of directives.
- The `#define` and `#local` directives now apply replacements to their value argument.
- Updated the `GenSrc` class to use the `BasicTool` class.
- Updated the template loader and parser to use the `FileSource` class from the `parsing` library.

Dyvil v0.28.0
=============

- Added support for Optional and Implicitly Unwrapped Optional Types. #298
- Added the bottom type `none`. #310
- Binding Patterns with `var` and `let` can now specify the type using ascription syntax. #311
- Repurposed the `const` keyword as a variable kind indicator similar to `let` and `var`. #312

## Dyvil Library v0.28.0

- Added Nullity annotations for all Java classes.
- Added primitive unwrap methods with nullable parameters.
- Added the `!` and `??` operators for optionals.
- Added the `Output.print(*)` methods.
- Added the `Output.print(Object!..., String =, String =)` method.
- Added the `dyvil.lang.None` and `dyvilx.lang.model.type.NoneType` class. #310
- Added the `dyvil.lang.Optionals.unsafeUnwrap<T>(T?): T` method.
- Added the `dyvil.lang.Optionals` class and moved related methods from `ObjectExtensions`.
- Added the unsafe unwrap operator `!!`.
- Converted `dyvil.collection.Range` and most `dyvil.collection.range` classes to Dyvil.
- Converted all primitive Array classes to Dyvil.
- Converted all `dyvil.ref` classes to Dyvil.
- Converted most `dyvil.annotation` classes to Dyvil.
- Converted the `PrimitiveObjectArray` class to Dyvil.
- Deprecated all `Output.println(*)` methods.
- Fixed a compilation error in the `dyvil.lang.Primitives` class.
- Fixed bytecode and runtime errors in the `ReferenceFactory` class.
- Fixed the pointer assignment operator `*_=` working incorrectly for `ObjectRef`s.
- Intrinsified the unwrap operator `!` by adding an `unwrapNullCheck` helper method.
- Moved a common replacements to the `Base.dgs` spec file.
- Moved the `dyvil.lang.Null` and `.None` classes to the `dyvil.lang.internal` package. #310
- Updated the `@Nullable` and `@NotNull` annotation classes.
- Updated the `AbstractBitSet` class.
- Updated the `AutoPrinter` class.
- Updated the `I18n` and `StringExtensions` classes.
- Updated the `StringExtensions` class.
- Updated the `dyvil.collection.Entry` class to fix an infinite recursion issue.
- Updated the `dyvil.util.None` class to make use of the `none` bottom type. #310
- Updated the equality operators in the `dyvil.lang.ObjectExtensions` class to support nullable values.
- Updated various usages of `const` as a modifier.

## Dyvil Compiler v0.28.0

- Added the `OptionalType` and `ImplicitOptionalType` classes. #298
- Added additional checks to ensure non-null fields are not default-initialized to `null`.
- Added references to `@Nullable` and `@NotNull` to the `AnnotationUtil` class.
- Added support for LOAD and STORE instructions in Intrinsic Annotations.
- Added support for the builtin `none` type via the `NoneType` class. #310
- Added the `CompleteCommand.toString()` implementationl.
- Added the `IBuiltinType` class as a common super type for `AnyType` and `NullType`.
- Added the `IPattern.isWildcard` method.
- Added the `IType.extract(Class)` method family to allow dynamic `IType` object casts.
- Added the `IType.withAnnotation(IType, IAnnotation, TypePath)` method and directed usages of `IType.withAnnotation(IType, IAnnotation, TypePath, int, int)` to it.
- Added the `IType.writeAnnotations(IType, TypeAnnotatableVisitor, int, String)` method and directed usages of `IType.writeAnnotations(TypeAnnotatableVisitor, int, String)` to it.
- Added the `ObjectArray.deepToString(Object!): String` method.
- Added the `Pattern.toString()` method.
- Added the option to skip sorting in the `MatchList` class.
- Cleaned up the `*IntrinsicData` classes.
- FIxed an issue that caused `TypeCheckPattern`s to generate invalid bytecode when used with primitive types.
- Fixed Or Patterns not resetting local variable slots and causing verify errors in some cases.
- Fixed Subtyping Relations for Nullable Types.
- Fixed an NPE that was caused by invalid `TypePath`s of type annotations.
- Fixed an issue that caused Array Types to be decompiled as non-nullable types instead of implicitly unwrapped nullable types.
- Fixed an issue that caused External Class Parameters to use invalid descriptors.
- Fixed an issue that caused Jump Instructions in Intrinsic Annotations to work incorrectly.
- Fixed an issue that caused Overload Resolution to fail when invoking an overload with the same name as the enclosing method.
- Fixed an issue that caused Override Checks and synthetic bridge methods to work incorrectly for Methods with a mismatching number of Type Parameters.
- Fixed an issue that caused Statement Lists not to use implicit conversions for the last expression.
- Fixed an issue that caused Tuple and Case Class Patterns to check for type correctness incorrectly.
- Fixed an issue that caused `@NonNull` annotation to be generated for Type Variable Types.
- Fixed an issue that caused `null` patterns to work incorrectly when used within tuple or case class patterns.
- Fixed an issue that caused annotations on wildcard type bounds to behave incorrectly.
- Fixed an issue that caused certain expressions  to be compiled incorrectly in annotation value contexts.
- Fixed an issue that caused incorrect overload behaviour with variadic methods and named argument lists.
- Fixed an issue that caused methods with a variadic non-last parameter to generate an `ACC_VARARGS` modifier flag.
- Fixed an issue that caused overload resolution to fail with variadic methods in rare cases.
- Fixed an issue that caused parameter type signatures to be compiled incorrectly.
- Fixed an issue that caused static Fields to be omitted from compiled annotation classes.
- Fixed an issue that caused synthetic bridge methods in Interfaces to generate invalid bytecode.
- Fixed an issue that caused the SAME-TYPE relation to work incorrectly for Nullable Types.
- Fixed an issue that caused the `IType.writeCast(...)` implementations to handle a cast to `void` incorrectly.
- Fixed an issue that caused variables to leak out of Brace Access Expressions.
- Fixed an issue that caused variables used for side-effects to be captured incorrectly.
- Fixed error reporting for uninitialized variables.
- Fixed incorrect `@NotNull` annotation decompilation.
- Fixed nullity annotations for `Collection.toArray(*)` and other methods.
- Fixed wildcard type comparisons working incorrectly in some cases.
- Improved Binding Pattern parsing and cleaned up the `BindingPattern` class. #311
- Improved error reporting and exhaustiveness checks for AND and OR patterns.
- Improved exhaustiveness checks for Tuple and Case Class Patterns.
- Improved the scoping rules for implicit receiver values. #313
- Improved the way `SpecialIntrinsicData` handles `INVOKE*` instructions and minimized the amount of class lookups required.
- Intrinsic Methods with generic return types no longer generate unnecessary CHECKCAST instructions.
- Misc. cleanups.
- Moved and renamed the `dyvil.annotation.analysis.NotNull` class to `dyvil.annotation.internal.NonNull`.
- Moved the `Nullable` annotation from the `dyvil.annotation.analysis` package to `dyvil.annotation.internal`.
- Nullable primitive types are now automatically converted to their boxed equivalent.
- Removed Cast Optimization for Intrinsic Methods.
- Removed subtype-specific `IType` methods.
- Removed the `ICallableSignature` class.
- Removed the `IType.clone(): IType` and all overriding methods.
- Removed the `IValue.isPrimitive()` method and all overriding methods.
- Removed the `OptionType` and `ImplicitOptionType` classes. #298
- Replaced references to `OptionType` and `ImplicitOptionType` classes with `NullableType` and `ImplicitNullableType`.
- Updated For Each Statements to properly support implicitly unwrapped optional types.
- Updated Type Parameter Type Checks to properly support Nullable Types.
- Updated `try` and `throw` statements to properly comply to the type system.
- Updated compiler support for `Simple*Ref` classes.
- Updated implicit conversion checking to properly support implicitly unwrapped optional types.
- Updated the Union and Intersection Type implementation to properly support `null` and `none` types. #310
- Updated the `ImplicitNullableType` reference to the `!` method.
- Updated the `MemberParser` class to correctly support `const`.
- Updated the `NullType` implementation to be only a subtype of nullable types.
- Updated the `NullValue` and `NullPattern` classes to delegate assignability behaviour to the `NullType` class.
- Updated the `Types.isAssignable` method signature.
- Updated the compiler for new `Null` and `None` class locations. #310
- Updated the decompiler to produce `ImplicitNullableTypes` in most contexts.

## Dyvil REPL v0.27.2

- Fixed an error that was caused by declaring an uninitialized variable.
- Updated the `:complete` implementation to skip `MatchList` sorting.

## Dyvil Property Format v0.9.0

- Updated DPF classes to adapt to new language changes.
- Updated all DPF classes to use new optional features.
- Updated some DPF classes that relied on default-null initialization.
- Updated the `Value` class.

## Dyvil GenSrc v0.3.0

- Added support for the `#import` directive. #314
- Added the `#local` and `#delete` directives. #315
- Removed the `ForReplacementMap` class.
- Updated `#define`, `#undefine` and `#import` to operate on file-level scope. #315

Dyvil v0.27.1
=============

## Dyvil Library v0.27.1

- Added accessor methods for `Version` fields.
- Updated `Version` constructors to check prerelease and build info.
- Updated library classes to use `:` instead of `extends` for Type Parameters.
- Updated the `Version.equals`, `.hashCode` and `.compareTo` methods to disregard build info for equality.
- Updated the `Version.toString` method.
- Fixed the `Option.orElse` signature being conceptually incorrect and causing runtime errors when used with `None`.

## Dyvil Compiler v0.27.1

- Union and Intersection Types now generate `@DyvilType` annotations.
- Added decompilation support for `@DyvilType` annotations.
- Added the `IType.appendDescriptor` method as a generalized version of `IType.appendExtendedName` and `.appendSignature`.
- Fixed Type Parameters with `:`-separated upper bounds being parsed incorrectly.
- Fixed Union and Intersection Type Subtyping working incorrectly in some cases.
- Fixed Union and Intersection Types not being reifiable from bytecode. #3s09
- Updated the `ClassFormat` class to allow decompilation of the new Dyvil signature extensions.

## Dyvil REPL v0.17.1

## Dyvil Property Format v0.8.0

## Dyvil GenSrc v0.2.0

Dyvil v0.27.0
=============

- Upper Type Parameter Bounds can now be specified after `:` instead of `extends`.
- Removed the `include` keyword. #299

## Dyvil Library v0.27.0

- Added Dyvil implementations for tuples of 2 and 3 elements.
- Added `Array.flatten` methods for two-dimensional arrays.
- Added a generic `Array.flatMapped` method for all primitive types. #193
- Added a non-isomorphic version of the `map` method for primitive array.
- Added the `Array.copy(int)` method for primitive arrays.
- Added the `ArrayOperators` class.
- Added the `Collection.addAll(Iterable)` method.
- Added the `Collection.removeAll(Iterable)` method.
- Added the `PrimitiveObjectArray` class for low-priority array infix methods.
- Converted the `dyvil.lang.LiteralConvertible` class to Dyvil.
- Converted the `dyvil.random.Random` and `.JavaBasedRandom` to Dyvil.
- Converted the `dyvil.util.I18n`, `.Option`, `.Some` and `.None` classes to Dyvil.
- Fixed an issue that sometimes caused Marker locations to be displayed incorrectly.
- Fixed the `-` operator on arrays only removing the first occurence of the RHS.
- Improved `ObjectArray.toString/deepToString` methods.
- Improved the non-generic `Array.flatMapped` method implementation for primitive and Object arrays. #193
- Made `None` `nil`-convertible.
- Made all Funcion classes nested classes of `dyvil.function.Function`.
- Made all Tuple classes nested classes of `dyvil.tuple.Tuple`.
- Moved operator methods from `dyvil.lang.Primitives` to type-specific `*Extensions` classes.
- Moved the `Array.unboxed` methods to the `PrimitiveObjectArray` class.
- Removed `ObjectExtensions.hash` and `.##`.
- Removed `ObjectExtensions.isNull` and `.isNonNull`.
- Removed the `dyvil.annotation.Utility` annotation class.
- Removed the `dyvil.math.vector.*Vector` classes.
- Removed the temporary `Tuple.java` class.
- Renamed `dyvil.random.JavaBasedRandom` to `JavaRandom`.
- Renamed operator methods in Array classes.
- Renamed the `ObjectArray.toString(Object, StringBuilder)` method to `deepToString`.
- Renamed the `dyvil.annotation._internal` package to `internal`.
- Replaced the `ReferenceFactory.java` class file with the `ReferenceFactory.java.dgt` template.
- Rewrote `dyvil.math.Complex` in idiomatic Dyvil.
- Updated most `dyvil.lang.*Extensions` classes.
- Updated templates to use `#foreach`.
- Updated the `Array.java.dgt` template and the `Base-Boolean.dgs` file to auto-generate the `BooleanArray` class.
- Updated the other primitive `Base-*.dgs` files.

## Dyvil Compiler v0.27.0

- Added dupliate key checking for Map Literals. #117
- Added support for multiple source directories.
- Case Classes now automatically generate public field accessors for class parameters.
- Dropped support for `<:` and `>:` as bound separators in Type Parameter declarations.
- FIxed an issue that caused Lower Type Parameter Bounds to be interpreted as Upper Bounds.
- Fixed Fields with Properties not syncing the types correctly after resolution.
- Fixed Method Duplicate Checking disregarding Class Parameter Properties.
- Fixed Property Modifiers being propagated incorrectly to internal method AST nodes.
- Fixed Type Parameter Lists handling inferred semicolons incorrectly.
- Fixed an error that was caused when unresolved type aliases where referenced from outside their enclosing unit.
- Fixed an issue related to type argument resolution on raw types.
- Fixed an issue that 'leaked' warning colors when the `--ansi` flag was active.
- Fixed an issue that caused Field Properties not to go through the CHECK phase.
- Fixed an issue that caused classes with nested classes to generate invalid class files.
- Fixed an issue that caused fields, methods and parameters with Type Parameter types to generate invalid signature attributes.
- Fixed an issue that caused generated Properties to have incorrect getter and setter positions.
- Fixed an issue that caused invalid bytecode to be generated when a generic function with a return type specialized to `boolean` was called as an `if` condition.
- Fixed an issue that caused nested classes to be compiled incorrectly.
- Fixed an issue that caused the `null` type to be parsed incorrectly within compound types.
- Fixed an issue that caused type relation checks to behave incorrectly for Arrays.
- Fixed an issue that prevented Dyvil Class Modifiers from being compiled to the bytecode.
- Fixed an issue that would produce invalid bytecode when class operators were used as constant field values.
- Fixed incorrect handling of the `{since}` in `@Deprecated` description messages.
- Fixed scoped method resolution sorting candidates incorrectly.
- Fixed the `MemberSorter.compareTypes` method breaking the comparator contract.
- Improved Library Resolution Order to search in custom libraries first.
- Improved Type Parameter formatting.
- Improved warning messages for Java syntax artifacts.
- Removed the deprecation warning for `include` statements. #299
- Updated compiler references to Function classes.
- Updated compiler references to Tuple classes.
- Updated references to `dyvil.annotation._internal`.
- Updated scoped method resolution by adding the `MatchList.hasCandidate` method.
- Updated the `FileLibrary` class.
- Updated the compiler to recognize new primitive `*Extensions` classes.

## Dyvil REPL v0.17.1

- Updated the `REPLVariable` class.

## Dyvil Property Format v0.7.0

- Added missing `override` modifiers in the `Node`, `NodeAccess` and `Property` classes.
- Cleaned up most AST classes by removing unnecessary semicolons and using properties instead of bean getters/setters.
- Cleaned up most AST classes by removing unnecessary semicolons and using properties instead of bean getters/setters.
- Fixed the `Expandable.expand(…)` implementation.
- Removed all `position` variables from `NodeElement` subclasses.
- Removed duplicate `value` properties from primitive DPF Value classes.

## Dyvil GenSrc v0.2.0

- Added support for indexed `#for` directives. #307
- Added support for the `#for` and `#endfor` directives. #306
- Added the `#end` directive to denote the end of the enclosing block.
- Fixed Specialization File Resolution working incorrectly with non-canonical pathnames.
- Fixed an error caused by specialization files without a `-` in the file name.
- Renamed the `#for` directive to `#foreach`.
- Templates without specializations now use a default one based on their filename.
- The `#foreach` directive now uses `,` as the separator.

Dyvil v0.26.0
=============

- Added support for infix, prefix and postfix type operators. #303

## Dyvil Library v0.26.0

-  Added the `@base` property for all `Base-*.dgs` files.
-  Updated all GenSrc template and specialization files for the new syntax.
-  Updated all `.dgs` files to use `@fileName` and `@enabled`.
-  Updated all `.dgs` files to use `@inheritFrom`.
-  Updated the `Array.java.dgt` file to use `#if/endif` directives for `Range` methods. #305
-  Updated the `FileUtils` class.
-  Updated the `FileUtils` class.
-  Updated the `StringExtensions` class.
-  Updated the `StringUtils` class.
-  Fixed the `FileUtils.write(File, byte[])` method working incorrectly if the file does not exist.
-  Removed the `SubRangeArray.java.dgt` file.

## Dyvil Compiler v0.26.0

-  Added parser support for infix, prefix and postfix type operators. #303
-  Added intrinsics for the prefix `+` and `-` type operators. #303
-  Added intrinsics for the postfix `*` and `^`, `?` and `!` type operators. #303
-  Added intrinsics for the infix `|` and `&` type operators. #163 #164 #303
-  Added the `CodeTypeParameter` and `ExternalTypeParameter` classes.
-  Added the `ICompilableList` interface for headers.
-  Added the `IResolvable` interface.
-  Added the `IUnresolvedType` interface.
-  Added the `InfixType`, `PrefixType` and `PostfixType` classes.
-  Added the `NamedGenericType(ICodePosition, Name, IType[], int)` constructor.
-  Improved Error Reporting for missing or misplaced `>` or `)` in `type` and `class` operators.
-  Improved Operator Parsing to treat more expressions as juxtaposition.
-  Improved Syntax errors for Colon and Assignment Operators.
-  Improved the `ParserManager.split(IToken, int)` implementation.
-  Improved the `type` operator for primitive type operands.
-  Improved the distinction between `any` and `Object` by generating a `@Primitive` type annotation for the former.
-  Updated `cleanup` methods.
-  Updated and fixed the `FieldThis` class.
-  Updated the `ExternalMethod` class.
-  Updated the `ITypeParameter` interface.
-  Updated the `TypeParameter` class to use `IntersectionType`s for their upper bound representation.
-  Updated the `TypePosition` class to use int flags instead of an enum.
-  Made `IUnresolvedType` a super-interface of the `NamedType`, `NamedGenericType` and `UnknownType` classes.
-  Made the `TypeParameter` class `abstract`.
-  Fixed the Operator Declaration Parser not accepting EOFs in REPL input.
-  Cleaned up the `TypeParameterParser` class.
-  Cleaned up type-related `SemanticMarkers` entries.
-  Deprecated the `<:` and `>:` symbols for type parameter bounds.
-  Dropped support for manual name mangling using `_$_`. #302
-  Moved parts of `OperatorChain` functionality to the abstract `OperatorStack` class.
-  Renamed the `OperatorChain` class to `InfixCallChain`.
-  Separated the `ICompilable` interface from the `IClassCompilable` interface.

## Dyvil REPL v0.17.0

-  Updated the REPL for compiler API changes.
-  Changed the prefix for result classes to `Result_<n>`
-  Replaced the `REPLCompiler` class with the `REPLClassLoader` class.
-  Fixed REPL Variables not generating bytecode for (type-) annotations.
-  Fixed nested classes causing verification errors.
-  Renamed the package for generated classes to `repl_classes`.

## Dyvil GenSrc v0.1.0

-  Initial Alpha Release.
-  Added support for custom output streams.
-  Added support for the `#define` and `#undefine/#undef` directives. #305
-  Added support for the `#if`, `#else` and `#endif` directives. #305
-  Added support for the `#ifdef` and `#ifndef` directives. #305
-  Added support for the `#process` and `#literal` directives. #305
-  Added support for the `-t`, `--target`, `-s` and `--source` arguments.
-  Added support for the `@base` property for specializations that work without templates.
-  Added a localization file and the `I18n` class.
-  Added a scoping mechanism using the `ReplacementMap` interface and the `LazyReplacementMap` class.
-  Added a way to construct and use the `GenSrc` class in code.
-  Added the `##` token separator and concatenation operator.
-  Added the `Specialization.template` property.
-  Updated the specializer to replace all identifiers, even without leading and trailing `$`.
-  Made the `GenSources` class instance-based instead of using only `static` methods.
-  Moved specialization code to the new `Specializer` class.
-  Restructured the package layout.
-  Cleaned up the `Template` class.
-  Renamed the `GenSources` class to `GenSrc` and added the `Main` class.
-  Renamed the `fileName` and `enabled` properties to `@fileName` and `@enabled`.
-  Renamed the `genNotice` and `timeStamp` properties to `GEN_NOTICE` and `TIME_STAMP`.
-  Renamed the `inheritFrom` property to `@inheritFrom`.

## Dyvil Property Format v0.6.4

Dyvil v0.25.0
=============

- Added support for `import inline`. #299
- Added support for the new `import` + declaration kind syntax. #299
- Added the `@DyvilName` annotation to denote names used in Dyvil when present in Java files, and bytecode identifiers when present in Dyvil files. #302
- Added support for the `OverloadPriority` annotation. #300
- Deprecated `include` Declarations in favor of `using` or `import inline`. #299
- Dropped support for the `[Type...]` syntax for `List<Type>`.

## Dyvil Library v0.25.0

- Added separate classes for `closed` and `halfopen` primitive ranges.
- Added the `@DyvilName(String)` annotation. #302
- Added the `@OverloadPriority(int)` annotation. #300
- Added the `[final T].asList(): ImmutableList<T>` and `[T].toList(): ImmutableList<T>` methods.
- Added the type-alias `BytecodeName` for the `DyvilName` annotation.
- Fixed the `**` operator implementation for `float`s.
- Fixed the `InvariantEventBus.register(Object, Class)` method.
- Fixed the `List.subscript(Range<int>)` implementation.
- Removed support for custom primitive Range `increment`s.
- Renamed the `ObjectRef.unboxed_$_T` methods to `.unboxedT` and added a `@DyvilName("unboxed")` method.
- Renamed the `Range.count` method to `size`.
- Renamed the `[T].apply(T from, T to)` methods to `rangeClosed` with `@DyvilName("apply")`.
- Renamed the `[T].apply(T from, T toExclusive)` methods to `range` with `@DyvilName("apply")`.
- Renamed the `[T].toIterable()` method to `asIterable`.
- Replaced `include` declarations with `using` declarations.
- Split the `LanguageCommons` and `LanguageFeatures` classes from the `dyvil.lang` package into the `Assert`, `Output` and `Misc` classes.
- Updated `Range` apply constructors.
- Updated the Lang Header.
- Updated the `PowImpl.pow(long, int)` method to use a faster implementation.
- Updated the `Primitives` class.

## Dyvil Compiler v0.25.0

- Added an error for failed contextual `this` type resolution.
- Added decompilation support for the `@DyvilName` annotation. #302
- Added default constructors for the `SingleImport`, `MultiImport` and `WildcardImport` classes.
- Added support for the `@BytecodeName` annotation in source code.
- Added support for the `OverloadPriority` annotation on Methods and Constructors. #300
- Added the `IClass.getReceiverType()` method, which returns a covariant copy of the `this` type.
- Added the `IClass.isAnonymous` method.
- Added the `IDefaultContext.DEFAULT` constant.
- Added the `IImport.asContext` method.
- Added the `IImport.resolve(MarkerList, IContext, int)` method.
- Added the `ImportDeclaration.getContext` method.
- Added the `KindedImport` class.
- Added the `RESOLVE_HEADERS` phase for Import type resolution.
- Added the `SourceHeader` class and the `ISourceHeader` interface.
- Added the `Types.getTypeMatch(superType, subType)` method.
- Changed the `IImport.resolveTypes(MarkerList, IImportContext, boolean using)` parameter to `int mask`.
- Cleaned up the `IImport.readImport(DataInput)` and `.writeImport(IImport, DataOutput)` methods.
- Cleaned up the `IImport` and `MultiImport` classes.
- Cleaned up the `ImportDeclaration` class.
- Cleaned up the `dyvil.tools.asm.ClassReader` class.
- Delegate symbol resolution for Import Declarations to the `getContext` method.
- Delegate symbol resolution for `IImport`s to the `asContext` method.
- Deprecated manual name-mangling with the `_$_` indicator.
- Deprecated the `IClass.getType()` method in favor of `.getThisType()`.
- Fixed Lambda Parameter resolution stopping after the first `auto` parameter.
- Fixed Semicolon Inference handling `@` or `#` symbols on the next line incorrectly.
- Fixed Type Aliases mapping to unresolved types in some cases.
- Fixed `NamedType.resolveType(MarkerList, IContext)` returning `null` when the parent type is unresolved.
- Fixed a compiler error caused by incorrect class initialization order of Map Types and Map Expressions.
- Fixed an error caused by Overload Candidate ordering of static calls.
- Fixed external inner class resolution causing NPEs if the class has no inner classes.
- Fixed extra space in error messages containing class-types.
- Fixed float literals not being assignable to super-types of the `java.lang.Float` type, including `Object`.
- Fixed generic type resolution failing in some rare cases.
- Fixed the `MemberSorter.compareTypes(IType, IType)` implementation.
- Fixed ‘implements non-interface’ error for unresolved types.
- Fixed ‘non-qualified access’ warnings for anonymous class fields and methods.
- Import Statements can now import Headers, Type Aliases and Operators.
- Import Statements can now make `implicit` conversion functions available.
- Improved Overload Candidate Comparison to favor the most specific return types.
- Improved several `IType.getConcreteType(ITypeContext)` implementations to reduce type copying.
- Improved the `GenericType.clone()` implementation.
- Improved the `SingleImport.writeData(DataOutput)` and `.readData(DataInput)` method implementations.
- Improved type inference for Lambda Expressions without explicit return type. #301
- Improved type inference for partially applied method calls. #301
- Made the `IImportContext` interface extend `IImplicitContext`.
- Made the `IImport` class use `IImportContext` for the `asContext`, `asParentContext` and `resolveTypes(…)` methods.
- Made the `IMemberContext` interface extend `IImportContext`.
- Made use of the `IDefaultContext.DEFAULT` constant in the `WildcardImport` class.
- Mapped `include` declarations to `import inline` statements. #299
- Mapped `using` declarations to `import static inline` statements. #299
- Merged the `DyvilHeader.importDeclarations` and `.usingDeclarations` arrays.
- Methods with mangled names now generate `@DyvilName` annotations. #302
- Moved `*Import` classes from the `d.t.c.ast.header` package to the `d.t.c.ast.imports` package.
- Moved `include` access checks to the `SingleImport` class. #299
- Moved all classes except `Package` and `RootPackage` from the `d.t.c.ast.structure` package to the `d.t.c.ast.header` package.
- Remapped `using` declarations to `import static` with `KindedImport`.
- Removed `include`-related classes and code. #299
- Removed the Hardcoded `[T]` → `Iterable<T>` conversion in favor of implicit conversion methods.
- Removed the `*Using*` methods from the `IDyvilHeader` interface.
- Renamed the `DyvilHeader` class to `HeaderUnit`.
- Renamed the `DyvilUnit` class to `ClassUnit`.
- Renamed the `HeaderUnit` class to `AbstractHeader`.
- Renamed the `IDyvilHeader` class to `IHeaderUnit`.
- Renamed the `IImport.getContext` method to `asParentContext`.
- Simplified `Candidate` constructors.
- Split Import resolution into type-resolution and member-resolution phases.
- Updated Implicit Conversion Method Resolution to search the original type of the value first.
- Updated `IImport` resolution with kind masks.
- Updated the Header Context Model to use a chain that first matches header declarations and then imported / included declarations.
- Updated the `ExternalHeader` class.
- Updated the `ReceiverTypeVisitor` class.

## Dyvil REPL v0.16.2

- Removed `include`-related input handling.
- Updated the REPL for `IDyvilHeader` API changes.

## Dyvil Property Format v0.6.3

- Bump DPF version number because of binary changes.

Dyvil v0.24.0
=============

- Added support for virtual dispatch for static methods called on type variable types. #296
- Added the `struct` and `template` keywords.
- Deprecated the `[Type...]` syntax for `List<Type>`.
- Dropped support for the old C-Style Statement Label syntax.
- Generic Method Calls without Arguments can now be used as the left-hand operand of infix and postfix operators.
- The `operator` keyword can now be used in place of the `func` keyword in declarations.

## Dyvil Library v0.24.0

- Added and implemented the `Map.getEntry(Object)` method in all `Map` classes.
- Added the `*Ref.boxed(): ObjectRef<*>` methods to all primitive `*Ref` classes.
- Added the `DynamicLinker.linkClassMethod(Lookup, String, MethodType)` bootstrap method.
- Added the `InvalidReferenceException` class.
- Added the `Name.read(DataInput)` and `Name.write(DataOutput)` methods.
- Added the `ObjectRef.unboxed(ObjectRef<*>): *Ref` methods for all primitive `*Ref` classes.
- Added the `Option.fromNil` method to avoid type warnings in Dyvil source code.
- Added the `ReferenceFactory.instanceRefMetafactory(…)` bootstrap method.
- Added the `ReferenceFactory.staticRefMetafactory(…)` bootstrap method.
- Added the `StringExtensions.String` constructor.
- Added the `StringExtensions.getCharArray(String)` method.
- Added the `Wrapper.referenceType(Class)` method.
- Added the `dyvil.annotation._internal.Primitive` annotation.
- Cleaned up the `Some` class.
- Fixed empty Code Positions beeing produced by `ICodePosition.between(...)` causing display errors for markers.
- Fixed the `!=` operator for `Object`s referring to the non-existant class `Predef`.
- Improved the `DynamicLinker` implementation.
- Improved the `subscript_&` implementation for mutable and immutable `HashMap`s.
- Increased the max class hierarchy depth of the `InliningCacheCallSite` class from `3` to `256`.
- Made the `Option.!` and `.??` operators `infix` methods.
- Made the `ReflectUtils.JAVA_LANG_ACCESS` field `public`.
- Moved the `dyvil.lang.literal.*Convertible` classes as inner classes to `dyvil.lang.LiteralConvertible.From*`.
- Removed the `Option.?` operator.
- Removed the `dyvil.lang.Void` class. #297
- Un-deprecated the `Name.fromUnqualified(String)` method.
- Updated references to the former `*Convertible` classes in Dyvil source code.
- Updated the `@Reified` annotation to take a `Reified.Type` enum argument.
- Updated the `BooleanArray`, `ByteArray`, `CharArray` and `ShortArray` classes.
- Updated the `Collections` header to remove List type syntax usages.
- Updated the `FloatArray` and `DoubleArray` classes.
- Updated the `IntArray` and `LongArray` classes.
- Updated the `ObjectArray` class.
- Updated usages of the `@Reified` annotation.

## Dyvil Compiler v0.24.0

- Named Argument Lists now ensure their Arguments to be evaluated in source order. #279
- Added an additional `genericArg` parameter to the `IType.appendSignature(StringBuilder)` method.
- Added the `AnnotationUtil.getEnumValue(IArguments, IParameter, Class)` and `.getStringValue(IArguments, IParameter)` methods.
- Added the `ArrayType.getPosition` method.
- Added the `ClassFormat.BSM_HEAD` and `.BSM_TAIL` constants for common Bootstrap Method Descriptors.
- Added the `IMember.getInternalName(): String` method.
- Added the `IType.needsSignature()` method.
- Added the `ITypeParameter.getErasure(): IType` method.
- Added the `IValue.asIgnoredClassAccess()` method to convert a receiver to a Class Access.
- Added the `IValue.isClassAccess()` method to determine if a receiver is a Class Access.
- Added the `MapType.getPosition` method.
- Cleaned up the `IncludeDeclaration` class.
- Cleaned up the `Property` class.
- Fixed Class Access Method Receivers being discarded for static methods.
- Fixed InnerClass attributes being compiled incorrectly.
- Fixed Primitive Generic Arguments for Array Type Aliases being converted to their boxed counterpart.
- Fixed Primitive Types generating invalid signatures when used as generic type arguments.
- Fixed Type Expressions working incorrectly when used with Nested Types.
- Fixed Type Variables not being usable in place of Class Accesses.
- Fixed class-compilables being compiled after normal fields, causing errors in REPL inputs with static field references.
- Fixed invalid Bridge Methods being generated when reified type parameters are present in the overriding method. #291
- Fixed runtime error for varargs arrays of a generic type where the generic type is inferred to a primitive type.
- Fixed the `IntersectionType.typeTag` method returning `UNION`.
- Fixed the `dynamic` type being reported as unresolved, breaking compilation of most expressions with the type.
- Fixed the return value of `MapExpr.isResolved()` being incorrect in some contexts.
- Improved Error Messages for un-reified Type Parameter references in Class Type contexts.
- Improved Instance Field References to cache the field address in the callsite class.
- Improved Parameter Name Decompilation for External Methods.
- Improved String Interpolation bytecode output for a single value surrounded by empty spaces.
- Improved Super Expression errors for indirect Super Interfaces.
- Improved Type Parameter bound compilation.
- Improved the `ParameterList.readSignature(DataInput)` and `.writeSignature(DataOutput)` methods to persist parameter names.
- Improved the `ParameterList.signatureToString(…)` method to output parameter names.
- Improved the evaluation order for Map Expressions to be entry-wise instead of all keys then all values.
- Improved the method Name Mangling scheme to use parameter names instead of types.
- Removed all `IType.getSignature()` implementations and replaced them with a default one.
- Removed the `AbstractFieldReference` class.
- Removed the `IContext.resolveClass(IMemberContext, Name)` method.
- Removed the `IContext.resolveType(IContext, Name)` method.
- Removed the `IMethod.setMangledName(String)` method.
- Renamed the `AbstractMethod.mangledName` field to `internalName`.
- Renamed the `CaptureField.name` field to `internalName`.
- Renamed the `IMethod.getMangledName()` method to `getInternalName`.
- Replaced references to `dyvil.lang.Void` with `java.lang.Void`. #297
- Updated Compound Assignment AST Conversion.
- Updated External Classes to load inner classes on demand instead of all at once.
- Updated Instance Field References to generate `invokedynamic` calls.
- Updated Primitive Type compilation to generate `@Primitive` type annotations when used as a generic argument.
- Updated Signature Compilation to generate a Signature attribute only for Fields, Parameters and where it is needed.
- Updated Static Field References to generate `invokedynamic` calls.
- Updated Type Resolution for named Types and named Generic Types.
- Updated `fullName` computation for the `ExternalClass` and `Package` classes.
- Updated the Compiler to adapt to new `LiteralConvertible` locations.
- Updated the Package Resolution mechanism.
- Updated the `AnnotationValue` class.
- Updated the `IType.writeClassExpression(MethodWriter)` method to take a `boolean wrapPrimitives` argument.
- Updated the `ITypeParameter.getReifiedKind` method to return a `Reified.Type`.
- Updated the `InternalGenericType` class.
- Updated the `LambdaExpr` and `AbstractMethod` classes.
- Updated the `MapType` constructors.
- Updated the `StaticFieldReference` class.
- Updated usages of `Name` API methods.

## Dyvil REPL v0.16.1

- Adapted to Compiler API changes.

## Dyvil Property Format v0.6.2

-  Updated references to the former `*Convertible` classes in Dyvil source code.
-  Updated usages of `Name` API methods.

Dyvil v0.23.0
=============

- Added a new Statement Label syntax using the `label` keyword. #295
- Added the `**` exponentiation operator.
- Added the `label` keyword. #295
- Added support for Implicit Type Conversion Methods. #170
- Deprecated the old C-Style Statement Label syntax. #295
- Dropped support for explicit `this` and `super` typing with Square Bracket syntax.
- Dropped support for the Hash Symbol to be used to suppress Semicolon Inference.
- Overload Resolution now respects generic types correctly. #281
- Primitive Types are now available as static method call receivers.
- Removed the `nil` and `null` Class Modifiers.
- Type Parameters can now be optionally declared with the `type` keyword between Annotations and the Variance Symbol or Name.

## Dyvil Library v0.23.0

- Adapted library classes to new `prefix` semantics.
- Added Implicit Range → Array Conversion Methods to the Array classes.
- Added Primitive `to*Array` methods for Primitive `Range` classes.
- Added Range Operator Functions for Primitives and `Rangeable`s.
- Added `pow` implementations for integer and `BigDecimal` exponentiation in the `dyvil.math.PowImpl` class.
- Added the `**` operator for `BigInteger`s and `BigDecimal`s.
- Added the `BigDecimalOperators` class to allow math operators to be used with `BigDecimal`s.
- Added the `BigIntegerOperators` class to allow math operators to be used with `BigInteger`s.
- Added the `FileUtils.antToRegex(String)` method.
- Added the `LanguageFeatures.convert<T>(T): T` method to apply implicit conversions easily.
- Fixed the `toArray` implementations of all Primitive `Range` classes.
- Fixed the return types of the `Collection.union(Collection)`, `.difference(Collection)`, `.intersection(Collection)` and `Set.symmetricDifference(Collection)` methods using wildcards.
- Improved the `Name` caching implementation.
- Increased the default capacity for Array-based Lists and Maps from 10 to 16.
- Made the `TryParserManager` class more usable by abstracting token caret internals away with the `tryParse` method.
- Moved the utility methods for qualifier <->︎ unqualified Name conversions from the `BaseSymbols` class to the `Qualifier` class.
- Overloaded the return type of Primitive Range `toArray()` methods.
- Removed deprecated `TryParserManager.parse(…)` methods.
- Renamed the `IParserManager.jump(IToken)` and the `TokenIterator.jump(IToken)` methods to `setNext`.
- Updated all Array factory Methods.
- Updated the `LanguageCommons` class to fix an Overload Error.
- Updated the `Modifiers` class.
- Updated the `Name` factory methods.

## Dyvil Compiler v0.23.0

- Adapted the `StatementListParser` class to the `TryParserManager` changes.
- Adapted to `Name` interface changes.
- Added a Name hash cache for Methods, Properties and Fields in Class Bodies.
- Added a `ClassBody` cache for Implicit Conversion Methods.
- Added a localization key for 'Argument Types: ...'
- Added a warning for automatically name-mangled methods.
- Added missing Syntax Marker messages for Class and Type Operators.
- Added special error markers for ambiguous method, constructor and initializer calls. #280
- Added support for Generic Method Overloading using Name Mangling.
- Added the `IArguments.copy()` method and implemented it in subclasses.
- Added the `IContext.getConstructorMatch(…)` method.
- Added the `IImportContext` class.
- Cleaned up the Method and Constructor Resolution implementations.
- Completely overhauled Method Overload Ordering. #281
- Dropped Compiler support for the `nil` and `null` Class Modifiers.
- Fixed Anonymous Classes being decompiled in some cases.
- Fixed Cast Operators not accepting Type Parameter types.
- Fixed External Parameter Type Annotations decompilation causing type resolution.
- Fixed Postfix Operators consuming the opening brace of If, While and For Statements.
- Fixed incorrect Super Type Checking for Wildcard Types.
- Fixed special Operators within Expressions causing Compiler Errors. #282
- Fixed the `Candidate.compareTo(Candidate)` method implementation violating parts of the `Comparable` contract.
- Fixed the `ColonOperator.isResolved()` method implementation.
- If, While, Match and Synchronized Statements no longer have special grammar cases for an opening brace after the keyword. #283
- Implemented Implicit Conversion Method Resolution for `IContext` subclasses and others.
- Implemented `IImplicitContext` methods for `IType` subclasses.
- Implicit Conversions are now resolved via the `TypeChecker.convertValueDirect(...)` method.
- Implicit Conversions are now resolved via the `TypeChecker.getTypeMatch(IValue, IType, IImplicitContext)` method.
- Implicit Search now uses the target type as well.
- Improved Semicolon Inference performance by merging the inference and linker loops.
- Improved `ClassBody` initialization by lazily initializing the Property and Initializer arrays.
- Improved static Method resolution when applied for the wrong type.
- Improved the Syntax Error for Prefix Operators without an operand.
- Improved the `IStatement.checkStatement(…)` method implementation.
- Made the `ForEachStatement` and `TryStatement` classes use `IStatement.checkStatement(…)` method.
- Made the `IMemberContext` interface extend `IImplicitContext`.
- Made the `LiteralConversion` class extend `AbstractCall` instead of `MethodCall`.
- Made the `MatchList<T>` class `Iterable<Candidate<T>>`.
- Merged the `MethodMatchList` and the `ConstructorMatchList` classes.
- Method Resolution now takes all available implicit conversions for the receiver into account.
- Moved the `CompoundCall` methods to the `InfixCall` class.
- Removed all methods used to compute ‘distances’ between types.
- Removed the `IStatement.checkCondition(…)` method.
- Removed the `RangeOperator` class.
- Removed the special semantics of the `prefix` keyword. #286
- Renamed most occurences of `instance` where `receiver` was meant.
- Renamed the `ExpressionParser.addFlag(int)` and `.removeFlag(int)` methods to `addFlags` and `removeFlags`.
- Renamed the `ExpressionParser.withFlag(int)` and `MemberParser.withFlag(int)` methods to `withFlags`.
- Simplified the `LiteralConversion` class by making it extend `MethodCall`.
- Simplified the `NilExpr` implementation by making it extend `LiteralConversion`.
- Static Methods can now be accessed from non-static expressions.
- The `ClassBody`, `ClassMetadata` and `CaseClassMetadata` classes now use the `IContext.getConstructorMatch(…)` method.
- The `IConstructor.getSignatureMatch` method now takes an `IImplicitContext` parameter.
- The `IMethod.getSignatureMatch` method now takes an `IImplicitContext` parameter.
- The `IParserManager.report(IToken, String)` method now takes an `ICodePosition` instead of an `IToken`.
- The `MethodMatchList` and `ConstructorMatchList` classes now store an `IImplicitContext`.
- The `SideEffectHelper.processArguments(IArguments)` now makes and returns a defensive copy of the argument list.
- The `exclude` config entries can now use Ant-style filename patterns.
- The `fullName` and `internalName` are now computed lazily for the `CodeClass` class.
- The `implicit` Modifier can now be used on Methods.
- The `prefix` keyword now works as an alias for `static`. #286
- The methods defined in the Primitive Wrapper classes now take priority over methods defined in the `Primitives` class.
- Updated Method Overload Ordering to use are more reliable system to check for the most specific type. #281
- Updated Qualified Type resolution in Expression contexts.
- Updated Range For Statement handling to not rely on `RangeOperator`s anymore.
- Updated and fixed Wildcard Type checking.
- Updated the `AbstractMethod.getSignature()` method by moving the `needsSignature()` method and calls to the `CodeMethod` class.
- Updated the `DyvilKeywords` class.
- Updated the `IContext.resolveConstructor(IMemberContext, IArguments)` method to take an `IImplicitContext` as the first parameter.
- Updated the `TupleExpr.isResolved()` and `IfStatement.isResolved()` implementations.

## Dyvil REPL v0.16.0

- Adapted the `CompleteCommand` class to lib interface changes.
- Adapted to `Name` interface changes.
- Adapted to compiler API changes.
- Added the `REPLParser` class to abstract REPL parsing internals.
- Fixed all REPL Variables in the same input displaying the same value.
- Removed the `REPLMemberClass` class.
- Removed the `repl$compute$` method for REPL variables.
- Renamed the `REPLContext.reportErrors()` method to `endEvaluation`.
- The REPL now supports input of multiple expressions separated by `;`. #292
- The `:complete` command now displays available Implicit Conversions.
- Updated REPL Input Compilation to support multiple expressions.
- Updated the REPL for `MethodMatchList` constructor changes.
- Updated the `REPLMemberClass` class.

## Dyvil Property Format v0.6.1

- Adapted the DPF implementations to API changes in the library and compiler.

Dyvil v0.22.0
=============

- Added Verbatim Char Literals. #263
- Class and Type Operators can now use Angle Brackets around their type. #275
- Improved Juxtaposition of Expressions. #265
- Dropped support for Double Arrows in Lambda Types.
- Dropped support for Square Brackets for Generic Types and Declarations.
- Dropped support for the old Wildcard Type syntax.
- Match Expressions can now have the `match` keyword before the matched expression. #270
- The Hash Symbol can now be used at the end of a line to bypass Semicolon inference. #276
- This and Super Expressions can now use Angle Brackets to declare their type explicitly.

## Dyvil Library v0.22.0

- The `Map` and `Queryable` classes now implement `SizedIterable`.
- Added the `Configure<T>` type alias to the Lang Header.
- Added the `IParserManager.splitReparse(IToken, int)` method.
- Added the `LanguageFeatures.milliTimed<R>(-> R)` and `.nanoTimed<R>(-> R)` methods.
- Added the `SizedIterable` class.
- Added the `dyvil.tools.parsing.Name.apply(String)` and `.wrap(Object)` methods.
- Added the `dyvil.util.Configurable` marker interface.
- Updated all Collection and Map constructors and factory methods to be more consistent and convenient.
- Updated and cleaned up the `d.c.impl.AbstractArraySet`, `d.c.mutable.ArraySet`, `d.c.immutable.ArraySet` and `d.c.immutable.ArraySet.Builder` classes.
- Updated the `dyvil.Math` header to support juxtaposition multiplication.
- Improved some `LanguageFeatures` method implementations.
- Made `AutoPrinter` an `object`.
- Made the `dyvil.tools.parsing.Name` class `StringConvertible`.
- Fixed Array Set having duplicate entries when initialized via the `apply` methods.
- Fixed Method Type Arguments not going through all compiler phases.
- Fixed Underscore Tokens becoming Identifiers instead of reserved Symbols when split.
- Fixed the `AbstractMapBasedSet.toString()` implementation.
- Fixed the `ParserManager.splitJump(IToken, int)` method ignoring the `length` parameter.

## Dyvil Compiler v0.22.0

- For Each Statements with C-Style Variables and `:` now produce a Syntax Warning instead of multiple errors.
- Parameter Default Value Type Checking now uses the `TypeChecker` system.
- The Type Parser can now handle Java-style Array Types gracefully. #272
- Type Parameters now check and produce an error if they have multiple `class` Upper Bounds.
- Verbatim Char Literals can now be used in Expressions and Pattern Matching. #263
- Wrapped Parameter List Arrays for Methods, Constructors, Classes and Lambdas into a proper `ParameterList` class.
- Added a dummy `IValue` implementation.
- Added an error for Array Expression with element type `void`.
- Added the `AbstractParameter` class.
- Added the `ExternalClassParameter` class.
- Added the `ExternalParameter` class to represent External Method Parameters.
- Added the `ICaseConsumer` class.
- Added the `IType.getSubTypeDistance(IType)` for special types such as type variable references or union/intersection types.
- Added the `MemberSorter` class.
- Added the `Types.isExactType(IType, IType)` method to check if one types equals the other without special type checks.
- Added the `Types.isVoid(IType)` method to check if a type is `void`.
- Added the `Util.startsWithSymbol(Name)` and `.endsWithSymbol(Name)` methods.
- Added the `d.t.c.a.operator.InfixCall` class to represent binary operators.
- Implemented the Juxtaposition Parsing rules in the Expression Parser. #265
- Implemented the `InitializerCall.toString()` and `TupleExpr.toString()` methods.
- Updated Initializer Resolution to avoid misleading generic warnings.
- Updated Modifier Checks to be performed by the `ModifierSet` implementation.
- Updated Type Inference for Constructors of Generic Classes.
- Updated `ExternalClass` lazy parameter resolution.
- Updated `this` Statement resolution.
- Updated loop resolution for `break` and `continue` statements.
- Updated the `Invalid Dot Access` syntax error message.
- Updated the `PatternParser` and `PatternListParser` classes.
- Updated the `Types.getDistance(IType, IType)` implementation.
- Improved Compiler initialization messages.
- Improved Error Diagnostics for invalid Dot Accesses.
- Improved Error handling for malformed Member declarations. #272
- Improved Error reporting for unresolved types.
- Improved Lambda Expression Type Inference Checks.
- Improved Method Match Resolution for Varargs expansion operators.
- Improved Operator Resolution for different scopes with overloaded operators.
- Improved compiler errors for Java-style Static Imports and Wildcard Imports. #272
- Improved parsing for symbolic Types within Generic Angle Brackets. #273
- Improved type inference error messages in Closures.
- Inlined the Operator Chain Resolution Algorithm for the special case of two operators and three operands.
- Merged the `d.t.c.a.parameter.Parameter` and `.MethodParameter` classes.
- Extracted a Case Statement Parser from the Match Expression Parser.
- Fixed Anonymous Classes not compiling their initialization code.
- Fixed Array Expression Type Inference.
- Fixed Array creation generating invalid Stack Frames in some cases.
- Fixed Control Flow Statements like `break` and `continue` not being resolved in Repeat Statements.
- Fixed Exceptions being checked incorrectly for being unhandled.
- Fixed Generic Classes and Methods with Symbolic Names being formatted incorrectly.
- Fixed Lambda Expressions inferring Wildcard Types incorrectly.
- Fixed Lambda Type Inference causing runtime errors in some cases.
- Fixed Lambda Types not serializing their `extension` flag into Type Alias object code.
- Fixed OR and AND Patterns being parsed incorrectly when preceded by identifiers.
- Fixed Parameter Type Annotations using incorrect indices and causing compilation errors.
- Fixed Parameters using incorrect Type Positions arguments for `CHECK_TYPES`.
- Fixed Prefix Operators without operands causing compiler errors.
- Fixed Return Statements type-checking their value incorrectly in some cases.
- Fixed Semicolons not being inferred after the `null` keyword.
- Fixed Super / Sub Typing checks between Covariant Type Var Types and ordinary Type Var Types.
- Fixed Try Statements behaving incorrectly and causing compilation errors in some cases.
- Fixed Type errors appearing in the wrong location in some cases.
- Fixed Variable Capture working incorrectly if the same Variable or Parameter is captured more than once.
- Fixed Warning Messages being displayed incorrectly in some cases.
- Fixed Wildcard Types causing compiler errors in some cases.
- Fixed Wildcard Types not implementing Position methods. #274
- Fixed `ArrayExpr.isResolved()` returning an incorrect result in some cases.
- Fixed `ClassConstructor`s causing compiler errors with unresolved types.
- Fixed `CodeMethod` type inference causing compiler errors.
- Fixed `ReturnStatement` type error reporting for unresolved expressions.
- Fixed `UnionType.getTheClass` causing compiler errors in some cases.
- Fixed `continue` statements working like `break` statements. #278
- Fixed `inline` Methods never being inlined.
- Fixed a compiler Error caused during compilation of Inlined Method Arguments.
- Fixed fields being captured incorrectly in nested Anonymous Classes.
- Fixed incorrect Member Access (`.`) precedence.
- Fixed infix associativity errors being reported for misplaced prefix and postfix operators in infix position.
- Fixed the `BraceAccessExpr.withType(…)` method setting the `statement` field to `null` when the type mismatches, causing a compiler error later.
- Fixed the `CHECK_TYPES` phase not being applied for `else` branches of If Statements.
- Fixed the `NamedArgumentList.withLastValue(Name, IValue)` method being implemented incorrectly.
- Fixed the `applyStatement` method not being applied for the last Statement in a Statement List in all cases.
- Fixed unresolved Apply Call error messages being misleading.
- Cleaned up the `ExpressionParser` class.
- Misc. API changes.
- Removed the `IArguments.dropFirstValue()` method.
- Removed the `IArguments.inferType(int, IParameter, ITypeContext)` method.
- Removed the `dotless` fields for `MethodCall` and `FieldAccess` AST nodes.
- Renamed the `MethodParameter` class to `CodeParameter`.
- Moved Intrinsic Operator Resolution to the respective AST classes instead of handling it in `MethodCall`.
- Moved the `ExternalParameter` class to the `d.t.c.a.external` package.

## Dyvil REPL v0.15.0

- REPL Variables now highlight their name in blue when they are displayed and ANSI colors are enabled.
- The `:complete` command now prints `Variables:`  instead of `Fields:` for REPL completions.
- The `:methods` and `:variables` commands now display a message when there are no members to show.
- The outputs of the `:complete`, `:variables` and `:methods` commands are now sorted correctly.
- Added the `:m` alias for the `:methods` command.
- Added the `:v` and `:vars` aliases for the `:variables` command.
- Improved REPL initialization messages.
- Localized all Compiler Messages in the `Compiler.properties` file. #264
- Localized all REPL Messages in the `REPL.properties` file. #264
- Fixed REPL variables generating misleading errors when the `toString` method of their value causes an exception.
- Fixed the `:complete` command displaying REPL method completions incorrectly.

## Dyvil Property Format v0.6.0

- Converted the DPF source files to Dyvil.
- Verbatim Char and String Literals can now be used in DPF files. #263
- The `Value.wrap(any)` method now respects the `DPFConvertible` interface.
- Added the `dyvil.tools.dpf.DPFConvertible` interface.
- Added the `dyvil.tools.dpf.DSL` class for easy DPF integration in Dyvil source code.
- Updated the DPF `Parser` class to work correctly. #277
- Updated the `Printer` class to support `Builder`s and be more structured.
- Fixed the DPF compilation tasks being executed in incorrect order.
- Removed the `Value.toPropertyValue(any)` extension method.

Dyvil v0.21.1
=============

## Dyvil Library v0.21.1

- Added a Lexer error for empty Backtick Identifiers.
- Fixed String Tokens being displayed with incorrect length in Marker Messages.
- Fixed Syntax Error Diagnostics working incorrectly with `TryParserManager`s in some cases.

## Dyvil Compiler v0.21.1

- Improved Type Parsing for `<>` and `_>` edge cases.
- Improved Error Diagnostics for unresolved types.
- Improved and fixed nullary Lambda Type Parsing in special contexts.
- Fixed Annotations with invalid Types causing compiler errors.
- Fixed nullary Lambda Expressions being treated as invalid syntax.

## Dyvil REPL v0.14.1

- Fixed Extension Methods available through `using` declarations not being displayed by the `:complete` command.

## Dyvil Property Format v0.5.0

Dyvil v0.21.0
=============

- Class Declarations can now declare their Type Parameters in Angle Brackets. #180
- Closure Lambda Expressions in Statement Lists can now define an explicit Return Type. #253
- Generic Type Aliases can now be declared with Angle Brackets. #180
- Generic Types can now uses Angle Brackets. #180
- Lambda Expressions can now define an explicit Return Type. #253
- Method Call Expressions can now use Angle Brackets. #180
- Method Declarations can now declare their Type Parameters in Angle Brackets. #180
- The Double Arrow `=>` is now deprecated for Lambda Types. #253
- The Single Arrow `->` can now be used for Function and Lambda Return Types. #253
- The `nil` literal can now be used in place of `String`s.
- Type Aliases with different arities can now be overloaded. #260
- Added Unicode Escape Sequences in String Literals in the form `\u{hexDigits}`. #259
- Added the `\v`, `\a`, `\e` and `\0` escape codes. #259
- Added the `nil` and `null` class modifiers. #257
- Updated the Wildcard Type Syntax. #255
- Removed `do` Statements Notation. #213
- Removed the `functional` keyword. #227
- Deprecated Method Calls with Square Brackets for Generic Types. #180

## Dyvil Library v0.21.0

- Lexer Errors can now be localized with the `dyvil.tools.parsing.lang.SyntaxMarkers` property file.
- The `ParserManager` class now requires a `Symbols` instance to be passed.
- The `TryParserManager` class can restore split tokens when being reset.
- Added Lexer Errors for Unclosed Backtick Identifiers and String Literals.
- Added a Lexer Error for Newlines within Single-Quoted String Literals.
- Added a Lexer Error for Unclosed Block Comments.
- Added support for direct casts (without implicit conversion) using the `LanguageFeatures.cast<T>(any)` method.
- Added the `IParserManager.split(IToken, int)` method for splitting a token into two.
- Added the `Marker.addError(Throwable)` class.
- Added the `dyvil.Functions` and `dyvil.Tuples` headers.
- Added the `dyvil.util.I18n` class.
- Rewrote and structured the `DyvilLexer` class.
- Updated the `dyvilx.lang.model.type.GenericType.toString` implementation to use Angle Brackets.
- Improved Error reporting in the `DyvilLexer` class.
- Improved Unicode support in the Lexer.
- Fixed Errors and Warnings not being reset from the `MarkerList` by `TryParserManager`s.
- Fixed single Characters not being parsed by the Dyvil Lexer.
- Fixed the `AbstractTupleMap.toString()` implementation using the wrong Entry separator.
- Removed the `Names.writeReplace` and `.readResolve` fields.
- Cleaned up the `dyvil.random.Random` class.
- Moved the `dyvil.lang.FunctionConversions` class to the `dyvil.function` package.
- Moved the `dyvil.tools.compiler.parser.IParserManager`, `ParserManager`, `TryParserManager` and `Parser` classes to the `dyvil.tools.parsing` package.
- Renamed the `dyvil.random.RandomUtils` class to `JavaRandoms`.

## Dyvil Compiler v0.21.0

- All formatting methods now use Angle Brackets for Generics.
- External Fields now resolve their Constant Value lazily.
- Object Classes now produce an error if they explicitly declare an `instance` field.
- The `External*` classes now use bitflags instead of boolean fields to indicate which types have already been resolved.
- Added a default implementation for the `IType.asParameterType` method.
- Added a new `ConstructorCall` constructor.
- Added a new `FieldAssignment` constructor.
- Added deprecation warnings for Generic Types and Declarations with Square Brackets instead of Angle Brackets.
- Implemented the `nil` class modifier. #257
- Implemented the `null` class modifier. #257
- Updated Lambda Type Formatting to use the Single Arrow `->` instead of `=>`.
- Updated Member Resolution for Case and Object Classes.
- Updated Type Alias resolution to use the new `IContext.resolveTypeAlias(Name, int)` method.
- Updated Wildcard Type formatting.
- Updated and Improved Generic Type Parameter Inference for Method Calls.
- Updated and moved the `AnonymousClassMetadata` class into the `AnonymousClass` file.
- Updated the Type Parser to improve support Lambda Types with one Parameter Type.
- Updated the `BaseModifiers` class.
- Updated the `CombiningLabelContext` class.
- Improved Lambda Type formatting.
- Improved Object Class Resolution and Diagnostics.
- Improved String and Char Literal Formatting.
- Improved Type Inference for Lambda Expressions.
- Improved `MethodMatchList` resizing.
- Improved bytecode Type decompilation.
- Improved the Error markers for Constructors Calls of Interface Types.
- Improved the `Util.methodSignatureToString(...)` and `.memberSignatureToString(...)` methods.
- Resolved some edge cases in the Type Parser.
- Fixed Annotated Type bytecode decompilation.
- Fixed Array Expressions causing JVM errors when used in place of Type Parameter Types.
- Fixed Break and Continue Statements causing compiler errors with unresolved labels.
- Fixed Explicit Generic Type Arguments being overwritten by Type Inference.
- Fixed External Fields of type `char` with constant values being inlined incorrectly.
- Fixed Float and Double Literals being formatted incorrectly with NaN or Infinity values.
- Fixed If Statements causing compiler errors in some cases.
- Fixed Member Signature Formatting working incorrectly in some cases.
- Fixed Method Calls being type-resolved twice.
- Fixed Primitive Types not restoring their source code position.
- Fixed Statement List Labels being resolved incorrectly.
- Fixed Type Mismatch Errors not printing the concrete type.
- Fixed Type Mismatch Errors showing uninferred types instead of concrete ones.
- Fixed `static final` fields without a Constant Value being treated as if it was `null`.
- Fixed the `:complete` command formatting Methods without their Parameter Types.
- Fixed the `ResolvedGenericType.checkType` implementation working incorrectly with too many type arguments.
- Fixed the compiler error when it is unable to load a file.
- Removed compiler support for nominal Function and Tuple Types.
- Removed the `CompleteCommand.getSignature(...)` methods.
- Removed the `IContext.resolveType(Name)` method.
- Disabled external Methods with Jump instructions in the bytecode from being inlined.
- Moved the `*Metadata` classes from the `dyvil.tools.compiler.ast.classes` package to `d.t.c.a.classes.metadata`.
- Moved the `dyvil.tools.compiler.parser.IParserManager`, `ParserManager`, `TryParserManager` and `Parser` classes to the `dyvil.tools.parsing` package.

## Dyvil REPL v0.14.0

- The `:complete` REPL command can now display available Extension Methods.
- Updated the `REPLContext` class to always provide a member class.
- Updated the `REPLMemberClass` class.
- Improved `:complete` Error diagnostics.
- Fixed Escaped Backslash Character (`\\`) being handled incorrectly in the REPL.
- Fixed Lexer errors not being reported in the REPL.
- Fixed REPL methods causing runtime errors when called from the REPL.
- Fixed Static Completions by the `:complete` command working incorrectly.

## Dyvil Property Format v0.5.0

- Removed the `FlatMapConverter.parse(String, ...)` methods.

Dyvil v0.20.0
=============

- Apply and Named Method Calls can now define custom Reference Methods, similar to `subscript_&`. #240
- Extension Lambda Types can now be declared with any type as the receiver type.
- Named Argument Lists can now handle Varargs Parameters. #248
- Named Argument Lists no longer require every Parameter to have a Name Label. #242
- Operator Definitions can now use reserved symbols as the Operator Name. #246
- Postfix Operators now check if their Operator can be resolved and is declared `postfix`.
- Prefix Operator Calls now check if their Operator can be resolved and is declared `prefix`.
- Statement Lists can now define Lambda Parameters in the first line. #244
- String Interpolations can now contain nested Parentheses. #238
- The `var` and `let` keywords are now allowed in Parameter Lists.
- Infix Operator Definitions no longer require the additional options in braces.
- Prefix and Postfix operators can now be declared with (empty) braces.
- Added support for the definition of multiple Varargs Parameters. #249
- Added Field and Class Parameter Properties. #232
- Added Property References using anonymous inner classes. #234
- Added support for Prefix Assignment Operators. #243
- Added support for Ternary Operator Definitions. #245
- Added the Varargs Expansion Operator. #247
- Improved the Parsing Rules for Prefix and Postfix Operators. #181, #236
- Re-added support for Lazy Fields. #230
- Removed `*` as the Reference Operator. #235
- Renamed the `subscriptRef` method to `subscript_&`. #240

## Dyvil Library v0.20.0

- The `::` Operator is now right-associative and requires a List as the tail.
- The `AnnotationProxyFactory` class now generates a simple `toString` method.
- The Dyvil Lexer now produces `SYMBOL_IDENTIFIER` tokens for `DOT_IDENTIFIER`s.
- Added `PrependList.apply` methods.
- Added all Collection Operator Methods as `infix` methods in the `CollectionOperators`, `ListOperators` and `SetOperators` classes.
- Added all Map Operator Methods as `infix` methods in the `MapOperators` class.
- Added the Dereference Operators `*` for all `dyvil.ref.*Ref` classes.
- Added the Reference Assignment Operator `*_=` for all `dyvil.ref.*Ref` classes.
- Added the Ternary Conditional Operator definition in the Lang Header.
- Added the `LanguageFeatures.println(AutoPrinter.() => void)` that mimics the `AutoPrinter.apply` behaviour.
- Added the `Stack.peek(int)` method.
- Added the `dyvil.runtime.BytecodeDump` class for runtime bytecode dumps.
- Added the `dyvilx.lang.model.type.Type.typeArgumentCount()` and `.typeArgument(int)` methods.
- Added unary Plus operators in the `Primitives` class.
- Fixed the `Function1.compose` method being implemented incorrectly.
- Fixed the `dyvil.array.ObjectArray.mapped` and `flatMapped` methods causing runtime errors with primitive types.
- Fixed the `dyvil.collection.Entry` type parameters not being covariant.
- Cleaned up the `AnnotationProxyFactory` class.
- Moved the Collection Type Aliases from the Lang Header to the `dyvil.Collections` header.
- Removed all mutating Collection Operator Methods.
- Removed all mutating Map Operator Methods.
- Removed the `LanguageFeatures.repeat(int, => void)` method.
- Removed the `dyvil.ref.*.String*Ref` classes.
- Renamed all non-mutating Collection Operator Methods to descriptive names.
- Renamed all non-mutating Map Operator Methods to descriptive names.
- Renamed the `Collection.intersect` method to `retainAll`.
- Renamed the `dyvil.tools.asm.Opcodes` class to `ASMConstants`.
- Updated the Lang Header to add the Reference, De-reference and Unwrap Operators.
- Updated the Library to use the Varargs Expansion Operator.

## Dyvil Compiler v0.20.0

- All Varargs Parameters are now stored as such in the Bytecode.
- Argument Lists now check for and require Varargs Expansions instead of testing for array types. #247
- Array Constructors can now handle Named Argument Lists.
- Array Constructors no longer need to specify all length parameters.
- Array Literal conversions now support non-varargs methods.
- Colon and Assignment Operator Resolution now happens during `OperatorChain` resolution.
- Deferred the Parameter Type Inference error to the `RESOLVE` phase.
- Field Accesses and Assignments that target an implicit value will resolve as a method call before falling back to field resolution now.
- Lambda Expressions now have a `flags` variable for various metadata.
- Map Expression now infer the Immutable Map type.
- Overhauled the `Operator` class.
- Statement Lists can now also be separated by commas.
- Subscript Access expressions can now use named arguments.
- The Expression Parser no longer needs to lookup operators, instead it produces `OperatorChain`s that are resolved later. #236
- The `AnnotationVisitor` class now throws a `BytecodeException` if the `value` argument of the `visit(String, Object)` method cannot be converted to an `IValue`.
- The `IContext.resolveOperator(Name)` method now takes an additional `type` parameter to tell operators with the same name apart.
- The `LambdaExpr.captureHelper` field is now only initialized when needed.
- Varargs Calls are now replaced with Array Literals instead of inlining their compilation.
- Added a global method to check if one type is a super class of another.
- Added a syntax error for Prefix Operators without operands.
- Added an Argument List Parser that can parse Argument Lists with optional labels.
- Added an error for Array Types with `void` as the element type.
- Added an internal representation for Intersection Types.
- Added missing `IType.asParameterType` implementations.
- Added the `ClassFormat.insnToHandle(int)` method.
- Added the `IArgumentsConsumer` interface.
- Added the `IMethod.toHandle()` method.
- Added the `IOperator` interface for the new Operator API.
- Added the `IParameterList.setParameters([IParameter], int)` method.
- Added the `IValue.toAssignment(IValue, ICodePosition)` method to allow each AST node to define a custom Assignment conversion.
- Added the `ModifierSet.removeIntModifier(int)` method.
- Extracted the `ConstructorCallParser` class from the `ExpressionParser` implementation.
- Extracted the `ThisSuperInitParser` class from the `ExpressionParser` implementation.
- Updated Parenthesis handling for the `class` and `type` Operators.
- Updated Tuple Type Checking to allow implicit Value conversions.
- Updated Type Argument inference and Bound Checking for Constructors.
- Updated `ArgumentMap` resolution to work correctly with unnamed arguments.
- Updated the `LambdaExpr` class for `Parameter` type checking and formatting changes.
- Updated the `MethodVisitor.visitMultiANewArrayInsn(...)` methods and callers.
- Improved Annotation Value Conversion.
- Improved Constructor and Apply Method generation.
- Improved Documentation and Naming in the `LambdaExpr` class.
- Improved Parameter Type Ascription and Default Value parsing.
- Improved Postfix Operator → Assignment conversion.
- Improved Wildcard Type resolution when other wildcard types are inferred as their bound.
- Improved the Error Message for incompatible Literal Conversions.
- Improved the Expression Parser for Method Calls without Parentheses.
- Improved the Generic Array Creation error message.
- Improved the Missing Lambda Parameter Type Error for Lambda Expressions with implicit Parameters.
- Improved the Parsing Rules for Field Accesses over Method Calls in the Expression Parser.
- Improved the `Incompatible Method Return Type` semantic error message.
- Improved the `Invalid Dot Access` syntax error message.
- Fixed Annotations with Package Types causing compiler errors.
- Fixed Field Accesses inlining Array Literals.
- Fixed Lambda Compilation for Lambda Expressions that return Method Calls whose receiver references a Lambda parameter.
- Fixed Lambda Expressions without return values causing compiler errors when formatted or printed.
- Fixed Lambda Parameters not going through all compiler phases.
- Fixed Union and Intersection Types working incorrectly with Type Parameters.
- Fixed Varargs Class Parameters being `transient`.
- Fixed Variable Capture in For, For Each and Catch Statements.
- Fixed an issue with Lambda Expressions and Varargs calls causing runtime Lambda errors.
- Fixed empty Named Argument Lists causing compiler errors when formatted.
- Fixed missing Varargs Arguments causing compiler errors upon bytecode generation.
- Fixed non-final non-static Fields generating `ConstantValue` attributes instead of a field assignment in the initializer.
- Fixed the Variable Name Shadowing warning message.
- Fixed the `InlineIntrinsicData.writeJump(…)` and `writeInvJump(…)` methods working incorrectly if the last instruction is not a jump instruction.
- Fixed the `Parameter.getInternalType()` method causing infinite recursion.
- Fixed the `ParserUtil.findMatch(IToken)` method working incorrectly with braces.
- Simplified Varargs Method Match Computation, Resolution and Type Checking.
- Cleaned up the `ExternalClass` class.
- Cleaned up the `TypeConverter`, `AnonymousClassLMF` and `AnnotationProxyFactory` classes.
- Removed `MethodParameter` handling for the `var` modifier.
- Removed `var` as a modifier.
- Removed a redundant parameter from the `ImportDeclaration.resolveTypes(...)` method.
- Removed the `ExpressionMapParser` class.
- Removed the `IValueMap` interface.
- Removed the `PARSE_HEADER` and `RESOLVE_HEADER` compiler phases.
- Removed the deprecation warning for C-Style For Statements.
- Renamed the `ArgumentMap` class to `NamedArgumentList`.
- Renamed the `IClass.isSubTypeOf` method to `isSubClassOf`.
- Renamed the `IExternalMethod.getParameter_(int)` method to `getParameterNoResolve`.
- Moved Operator Resolution from `ParserManager` to `IContext`.
- Moved the `ClassOperator`, `TypeOperator`, `InstanceOfOperator` and `CastOperator` classes to the `dyvil.tools.compiler.ast.expression` package.
- Moved the `ExpressionParser.parseArguments(IParserManager, IToken)` method to `ArgumentListParser.parseArguments(IParserManager, IToken, IArgumentsConsumer)`.
- Moved the `IParameterList.setVariadic` and `.isVariadic` methods to the `IParametric` class.
- Moved the `IType.resolveTypeSafely(ITypeParameter)` to the `Types` class as `resolveTypeSafely(IType, ITypeParameter)`.
- Moved the `StringConcatExpr`, `AndOperator`, `OrOperator`, `NotOperator`, `IncOperator` and `NullCheckOperator` classes to the `dyvil.tools.compiler.ast.intrinsic` package.

## Dyvil REPL v0.13.0

- The REPL now handles Field Properties correctly.
- REPL classes are loaded, verified and initialized upon definition now.
- Improved Error handling in the REPL.
- Improved Name Mangling for Result Class names in the REPL.
- Improved REPL Variable Formatting for Array Values.
- Refactored the compilation mechanism in the REPL to the new `REPLCompiler` class.
- Removed the `REPLResult` class.

## Dyvil Property Format v0.4.1

Dyvil v0.19.0
=============

- Array Literals now infer an Immutable Array Type.
- Class-Level Fields and Properties can now be declared with the `var` keyword. #220
- Concrete Classes now call the Trait Initializer Methods from their constructors. #226
- Constructors now always execute the Initializer Call before initializing the class and running the rest of their body. #228
- Constructors now only execute the Class Initializers if their Initializer refers to the super class. #228
- Final Field and Variable Declarations can now use `let` instead of `final var`. #237
- Properties can now infer their type from the Getter if necessary. #222
- The Cast (`as`) Operator now allows casting to `void`. #219
- The automatic Conversion from Array Literal to Iterable now works for all expressions that return an Array type. #218
- The default Property Setter Parameter name is now `newValue`.
- Interfaces can now have methods with default implementations.
- Methods at the Class level can now be defined with the new `func` keyword. #221
- Methods produce an error marker if they reduce the visibility of their super method(s). #229
- Methods, Fields and Properties can now be declared with Type Ascriptions. #223
- Variables of an Option Type can now be assigned with a concrete value. #68
- Added Brace Access Expressions as a syntax element. #215
- Added Implicit Reference Types. #233
- Added Implicitly Unwrapped Option Types, denoted with a `!` after the contained type. #68
- Added support for Modifiers and Annotations on For (Each) and Catch Variables. #225
- Added support for Nested Block Comments. #239
- Added support for Type Ascriptions in Catch Block Variables. #223
- Added support for Type Ascriptions in For (Each) Statements. #223
- Added support for Type Ascriptions in Parameter Declarations. #223
- Added the `func` keyword. #221
- Added the `repeat` keyword. #213
- Added the `let` keyword. #237
- Allow Method Declarations to have only one `throws` declaration list.
- Allow `&` to be used for the prefix Reference Operator.
- Improved Dots Handling in Symbol Identifiers #217
- Made Parenthesis optional around For and For Each Statements. #224
- Made Parenthesis optional for Catch Blocks. #224
- Made Parenthesis optional for If, While and Synchronized Statements. #224
- Deprecated `var` Parameters.
- Deprecated the `functional` Modifier. #227
- Removed support for `in` in For Each Statements.

## Dyvil Library v0.19.0

- The `Map.keys` and `values` methods now both return instances of `Queryable`.
- Added `Map.mapKeys`, `.mapValues`, `.keyMapped` and `.valueMapped` overloads.
- Added a sugar method for writing values to PrintStreams with the `<<` syntax known from C++.
- Added the Ternary Operator reference implementation in the `LanguageCommons` class.
- Added the `Map.filter(ed)ByKey` and `.filter(ed)ByValue` methods.
- Added the `Queryable.mapped`, `.flatMapped` and `.filtered` methods.
- Updated standard Operator Precedence Values.
- Updated the Library and Test Classes to use the Implicit Reference Type operator instead of `var` parameters.
- Updated the Operator Precedence for the `<<`, `>>` and `>>>` operators.
- Updated the `Some.toString` implementation.
- Structured the Lang Header.
- Improved the implementations of primitive `println` methods in `LanguageCommons`.
- Cleaned up some ASM classes.
- Removed the `Quad` alias for `Tuple4` from the Lang Header.-

## Dyvil Compiler v0.19.0

- The Compiler now supports ANSI colors via the `--ansi` argument.
- The Parameter Parser can now handle single identifiers without a syntax error.
- The Statement List Parser now uses the Member Parser to parse Variables and Nested Methods.
- The `AbstractMethod.checkOverride(...)` method no longer automatically adds the candidate to the List of overriden Methods.
- The `IClassConsumer` and `IMemberConsumer` classes now have methods to create custom Member implementations.
- The `IMemberConsumer.addField` method now takes an `IDataMember` parameter.
- The `MethodVisitor.visitCode` method can now return whether or not the code should be visited rather than relying on a global constant flag.
- The `MethodWriter` interface now extends the `MethodVisitor` interface.
- The `TryParserManager` now correctly checks for success when an Exception occurs.
- The `TryParserManager` now handles Syntax Warnings correctly.
- The `dynamic` type can now resolve and virtually call `Object` methods.
- Statement Lists now store their Variables in a List.
- String Concat Expressions can now fold consecutive constant parts. #71
- Traits now generate a special `<traitinit>` method that contains Trait Initialization code. #226
- Interfaces and Traits now mandate warnings for members that are explicitly declared `public`.
- Interfaces now mandate errors for Properties with initializers.
- Types Aliases and Operators are now stored in arrays instead of a `Map` in the `DyvilHeader` class.
- Added Visibility Checks for Types.
- Added a `TryParserManager(TokenIterator, MarkerList, IOperatorMap)` constructor.
- Added a flag for the `TryParserManager` to exit when the `parser` becomes `null`.
- Added a warning for declarations with Symbol Identifiers not preceded with the `operator` keyword.
- Added a warning for declarations with non-Symbol Identifiers preceded with the `operator` keyword.
- Added additional checks for (inferred) Method Type Arguments to match the bounds.
- Added advanced checks for the `FunctionalInterface` annotation. #227
- Added an additional `StatementListParser` constructor that takes a boolean `closure` flag.
- Added an optional flag to the `ParameterListParser` class that allows untyped Parameters in Parameter Lists.
- Added auto-unwrapping for Implicit Reference Types.
- Added overloaded `markerSupplier` methods to the `TypeChecker` class.
- Added the `CodeConstructor` to represent Constructors for which the source is available.
- Added the `ExpressionParser.IGNORE_LAMBDA` and `.IGNORE_CLOSURE` flags to exclude Lambda Arrows and Open Braces from the parsed Expression.
- Added the `IInstruction.getOpcode()` method and implemented it in all subclasses.
- Added the `IType.getLocalSlots` methods that returns `2` for `long` and `double` and `1` for every other type.
- Added the `IType.isConvertibleTo` and `.convertValueTo(IValue, IType, ...)` methods for custom conversion from value type to target type.
- Added the `IVariableConsumer` interface.
- Added the `ModifierUtil.checkVisibility(IMember, MarkerList, IContext)` method for shared visibility checks.
- Added the `Types.isAssignable(IType, IType)`, `.getDistance(...)` and `.isConvertible(...)` methods.
- Added the `Types.isSuperType(IType, IType)` method for more controllable super type checks.
- Re-added the `inline` modifier and implemented it through the `Intrinsic` method system.
- Changed the `IValue.getTypeMatch` return type from `float` to `int`.
- Cleaned up many `IValue` and `IPattern` subclasses.
- Cleaned up the `ArrayExpr` class.
- Cleaned up the `Deprecation` class.
- Cleaned up the `DyvilKeywords` class.
- Cleaned up the `ExpressionParser` class.
- Cleaned up the `Parser` and `ParserManager` classes.
- Cleaned up the `SuperExpr` class.
- Cleaned up the `TypeListParser` class.
- Cleaned up the `UnionType.combine(IType, IType)` method.
- Constructors and Class Parameters in Traits now mandate error markers.
- Direct Lambda Method References now also work when calling a non-static method on a primitive receiver.
- Extracted a single Member Parser from the Class Body Parser.
- Fields now cache their Descriptor.
- Initializers, Constructors and Class Parameters in Interfaces now mandate error markers.
- Intrinsified the Ternary Operator in the Compiler by providing an automatic desugaring to an If Statement. #214
- It is no longer possible to declare Variables without an initial value in Statement Lists.
- Nested Methods now produce an Error Marker if there is duplicate in the same Statement List.
- Operator Methods in Statement Lists now require the `operator` keyword before Symbol Identifiers.
- Option Types are now restored from bytecode.
- Parameters now maintain a Cache for their Internal Covariant Parameter Type.
- Properties, Constructors, Initializers and Nested Classes in Statement Lists are now added to the AST and produce an error.
- Variables that shadow other variables with the same name now produce a Warning Marker.
- Finding the common super type of two types now happens lazily through the use of Union Types in the Type System.
- Implicitly Unwrapped Reference and Option Types can now be compiled and restored from the bytecode.
- Generified the `IDataMemberConsumer` and `DataMemberParser` classes.
- Generified the `MemberParser` class.
- Improved Condition parsing in Match Cases.
- Improved Formatting Indentation in Class Bodies.
- Improved Formatting for Type Ascriptions.
- Improved Parser Errors for Expressions, Parameter Lists and For Statements.
- Improved Variable Declarations without an initial value.
- Improved constant Field compilation.
- Improved storage Array growth for Import, Using, Include, Field and Method declarations.
- Improved the Type Annotation system.
- Improved the `IType.withAnnotation(IAnnotation)` method contract.
- Improved the `ParameterListParser` implementation.
- Updated Compiler-internal Static Field Reference Duplicate checking.
- Updated Variable Parsing in For (Each) and Try/Catch Statements.
- Updated `NestedMethod` and `Variable` constructors.
- Updated the Lambda Expression Parser to be more reliable and efficient.
- Updated the `IfStatementParser` class.
- Updated the `Operators` class.
- Updated the `ParserManager` API.
- Updated the `StringInterpolationExpr` class.
- Updated the checks for empty and `void` Properties.
- Updated the way initial Field, Variable and Method values are type checked.
- Made the `ClassParameter` class extend `Field` and implement `IParameter`.
- Made the `IMemberConsumer` class generic and extend `IDataMemberConsumer`.
- Fixed Annotations without a type (single `@` symbol) causing compiler errors.
- Fixed Array Literals being parsed as Subscript Accesses after Infix Operators.
- Fixed Colon Operator Type Checking failing for automatic Lambda wrapping in Tuple Types.
- Fixed Colon Operators without a value on either side causing compiler errors.
- Fixed Dynamic Method Calls working incorrectly in Statement Contexts.
- Fixed Error Markers in REPL Method and Property Definitions not being printed.
- Fixed Field Resolution not looking into the Lang Header.
- Fixed Infix Method Receiver Type Mismatch errors causing compiler errors.
- Fixed Method Override Checking not properly adding the overriden Method to the List.
- Fixed Option and Union Types causing compiler errors when used in Type Expressions and reified methods.
- Fixed Primitive Types resolving Type Parameters incorrectly.
- Fixed Property Assignments always returning `void` instead of the assigned value.
- Fixed Property Formatting not handling the `newValue` identifier correctly.
- Fixed Repeat Statements without action blocks causing compiler errors on bytecode generation.
- Fixed Super Constructor Resolution causing compiler errors in some contexts.
- Fixed Type Annotations not calling the `IType.withAnnotation(IAnnotation)` method.
- Fixed Type Parameter Annotation compilation.
- Fixed `ClassGenericType.getParameterType` causing Lambda type checking to break.
- Fixed `IntValue.withType(...)` not looking for type annotations.
- Fixed `OptionType.getDefaultValue` returning `null`.
- Fixed invalid bytecode generation when Arrays are used in place of Varargs Parameters.
- Fixed nested Range ForEach Statements causing invalid bytecode to be generated.
- Fixed the `AnnotationVisitor.visitEnd` method not being called in all contexts on Annotation bytecode generation.
- Fixed the `TypeChecker.typeError(ICodePosition, IType, IType, String, Object...)` swapping error key and info key.
- Fixed the `WildcardType.asParameterType` method giving an incorrect result for lower-bounded Wildcard Types.
- Fixed the `dynamic` type resolving to `any` in some contexts.
- Fixed unimplemented static Methods producing two Error Markers.
- Moved and renamed the `Util.toConstant` method to `IValue.toAnnotationConstant`.
- Moved the Reference Class Constants and Methods from `Types` to `ReferenceType.LazyFields`.
- Moved the `AbstractMethod.value` and `.overrideMethods` fields to the `CodeMethod` class.
- Moved the `BaseModifiers.parseModifier(...)` method to `ModifierUtil`.
- Moved the `DataMemberParser` class to the `dyvil.tools.compiler.parser.classes` package.
- Moved the `DyvilUnitParser` and `DyvilHeaderParser` classes to the `header` package.
- Moved the `TryParserManager.parse(...)` method to the `DyvilREPL` class.
- Refactored `Parameter` compilation.
- Removed redundant `Parser.reportErrors` implementations.
- Removed redundant and non-transitive `getSub...` methods from the `IType` interface.
- Removed the Parser Factoy methods from the `IParserManager` class.
- Removed the Side Effect Checks for Methods and Method Calls; all method calls are considered to have side effects now.
- Removed the `EmulatorParser` class.
- Removed the `IDataMember.capture(IContext, IDataMember)` method.
- Removed the `Parameter.varargs` field and implemented it with Modifiers.
- Removed the `TryParserManager.parse(MarkerList, TokenIterator, Parser, boolean)` method.
- Removed the code for `lazy` field compilation.
- Removed the syntax error for untyped Parameters since they generate a semantic error later.
- Removed the unused `MarkerList` and `IClass` parameters from most methods related to Method Override Checking.
- Removed the unused `MemberType` enum.
- Removed unused Methods from the `ParserUtil` class.
- Removed unused methods from the `Types` and `TypeChecker` class.
- Renamed the `Constructor` class to `AbstractConstructor`.
- Renamed the `IClassCompilable.writeInit(...)` method to `writeClassInit`.
- Renamed the `IDataMember.getDescription` method to `getDescriptor`.
- Renamed the `IMemberConsumer.addField` method to `addDataMember`.
- Renamed the `IParameter.getInternalParameterType` method to `getInternalType`.
- Renamed the `IType.classEquals(IType)` method to `isSameClass`.
- Renamed the `IType.getReturnType` and `.getParameterType` methods to `.asReturnType` and `.asParameterType`.
- Renamed the `IValue.isConstant` method to `isAnnotationConstant`.
- Renamed the `IValue.toConstant(...)` method to `toAnnotationConstant`.
- Renamed the `MethodStatement` class to `MemberStatement`.
- Renamed the `ModifierUtil.readClassTypeModifier(...)` method to `parseClassTypeModifier`.
- Renamed the `REPLParser` class to `TryParserManager` and moved it to the Compiler API.
- Renamed the `Types.getObjectArray` method to `.getObjectArrayClass`.
- Renamed the `VariableParser` class to `DataMemberParser`.
- Renamed the `dyvil.tools.compiler.parser.imports` package to `header`.
- Replaced code occurences where Primitive Types Instances are checked by Reference (`== Types.VOID`, ...).

## Dyvil REPL v0.12.0

- The REPL now supports ANSI colors via the `--ansi` argument.
- The REPL now uses the same `MemberParser` configuration as the `StatementList` Parser.
- Improved REPL Variable parsing.
- Fixed Operators being resolved incorrectly in the REPL.
- Fixed REPL variables that mandated errors during declaration causing compiler errors when accessed.
- Fixed the `dumpDir` REPL argument being parsed incorrectly.

## Dyvil Property Format v0.4.1

Dyvil v0.18.0
=============

- Enabled Nested Methods and removed the error that was produced upon their declaration. #48
- Variables in Statement Lists and For(Each) Statements can now be declared using the `var` keyword instead of `auto`. #208
- For Each Statements can now operator on `Iterator`s. #212
- For Each Statements can now be written using the `<-` symbol or the contextual `in` keyword. #210
- Added support for the `applyStatement` method for free-standing values in statement lists. #204
- Added special syntax to define Extension Lambda Types without parameter modifiers. #203
- Reserved the `->` and `<-` operators as Symbols.
- Type Parameters Bounds can now be declared with the `extends` and `super` keywords. #206
- Deprecated the `auto` type name. #209
- Deprecated C-Style For Loops (with Condition and Update). #211
- Deprecated the Colon Operator in For Each Statements. #210
- Dropped `new` support for Constructor Declarations and Initializer Calls.

## Dyvil Library v0.18.0

- Updated the `dyvil.Lang` and `dyvil.Collections` Headers.
- Fixed the `FileUtils.create(File)` method returning incorrect results.
- Fixed the `StringExtensions.substring(String, Range[int]) method including one character too much in the resulting String.
- Removed the `->` and `<-` Operators from the `LanguageFeatures` class.

## Dyvil Compiler v0.18.0

- Classes, Methods and Constructors now use a `CombiningContext` instead of directly referring to the enclosing class during resolution.
- ClassAccess expressions that do not refer to Object Types no longer report errors in certain contexts.
- Return Statements with type mismatches now show display the expected return type in the error message.
- If Statements no longer report type mismatch errors if the `then` or `else` branch has not resolved correctly.
- Added an error for when the last value in a statement list cannot be used as the return value because of a type mismatch.
- Added a semantic error for Lambda Expressions without a return value.
- Added error for free-standing Modifiers or Annotations in Class Bodies and Headers.
- Added the `TypeChecker` class for Type Checking utility methods.
- Added the `IParameterConsumer` interface, which can also handle `IParameter` instance creation.
- Added a default implementation for `IDataMember.checkAssign(…)` and made use of it in sub-classes.
- Added extension Methods for `Comparable` in the `ObjectExtensions` class.
- Added the `classOf` and `typeOf` methods in the `LanguageFeatures` class.
- Added the `LanguageCommons.consume(any)` blackhole method.
- Added modifier bitmask constants for the `infix` and `extension` modifiers in the `dyvil.reflect.Modifiers` class.
- Unified the API methods to compile Parameters and Local Variables.
- Updated Try / Catch type checking.
- Updated the way Static Contexts are handled and checked.
- Updated Nested Method resolution and contexts.
- Updated Nested Method invocation and implementation bytecode generation.
- Updated Type Checking in most `withType` implementations.
- Updated the `LambdaTest` class to include a test for currying and type inference.
- Updated the `ParameterListParser` to only require a `IParameterConsumer` instead of a `IParameterList`.
- Updated the `ParameterListParser` class.
- Updated compilation units to handle file loading on their own.
- Improved the For / For Each Statement API and For / For Each Statement Parser.
- Improved For Each Statement type-checking.
- Improved Nil Expressions in place of Object Arrays.
- Improved parameter modifier persistence.
- Improved Type Inference for Lambda Expressions with explicit parameter types.
- Improved `ClassFormat` parameter type decompilation.
- Simplified Anonymous Class Resolution making use of the new class context system.
- Fixed Var Parameter transformations not being applied when the Reference argument is used in Receiver position. #201
- Fixed the `auto` type being an error.
- Fixed Parse Errors propagating as compiler errors when thrown during EOF parsing.
- Fixed Try Statements being unusable in all cases, even when used as a statement.
- Fixed Class Parameters not being checked for visibility modifiers.
- Fixed Throw Statements applying RESOLVE to their arguments incorrectly.
- Fixed Range For Each Statements causing type errors for untyped variables.
- Fixed Range Operators not producing an error if their element type is neither numeric nor a sub-type of `dyvil.collection.range.Rangeable`.
- Fixed localization error caused by mismatching receiver types.
- Fixed the `IType.isSuperClassOf(IType)` method causing compiler errors for unresolved types.
- Fixed `IStatement.checkCondition` producing an error for unresolved values.
- Fixed unresolved Types causing Compiler Errors when calling `isSuperTypeOf`.
- Fixed `CompoundCall`s producing invalid argument lists.
- Fixed the Colon Operator not calling `withType` on it’s arguments.
- Fixed Return Statements in Lambda Expressions not being Type-checked.
- Fixed Nil Expressions not reporting errors in some cases.
- Fixed Implicit Closure Parameters not having a Code Position attribute.
- Fixed the message that reports markers for a file printing the entire code of a compilation unit.
- Fixed Reference Types being handled incorrectly by the `type` operator.
- Moved Try Catch Block compiler phase code to the `CatchBlock` class.
- Removed many direct constructor calls to `CombiningContext` and replaced them with calls to `IContext.push(IContext)`.
- Removed the `dyvil.tools.parsing.CodeFile` class.
- Removed the `ITypeList` interface from the `IConstructor` super interfaces.
- Cleaned up the `LambdaExpr` class.
- Moved the `MethodParameter.write(MethodWriter)` and `ClassParameter.write(...)` methods to the `Parameter` class.
- Renamed all inner classes for lazy type resolution `LazyFields`.
- Renamed the `IMethodSignature` interface to `ICallableSignature`.
- Renamed the `IExternalMethod` interface to `IExternalCallableMember`.

## Dyvil REPL v0.11.2

- Improved Syntax Error Reporting in the REPL for some inputs.
- Improved the way Result Classes are generated for `void` statements in the REPL.

Dyvil v0.17.1
=============

## Dyvil Library v0.17.1

- Fixed Markers displaying the occurence incorrectly if the position is outside the code range (EOF).

## Dyvil Compiler v0.17.1

- Added the `ModifierSet.count` and `.isEmpty` methods.
- Updated Field Access return type computation.
- Fixed automatic Lambda conversion being applied in contexts where it will cause a runtime error. #196
- Fixed Apply Method Calls not being resolved for ordinary method calls with a receiver. #198
- Fixed Field Assignments not having concrete types. #199
- Fixed Var Parameter transformations not being applied when the Reference argument is used in Receiver position. #201
- Fixed Apply Methods not being resolved for qualified Types. #205
- Fixed Return Statements without a return value causing NPEs in the `isResolved` method.
- Fixed Apply Method Resolution causing compiler errors (caused by previous commit).
- Fixed Method Type Parameter Annotations not being stored in the bytecode.
- Fixed the `TypeVarType.getName()` method returning `null`.
- Fixed the Implicit Closure Value not being resolved, making it unusable.
- Fixed the `Mismatching Array Constructor Argument Count` error message localization.
- Fixed empty Tuple Patterns causing compiler errors.

## Dyvil REPL v0.11.1

- Fixed the method modifiers `infix`, `prefix` and `postfix` being unusable in the REPL. #197
- Fixed Class and Type Operators being unusable in the REPL. #200
- Fixed Return Statements being disallowed in the REPL. #202

## Dyvil Property Format v0.4.1

Dyvil v0.17.0
=============

- Added a Colon Operator as syntactic sugar for Tuples. #194
- Added a Syntax Warning for Initializer Calls using the `new` keyword.
- Added a Syntax Warning for Constructor Declarations using the `new` keyword.
- References Types and -Operators now use `*` again, as opposed to the `&` symbol.
- Removed the Dyvil-specific Primitive Wrapper classes and redirected them to the standard Java wrappers.

## Dyvil Library v0.17.0

- `Map.toString` now uses `: ` as the key-value separator instead of `->`.
- Added the `dyvil.lang.Primitives` class to host all primitive operators.
- Added primitive Range classes for `int`, `long`, `float` and `double`.
- Added the `dyvil.collection.immutable.BitSet` class.
- Added the `@ColonConvertible` annotation for user-defined interaction with the Colon Operator.
- Added Type Aliases for Mutable and Immutable Arrays, Lists and Maps in the `dyvil.Lang` header.
- Added the `dyvil.util.DateExtensions` class for a Date DSL.
- Added the `StringExtensions.char(char)` method for character literals and an overload taking a `String` to provide a warning.
- Added the `@DyvilType` annotation.
- Added the `Map.remap(Object, K)` method.
- Added default implementations for the `Map.remap` method in `MutableMap` and `ImmutableMap`.
- Added a cache for faster index lookup in the `StringPoolWriter` class.
- Split the large Predef class into multiple smaller components.
- Made all `Map` interfaces and implementations `ColonConvertible`.
- Updated all library classes for the primitive changes (including Lambda Factory utility classes).
- Updated all Constructors in Dyvil files to use the `init` keyword instead of `new`.
- Updated the `Rangeable` interface to require `Comparable` instead of `Ordered`.
- Updated the `Range` implementations accordingly.
- Updated the `Collection.toString` and `Map.toString` implementations.
- Updated the `FileUtils` utility class to have throwing and non-throwing (`try*`) methods.
- Fixed the `AbstractTupleMap(Entry…)` constructor not setting the size correctly.
- Fixed the `DEPRECATED` modifier being an access modifier.
- Fixed `Deprecated`, `Experimental` and `UsageInfo` annotations not being present in compiled class files.
- Fixed the `MutableMap.remap(Object, K)` method adding `null` entries when the old key was not present.
- Deprecated the `$` prefix operator for character literals.
- Deprecated the `->` and `<-` operators in the `dyvil.lang.LanguageFeatures` class.
- Removed the primitive classes in the `dyvil.lang` package.
- Removed the `MatchError` constructors that take and wrap a primitive value.
- Cleaned up the `AbstractTupleMap` class.
- Cleaned up the `StringPoolWriter` and `StringPoolReader` classes.
- Cleanup & Formatting across the entire code base.
- Moved common base language extension class to the `dyvil.lang` package.
- Renamed the replacement tokens for use in Deprecated annotations to `{member.kind}` and `{member.name}`.

## Dyvil Compiler v0.17.0

- The Compiler is now usable as a `javax.tools.Tool`.
- Major structure changes and moved files in the `dyvil.tools.compiler.ast.type` package.
- The Compiler Config can now contain relative and absolute paths (the latter starting with a `/`).
- Generic Types now check if their argument types fulfill the Type Parameter contract in the CHECK_TYPES phase.
- Added support for Syntax Warning for deprecated language features.
- Added the `IClassConsumer` interface.
- Added the `TypeDelegate` class.
- Added the `ListType.toString` implementation.
- Added the `toString` method implementation for the `VoidValue` and `ArrayExpr` classes.
- Updated the compiler to link wrapper classes and primitive operators correctly.
- Updated the Common Super Type Algorithm in the `Types` class.
- Updated the `RangeOperator` and `RangeForStatement` to support the new primitive range classes.
- Updated and cleaned up the `ClassBodyParser`, `DyvilHeaderParser` and `DyvilUnitParser` classes.
- Updated Enclosing Class and Header resolution.
- Updated the way Parser errors are reported.
- Updated the `Library.load(File)` method to throw an exception instead of printing to stdout.
- Updated the Multi Import Parser to support (inferred) semicolons in addition to commas.
- Improved automatic type promotions for `int`, `long` and `float` literals.
- Improved the 'Incompatible Generic Type Argument' error message.
- Improved the `ClassDeclarationParser` and `DyvilHeaderParser` implementation.
- Improved the `AbstractMethod.getSignatureMatch(...)` implementation.
- Fixed Type Mismatches for Tuple Expressions not being reported correctly, leading to compiler errors upon bytecode generation.
- Fixed Range Operators generating invalid bytecode.
- Fixed Range For Statements generating invalid bytecode for primitive types.
- Fixed Constructors using the `new` keyword instead of `init` in the `toString` and formatting implementation.
- Fixed the Compiler exiting before the Test thread has finished in some cases.
- Fixed certain Type kinds being decompiled incorrectly from Object Files.
- Fixed most Type kinds not having a code position attribute.
- Fixed the `libraries` paths in the compiler config not being relative to the parent directory of the config file.
- Fixed the 'Invalid Operator Property' syntax error not showing the problematic token.
- Fixed the `operator.prefix.associativity` syntax error not being localized.
- Fixed Reference Types being linked incorrectly for Primitive Types.
- Fixed alphanumeric letters being part of a single identifier when succeeding a sequence of symbols preceded by a `$` or `_`.
- Fixed Throw Statements reporting type errors for unresolved expressions.
- Removed the `PartialFunctionExpr` class from the Compiler AST.
- Removed most globals from the `DyvilCompiler` class.
- Cleaned up the `CompilerConfig` class.
- Cleaned up the `ExpressionParser` class.
- Renamed the `SubscriptGetter` class to `SubscriptAccess`.
- Renamed the `SubscriptSetter` class to `SubscriptAssignment`.
- Renamed the `IParameterized` interface to `IParametric`.
- Renamed the `ITypeParameterized` interface to `ITypeParametric`.
- Renamed the `IClassMember.getTheClass()` and `.setTheClass(IClass)` methods to `getEnclosingClass` and `setEnclosingClass`.
- Renamed the `IClass.getOuterClass()` and `.setOuterClass(IClass)` methods to `getEnclosingClass` and `setEnclosingClass`.
- Renamed all `theClass` fields from `IClassMember` subtypes to `enclosingClass`.
- Removed the code for hardcoded `dyvilBinLibrary` in the `Library` class.
- Moved the `MapTypeContext` class to the `dyvil.tools.compiler.ast.generic` package.
- Moved the `classVersion` and `asmVersion` constants from the `DyvilCompiler` class to the `ClassFormat` class.

## Dyvil REPL v0.11.0

- The REPL now only recognizes commands as such if they consist of a colon followed by one or more letters.
- Commands now take the raw string after the command name instead of a space-separated array.
- The `:complete` command can now process and show completions for compound expressions and types (without evaluating them).
- The `:debug` command now uses an optional argument to enable or disable Debug Mode.
- The `:help` command can now show usage information about commands and aliases.
- Added the `:library` command for loading external libraries in the REPL.
- Added the `:rename` REPL command for renaming variables.
- Added the `dyvil.tools.repl.Main` class as the entry point for the REPL.
- Improved variable names for Reference Types.
- Fixed Constructors being unusable in the REPL. #195
- Fixed Member and Header Element parsing in the REPL.
- Fixed Constructors Calls being impossible in the REPL because Constructor Declaration errors are reported.
- Fixed Tuple and Function Types causing errors during variable name generation in the REPL.
- Fixed automatic variable name generation in the REPL causing errors for unresolved / package types.
- Removed the `:?` command shortcut.
- Removed the `output`, `errorOutput` and `inputManager` fields from the `DyvilREPL` class.
- Cleaned up the `CompleteCommand` class.
- Moved classes from the `dyvil.tools.repl` package to new subpackages `context` and `input`.

## Dyvil Property Format v0.4.1

- Updated all DPF classes for primitive changes.

Dyvil v0.16.1
=============

## Dyvil Library v0.16.1

- Fixed the `dyvil.runtime.Wrapper` class performing it’s internal hash lookups incorrectly, leading to runtime errors.

## Dyvil Compiler v0.16.1

- Introduced a cache for Method Override Checking in the `AbstractMethod` class.
- Improved Bridge Method generation.
- Updated the `ClassDeclarationParser` and `DyvilUnitParser` implementation code.
- Fixed Case Class Patterns working incorrectly with dynamic casts and causing JVM errors. #191
- Fixed Mutable Maps and Immutable Maps being incorrectly mapped to Map Types with Mutability Modifiers.
- Fixed potential infinite recursion caused by Fields when used in Annotations.
- Fixed Constants with Arrays being inlined incorrectly in Annotations.
- Fixed EOFs at the end of class declarations being handled incorrectly.
- Fixed the `INOT` and `LNOT` custom instructions being handled incorrectly by max stack size calculation.
- Fixed Variables in For Statements without an initial value causing compiler errors.
- Fixed Class Access expressions causing compiler errors for package types.
- Fixed various compiler errors caused by unresolved types.
- Fixed type checking with wildcard types in ForEach statements and with type parameter references.
- Fixed a typo in the ‘Receiver Type Mismatch’ semantic error.
- Cleaned up the `SpecialIntrinsicData` class.

## Dyvil REPL v0.10.0

- Improved Syntax Error reporting in the REPL for header, class and member declarations.
- Moved REPL input handling to the new `InputManager` class.
- Fixed backtick identifiers being supported incorrectly by the REPL.

## Dyvil Property Format v0.4.0

Dyvil v0.16.0
=============

- Added Initializer Blocks. #118
- Added the `init` keyword.
- Initializer Calls now use the `init` or `new` keyword.
- Constructors can now be declared using the `init` or `new` keyword.
- Annotations can now use arbitrary types rather than just a single identifier.
- Reference Types and Operators now use the `&` symbol. #183
- Parameters with the explicit type `auto` now cause a compilation error.
- Field References can now be used in Patterns. #189
- Improved Operator associativity for mixed left- and right-associative operators with the same precedence.
- Pattern Matching now supports usage of qualified and generic Types for Binding, Case Class and Object Patterns.
- Removed Bytecode Statements. #177

## Dyvil Library v0.16.0

- The `Predef.operator==` and `.operator!=` methods now check for nullness before invoking `equals`.
- Added the `Collection.emptyCopy` and `Collection.immutableBuilder` methods.
- Added the `Map.emptyCopy` and `Map.immutableBuilder` methods.
- Added the ANSI Color codes as constants to the `dyvil.io.Console` class.
- Added the `dyvil.reflect.Modifiers.VARIABLE_MODIFIERS` constant.
- Added the static `Collection.unorderedHashCode(Collection)` method from the `Set.setEquals(Set, Set)` method.
- Added the `Predef.function1`, `.function2` and `.function3` methods.
- Added a Builder implementation that produces `AppendList`s.
- Updated the `dyvil.io.AppendablePrintStream` and `.LoggerPrintStream` classes.
- Updated the `Set.setEquals` method to use `Collection.unorderedHashCode`.
- Updated documentation in the `Modifiers` class.
- Updated the `Map[K, V].putIfAbsent(K, V)` method to return either the old value or the given value rather than a boolean.
- Updated the Classes in the `dyvilx.lang.model.type` package to be usable in Pattern Matching.
- Updated `MapBasedSet`s.
- Updated the `dyvil.collection.List` documentation.
- Made the StringPoolWriter and -Reader classes from the compiler public and updated their API.
- Fixed the `dyvil.Analysis` file being a class file (`.dyv`) rather than a header (`.dyh`).
- Fixed the `MapBasedSet.add(E)` method implementation.
- Fixed the `ArraySet.operator -` implementation.
- Fixed the `Predef.assert(boolean, => any)` method not calling the closure.
- Renamed the `MutableMap.apply(int)` method to `withCapacity`.
- Cleaned up most Collection implementation classes.
- Cleaned up the EnumMap classes.
- Cleaned up the TupleMap classes.
- Cleaned up the TreeMap classes.
- Cleaned up the IdentityHashMap classes.
- Cleaned up the HashMap classes.
- Cleaned up the ArrayMap classes.
- Cleaned up the DynamicLinker class.
- Removed the `Number.apply` and `Integer.apply` methods.

## Dyvil Compiler v0.16.0

- Object Patterns now work correctly in combination with the Array Operator `=>`. #190
- Compound Calls are no longer resolved if the receiver is unresolved.
- Generic Types referencing non-generic classes now cause a compiler error.
- Override Return Type Check Errors are no longer reported if either type is unresolved.
- Named Argument Lists now check for duplicate keys.
- Marker Output now groups additional information between the title and the source line.
- Added the `IMember.getKind()` method and the `MemberKind` enum to describe the kinds of different members (fields, properties, variables, methods, …).
- Added the `ModifierUtil.JAVA_MODIFIER_MASK` constant.
- Added an error for Constructors without an implementation.
- Added formatting properties for Property Setters.
- Added Constant Folding for String.length.
- Updated the Constant Folding mechanism to work in more cases.
- Updated the way members are checked for invalid modifiers.
- Updated the `dyvil.tools.compiler.util.Util` class.
- Updated the Compiler Phase classes.
- Updated the FileFinder class in the compiler.
- Updated the Instruction Classes to remove unneccessary features.
- Improved Constructor parsing in contexts where constructors are disallowed.
- Simplified semantic analysis for Class and Method Parameters.
- Fixed Match Expressions with Strings with colliding hashes generating invalid / not working bytecode. #187
- Fixed Match Expressions with empty case actions causing compiler errors.
- Fixed Constructors causing compiler errors when occuring in invalid contexts.
- Fixed implicit type promotion between primitives working incorrectly.
- Fixed errors during the PRINT phase causing compilation to halt.
- Fixed external Object Classes causing compiler errors.
- Fixed Case Class Patterns working incorrectly when the matched value is on the stack.
- Fixed Inner Classes being resolved incorrectly for external classes.
- Fixed Token Lengths being computed incorrectly for Long, Float, Double, String and Backtick Identifier Tokens.
- Fixed Annotation Markers for invalid Element Types using invalid localization keys.
- Fixed Lambda Return Types being checked incorrectly for generic target methods.
- Fixed Try-Catch Statements causing error markers everywhere.
- Fixed Receiver Type compilation causing compiler errors in some cases.
- Fixed Receiver Types not being persisted in the bytecode. #182
- Moved constructor classes to the `dyvil.tools.compiler.ast.constructor` package.
- Moved the `AnnotationUtil.writeModifiers(…)` method to `dyvil.tools.compiler.ast.modifier.ModifierUtil`.
- Moved the `dyvil.tools.compiler.util.AnnotationsUtils` class to `dyvil.tools.compiler.ast.annotation`.
- Moved the `dyvil.tools.compiler.util.ParserUtil` class to `dyvil.tools.compiler.parser`.
- Renamed the `dyvil.tools.compiler.ast.annotation.AnnotationUtils` class to `AnnotationUtil`.
- Removed the `MethodWriter.getClassWriter()` method.

## Dyvil REPL v0.9.0

- The REPL now displays a pipe (`|`) character at the beginning of a multi-line input.
- The `:complete` REPL command no longer shows static methods for concrete instances.
- The `:complete` REPL command now outputs a message when no completions were found.
- Improved the `:help` REPL output format.
- Fixed the `:complete` REPL command not showing class parameters.

## Dyvil Property Format v0.4.0

- Added a DPF visitor for conversion to a special binary format. #184
- Added a DPF visitor that ignores all input.
- Implemented the `equals` and `hashCode` methods for all DPF AST classes.
- Improved the DPF Parser API.
- Fixed REPL class definition output message not showing the class kind.
- Fixed error reporting in the REPL working incorrectly for compiler errors.
- Fixed DPF Builder expressions being parsed incorrectly.
- Fixed DPF Builders accepting visitors incorrectly.
- Renamed the `dyvil.tools.dpf.flatmapper` package to `flatmap`.
- Moved and renamed the `dyvil.tools.dpf.ast.value.ValueCreator` class to to `dyvil.tools.dpf.converter.DPFValueVisitor`.
- Moved the `dyvil.tools.dpf.Parser` class to `dyvil.tools.dpf.converter.string`.
- Moved the `dyvil.tools.util.Printer` class to `dyvil.tools.dpf.converter.string`.

Dyvil v0.15.0
=============

- Added Property Initializers. #178
- Added Reified Method Type Parameters. #131
- Added Type Parameter annotations. #171
- Improved the Property syntax to be more flexible with statement lists. #175
- Improved the Match Cast syntax to be more flexible with statement lists. #176
- Disallowed properties without `get` or `set` tags. #175
- Removed implicit variables in ForEach statements. #173

## Dyvil Library v0.15.0

- Added the `AbstractBitSet`, a `Set[int]` base implementation written fully in Dyvil.
- Updated the Lang Header to `import` the `@Reified` annotation.
- Updated the various `List.set`, `.add`, `.insert` and `.subscript` methods to be more clearly defined and implemented.

## Dyvil Compiler v0.15.0

- Properties can now be used like methods at the use-site.
- Properties no longer implement the `IDataMember` interface.
- Added compiler support for the `@Reified` Type Parameter annotation.
- Added a check for Return Statements to see if the returned value is compatible with the return type.
- Added a new Property Test class that replaces the AbstractPropertyTest class.
- Added an additional check for expressions to be usable as statements.
- Updated Loop Resolution and Type-Checking for Conditions and Actions.
- Updated the `Util.*Eq` methods.
- Updated the `dyvil.tools.compiler.transform.Names` class.
- Improved the Formatter for Properties.
- Improved the way Field Initializers are compiled.
- Fixed Cast and Instance Check operators causing compiler errors.
- Fixed every type being a sub-type of the primitive type `void`.
- Fixed Anonymous Class Bodies being formatted incorrectly.
- Fixed Subscript Getters without arguments causing compiler errors when formatted.
- Fixed Type Aliases without a type causing compiler errors.
- Fixed Type Aliases not being resolvable in Class contexts.
- Fixed `TypeOperator.Types.TYPE` being initialized incorrectly.
- Fixed incompatible casts at runtime due to `ArrayType.getConcreteType` behaviour.
- Fixed ForEach Statements being type-checked incorrectly for arrays.
- Fixed Autocasting working incorrectly in ForEach Statements.
- Removed implicit Return for all expressions that cannot be usable as statements.
- Removed bloat methods in the `IProperty` interface.
- Removed property marker messages.
- Removed the `ITypeList` interface from the `IMethodSignature` super interfaces.
- Dropped compiler support for the implicit variables in ForEach statements.
- Renamed the `MarkerMessages` class to `Markers`.
- Renamed many methods of the `Markers` class to more useful names.
- Renamed the files that store Marker localizations.

## Dyvil REPL v0.8.1

- Updated Property resolution in the REPL.
- Updated the :complete REPL command for Compiler API changes.

## Dyvil Property Format v0.3.2

Dyvil v0.14.1
=============

## Dyvil Library v0.14.0

## Dyvil Compiler v0.14.1

- Added an additional info for which supertype Method Override Type Checking failed.
- Variables no longer report type errors for unresolved assignments.
- Fields no longer report type errors for unresolved assignments.
- The compiler no longer generates `@DyvilModifiers` annotations for internal compiler modifiers. #174
- Updated the `IPattern.writeInvJump` method to take a `matchedType` parameter.
- Improved Match Expression bytecode generation.
- Fixed Method Exception Lists via `throws` being parsed incorrectly.
- Fixed Return Statements that return from a Void method causing compiler errors on bytecode generation.
- Fixed Semicolon Inference working incorrectly with Wildcards and Ellipsis at the end of the line.
- Fixed incorrect Match Expression behaviour when used as a statement.
- Fixed Bridge Methods using incorrect Parameter indices in the body.
- Fixed Fields in Anonymous Classes not being assigned their default value in constructors.
- Fixed Bridge Methods being generated multiple times in some cases.
- Fixed a Type Parameter resolution bug + compiler error for External classes.
- Fixed the Type Signature Parser for Wildcard Types.
- Fixed Method Override Checking counting abstract methods as valid implementations.
- Fixed External Traits and Interfaces changing member modifiers.
- FIxed External Classes trying to add synthetic constructors and methods.
- Fixed Abstract Classes being treated like Interfaces.
- Fixed Abstract Classes not being allowed in `extends` clauses.
- Fixed Override Method Resolution working incorrectly for Generic Methods.
- Fixed Override Method Resolution working incorrectly for unresolved External Methods.
- Fixed Methods checking the overriden return type with raw types.
- Fixed Prefix Method resolution happening before local resolution.
- Disabled Override Method Resolution for static methods.

## Dyvil REPL v0.8.0

## Dyvil Property Format v0.3.2

Dyvil v0.14.0
=============

- Added Traits. #160
- Added Super Type Arguments. #165
- Added support for custom-named Property Setter Parameters. #161
- Interfaces now automatically declare fields as `public static final` and methods as `public abstract`.
- Concrete methods in Interfaces are no longer permitted.
- Removed the Swap Operator. #166

## Dyvil Library v0.14.0

- Added `subscriptRef` methods for all Array Types.
- Added `subscriptRef` methods for Lists and Maps.
- Updated the `Map.apply(Entry...)` method to take `Entry`s rather than tuples.
- Improved immutable Tuple Map builders.
- Fixed infinite recursion in Tuple Maps.
- Cleaned up the SingletonMap class.
- Removed the `MutableMap.` and `ImmutableMap.apply(Object, Object)` methods.

## Dyvil Compiler v0.14.0

- Properties now use two underlying methods.
- Added compiler support for `trait`s.
- Added compiler support for the `subscriptRef` method. #168
- Added an error for when Type Parameters are referenced in Type Operators. #162
- Improved Override Checking for Properties.
- Fixed Super/Subtype relationship behaviour being implemented incorrectly for Reference Types.
- Fixed Reference Types not converting Primitive Types in place of Type Parameters to Object Types.
- Fixed Properties being added to the Field list of Class Bodies.
- Removed the `ExternalProperty` class.
- Removed unused `Property` constructors.
- Cleaned up the `Property` class.

## Dyvil REPL v0.8.0

- Added an Applet version of the REPL.
- Updated the REPL I/O system to use custom Inputs and Outputs.
- Updated several REPL APIs.

## Dyvil Property Format v0.3.2

Dyvil v0.13.0
=============

- Added extension Lambda Parameters for Closures with Implicit Values.
- Closures in the context of an Extension Lambda Type now provide an implicit value pointing to the first parameter of the resulting Lambda Expression.
- Added List Types, with support for mutability modifiers. #120
- Added mutability modifiers for Array Types. #158
- Added support for mutability modifiers in Map Types. #159
- Implemented Operator Precedence rules for the `|` and `&` operators in Patterns. #156

## Dyvil Library v0.13.0

- Added the `dyvilx.LangModel` header.
- Added `@Mutating` Annotations for the `Array.subscript_=` methods.
- Added the `dyvil.annotation.Mutable` annotation.
- Added `@Immutable` and `@Mutating` annotations for the Iterator classes in the `dyvil.collection.iterator` package.
- Made the `Immutable` Interface an Annotation and updated all depending library classes accordingly.
- Made the `Predef.run(any, any => any)` and `Predef.use(any, any => void)` methods use the new Extension Lambda Type Parameters.
- Updated the `dyvil.annotation.Immutable` documentation and meta-annotations.
- Moved classes and subpackages from the `dyvil.lang.ref` package to the `dyvil.ref` package.
- Moved the `dyvil.runtime.ReferenceFactory` class to the `dyvil.ref` package.
- Moved classes from the `dyvil.reflect.types` package to the `dyvilx.lang.model.type` package.
- Moved the `dyvil.lang.Type` class to the `dyvilx.lang.model.type` package.
- Moved `dyvil.util.Immutable` to the dyvil.annotation package.
- Renamed the `dyvil.annotation.mutating` annotation to `Mutating`.
- Renamed some methods in the `dyvil.lang.Type` class.

## Dyvil Compiler v0.13.0

- Added the Mutability enum in the compiler for mutability modifiers in Array, Map and List Types.
- Added the `IType.getMutability()` interface method.
- Added the `CaptureHelper` class.
- Added syntax support for Nested Methods.
- Added the `extension` boolean flag for Lambda Types, available via getters and setters in the IType interface.
- Updated the compiler to support the new ref class locations.
- Updated the Capture System to work correctly for Anonymous Classes.
- Updated the `Types.combine(IType, IType)` method.
- Updated the Compiler to adapt to changes to the Immutable Annotation / class.
- Updated the Modifier Parsing mechanism to recognize all modifiers in all contexts.
- Improved behavior of Captures and Reference Variables in Inc Operators.
- Improved Try Statement type checking and error reporting.
- Improved Try Statement parsing.
- Improved Try Statement formatting.
- Fixed the Field Assignment type mismatch marker info.
- Fixed Or Patterns generating invalid bytecode sequences.
- Fixed Field Type and Method Return Type Annotations being compiled incorrectly.
- Fixed empty (null) Type Paths for Type Annotations causing compiler errors.
- Fixed the `SimpleMethodVisitor` not reading basic non-Dyvil parameter modifiers.
- Fixed Case Classes looking for existing Apply Methods incorrectly.
- Cleaned up the `TypeParser` class.
- Cleaned up the `MethodWriterImpl` class.
- Cleaned up the `MapType` class.
- Cleaned up the `ClassBodyParser` class.
- Replaced the `IContext.getAccessibleImplicit():IAccessible` method with the new `.getImplicit():IValue` method.
- Removed the `Types.findCommonSuperType(IType, IType)` method.
- Moved the `MapTypes` class from `MapExpr` to `MapType`.
- Renamed the `MethodWriter.writeInsn(int, int)` method to `writeInsnAtLine`.
- Renamed the `MapExpr.Types` class to `MapTypes`.

## Dyvil REPL v0.7.0

## Dyvil Property Format v0.3.2

Dyvil v0.12.0
=============

- Added Reference Types.
- Added Variable, Field and Array References.
- Added by-reference Method Parameters using the `var` modifier.
- Added And Patterns using the `&` operator. #152
- Added Or Patterns using the `|` operator. #151
- Added Object Patterns for matching Object Class Instances.
- Added Expression (non-jump) Opcodes for the 0-based `IF*` jump opcodes.
- Binding Patterns can now be directly declared with a type as if they were a variable.
- Type Check Patterns now accept primitive types.
- Unhandled Throwables are now only reported as an error when they are subtypes of `java.lang.Exception`.

## Dyvil Library v0.12.0

- Added Inc Methods for the `int`, `long`, `float` and `double` data types to `Predef`.
- Added the `@DyvilModifiers` annotation
- Added the `Option.??` operator as a delegate to `.orElse`.
- Added the `Predef.assert(boolean)` method.
- Added the `Predef.assert(boolean, => any)` method.
- Added getters for `Marker.message` and `Marker.position`.
- Updated the `DyvilLexer.parseInteger` implementation under the assumption that numbers can never be negative.
- Improved the symbols used to mark the location of Markers in a line.
- Fixed the Intrinsic annotations for `subscript_=`, `lenght` and `isEmpty` in all array classes.
- Removed the `.toString()` methods in all `Simple*Ref` classes.
- Removed old Dyvil-specific modifier annotations.
- Removed the error for Marker Messages without a position.
- Removed the `Marker()` and `Marker(ICodePosition)` constructors.
- Moved the `dyvil.tools.compiler.library.Library.getClassLocation(Class)` method to `dyvil.reflect.ReflectUtils`.
- Renamed the `Option.!` and `.?` operators to `.get` and `.isPresent` and added the previous names as delegate methods.

## Dyvil Compiler v0.12.0

- Added compiler support for the new @DyvilModifers annotation.
- Added support for qualified type names in expressions.
- Added an analysis to check for effectively final variables and parameters.
- Added the `IType.getRefClass()` interface method.
- Added an error for Lambda Types with missing Return Types.
- Added the `PackageDeclaration.toString()` implementation.
- Inc Operator Assignment Resolution now happens in the `RESOLVE` phase, as intended.
- Updated Variable Capture to be more consistent for non-final variables. #154
- Updated the way Class Parameters are read from external classes.
- Updated Lambda Expression -> Method Reference conversion not working for primitives.
- Improved intrinsic infix and postfix operator resolution.
- Improved the Pattern Parser implementation for negative numbers.
- Improved Method Override Checking to not check the same class multiple times.
- Improved If, While and Do-While semantic error messages.
- Internationalized all Syntax Error Messages in the Dyvil Parsers.
- Case Class Patterns can now match correctly for non-public class parameters as long as a getter method is available.
- Fixed the `AUTO_DUP` instruction being implemented incorrectly for 2Word-1Word and 1Word-2Word variations.
- Fixed Parameters Indexes being set incorrectly in some cases.
- Fixed Parameters compiling annotations incorrectly.
- Fixed Capture in Statements Lists without variables causing compiler NPEs.
- Fixed Subscripts not being converted to Arrays in some cases.
- Fixed References to unresolved fields causing compiler errors.
- Fixed intrinsic prefix operators being resolved incorrectly.
- Fixed primitive values that need to be wrapped for instance methods being allowed for use in Lambda -> Function Reference conversion.
- Fixed Match Expressions generating incorrect type casts for the matched value.
- Fixed Binding Patterns generating special switch check variables when they are not needed.
- Fixed empty Match Expressions causing compiler errors upon bytecode generation when trying to generate empty LookupSwitches.
- Fixed Tuple Patterns with only one Pattern not being extraced automatically.
- Fixed a missing word in the type parameter bound syntax error message.
- Fixed Method Override Checking causing compiler errors with unresolved interface types.
- Fixed Field Assignments generating casts when used as statements.
- Fixed the `Property.toString()` implementation causing NPEs when either getter or setter modifiers are null.
- Fixed Variables causing JVM verification errors when the initial value is a match expression or creates variables.
- Fixed Match Expressions with invalid Patterns causing compiler errors.
- Fixed Increment / Decrement Operators being captured incorrectly. #150
- Dropped compiler support for old modifier annotations.
- Removed redundand Class Parameter local index setters.
- Removed Unbox Patterns.
- Removed the unused `IValue.COMPOUND_CALL` constant.
- Cleaned up the `PrimitiveType`, `IObjectType` and `LambdaType` classes.
- Cleaned up the `PatternParser` class.
- Cleanup up the `MatchCase` class.
- Renamed the `I18n` class to `MarkerMessages`.
- Renamed the `IVariable.isCapturable()` method to `isReferenceCapturable` for clarity.

## Dyvil REPL v0.7.0

- Added the `:javap` REPL Command.
- Fixed the REPL Parser generating NPEs on invalid expressions.
- Fixed Input Errors being caused when quitting the REPL.

## Dyvil Property Format v0.3.2

- Fixed the accept method implementation for DPF String Interpolations.

Dyvil v0.11.1
=============

## Dyvil Library v0.11.1

- Added comments to the `dyvil.Collections` and `dyvil.Math` headers.
- Added the `dyvil.tools.parsing.position.CodePosition.toString()` implementation.
- Updated the `dyvil.JavaUtils` header to add type aliases for common Java Collection classes.
- Fixed the `AbstractHashSet.ensureCapacity()` method creating an infinite loop.
- Semantically cleaned up the Collection Interfaces by getting rid of various unchecked warnings.
- Renamed `List.fromNil`, `Set.fromNil`, `Collection.fromNil` and `Map.fromNil` to `.empty`.

## Dyvil Compiler v0.11.1

- Invalid Field Assignments are no longer reported as unresolvable if the receiver cannot be resolved.
- Fixed generic Type Aliases with multiple Type Variables being expanded incorrectly.
- Fixed If Expressions without an Else clause being compiled incorrectly.
- Fixed Increment and Decrement Operators not checking if the field is final.
- Fixed Compound Operators acting like postfix instead of prefix Increment / Decrement operators.
- Fixed the `StatementList.isResolved()` method returning false for empty Statement Lists.
- Fixed Return Statements generating invalid bytecode when used as Expressions.
- Fixed Dyvil-specific Modifiers on Methods not being compiled to their respective annotations.
- Fixed the `WildcardValue.toString()` implementation returning `...` instead of `_`.
- Renamed `IMethod.getExceptions()` to `.getInternalExceptions()`.

## Dyvil REPL v0.6.2

- Fixed REPL method and field access causing JVM errors in some cases.
- Fixed REPL Exception Stack Trace Filtering working incorrectly in certain situations when an `ExceptionInInitializerError` is thrown.

## Dyvil Property Format v0.3.1

- Made most of the AST Node Classes `Expandable` to integrate more smoothly with FlatMap conversion.
- Boolean Values supplied to the `DyvilValueVisitor` are now automatically converted to `dyvil.lang.Boolean` instances.
- Properties and Qualified Nodes / Node Accesses are now stored in separate lists in Nodes.
- Added `converter.NameAccess.toString()` implementation.
- Added `converter.StringInterpolation.toString()` implementation.
- Fixed `FlatMapConverter` working incorrectly for nested and qualified nodes.

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
