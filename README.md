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

Dyvil compiles to JVM bytecode and runs on the Java Virtual Machine. Programs written in Dyvil benefit from the JVM's
automatic memory management and garbage collection. The language is compatible with Java 8 and later versions.

**Supported Operating Systems:** Dyvil can be used on any platform that supports the JVM, including Windows, macOS,
Linux, and other Unix-like systems.

**Mobile Platforms:** As a JVM-based language, Dyvil can target Android through standard Android development tools
and workflows. Targeting iOS or other non-JVM mobile platforms is not directly supported.

## Use Cases

Dyvil is a general-purpose language suitable for a variety of application domains:

- **Backend and Server Applications:** Full JVM ecosystem compatibility makes it suitable for server-side development
- **Desktop Applications:** Can be used with JavaFX, Swing, or other JVM-based UI frameworks
- **Command-Line Tools:** The language's concise syntax is well-suited for utility and automation scripts
- **Library Development:** Full Java interoperability allows creating libraries usable from Java and other JVM languages

Dyvil is not specifically designed for web frontend development (browser-based JavaScript), though it can generate
code for JVM-based web frameworks.

## Language Features

**Standard Library:** Includes an extensive collection framework, I/O utilities, and general-purpose APIs. The standard
library is designed to integrate seamlessly with Java's standard library.

**Concurrency:** Dyvil has access to the full range of Java concurrency utilities, including threads, executors,
and concurrent collections. The language does not provide a built-in concurrency model like Go's goroutines, but
supports Java's standard threading and parallelization approaches.

**Networking:** Network programming is supported through Java's networking APIs (java.net, java.nio) and can use
any JVM-based networking libraries and frameworks.

**Interoperability:** Full bidirectional compatibility with Java allows using existing Java libraries and frameworks,
and Dyvil code can be called from Java without special considerations.

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
