package lox;

import java.util.List;
/*
    There are all manner of ways, a language implementation can make a computer
    do what the user's source code commands.
        * Turn it into machine code
        * Turn it into bytecode for a virtual machine to run
        * Shortest, simplest path: Execute the syntax tree itself
*/

/*
    In Lox, values are created by literals, computed by expressions and stored
    in variables. The user sees these as Lox objects, but they are implemented
    in the underlying language our interpreter is written in. That means
    briding the lands of Lox's dynamic typing and Java's static types. A variable
    in Lox can store a value of any (Lox) type, and can even store values of
    different types at different points in time. What Java type might we use to 
    represent that? Good old java.lang.Object

    Given a value of static type Object, we can determine if the runtime value is
    a number or a string or whatever using Java's built-in instanceof operator.
*/

/*
    We need blobs of code to implement the evaluation logic for each kind of
    expression we can parse.

    We could stuff that code into the syntax tree classes in something like an 
    interpret method (but it gets messy if we jam all sorts of logic into the tree
    classes).

    Instead we reuse the Visitor pattern
*/

/*
    The return type of the visit methods will be Object, the root class that
    we use to refer to a Lox value in our Java code.
    To satisfy the Visitor interface, we need to define visit methods for each of
    the four expression tree classes our parser produces
*/
class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    /*
        The visit methods are sort of the guts of the Interpreter class, where
        the real work happens. We need to wrap a skin around them to interface
        with the rest of the program. The below public API is simply one method
    */
    void interpret(List<Stmt> statements) {
        try {
            /*
                Object value = evaluate(expression);
                System.out.println(stringify(value));
            */
            // Modify the old interpret() method to accept a list of statements (a program)
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    // The statement analogue to the evaluate() method we have for expressions
    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    /*
        Unlike expressions, statements produce no values, so the return type of 
        the visit methods is Void, not Object.
    */

    /*
        Java doesn't let you use lowercase "void" as a generic type argument
        for obscure reasons having to do with type erasures and the stack. Instead,
        there is a separate Void type specifically for this use. Sort of a "boxed
        void" like "Integer" is for "int".
    */
    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        /*
            We evalute the inner expression using our existing evaluate() method
            and discard the value. Then we return null. Java requires that to
            satisfy the special capitalized Void return type.
        */
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        /*
            Before discarding the expression's value, we convert it to a string
            using the stringify() method and then dump it to stdout.
        */
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    /* A grouping node has a reference to an inner node for the expression
    contained inside the parentheses. To evaluate the grouping expression itself,
    we recursively evaluate that subexpression and return it. */
    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if(left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    /* Like grouping, unary expressions have a single subexpression that we must
    evaluate first. The difference is that the unary expression itself does a little
    work afterwards. */
    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch(expr.operator.type) {
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double)right;
            case BANG:
                return !isTruthy(right);
        }

        // Unreachable
        return null;
    }

    /* We need to decide what happens when you use something other than true
    or false in a logic operation.
    Lox follows Ruby's simple rule: false and nil are falsey, and everything
    else is truthy */
    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }

    /* Sidenote:
    In JavaScript, strings are truthy, but empty strings are not. Arrays are truthy
    but empty arrays are also truthy. The number 0 is falsey, but the string "0"
    is truthy.

    In Python, empty strings are falsey like in JS, but other empty sequences
    are falsey too
    */

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch(expr.operator.type) {
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double)left / (double)right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
            case PLUS:
                if(left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }

                if(left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }

                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
                /*
                    We could have defined an operator specifically for string
                    concatenation. That's what Perl(.), Lua(..), Smalltalk(,),
                    Haskell(++) and others do
                    Java, JavaScript, Python and others overload the + operator
                    to support both adding numbers and concatenating strings.
                    Even in other languages, that don't use + for strings, they
                    still often overload it for adding both integers and floating
                    point numbers
                */

            /*
                Allowing comparisons on types other than numbers could be useful.
                Eg: 'a' < 'z'
                Even comparisons among mixed types, like 3 < "pancakes" could be
                handy to enable things like ordered collections of heterogenous types.
                Or it could simply lead to bugs and confusion.
            */
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case BANG_EQUAL:
                return !isEqual(left, right);
        }

        // Unreachable
        return null;
    }

    /*
        Unlike the comparison operators, which require numbers, the equality
        operators support operands of any type, even mixed ones.
    */
    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

    /*
        This is another of those pieces of code, that crosses the membrane
        between the user's view of Lox objects and their internal representation
        in Java.
        The two edge cases are nil, which we represent using Java's null and
        numbers.
        Lox uses double-precision numbrers even for integer values, they should
        print without a decimal point.
    */
    private String stringify(Object object) {
        if(object == null) return "nil";

        if(object instanceof Double) {
            String text = object.toString();
            if(text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }
}