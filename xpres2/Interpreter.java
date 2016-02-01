package xpres2;

import xpres2.grammar.*;
import java.util.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Interpreter extends XpresBaseListener {
    private static class Var { int val; }
    
    private final String infnam;
    private final HashMap<String, Var> vars = new HashMap<String, Var>();
    private Stack<Integer> exprVal = new Stack<Integer>();

    Interpreter(String infnam) { this.infnam = infnam; }

    private Var getVar(Token tok) {
        String name = tok.getText();
        Var v = vars.get(name);
        if (v == null) {
            error(tok.getLine(), "undefined " + name);
            return new Var();   // avoid null pointer exception
        } else {
            return v;
        }
    }
    
    private void error(int line, String msg) {
        System.err.println(infnam + ":" + line + ": " + msg);
    }
    
    @Override
    public void enterDecl(XpresParser.DeclContext ctx) {
        String name = ctx.ID().getText();
        Var old = vars.put(name, new Var());
        if (old != null) {
            error(ctx.ID().getSymbol().getLine(), "redefined " + name);
        }
    }

    @Override
    public void exitAssign(XpresParser.AssignContext ctx) {
        getVar(ctx.ID().getSymbol()).val = exprVal.pop();
    }

    @Override
    public void exitPrint(XpresParser.PrintContext ctx) {
        System.out.println(exprVal.pop());
    }
    
    @Override
    public void exitExpr(XpresParser.ExprContext ctx) {
        if (ctx.expr() != null) {
            exprVal.push(exprVal.pop() + exprVal.pop());
        }
    }
    
    @Override
    public void exitMulExpr(XpresParser.MulExprContext ctx) {
        if (ctx.mulExpr() != null) {
            exprVal.push(exprVal.pop() * exprVal.pop());
        }
    }
    
    @Override
    public void enterAtomExpr(XpresParser.AtomExprContext ctx) {
        if (ctx.ID() != null) {
            exprVal.push(getVar(ctx.ID().getSymbol()).val);
        } else if (ctx.INT() != null) {
            exprVal.push(Integer.parseInt(ctx.INT().getText()));
        }
    }
}
