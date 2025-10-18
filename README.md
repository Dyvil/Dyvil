# The Dyvil Programming Language

![Java CI](https://github.com/Dyvil/Dyvil/workflows/Java%20CI/badge.svg)
[![Join the Chat](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/Clashsoft/Dyvil?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

**Dyvil** is a multi-paradigm, general purpose programming language that is based on Java and the JVM.
It is compiled, statically and strongly typed and supports object-oriented, functional and imperative
programming styles. The modern and extensible syntax is based on Swift, Kotlin and Scala.

As a new programming language in active development, the main goals of the Dyvil project are the following:

- to provide modern syntax and semantics
- to avoid common boilerplate code
- to achieve performance comparable to Java programs
- to be fully compatible and interoperable with Java and other JVM languages like Scala, Kotlin or Groovy

In addition to the **Dyvil-to-JVM-Bytecode compiler**, the toolchain consists of an executable **REPL**, a full-fledged
**standard library** including an extensive collection framework, the **Dyvil Property Format** library and
specification, and the **GenSrc** source code generation and text template specialization tool. Information on all
components can be found in the [Language Reference][1].

The syntax and semantics of the language are still highly experimental and are likely to change in upcoming
releases. Therefore it is not recommended to use Dyvil in any kind of production environment.
Feature or change requests or bug reports in the form of GitHub Issues or Pull Requests are welcome and encouraged.

## Platform and Runtime

Dyvil compiles to JVM bytecode and runs on the Java Virtual Machine. Programs written in Dyvil benefit from the JVM's automatic memory management and garbage collection. The language is compatible with Java 8 and later versions and can be used on any platform that supports the JVM, including Windows, macOS, Linux, and other Unix-like systems.

## Use Cases

Dyvil is a general-purpose programming language with full Java interoperability. It supports all Java APIs and libraries, making it suitable for any application domain where Java is used, including backend services, desktop applications, command-line tools, and Android development.

## Language Features

Dyvil provides modern language features designed to reduce boilerplate and improve code readability:

- **Type Inference:** Variable types can be inferred using the `let` and `var` keywords
- **Lambda Expressions:** Concise syntax for functional programming with closures and higher-order functions
- **Extension Methods:** Add methods to existing classes without inheritance
- **Pattern Matching:** Powerful `match` expressions for control flow and data extraction
- **String Interpolation:** Embed expressions directly in string literals
- **Operator Overloading:** Define custom operators and customize existing ones
- **Properties:** Built-in property syntax with customizable getters and setters
- **Tuples:** First-class support for tuple types and destructuring
- **Optional Types:** Nullable and non-nullable type annotations for better null safety
- **Class Extensions:** Extend existing classes with new functionality including operators and properties

For a complete list of features and detailed documentation, see the [Language Reference][1].

## Links

### Info

- [Blog](http://dyvil.github.io/)
- [Language Reference][1]

### Downloads

- [GitHub Releases](https://github.com/Dyvil/Dyvil/releases) (up to v0.44.0)
- [Maven](https://mvnrepository.com/artifact/org.dyvil) (from v0.44.1)
- [Release Statistics](https://docs.google.com/spreadsheets/d/13imk47mUlV9nbi2fsGAXuUr1f3cOdWhTyi_AoKlIgqA/edit?usp=sharing)

### Development

- [GitHub Repository](https://github.com/Dyvil/Dyvil)
- [Issue Tracker](https://github.com/Dyvil/Dyvil/issues)
- [Changelog](https://github.com/Dyvil/Dyvil/releases)

### Community

- [Gitter Chat](https://gitter.im/Clashsoft/Dyvil)
- [Subreddit](https://www.reddit.com/r/Dyvil/)

[1]: https://dyvil.gitbooks.io/dyvil-language-reference/content/
