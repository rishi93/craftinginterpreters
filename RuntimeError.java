package lox;

/*
    Unlike the Java cast exception, our class tracks the token that caused the
    runtime error in the user's code
*/
class RuntimeError extends RuntimeException {
    final Token token;

    RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }
}