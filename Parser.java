package lox;

import java.util.List;
import java.util.ArrayList;

import static lox.TokenType.*;

/*
    Like the Scanner, the Parser consumes a flat input sequence,
    only now we're reading tokens instead of characters
*/
class Parser {
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /* An initial method to kick it off */
    /*
        The parser's parse() method that parses and returns a single expression
        was a temporary hack to get the last chapter up and running. Now that our
        grammar has the correct starting rule, program, we can turn parse() into
        the real deal.
    */
    /*
    Expr parse() {
        try {
            return expression();
        } catch (ParseError error){
            return null;
        }
    }
    */
    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while(!isAtEnd()) {
            // statements.add(statement());
            statements.add(declaration());
        }

        return statements;
    }

    private Stmt declaration() {
        try {
            if (match(VAR)) return varDeclaration();
 
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt statement() {
        if(match(PRINT)) return printStatement();

        /*
            A print token means it's obviously a print statement.

            If the next token doesn't look like any known kind of statement, we
            assume it must be an expression statement. That's the typical fallthrough
            case, since it's hard to proactively recognize an expression from it's
            first token.
        */
        return expressionStatement();
    }

    private Stmt printStatement() {
        /*
            Since we already matched and consumed the print token itself, we
            don't need to do that here. We parse the subsequent expression, consume
            the terminating semicolon, and emit the syntax tree
        */
        Expr value = expression();

        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean check(TokenType type) {
        /*
            Returns true if the current token is of the given type.
            Unlike match(), it never consumes the token, only looks at it
        */
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if(!isAtEnd()) current++;
        return previous();
    }

    private boolean match(TokenType...types) {
        /*
            Checks to see if the current token has any of the given types.
            If so, it consumes the token and returns true. Otherwise, it returns
            false and returns the current token alone
        */
        for(TokenType type : types) {
            if(check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token consume(TokenType type, String message) {
        /*
            Similar to match(), checks to see if the next token is of the
            expected type. If so, it consumes the token. If some other token is
            there, we've hit an error
        */
        if(check(type)) return advance();

        throw error(peek(), message);
    }

    /*
        We return the error instead of throwing it, because we want to let
        the calling method inside the parser decide whether to unwind or not
    */
    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    /*
        With recursive descent, the parser's state - which rules it is in the
        middle of recognizing - is not stored explicitly in fields. Instead, we
        use Java's own call stack to track what the parser is doing. Each rule
        in the middle of being parsed is a call frame to the stack. In order to
        reset that state, we need to clear out those call frames.

        The natural way to do that in Java is exceptions. When we want to
        synchronize, we throw that ParseError object. Higher up in the method 
        for the grammar rule we are synchronizing to, we'll catch it. Since we
        synchronize on statement boundaries, we'll catch the exception there.
        After the exception is caught, the parser is in the right state. All 
        that's left is to synchronize the tokens.

        We want to discard tokens until we're right at the beginning of the next
        statement. After a semicolon, we're probably finished with a statement. 
        Most statements start with a keyword - for, if, return, var, etc. When
        the next token is any of those, we're probably about to start a statement 
    */

    private void synchronize() {
        /*
            It discards tokens until it thinks it has found a statement
            boundary. After catching a ParseError, we'll call this and then
            we're hopefully back in sync. When it works well, we have discarded
            tokens that would have likely caused cascaded errors anyway, and now
            we can parse the rest of the file starting at the next statement.
        */
        advance();

        while(!isAtEnd()) {
            if(previous().type == SEMICOLON) return;

            switch(peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }


    private Expr expression() {
        return equality();
    }

    private Expr equality() {
        Expr expr = comparison();

        while(match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /*
        Moving on to next rule...
    */
    private Expr comparison() {
        Expr expr = term();

        while(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /*
        In order of precedence, first addition and subtraction
    */
    private Expr term() {
        Expr expr = factor();

        while(match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while(match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if(match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary() {
        if(match(FALSE)) return new Expr.Literal(false);
        if(match(TRUE)) return new Expr.Literal(true);
        if(match(NIL)) return new Expr.Literal(null);

        if(match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if(match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        if(match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression");
            return new Expr.Grouping(expr);
        }

        /*
            As the parser descends through the parsing methods for each
            grammar rule, it eventually hits primary(). If none of the cases
            in there match, it means we are sitting on a token that can't start
            an expression. We need to handle that error too.   
        */
        throw error(peek(), "Expected expression.");
    }

    /*
        A parser really has two jobs:
        1. Given a valid sequence of tokens, produce a corresponding syntax tree
        2. Given an invalid sequence of tokens, detect any errors and tell the user
        about their mistakes
    */

}

/* 
https://sp23.datastructur.es/materials/lectures/lec2/ 
*/