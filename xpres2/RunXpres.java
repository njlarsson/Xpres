package xpres2;

import java.io.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import xpres2.grammar.*;

public class RunXpres {
    public static void main(String[] args) throws IOException {
        String infnam = args[0];
        ANTLRInputStream input = new ANTLRInputStream(new FileInputStream(infnam));
        XpresLexer lexer = new XpresLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        XpresParser parser = new XpresParser(tokens);
        ParseTree tree = parser.code();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new Interpreter(infnam), tree);
    }
}
