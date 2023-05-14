/*
    The core of a scanner is a loop. Starting at the first character
    of the source code, the scanner figures out what lexeme the character
    belongs to, and consumes it and any following characters that are part of
    the lexeme. When it reaches the end of the lexeme, it emits a token

    Then it loops back and does it again, starting from the next character in
    the source code. It keeps doing that, eating characters and occassionally
    emitting tokens, until it reaches the end of the input
*/
package lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static lox.TokenType.*;

class Scanner {
    /* 
        We store the raw source code as a simple string, and we have
        a list ready to fill with tokens we're going to generate
    */
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }

    // A couple of fields to keep track of where the scanner is in the 
    // source code
    // start field points to the first character in the lexeme being scanned,
    // and current points at the character currently being considered.
    // The line field tracks what source line current is on
    private int start = 0;
    private int current = 0;
    private int line = 1;

    // Constructor
    Scanner(String source) {
        this.source = source;
    }

    // A little helper function that tells us if we've consumed all the characters
    private boolean isAtEnd() {
        return current >= source.length();
    }

    // A few more helper functions
    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean match(char expected) {
        /*
            It's like a conditional advance(). We only consume the
            current character if it's what we're looking for
        */
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current += 1;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if(current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
               c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void string() {
        /* 
            Like with comments, we consume characters until we hit the closing "
            Lox supports multi-line strings
        */
        while(peek() != '"' && !isAtEnd()) {
            if(peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        // The closing "
        advance();

        // Trim the surrounding quotes
        String value = source.substring(start + 1, current + 1);
        addToken(STRING, value);
    }

    private void number() {
        /*
            We consume as many digits as we find for the integer part of
            the literal. Then we look for a fractional part, which is a
            decimal point(.) followed by atleast one digit. If we do have 
            a fractional part, we again consume as many digits as we can find
        */
        while(isDigit(peek())) advance();

        // Look for a fractional part
        if(peek() == '.' && isDigit(peekNext())) {
            // Consume the '.'
            advance();

            while(isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void identifier() {
        while(isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        // We add one final "end of file" token after we run out of tokens
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    /*
        In each turn of the loop, we scan a single token
    */
    private void scanToken() {
        char c = advance();
        switch(c) {
            /*
                Imagine if every lexeme were only a single character, 
                in this case we just consume the next character and 
                emit the right token
            */
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            /*
                What about '!' character? It's a single character right?
                Sometimes yes, but if the very next character is an = (equals)
                sign, then we should create a '!=' lexeme instead
                Similarly for '<', '>', and '=', all can be followed by '=' 
                character
            */
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            /* 
                Handling comments, A comment goes until the end of the line
                If we find a second /, we don't end the
                token yet. Instead we keep consuming characters until we reach
                the end of the line
            */
            case '/':
                if (match('/')) {
                    while(peek() != '\n' && !isAtEnd()) advance();
                    // Comments aren't meaningful, so we don't call addToken()
                } else {
                    addToken(SLASH);
                }
                break;
            /* 
                Skip newlines and whitespaces
                When encountering whitespace, we simply go back to the
                beginning of the scan loop, this starts a new lexeme after the
                whitespace character. For newlines, we do that same thing, but
                we also increment the line counter.
            */
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                break;
            /* Handle literals */
            case '"':
                string(); break;
            /*
                If source file contains some characters that Lox doesn't use,
                We report an error

                The erroneous character is still consumed by the earlier call
                to advance, we keep scanning...
                There may be errors later in the program, we detect as many of
                those as possible in one go
            */
            default:
            if (isDigit(c)) {
                number();
            } else if (isAlpha(c)) {
                identifier();
            } else {
                Lox.error(line, "Unexpected character:");
            }
            break;
        }
    }
}