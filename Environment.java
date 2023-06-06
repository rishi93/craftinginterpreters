package lox;

import java.util.HashMap;
import java.util.Map;

class Environment {
    /*
        Bare strings for the keys, not tokens. A token represents a unit of
        code at a specific place in the source text, but when it comes to
        looking up variables, all identifier tokens with the same name should
        refer to the same variable. Using the raw string ensures all of the of
        those tokens refer to the same map key.
    */
    private final Map<String, Object> values = new HashMap<>();

    /*
        A variable definition binds a new name to a value.
        When we add the key to the map, we don't check if it's already present.

        That means, we can do this:
        var a = "before";
        print a; // "before"
        var a = "after";
        print a; // "after"

        A variable statement doesn't just define a new variable, it can also be used
        to redefine an existing variable.
    */
    void define(String name, Object value) {
        values.put(name, value);
    }

    /*
        Once a variable exists, we need a way to look it up.
    */
    Object get(Token name) {
        if(values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }
/*
    Side note:
    
    Using a variable isn't the same as referring to it.

    fun isOdd(n) {
        if(n == 0) return false;
        return isEven(n - 1);
    }

    fun isEven(n) {
        if(n == 0) return true;
        return isOdd(n - 1);
    }

    The isEven() function isn't defined by the time, we are looking at the body
    of isOdd(). If we swap the order of the two functions, then isOdd() isn't
    defined when we're looking at the isEven().

    Since making it a static error makes recursive declarations too difficult,
    we'll defer the error to runtime. It's okay to refer to a variable, before
    it's defined as long as you don't evaluate the reference. That lets the
    program for even and odd numbers work, but you'd get a runtime error in:

    print a;
    var a = "Too late!";

    Some statically typed languages like Java and C# solve this by specifying
    that the top level of a program isn't a sequence of imperative statements.
    Instead, a program is a set of declarations which all come into being
    simulataneously. The implementation declares all of the names before looking
    at the bodies of any of the functions.

    Older languages like C and Pascal don't work like this. Instead, they force
    you to add explicit forward declarations to declare a name before it's fully
    defined. They wanted to be able to complie a source file in one single pass
    through the text, so those compilers couldn't gather up all of the declarations
    first before processing function bodies.
*/
}

