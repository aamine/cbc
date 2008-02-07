package net.loveruby.cflat.compiler;
import net.loveruby.cflat.ast.*;
import net.loveruby.cflat.asm.Label;
import net.loveruby.cflat.exception.*;
import java.util.*;

public class JumpResolver extends Visitor {
    static public void resolve(AST ast, ErrorHandler h)
                                    throws SemanticException {
        new JumpResolver(h).resolve(ast);
    }

    // #@@range/ctor{
    protected ErrorHandler errorHandler;
    protected LinkedList breakTargetStack;
    protected LinkedList continueTargetStack;
    protected DefinedFunction currentFunction;

    public JumpResolver(ErrorHandler h) {
        errorHandler = h;
    }
    // #@@}

    // #@@range/resolve{
    public void resolve(AST ast) throws SemanticException {
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
    // #@@}

    protected void resolve(Node n) {
        visitNode(n);
    }

    // #@@range/currentBreakTarget{
    private BreakableStmt currentBreakTarget() {
        return (BreakableStmt)breakTargetStack.getLast();
    }
    // #@@}

    private ContinueableStmt currentContinueTarget() {
        return (ContinueableStmt)continueTargetStack.getLast();
    }

    public void visit(SwitchNode node) {
        resolve(node.cond());
        breakTargetStack.add(node);
        visitNodeList(node.cases());
        breakTargetStack.removeLast();
    }

    // #@@range/_while{
    public void visit(WhileNode node) {
        resolve(node.cond());
        breakTargetStack.add(node);
        continueTargetStack.add(node);
        resolve(node.body());
        continueTargetStack.removeLast();
        breakTargetStack.removeLast();
    }
    // #@@}

    public void visit(DoWhileNode node) {
        breakTargetStack.add(node);
        continueTargetStack.add(node);
        resolve(node.body());
        continueTargetStack.removeLast();
        breakTargetStack.removeLast();
        resolve(node.cond());
    }

    // #@@range/_for{
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
    // #@@}

    // #@@range/_break{
    public void visit(BreakNode node) {
        BreakableStmt target = currentBreakTarget();
        if (target != null) {
            node.setTargetLabel(target.endLabel());
        }
        else {
            errorHandler.error(node.location(), 
                               "break from out of while/do-while/for/switch");
        }
    }
    // #@@}

    // #@@range/_continue{
    public void visit(ContinueNode node) {
        ContinueableStmt target = currentContinueTarget();
        if (target != null) {
            node.setTargetLabel(target.continueLabel());
        }
        else {
            errorHandler.error(node.location(),
                               "continue from out of while/do-while/for");
        }
    }
    // #@@}

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
