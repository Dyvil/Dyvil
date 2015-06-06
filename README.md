The Dyvil programming language
==============================

Dyvil (Dynamic Visual / Virtual Language) is a programming language that is based on Java and the JVM, but extends the language with several new language features including operator overloading, function types, tuples and many more.

### General

- Semicolon Inference
- Headers for include declarations and operator definitions

### Import Declarations

- Multi-Imports: `import java.util.{ Math, Random }`
- Import aliases: `import java.lang.String => jString` ... `jString s = "..."`
- 'Using' for static imports: `using java.util.Math.PI`
- 'Include' for headers

### Class Structure

- Local Type Inference: `var i = 10` -> `int i = 10`, `String s = if (...) "a" else "b"`
- Operator overloading: `3 + 3` -> `3.+(3)` ... `this.add(that)` -> `this.+(that)` -> `this + that`
- Custom Operators: `infix operator +- { left, 130 }`
- Postfix Methods: `postfix void print(String s) = println s` ... `"test" print`
- Infix Methods: `infix int +-(int i, int j) = i + -j` ... `int i = 10 +- 1`
- Prefix Methods: `class Int { prefix int -() = ... }`
- Properties: `private String s_; private String s { get: return s_; set: s_ = s; }` ... `this.s = "Hello World"` -> `this.set$s("Hello World")`
- More modifiers:
 - `lazy int i = notStored()` (lazily evaluated variables)
 - `public void foo(ref int i)` (call-by-reference)
 - `derived float f;` (sub-classes, but not package)
 - `inline void l()` (force inline)
 - `sealed class Utils` (private to library)
 - `functional interface IInterface` (@FunctionalInterface for lambda expressions)
 - `override void toString()`(@Override shortcut)

### Expressions

- 'Everything is an expression'
- Omitting the dot when accessing a field: `this.x = 5` -> `this x = 5`
- Omitting the dot and parenthesis when calling a method: `System.out.println(i)` -> `System out println i`
- Apply and Update methods: `[int] x = ...; x(y)` -> `x.apply(y)`, `x(y) = z` -> `x.update(y, z)`
- Format Strings: `var s = "Strings"; @"Format \(s)"`
- Tuples: `new Tuple2("hello", "world")` -> `("hello", "world")`
- Sugar Function Types: `Function<String, Integer>` -> `(String) => Integer`
- Lambda Expressions: `Strings => String fun = s => s toUpperCase`
- More ways to control your loops: `for (int i : array) { ... /* what's the index? */ }` -> `for (int i : array) { println $index }`
- Ranges: `for (var i : 1 .. 10) println i` prints all numbers from 1 to 10 (inclusive)

### Special Language Constructs

- Method Invocation Bytecode Substitutes: `3.+(3)` does not get translated to a method call to `dyvil.lang.Int.$plus(int)`, but to the direct `iadd` bytecode instruction
- Bytecode Expressions: `return 1 + 2` -> `@ { iconst_1; iconst_2; iadd; ireturn; }`
- Literal Convertibles: Classes with special Annotations that can be instantiated with literals (numbers, Strings, Arrays, Tuples, `nil`, Booleans)
