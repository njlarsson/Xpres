package xpres2;

import xpres2.grammar.*;
import java.util.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Compiler extends XpresBaseListener {
    private final String infnam;
    private final boolean traceOn;
    
    private final HackGen out;
    private final HashMap<String, Integer> varAddr = new HashMap<String, Integer>();
    private final Stack<Integer> addrStack = new Stack<Integer>();
    
    Compiler(String infnam, HackGen out, boolean traceOn) {
        this.infnam = infnam;
        this.out = out;
        this.traceOn = traceOn;
    }

    private void tracePrint(String message) {
        if (traceOn) {
            System.out.println("At operation "+out.currentCodeAddress()+": "+message);
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
    
    @Override
    public void enterFile(XpresParser.FileContext ctx) {
        tracePrint("Initialize SP");
        out.emitInitSP();
    }

    @Override
    public void enterDecl(XpresParser.DeclContext ctx) {
        String name = ctx.ID().getText();
        int addr = out.newVarAddr();
        Integer old = varAddr.put(name, addr);
        if (old != null) {
            error(ctx.ID().getSymbol().getLine(), "redefined " + name);
        }
    }

    @Override
    public void exitAssign(XpresParser.AssignContext ctx) {
        int a = getVarAddr(ctx.ID().getSymbol());
        tracePrint("Pop from stack and put in "+a);
        out.emitPopD();
        out.emitAInstr(a);
        out.emitCInstr(HackGen.DestM, HackGen.CompD, 0);
    }

    @Override
    public void enterPrint(XpresParser.PrintContext ctx) {
        error(ctx.getStart().getLine(), "print not implemented");
    }
    
    @Override
    public void exitAddExpr(XpresParser.AddExprContext ctx) {
        ParseTree operator = ctx.getChild(1); // the second token, if it's there, is the operator
        if (operator != null && "+".equals(operator.getText())) { // if it's plus, this is an addition
            // Add the top two numbers on the stack, leaving only the sum.
            tracePrint("Add top two numbers on the stack, leaving the sum");
            out.emitGetTwoOperands();         // Get operands.
            out.emitCInstr(HackGen.DestD, HackGen.DPlusM, 0); // Add them.
            out.emitReplaceTopWithD();        // Replace top of stack with sum.
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
            out.emitAInstr(a);
            out.emitCInstr(HackGen.DestD, HackGen.CompM, 0);
            out.emitPushD();
        } else if (ctx.INT() != null) {
            int i = Integer.parseInt(ctx.INT().getText());
            tracePrint("Push "+i+" on stack");
            out.emitAInstr(i);
            out.emitCInstr(HackGen.DestD, HackGen.CompA, 0);
            out.emitPushD();
        }
    }
}
