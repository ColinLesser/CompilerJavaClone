# CompilerJavaClone
Design and Construction of a Compiler for a Java clone
A large-scale project consisting of developing a lexical analyzer, parser, abstract syntax tree, symbol table, activation code, and intermediate code generation, and finally generating assembly code will be implemented. 

First, I developed a lexical analyzer to define what is a legal token and what is not. The lexical analyzer breaks down syntaxes into series of tokens. If a token is deemed invalid, it generates an error. I used the Java Compiler Compiler (JavaCC) facilitate the development of this step.

Next, I wrote a parser. This part is where data is broken down into smaller elements coming from the lexical analysis phase. The main function of the parser is to take input in the form of sequence of tokens and produces a parse tree. Again, I used JavaCC to complete this phase. Along with that, this is where I defined the grammar in which the language is based off of.

Third, a semantic analyzer was made. I developed a symbol table and then it will perform type checking. The construction of the symbol table involved implementing visitors that traverse the abstract syntax tree. 

Lastly, the code generation phase using MIPS architecture. I traverse the AST and emit assembly code which can be used to generate executable code.
