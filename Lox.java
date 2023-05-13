/* 
The first step in any interpreter/compiler is scanning
The scanner takes in raw source code as a series of 
characters and groups it into a series of chunks we call 
tokens
*/
package lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class List {
    public static void main(String[] args) throws IOException {
        /*
        Our interpreter supports two ways of running code.
        1. Start from command line and give it a path to a file, it reads
        the file and executes it
        2. Fire up lox without arguments and it drops you into a prompt where
        you can enter and execute code one line at a time
        */
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (hadError) System.exit(65);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        /*
        The readLine() function reads a line of input from the user on the 
        command line and returns the result.
        To kill an interactive command line app, you type Ctrl+D, doing so 
        signals an "end-of-file" condition to the program. When that happens, 
        readLine() returns null, so we check that to exit the loop
        */
        for(;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);

            // If the user makes a mistake, it shouldn't kill their entire session
            hadError = false;
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        // For now, just print the tokens
        for(Token token : tokens) {
            System.out.println(token);
        }
    }

    /*
        If there is an error in the source code, tell the user
        where the error is

        It's good engineering practice to separate the code that generates
        the errors from the code that reports them

        Various phases of the front-end will detect errors, but it's not really
        their job to know how to present that to a user
    */
    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        // We use this to ensure we don't try to execute code that has a known error
        // Also it let's us exit with a non-zero exit code like a good command line
        // citizen should
        hadError = true;
    }
}