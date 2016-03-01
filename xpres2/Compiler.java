package xpres2;

import xpres2.grammar.*;
import java.util.*;
import java.io.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Compiler extends XpresBaseListener {
    // Encapsulates IOException, to throw from overridden methods.
    static class IORuntimeException extends RuntimeException {
        final IOException iox;
        IORuntimeException(IOException iox) { this.iox = iox; }
    }
    private static final int SPAddr = 1024, varBase = 1025, stackBase = 2048;

    private static final int DestM = 0b001;
    private static final int DestD = 0b010;
    private static final int DestA = 0b100;
        
    private static final int Comp0 = 0b0101010;
    private static final int Comp1 = 0b0111111;
    private static final int Minus1 = 0b0111010;
    private static final int CompD = 0b0001100;
    private static final int CompA = 0b0110000;
    private static final int NotD = 0b0001101;
    private static final int NotA = 0b0110001;
    private static final int MinusD = 0b0001111;
    private static final int MinusA = 0b0110011;
    private static final int DPlus1 = 0b0011111;
    private static final int APlus1 = 0b0110111;
    private static final int DMinus1 = 0b0001110;
    private static final int AMinus1 = 0b0110010;
    private static final int DPlusA = 0b0000010;
    private static final int DMinusA = 0b0010011;
    private static final int AMinusD = 0b0000111;
    private static final int DAndA = 0b0000000;
    private static final int DOrA = 0b0010101;
    private static final int CompM = 0b1110000;
    private static final int NotM = 0b1110001;
    private static final int MinusM = 0b1110011;
    private static final int MPlus1 = 0b1110111;
    private static final int MMinus1 = 0b1110010;
    private static final int DPlusM = 0b1000010;
    private static final int DMinusM = 0b1010011;
    private static final int MMinusD = 0b1000111;
    private static final int DAndM = 0b1000000;
    private static final int DOrM = 0b1010101;
        
    private static final int JGT = 0b001;
    private static final int JEQ = 0b010;
    private static final int JGE = 0b011;
    private static final int JLT = 0b100;
    private static final int JNE = 0b101;
    private static final int JLE = 0b110;
    private static final int JMP = 0b111;
    
    private final String infnam;
    private final HashMap<String, Integer> varAddr = new HashMap<String, Integer>();
    private final Writer out;
    private final boolean traceOn;

    private int curVarAddr = varBase;
    private int emittedOps = 0;
    
    Compiler(String infnam, String outfnam, boolean traceOn) throws IOException {
        this.infnam = infnam;
        out = new OutputStreamWriter(new FileOutputStream(outfnam), "US-ASCII");
        this.traceOn = traceOn;
    }

    private void write(String s) {
        // Count newlines to keep track of how many lines (operations) are output.
        for (int i = 0; (i = s.indexOf('\n', i)) >= 0; i++) { emittedOps++; }
        try {
            out.write(s);
        } catch (IOException iox) {
            throw new IORuntimeException(iox);
        }
    }

    private void tracePrint(String message) {
        if (traceOn) {
            System.out.println("At operation "+emittedOps+": "+message);
        }
    }

    private int getVarAddr(Token tok) {
        String name = tok.getText();
        Integer a = varAddr.get(name);
        if (a == null) {
            error(tok.getLine(), "undefined " + name);
            return 0;
        } else {
            return a;
        }
    }
    
    private void error(int line, String msg) {
        System.err.println(infnam + ":" + line + ": " + msg);
    }

    private void emitAInstr(int a) {
        String binstr = Integer.toBinaryString(a);
        for (int i = binstr.length(); i < 16; i++) {
            write("0");         // pad with initial zeros
        }
        write(binstr);          // write "0" + a in binary
        write("\n");
    }

    private void emitCInstr(int dest, int comp, int jump) {
        int c = 0b1110000000000000 | comp << 6 | dest << 3 | jump;
        write(Integer.toBinaryString(c));
        write("\n");
    }

    private void emitPushD() {
        tracePrint("(push D)");
        emitAInstr(SPAddr);                     // @SP
        emitCInstr(DestA, CompM, 0);            // A=SP
        emitCInstr(DestM, CompD, 0);            // [SP]=D
        emitCInstr(DestD, APlus1, 0);           // D=SP+1
        emitAInstr(SPAddr);                     // @SP
        emitCInstr(DestM, CompD, 0);            // [SP]=SP+1
    }
    
    private void emitPopD() {
        tracePrint("(pop D)");
        emitAInstr(SPAddr);                     // @SP
        emitCInstr(DestD, MMinus1, 0);          // D=SP-1
        emitCInstr(DestM | DestA, CompD, 0);    // SP=SP-1; A=SP
        emitCInstr(DestD, CompM, 0);            // D=[SP]
    }
    
    @Override
    public void enterFile(XpresParser.FileContext ctx) {
        tracePrint("Initialize SP ("+SPAddr+") to "+stackBase);
        emitAInstr(stackBase);
        emitCInstr(DestD, CompA, 0);
        emitAInstr(SPAddr);
        emitCInstr(DestM, CompD, 0);
    }

    @Override
    public void exitFile(XpresParser.FileContext ctx) {
        try {
            out.close();
        } catch (IOException iox) {
            throw new IORuntimeException(iox);
        }
    }

    @Override
    public void enterDecl(XpresParser.DeclContext ctx) {
        String name = ctx.ID().getText();
        Integer old = varAddr.put(name, curVarAddr++);
        if (old != null) {
            error(ctx.ID().getSymbol().getLine(), "redefined " + name);
        }
    }

    @Override
    public void exitAssign(XpresParser.AssignContext ctx) {
        int a = getVarAddr(ctx.ID().getSymbol());
        tracePrint("Pop from stack and put in "+a);
        emitPopD();
        emitAInstr(a);
        emitCInstr(DestM, CompD, 0);
    }

    @Override
    public void enterPrint(XpresParser.PrintContext ctx) {
        error(ctx.getStart().getLine(), "print not implemented");
    }
    
    @Override
    public void exitExpr(XpresParser.ExprContext ctx) {
        ParseTree operator = ctx.getChild(1); // the second token, if it's there, is the operator
        if (operator != null && "+".equals(operator.getText())) { // if it's plus, this is an addition
            // Add the top two numbers on the stack, leaving only the sum.
            tracePrint("Add top two numbers on the stack, leaving the sum");
            emitAInstr(SPAddr);                 // @SP
            emitCInstr(DestA, MMinus1, 0);      // A=SP-1
            emitCInstr(DestD, CompM, 0);        // D=[SP-1]
            emitCInstr(DestA, AMinus1, 0);      // A=SP-2
            emitCInstr(DestM, DPlusM, 0);       // [SP-2]+=[SP-1]
            emitCInstr(DestD, APlus1, 0);       // D=SP-1
            emitAInstr(SPAddr);                 // @SP
            emitCInstr(DestM, CompD, 0);        // SP=SP-1
        } else {
            // No operator we know, so it must be a lone term. Just leave it on the stack.
        }
    }
    
    @Override
    public void exitMulExpr(XpresParser.MulExprContext ctx) {
        if (ctx.mulExpr() != null) {
            error(ctx.getStart().getLine(), "multiplication not implemented");
        }
    }
    
    @Override
    public void enterAtomExpr(XpresParser.AtomExprContext ctx) {
        if (ctx.ID() != null) {
            int a = getVarAddr(ctx.ID().getSymbol());
            tracePrint("Push contents of "+a+" on stack");
            emitAInstr(a);
            emitCInstr(DestD, CompM, 0);
            emitPushD();
        } else if (ctx.INT() != null) {
            int i = Integer.parseInt(ctx.INT().getText());
            tracePrint("Push "+i+" on stack");
            emitAInstr(i);
            emitCInstr(DestD, CompA, 0);
            emitPushD();
        }
    }
}
