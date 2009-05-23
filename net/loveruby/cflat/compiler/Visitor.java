package net.loveruby.cflat.compiler;
import net.loveruby.cflat.ast.*;
import net.loveruby.cflat.entity.DefinedVariable;
import java.util.*;

// We do not use return value of methods.
abstract public class Visitor implements ASTVisitor<Void, Void> {
    public Visitor() {
    }

    protected void visitStmt(StmtNode stmt) {
        stmt.accept(this);
    }

    protected void visitStmts(List<? extends StmtNode> stmts) {
        for (StmtNode s : stmts) {
            visitStmt(s);
        }
    }

    protected void visitExpr(ExprNode expr) {
        expr.accept(this);
    }

    protected void visitExprs(List<? extends ExprNode> exprs) {
        for (ExprNode e : exprs) {
            visitExpr(e);
        }
    }

    //
    // Statements
    //

    public Void visit(BlockNode node) {
        for (DefinedVariable var : node.variables()) {
            if (var.hasInitializer()) {
                visitExpr(var.initializer());
            }
        }
        visitStmts(node.stmts());
        return null;
    }

    public Void visit(ExprStmtNode node) {
        visitExpr(node.expr());
        return null;
    }

    public Void visit(IfNode n) {
        visitExpr(n.cond());
        visitStmt(n.thenBody());
        if (n.elseBody() != null) {
            visitStmt(n.elseBody());
        }
        return null;
    }

    public Void visit(SwitchNode n) {
        visitExpr(n.cond());
        visitStmts(n.cases());
        return null;
    }

    public Void visit(CaseNode n) {
        visitExprs(n.values());
        visitStmt(n.body());
        return null;
    }

    public Void visit(WhileNode n) {
        visitExpr(n.cond());
        visitStmt(n.body());
        return null;
    }

    public Void visit(DoWhileNode n) {
        visitStmt(n.body());
        visitExpr(n.cond());
        return null;
    }

    public Void visit(ForNode n) {
        visitStmt(n.init());
        visitExpr(n.cond());
        visitStmt(n.incr());
        visitStmt(n.body());
        return null;
    }

    public Void visit(BreakNode n) {
        return null;
    }

    public Void visit(ContinueNode n) {
        return null;
    }

    public Void visit(GotoNode n) {
        return null;
    }

    public Void visit(LabelNode n) {
        visitStmt(n.stmt());
        return null;
    }

    public Void visit(ReturnNode n) {
        if (n.expr() != null) {
            visitExpr(n.expr());
        }
        return null;
    }

    //
    // Expressions
    //

    public Void visit(CondExprNode n) {
        visitExpr(n.cond());
        visitExpr(n.thenExpr());
        if (n.elseExpr() != null) {
            visitExpr(n.elseExpr());
        }
        return null;
    }

    public Void visit(LogicalOrNode node) {
        visitExpr(node.left());
        visitExpr(node.right());
        return null;
    }

    public Void visit(LogicalAndNode node) {
        visitExpr(node.left());
        visitExpr(node.right());
        return null;
    }

    public Void visit(AssignNode n) {
        visitExpr(n.lhs());
        visitExpr(n.rhs());
        return null;
    }

    public Void visit(OpAssignNode n) {
        visitExpr(n.lhs());
        visitExpr(n.rhs());
        return null;
    }

    public Void visit(BinaryOpNode n) {
        visitExpr(n.left());
        visitExpr(n.right());
        return null;
    }

    public Void visit(UnaryOpNode node) {
        visitExpr(node.expr());
        return null;
    }

    public Void visit(PrefixOpNode node) {
        visitExpr(node.expr());
        return null;
    }

    public Void visit(SuffixOpNode node) {
        visitExpr(node.expr());
        return null;
    }

    public Void visit(FuncallNode node) {
        visitExpr(node.expr());
        visitExprs(node.args());
        return null;
    }

    public Void visit(ArefNode node) {
        visitExpr(node.expr());
        visitExpr(node.index());
        return null;
    }

    public Void visit(MemberNode node) {
        visitExpr(node.expr());
        return null;
    }

    public Void visit(PtrMemberNode node) {
        visitExpr(node.expr());
        return null;
    }

    public Void visit(DereferenceNode node) {
        visitExpr(node.expr());
        return null;
    }

    public Void visit(AddressNode node) {
        visitExpr(node.expr());
        return null;
    }

    public Void visit(CastNode node) {
        visitExpr(node.expr());
        return null;
    }

    public Void visit(SizeofExprNode node) {
        visitExpr(node.expr());
        return null;
    }

    public Void visit(SizeofTypeNode node) {
        return null;
    }

    public Void visit(VariableNode node) {
        return null;
    }

    public Void visit(IntegerLiteralNode node) {
        return null;
    }

    public Void visit(StringLiteralNode node) {
        return null;
    }
}
