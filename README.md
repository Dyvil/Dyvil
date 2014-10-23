Dyvil
=====

Dyvil (Dynamic Visual Language) is a programming language that extends Java with several new language constructs.
These include:

- Omitting the dot when accessing a field: `this.x = 5` -> `this x = 5`
- Omitting the dot when calling a method: `this.foo(that.bar())` -> `this foo that bar`
- Tuples: `new Tuple2("hello", "world")` -> `("hello", "world")`
- Operator overloading: `3 + 3` -> `3.+(3)` ... `this.add(that)` -> `this.+(that)` -> `this + that`
- Advanced Function Types: `Function<String, Integer>` -> `(String) => Integer`
- Extension Methods: `public implicit void doStuff(String s) {}` `"test".doStuff()`
- More ways to control your loops: `for (int i : array) { ... /* what's the index? */ }` -> `for (int i : array) { println($_index) }`
- More modifiers:
 - `lazy int i = notStored()` (lazily evaluated variables)
 - `public void foo(ref int i)` (call-by-reference)
 - `derived float f;` (sub-classes, but not package)
 - `inline void l()` (force inline)
