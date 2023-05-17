# Writing a programming language

Coding along to the Crafting Interpreters book by Bob Nystrom

We implement a programming language called Lox, and write a 
tree walking interpreter for it

1. First is the Scanner (aka Lexer/Tokenizer) -> Takes the source code which is
a list of characters and returns a list of tokens. We have the Token and TokenType
classes to help us represent our Tokens

2. Second is the Parser -> Takes a list of tokens, and returns a syntax tree (a 
tree structure to represent expressions). We have the Expr classes to help us
represent our syntax tree nodes

This is a work-in-progress (as all things in life are)