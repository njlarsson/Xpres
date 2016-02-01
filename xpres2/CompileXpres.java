package xpres2;

import java.io.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import xpres2.grammar.*;

public class CompileXpres {
    public static void main(String[] args) throws IOException {
        String infnam = args[0];
        String outfnam = args[1];
        ANTLRInputStream input = new ANTLRInputStream(new FileInputStream(infnam));
        XpresLexer lexer = new XpresLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        XpresParser parser = new XpresParser(tokens);
        ParseTree tree = parser.file();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new Compiler(infnam, outfnam), tree);
    }
}
