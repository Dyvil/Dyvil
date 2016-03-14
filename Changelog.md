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
