The Dyvil programming language
==============================

Dyvil (Dynamic Visual / Virtual Language) is a programming language that is based on Java and the JVM, but extends the language with several new language constructs.

### General

- Semicolon Inference
- Modules
- Fewer keywords

### Import Declarations

- Multi-Imports: `import java.util.{Math, Random}`
- Import aliases: `import java.lang.String => jString` ... `jString s = "..."`#
- 'Using' for static imports: `using java.util.Math.PI`

### Class Structure

- Operator overloading: `3 + 3` -> `3.+(3)` ... `this.add(that)` -> `this.+(that)` -> `this + that`
- Implicit / Extension Methods: `public implicit void doStuff(String s) {}` ... `"test".doStuff()`
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
- Omitting the dot and parenthesis when calling a method: `this.foo(that.bar())` -> `this foo that bar`
- Tuples: `new Tuple2("hello", "world")` -> `("hello", "world")`
- Sugar Function Types: `Function<String, Integer>` -> `(String) => Integer`
- More ways to control your loops: `for (int i : array) { ... /* what's the index? */ }` -> `for (int i : array) { println($index) }`

### Special Language Constructs

- Method Invokation Bytecode Substitutes: `3.+(3)` does not get translated to a method call to `dyvil.lang.Int.$plus(int)`, but to the direct `iadd` bytecode instruction
- Bytecode Expressions: `return 1 + 2` -> `@ { iconst_1; iconst_2; iadd; ireturn; }`
- Custom Constructors
