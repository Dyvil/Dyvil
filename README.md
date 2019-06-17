# The Dyvil Programming Language

[![Master Build Status](https://travis-ci.org/Dyvil/Dyvil.svg?branch=master)](https://travis-ci.org/Dyvil/Dyvil)
[![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/Dyvil/Dyvil.svg)](http://isitmaintained.com/project/Dyvil/Dyvil "Average time to resolve an issue")
[![Percentage of issues still open](http://isitmaintained.com/badge/open/Dyvil/Dyvil.svg)](http://isitmaintained.com/project/Dyvil/Dyvil "Percentage of issues still open")
[![Join the Chat](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/Clashsoft/Dyvil?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

| Module | Link |
|--------|------|
| All |  [![Download All](https://api.bintray.com/packages/dyvil/maven/dyvil/images/download.svg) ](https://bintray.com/dyvil/maven/dyvil/_latestVersion) |
| Library | [![Download Library](https://api.bintray.com/packages/dyvil/maven/library/images/download.svg) ](https://bintray.com/dyvil/maven/library/_latestVersion) |
| Compiler | [![Download Compiler](https://api.bintray.com/packages/dyvil/maven/compiler/images/download.svg) ](https://bintray.com/dyvil/maven/compiler/_latestVersion) |
| REPL | [![Download REPL](https://api.bintray.com/packages/dyvil/maven/repl/images/download.svg) ](https://bintray.com/dyvil/maven/repl/_latestVersion) |
| GenSrc | [![Download GenSrc](https://api.bintray.com/packages/dyvil/maven/gensrc/images/download.svg) ](https://bintray.com/dyvil/maven/gensrc/_latestVersion) |

| Branch | Status |
|--------|--------|
| master | [![Master Branch Build Status](https://travis-ci.org/Dyvil/Dyvil.svg?branch=master)](https://travis-ci.org/Dyvil/Dyvil) |
| develop | [![Develop Branch Build Status](https://travis-ci.org/Dyvil/Dyvil.svg?branch=develop)](https://travis-ci.org/Dyvil/Dyvil) |
| bugfix | [![Bugfix Branch Build Status](https://travis-ci.org/Dyvil/Dyvil.svg?branch=bugfix)](https://travis-ci.org/Dyvil/Dyvil) |

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

---

## Links

### Info

- [Blog](http://dyvil.github.io/)
- [Language Reference][1]

### Downloads

- [GitHub Releases](https://github.com/Dyvil/Dyvil/releases) (up to v0.44.0)
- [Maven](https://mvnrepository.com/artifact/org.dyvil) (from v0.44.1)
- [Bintray](https://bintray.com/dyvil/maven) (from v0.44.1)
- [Release Statistics](https://docs.google.com/spreadsheets/d/13imk47mUlV9nbi2fsGAXuUr1f3cOdWhTyi_AoKlIgqA/edit?usp=sharing)

### Development

- [GitHub Repository](https://github.com/Dyvil/Dyvil)
- [Issue Tracker](https://github.com/Dyvil/Dyvil/issues)
- [Travis CI](https://travis-ci.org/Dyvil/Dyvil)

### Community

- [Gitter Chat](https://gitter.im/Clashsoft/Dyvil)
- [Subreddit](https://www.reddit.com/r/Dyvil/)

---

## How to try it out

### The REPL

1. Download the `dyvil-X.Y.Z.jar` file from a [Release Page](https://github.com/Dyvil/Dyvil/releases).
2. View the [REPL Reference](https://dyvil.gitbooks.io/dyvil-language-reference/content/tools/repl.html) for usage information and examples.
3. Run the downloaded jar file with the command

        $ java -jar dyvil-X.Y.Z.jar

4. Start typing expressions, declarations and commands.

See  for more details.

### The Compiler

1. Download the `dyvil-X.Y.Z.jar` file from a [Release Page](https://github.com/Dyvil/Dyvil/releases).
2. View the [Compiler Reference](https://dyvil.gitbooks.io/dyvil-language-reference/content/tools/dyvil-compiler.html) to find out how to use compiler command line arguments and configurations.
3. Run the downloaded jar file with the command

        $ java -cp dyvil-X.Y.Z.jar dyvilx.tools.compiler.Main <args...>

4. The generated class files should now be in the specified output directory.

[1]: https://dyvil.gitbooks.io/dyvil-language-reference/content/
