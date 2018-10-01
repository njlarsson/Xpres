package xpres2;

import java.io.*;
import java.util.Scanner;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import xpres2.grammar.*;

public class CompileXpres {
    public static void main(String[] args) throws IOException {
        String infnam;
        String outfnam = args[1];
        boolean traceOn = args.length < 3 || "traceOn".equalsIgnoreCase(args[2]);

        if (args.length > 0) {
            infnam = args[0];
        } else {
            System.out.println("Vilken fil vill du köra?");
            Scanner scanner = new Scanner(System.in);
            infnam = scanner.nextLine();
        }

        ANTLRInputStream input = new ANTLRInputStream(new FileInputStream(infnam));
        XpresLexer lexer = new XpresLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        XpresParser parser = new XpresParser(tokens);
        ParseTree tree = parser.file();
        ParseTreeWalker walker = new ParseTreeWalker();
        HackGen out = new HackGen(1024, 2048, 1025);
        walker.walk(new Compiler(infnam, out, traceOn), tree);
        Writer w = new OutputStreamWriter(new FileOutputStream(outfnam), "US-ASCII");
        out.outputCode(w);
        w.close();
    }
}
