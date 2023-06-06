## Data types in Lox:
* Number  --> Java Double
* String  --> Java String
* Boolean --> Java boolean
* nil     --> Java null

Static and dynamic typing
Statically typed language --> type errors are detected and reported at compile
time --> eg: Java
Dynamically typed language --> defer checking for type errors until runtime right
before an operation is attempted.

Not so black and white, this lies on a spectrum.
Even most statically typed languages do some type checks at runtime. The type
system checks most type rules statically, but inserts runtime checks in the generated
code for other operations.

For example, in Java, casting to a different type is not always successful. The 
static type checker assumes the conversion to the destination type was successful,
and carries on. Only during runtime, do we detect the type cast failure (an exception
is thrown).

Static type checking ensures certain kinds of errors can never occur. Type checking
during runtime erodes this confidence.

## Statements and state

### Global variables
A variable declaration statement brings a new variable into the world
```var beverage = "expresso";```
This creates a new binding the associates a name with a value
```print beverage; // prints "expresso"```
Once that's done, a variable expression accesses that binding. When the identifier
is used as an expression, it looks up the value bound to that name and returns it.

Variable declarations are statements, but they are different from other statements.
The grammar restricts where some kinds of statements are allowed.
```
if(monday) print "Ugh, already?"; // This is allowed
```
But this is not allowed
```
if(monday) var beverage = "expresso"; // Not allowed
```
We could allow the latter, but it's confusing. What is the scope of the `beverage`
variable? Does it persist after the `if` statement? If so, what is its value on
days other than monday?

Some places where a statement is allowed - like inside a block or at the top level -
allow any kind of statement, including declarations. Others allow only the "higher"
precedence statements that don't declare names.

### Environments
The bindings that associate variables to names need to be stored somewhere (called environment).
Like a hashmap where the keys are variable names and the values are the uh.. values.
This is separated out into it's own class --> Environment.java