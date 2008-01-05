package net.loveruby.cflat.compiler;
import net.loveruby.cflat.ast.*;
import net.loveruby.cflat.asm.Label;
import net.loveruby.cflat.exception.*;
import java.util.*;

public class JumpResolver extends Visitor {
    static public void resolve(AST ast, ErrorHandler h)
                                    throws SemanticException {
        new JumpResolver(h).resolveAST(ast);
    }

    protected ErrorHandler errorHandler;
    protected LinkedList breakTargetStack;
    protected LinkedList continueTargetStack;
    protected DefinedFunction currentFunction;

    public JumpResolver(ErrorHandler h) {
        errorHandler = h;
    }

    public void resolveAST(AST ast) throws SemanticException {
        breakTargetStack = new LinkedList();
        continueTargetStack = new LinkedList();
        Iterator funcs = ast.functions();
        while (funcs.hasNext()) {
            currentFunction = (DefinedFunction)funcs.next();
            resolve(currentFunction.body());
            currentFunction.checkJumpLinks(errorHandler);
        }
        if (errorHandler.errorOccured()) {
            throw new SemanticException("compile failed.");
        }
    }

    private BreakableStmt currentBreakTarget()
                                throws SemanticException {
        if (breakTargetStack.isEmpty()) {
            throw new SemanticException("break from out of while/for/switch");
        }
        return (BreakableStmt)breakTargetStack.getLast();
    }

    private ContinueableStmt currentContinueTarget()
                                throws SemanticException {
        if (continueTargetStack.isEmpty()) {
            throw new SemanticException("continue from out of while/for");
        }
        return (ContinueableStmt)continueTargetStack.getLast();
    }

    public void visit(SwitchNode node) {
        resolve(node.cond());
        breakTargetStack.add(node);
        resolveNodeList(node.cases());
        breakTargetStack.removeLast();
    }

    public void visit(WhileNode node) {
        resolve(node.cond());
        breakTargetStack.add(node);
        continueTargetStack.add(node);
        resolve(node.body());
        continueTargetStack.removeLast();
        breakTargetStack.removeLast();
    }

    public void visit(DoWhileNode node) {
        breakTargetStack.add(node);
        continueTargetStack.add(node);
        resolve(node.body());
        continueTargetStack.removeLast();
        breakTargetStack.removeLast();
        resolve(node.cond());
    }

    public void visit(ForNode node) {
        resolve(node.init());
        resolve(node.cond());
        breakTargetStack.add(node);
        continueTargetStack.add(node);
        resolve(node.body());
        resolve(node.incr());
        continueTargetStack.removeLast();
        breakTargetStack.removeLast();
    }

    public void visit(BreakNode node) {
        try {
            node.setTargetLabel(currentBreakTarget().endLabel());
        }
        catch (SemanticException ex) {
            errorHandler.error(node.location(), ex.getMessage());
        }
    }

    public void visit(ContinueNode node) {
        try {
            node.setTargetLabel(currentContinueTarget().continueLabel());
        }
        catch (SemanticException ex) {
            errorHandler.error(node.location(), ex.getMessage());
        }
    }

    public void visit(LabelNode node) {
        try {
            Label label = currentFunction.defineLabel(node.name(),
                                                      node.location());
            node.setLabel(label);
        }
        catch (SemanticException ex) {
            errorHandler.error(node.location(), ex.getMessage());
        }
    }

    public void visit(GotoNode node) {
        node.setTargetLabel(currentFunction.referLabel(node.target()));
    }

    public void visit(ReturnNode node) {
        node.setFunction(currentFunction);
    }
}
