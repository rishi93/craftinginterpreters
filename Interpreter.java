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
package lox;

/*
    The return type of the visit methods will be Object, the root class that
    we use to refer to a Lox value in our Java code.
    To satisfy the Visitor interface, we need to define visit methods for each of
    the four expression tree classes our parser produces
*/
class Interpeter implements Expr.Visitor<Object> {
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

    private Object evaluate(Expr expr) {
        return expr.accept(this);
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
}