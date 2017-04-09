The Dyvil programming language
==============================

[![Build Status](https://travis-ci.org/Dyvil/Dyvil.svg?branch=master)](https://travis-ci.org/Dyvil/Dyvil)
[![Join the Chat](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/Clashsoft/Dyvil?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

[![In Progress](https://badge.waffle.io/Dyvil/Dyvil.svg?label=In%20Progress&title=In%20Progress)](http://waffle.io/Dyvil/Dyvil)
[![Ready](https://badge.waffle.io/Dyvil/Dyvil.svg?label=ready&title=Ready)](http://waffle.io/Dyvil/Dyvil)
[![Pull Request Stats](http://issuestats.com/github/Dyvil/Dyvil/badge/pr?style=flat)](http://issuestats.com/github/Dyvil/Dyvil)
[![Issue Stats](http://issuestats.com/github/Dyvil/Dyvil/badge/issue?style=flat)](http://issuestats.com/github/Dyvil/Dyvil)

**Dyvil** is a multi-paradigm, general purpose programming language that is based on Java and the JVM.
It is a compiled, statically and strongly typed language that supports object-oriented, functional and imperative
programming styles. The language features many high-level constructs as well as an extensible and expressive syntax,
making it highly useful for both rapid and safe prototyping, and the creation of domain-specific languages.

In addition to the **Dyvil-to-JVM-Bytecode compiler**, the toolchain consists of an executable **REPL**, a full-fledged
**standard library** which includes an extensive collection framework, the **Dyvil Property Format** library and
specification, and the **GenSrc** source code generation and text template specialization tool. Information on all
components can be found in the [Language Reference][1].

As a new programming language in active development, the main goals of the Dyvil project are the following:

- to **enhance** Java with modern programming language elements
- to **avoid** common boilerplate code
- to **give** programmers full **control** over the syntax and semantics of their code
- to be fully **compatible** and **inter-operable** with Java and other JVM languages like Scala or Groovy

Because the syntax and semantics of the language are still highly experimental and subject to change in upcoming
releases, it is not recommended to use Dyvil in any kind of production environment. Feature or change requests in the
form of GitHub Issues or Pull Requests are welcome and encouraged.

[![Throughput Graph](https://graphs.waffle.io/Dyvil/Dyvil/throughput.svg)](https://waffle.io/Dyvil/Dyvil/metrics)

---

## Links

### [Language Reference][1]

### [Dyvil on Reddit](https://www.reddit.com/r/Dyvil/)

---

## How to try out Dyvil

### The REPL

1. Download the `dyvil-vX.Y.Z.jar` file from a [Release](https://github.com/Dyvil/Dyvil/releases).
2. Run it with
        
        $ java -jar dyvil-vX.Y.Z.jar
        
3. Start typing declarations and commands.

See [the REPL Reference](https://dyvil.gitbooks.io/dyvil-language-reference/content/tools/repl.html) for more details.

### The Compiler

1. Download the `dyvil-vX.Y.Z.jar` files from a [Release](https://github.com/Dyvil/Dyvil/releases).
2. Visit [the Compiler Reference](https://dyvil.gitbooks.io/dyvil-language-reference/content/tools/dyvil-compiler.html) to find out how to use compiler command line arguments and configurations.
3. Run it with
        
        $ java -cp dyvil-vX.Y.Z dyvil.tools.compiler.Main <args...>
        
4. The generated class files should now be in the specified output directory.

[1]: https://dyvil.gitbooks.io/dyvil-language-reference/content/
