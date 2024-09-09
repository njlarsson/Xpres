package xpres;

import java.io.*;
import java.util.Scanner;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import xpres.grammar.*;

public class RunXpres {
    public static void main(String[] args) throws IOException {
        String infnam;
        if (args.length > 0) {
            infnam = args[0];
        } else {
            System.out.println("Vilken fil vill du köra?");
            Scanner scanner = new Scanner(System.in);
            infnam = scanner.nextLine();
        }
        CharStream input = CharStreams.fromFileName(infnam);
        XpresLexer lexer = new XpresLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        XpresParser parser = new XpresParser(tokens);
        ParseTree tree = parser.code();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new Interpreter(infnam), tree);
    }
}
