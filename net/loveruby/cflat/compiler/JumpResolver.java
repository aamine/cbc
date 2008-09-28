package net.loveruby.cflat.compiler;
import net.loveruby.cflat.ast.*;
import net.loveruby.cflat.asm.Label;
import net.loveruby.cflat.exception.*;
import java.util.*;

public class JumpResolver extends Visitor {
    // #@@range/ctor{
    protected ErrorHandler errorHandler;
    protected LinkedList<BreakableStmt> breakTargetStack;
    protected LinkedList<ContinueableStmt> continueTargetStack;
    protected DefinedFunction currentFunction;

    public JumpResolver(ErrorHandler h) {
        errorHandler = h;
    }
    // #@@}

    // #@@range/resolve{
    public void resolve(AST ast) throws SemanticException {
        breakTargetStack = new LinkedList<BreakableStmt>();
        continueTargetStack = new LinkedList<ContinueableStmt>();
        for (DefinedFunction f : ast.definedFunctions()) {
            currentFunction = f;
            resolve(f.body());
            f.checkJumpLinks(errorHandler);
        }
        if (errorHandler.errorOccured()) {
            throw new SemanticException("compile failed.");
        }
    }
    // #@@}

    protected void resolve(Node n) {
        visitNode(n);
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
        if (breakTargetStack.isEmpty()) {
            errorHandler.error(node.location(), 
                    "break from out of while/do-while/for/switch");
            return;
        }
        BreakableStmt target = breakTargetStack.getLast();
        node.setTargetLabel(target.endLabel());
    }
    // #@@}

    // #@@range/_continue{
    public void visit(ContinueNode node) {
        if (breakTargetStack.isEmpty()) {
            errorHandler.error(node.location(),
                        "continue from out of while/do-while/for");
            return;
        }
        ContinueableStmt target = continueTargetStack.getLast();
        node.setTargetLabel(target.continueLabel());
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
