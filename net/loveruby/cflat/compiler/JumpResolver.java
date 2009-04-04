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

    protected void resolve(StmtNode n) {
        n.accept(this);
    }

    protected void resolve(ExprNode n) {
        n.accept(this);
    }

    public SwitchNode visit(SwitchNode node) {
        resolve(node.cond());
        breakTargetStack.add(node);
        visitStmts(node.cases());
        breakTargetStack.removeLast();
        return null;
    }

    // #@@range/_while{
    public WhileNode visit(WhileNode node) {
        resolve(node.cond());
        breakTargetStack.add(node);
        continueTargetStack.add(node);
        resolve(node.body());
        continueTargetStack.removeLast();
        breakTargetStack.removeLast();
        return null;
    }
    // #@@}

    public DoWhileNode visit(DoWhileNode node) {
        breakTargetStack.add(node);
        continueTargetStack.add(node);
        resolve(node.body());
        continueTargetStack.removeLast();
        breakTargetStack.removeLast();
        resolve(node.cond());
        return null;
    }

    // #@@range/_for{
    public ForNode visit(ForNode node) {
        resolve(node.init());
        resolve(node.cond());
        breakTargetStack.add(node);
        continueTargetStack.add(node);
        resolve(node.body());
        resolve(node.incr());
        continueTargetStack.removeLast();
        breakTargetStack.removeLast();
        return null;
    }
    // #@@}

    // #@@range/_break{
    public BreakNode visit(BreakNode node) {
        if (breakTargetStack.isEmpty()) {
            errorHandler.error(node.location(),
                    "break from out of while/do-while/for/switch");
            return null;
        }
        BreakableStmt target = breakTargetStack.getLast();
        node.setTargetLabel(target.endLabel());
        return null;
    }
    // #@@}

    // #@@range/_continue{
    public ContinueNode visit(ContinueNode node) {
        if (breakTargetStack.isEmpty()) {
            errorHandler.error(node.location(),
                        "continue from out of while/do-while/for");
            return null;
        }
        ContinueableStmt target = continueTargetStack.getLast();
        node.setTargetLabel(target.continueLabel());
        return null;
    }
    // #@@}

    public LabelNode visit(LabelNode node) {
        try {
            Label label = currentFunction.defineLabel(node.name(),
                                                      node.location());
            node.setLabel(label);
        }
        catch (SemanticException ex) {
            errorHandler.error(node.location(), ex.getMessage());
        }
        return null;
    }

    public GotoNode visit(GotoNode node) {
        node.setTargetLabel(currentFunction.referLabel(node.target()));
        return null;
    }

    public ReturnNode visit(ReturnNode node) {
        node.setFunction(currentFunction);
        return null;
    }
}
