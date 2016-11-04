License
=====

Copyright (c) 2016, Johns Hopkins University Applied Physics Laboratory
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

1. Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.

3. Neither the name of the copyright holder nor the names of its
contributors may be used to endorse or promote products derived from
this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

About
=====

Exterminator is a proof of concept user interface for the
[SLMech](https://www.github.com/jhuapl-saralab/slmech) program
verification tool. Exterminator is intended to provide a familiar
debugger-like interface for stepping through a program proof using the
SLMech proof tactics. Our motivation is the observation that while
programming, developers often construct informal correctness proofs in
their heads then use traditional debuggers to validate their belief on
individual runs of a program. Exterminator seeks to extend this by
allowing developers to step through all program runs simultaneously in
the form of logical assertions. In short, we view Exterminator as
"like a debugger, but more so."

Exterminator is in a very nascent state. It is able to load simple
SLMech programs and step through the proofs. It recognizes some
assertions about heap and store variables and attempts to simplify
them into a view similar to that used to inspect the heap or stack in
a standard debugger. It also includes a simple proof editing
capability.

Planned enhancements include:
* Improved recognition of heap/store assertions
* Tactic navigation and suggestion
* Graphical depiction of heap decomposition
* Mode options for detailed vs. simplified display
* Support for Coq 8.5

Dependencies
============

Exterminator is written in Java and uses Maven to build. It depends
on:
* Java 7
* Maven 3
* Antlr 4.5
* Swing 1.6.1
* JUnit 4.12 (for tests)
* SLMech
* Coq 8.4

Building
========

```
$ mvn package -DskipTests
```

Running
=======

```
$ java -jar ./target/exterminator-0.1.jar
```
