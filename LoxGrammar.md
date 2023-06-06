Context-free grammar --> Play a game to generate strings

Parsers play that game in reverse.

Given a string (a series of tokens) -- we map those tokens to terminals in the
grammar to figure out which rules could have generated that string

Here's the Lox expression grammar:
expression -> literal | unary | binary | grouping;

literal -> NUMBER | STRING | "true" | "false" | "nil";

grouping -> "(" expression ")";

unary -> ("-"|"+") expression;

binary -> expression operator expression

operator -> "==" | "!=" | "<" | "<=" | ">" | ">=" | "+" | "-" | "*" | "/"

This is a valid expression:
6 / 3 - 1

This can be parsed in two ways:
6 / 3 -> binary
6 -> literal, / -> operator, 3 -> literal (this expression is done)
(6/3) - 1 -> binary
(6/3) -> expression, '-' -> operator, 1 -> literal

In other words, the grammar allows seeing the expression as (6/3) - 1 = 1

3 - 1 -> binary
3 -> literal, '-' -> operator, 1 -> literal (this expression is done)
6 / (3 - 1) -> binary
6 -> literal, '/' -> operator, (3 - 1) -> expression

In other words, the grammar allows seeing the expression as 6 / (3 - 1) = 3

The way we address this ambiguity is by defining rules for precedence and associativity

Precedence determines which operator is evaluated first in an expression containing
a mixture of different operators. Operators with higher precedence are evaluated
before operators with lower precedence. Higher precedence operators are said to
"bind tighter"

Associativity determines which operator is evaluated first in a series of the
same operator. When an operator is left associative, operators on the left evaluate
before those on the right

'-' is left associative, so
5 - 3 - 1 ==> (5 - 3) - 1 ==> 1

Assignment on the other hand is right associative,
a = b = c ==> a = (b = c)

Without well-defined precedence and associativity, an expression that uses multiple
operators is ambiguous -- it can be parsed into different syntax trees, which could evaluate
to different results

Lox has the same precedence rules as C: (Lowest to highest)
Equality        ==, !=              Left associativity
Comparison      >, >=, <, <=        Left associativity
Term            -, +                Left associativity
Factor          /, *                Left associativity
Unary           !, -                Right associativity

Right now, the grammar stuffs all expression types into a single expression rule.
That same rule is used as the non-terminal for operands, which lets the grammar
accept any kind of expression as a subexpression, regardless of whether the precedence
rules allow it. We fix that by stratifying the grammar. We define a separate rule
for each precedence level

expression      -> ...
equality        -> ...
comparison      -> ...
term            -> ...
factor          -> ...
unary           -> ...
primary         -> ...

Each rule here only matches expressions at its precedence level or higher
For example, unary matches a unary expression like !negated or a primary expression
like 1234
And term can match 1 + 2 but also 3 * 4 / 5
The final primary rule covers the highest precedence forms - literals and parenthesized expressions

Now we need to fill in the productions for each of those rules
The top expression rule matches any expression at any precedence level
Since equality has the lowest precedence, if we match that, then it covers
everything

expression      -> equality
primary         -> NUMBER | STRING | "true" | "false" | "nil" | "(" expression )";
unary           -> ("!" | "-" ) unary | primary; // !!true is a valid if weird expression
factor          -> factor ("/" | "*") unary | unary; 
// this enables the rule to match a series of multiplication and division expressions like 1 * 2 / 3. Putting the recursive production on the left side and unary on the right makes the rule
left-associative and unambigious

## Global variables
To accomodate the distinction for where we can and cannot declare variables, we add another rule

program     -> declaration* EOF;

declaration -> varDecl | statement;

statement   -> exprStmt | printStmt;

It's as if there are two levels of "precedence" for statements. Some places where a
statement is allowed -- like inside a block or at the top level -- allow any kind of
statement, including declarations. Others allow only the "higher" precedence statements
that don't declare names

In this analogy, block statements work sort of like parantheses do for expression. A block
is itself in the "higher" precedence level and can be used anywhere, like in the clauses
of an if statement. But the statements it contains can be lower precedence. You're allowed
to declare variables and other names inside the block. The curlies let you escape back into
the full statement grammar from a place where only some statements are allowed.

The rule for declaring a variable looks like:

varDecl     -> "var" IDENTIFIER | ("=" expression )? ";";

To access a variable, we define a new kind of primary expression.

primary     -> "true" | "false" | "nil" | NUMBER | STRING | "(" expression ")" | IDENTIFIER;

## Assignment syntax
Like most C-derived languages, assignment is an expression and not a statement.
As in C, it is the lowest precedence expression form.

That means the rule slots between expression and equality (the next lowest precedence expression).
expression -> assignment;
assignment -> IDENTIFIER "=" assignment | equality;

In some other languages, like Pascal, Python, and Go, assignment is a statement.

Assignment is an expression, that can be nested inside other expressions, like so:
```
var a = 1;
print a = 2; // 2
```

Consider:
```
var a = "before";
a = "value";
```
On the second line, we don't evaluate a (which would return the string "before").
The classic terms for these two constructs are l-value and r-value. All of the
expressions we've seen so far that produce values are r-values. An l-value
"evaluates" to a storage location that you can assign into.
We want the syntax tree to reflect that an l-value isn't evaluated like a normal
expression.

We parse the left-hand side, which can be any expression of higher precedence. If
we find an =, we parse the right hand side and then wrap it all up in an assignment
expression tree node.
One slight difference from binary operators is that we don't loop to build up a
sequence of the same operator. Since assignment is right-associative, we instead
recursively call assignment() to parse the right-hand side.

The trick is that right before we create the assignment expression node, we look
at the left-hand side expression and figure out what kind of assignment target it
is. We convert the r-value expression node into an l-value representation.

This conversion works because it turns out that every valid assignment target happens
to be also be valid syntax as a normal expression. Consider a complex field assignment
like:
```
newPoint(x + 2, 0).y = 3;
```
The left-hand side of that assignment could also work as a valid expression.
```
newPoint(x + 2, 0).y;
```
The first example sets the field, the second gets it.
If the left-hand side expression isn't a valid assignmen target, we fail with
a syntax error. That ensures we report an error on code like this:
```
a + b = c;
```
