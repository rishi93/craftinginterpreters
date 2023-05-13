package lox;

/*
    A lexeme with some additional information is a token

    A Token class helps us created an object with enough structure to be
    useful for all the later phases of the interpreter
*/
class Token {
    final TokenType type;
    final String lexeme;
    final Object literal;
    // We note the line on which the token appears, so that
    // we can tell the users where the error occured
    final int line;

    // Token constructor, creates a new instance of the class Token
    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}