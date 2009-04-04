package net.loveruby.cflat.compiler;
import net.loveruby.cflat.ast.*;
import java.util.*;

abstract public class Visitor implements ASTVisitor {
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
    // Declarations
    //

    public DefinedVariable visit(DefinedVariable var) {
        if (var.hasInitializer()) {
            visitExpr(var.initializer());
        }
        return null;
    }

    public UndefinedVariable visit(UndefinedVariable var) {
        return null;
    }

    public DefinedFunction visit(DefinedFunction func) {
        return null;
    }

    public UndefinedFunction visit(UndefinedFunction func) {
        return null;
    }

    public StructNode visit(StructNode struct) {
        return null;
    }

    public UnionNode visit(UnionNode union) {
        return null;
    }

    public TypedefNode visit(TypedefNode typedef) {
        return null;
    }

    //
    // Statements
    //

    public BlockNode visit(BlockNode node) {
        for (DefinedVariable var : node.variables()) {
            visit(var);
        }
        visitStmts(node.stmts());
        return null;
    }

    public ExprStmtNode visit(ExprStmtNode node) {
        visitExpr(node.expr());
        return null;
    }

    public IfNode visit(IfNode n) {
        visitExpr(n.cond());
        visitStmt(n.thenBody());
        if (n.elseBody() != null) {
            visitStmt(n.elseBody());
        }
        return null;
    }

    public SwitchNode visit(SwitchNode n) {
        visitExpr(n.cond());
        visitStmts(n.cases());
        return null;
    }

    public CaseNode visit(CaseNode n) {
        visitExprs(n.values());
        visitStmt(n.body());
        return null;
    }

    public WhileNode visit(WhileNode n) {
        visitExpr(n.cond());
        visitStmt(n.body());
        return null;
    }

    public DoWhileNode visit(DoWhileNode n) {
        visitStmt(n.body());
        visitExpr(n.cond());
        return null;
    }

    public ForNode visit(ForNode n) {
        visitStmt(n.init());
        visitExpr(n.cond());
        visitStmt(n.incr());
        visitStmt(n.body());
        return null;
    }

    public BreakNode visit(BreakNode n) {
        return null;
    }

    public ContinueNode visit(ContinueNode n) {
        return null;
    }

    public GotoNode visit(GotoNode n) {
        return null;
    }

    public LabelNode visit(LabelNode n) {
        visitStmt(n.stmt());
        return null;
    }

    public ReturnNode visit(ReturnNode n) {
        if (n.expr() != null) {
            visitExpr(n.expr());
        }
        return null;
    }

    //
    // Expressions
    //

    public CondExprNode visit(CondExprNode n) {
        visitExpr(n.cond());
        visitExpr(n.thenExpr());
        if (n.elseExpr() != null) {
            visitExpr(n.elseExpr());
        }
        return null;
    }

    public LogicalOrNode visit(LogicalOrNode node) {
        visitExpr(node.left());
        visitExpr(node.right());
        return null;
    }

    public LogicalAndNode visit(LogicalAndNode node) {
        visitExpr(node.left());
        visitExpr(node.right());
        return null;
    }

    public AssignNode visit(AssignNode n) {
        visitExpr(n.lhs());
        visitExpr(n.rhs());
        return null;
    }

    public OpAssignNode visit(OpAssignNode n) {
        visitExpr(n.lhs());
        visitExpr(n.rhs());
        return null;
    }

    public BinaryOpNode visit(BinaryOpNode n) {
        visitExpr(n.left());
        visitExpr(n.right());
        return null;
    }

    public UnaryOpNode visit(UnaryOpNode node) {
        visitExpr(node.expr());
        return null;
    }

    public PrefixOpNode visit(PrefixOpNode node) {
        visitExpr(node.expr());
        return null;
    }

    public SuffixOpNode visit(SuffixOpNode node) {
        visitExpr(node.expr());
        return null;
    }

    public FuncallNode visit(FuncallNode node) {
        visitExpr(node.expr());
        visitExprs(node.arguments());
        return null;
    }

    public ArefNode visit(ArefNode node) {
        visitExpr(node.expr());
        visitExpr(node.index());
        return null;
    }

    public MemberNode visit(MemberNode node) {
        visitExpr(node.expr());
        return null;
    }

    public PtrMemberNode visit(PtrMemberNode node) {
        visitExpr(node.expr());
        return null;
    }

    public DereferenceNode visit(DereferenceNode node) {
        visitExpr(node.expr());
        return null;
    }

    public AddressNode visit(AddressNode node) {
        visitExpr(node.expr());
        return null;
    }

    public CastNode visit(CastNode node) {
        visitExpr(node.expr());
        return null;
    }

    public SizeofExprNode visit(SizeofExprNode node) {
        visitExpr(node.expr());
        return null;
    }

    public SizeofTypeNode visit(SizeofTypeNode node) {
        return null;
    }

    public VariableNode visit(VariableNode node) {
        return null;
    }

    public IntegerLiteralNode visit(IntegerLiteralNode node) {
        return null;
    }

    public StringLiteralNode visit(StringLiteralNode node) {
        return null;
    }
}
