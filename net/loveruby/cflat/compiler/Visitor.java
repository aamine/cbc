package net.loveruby.cflat.compiler;
import net.loveruby.cflat.ast.*;
import java.util.*;

abstract public class Visitor implements ASTVisitor {
    public Visitor() {
    }

    protected void visitNode(Node node) {
        node.accept(this);
    }

    protected void visitNodeList(Iterator ns) {
        while (ns.hasNext()) {
            Node n = (Node)ns.next();
            visitNode(n);
        }
    }

    //
    // Declarations
    //

    public void visit(DefinedVariable var) {
        if (var.hasInitializer()) {
            visitNode(var.initializer());
        }
    }

    public void visit(UndefinedVariable var) {
    }

    public void visit(DefinedFunction func) {
    }

    public void visit(UndefinedFunction func) {
    }

    public void visit(StructNode struct) {
    }

    public void visit(UnionNode union) {
    }

    public void visit(TypedefNode typedef) {
    }

    //
    // Statements
    //

    public void visit(BlockNode node) {
        Iterator vars = node.variables();
        while (vars.hasNext()) {
            DefinedVariable var = (DefinedVariable)vars.next();
            visit(var);
        }
        visitNodeList(node.stmts());
    }

    public void visit(IfNode n) {
        visitNode(n.cond());
        visitNode(n.thenBody());
        if (n.elseBody() != null) {
            visitNode(n.elseBody());
        }
    }

    public void visit(SwitchNode n) {
        visitNode(n.cond());
        visitNodeList(n.cases());
    }

    public void visit(CaseNode n) {
        visitNodeList(n.values());
        visitNode(n.body());
    }

    public void visit(WhileNode n) {
        visitNode(n.cond());
        visitNode(n.body());
    }

    public void visit(DoWhileNode n) {
        visitNode(n.body());
        visitNode(n.cond());
    }

    public void visit(ForNode n) {
        visitNode(n.init());
        visitNode(n.cond());
        visitNode(n.incr());
        visitNode(n.body());
    }

    public void visit(BreakNode n) {
    }

    public void visit(ContinueNode n) {
    }

    public void visit(GotoNode n) {
    }

    public void visit(LabelNode n) {
        visitNode(n.stmt());
    }

    public void visit(ReturnNode n) {
        if (n.expr() != null) {
            visitNode(n.expr());
        }
    }

    //
    // Expressions
    //

    public void visit(CondExprNode n) {
        visitNode(n.cond());
        visitNode(n.thenExpr());
        if (n.elseExpr() != null) {
            visitNode(n.elseExpr());
        }
    }

    public void visit(LogicalOrNode node) {
        visitNode(node.left());
        visitNode(node.right());
    }

    public void visit(LogicalAndNode node) {
        visitNode(node.left());
        visitNode(node.right());
    }

    public void visit(AssignNode n) {
        visitNode(n.lhs());
        visitNode(n.rhs());
    }

    public void visit(OpAssignNode n) {
        visitNode(n.lhs());
        visitNode(n.rhs());
    }

    public void visit(BinaryOpNode n) {
        visitNode(n.left());
        visitNode(n.right());
    }

    public void visit(UnaryOpNode node) {
        visitNode(node.expr());
    }

    public void visit(PrefixOpNode node) {
        visitNode(node.expr());
    }

    public void visit(SuffixOpNode node) {
        visitNode(node.expr());
    }

    public void visit(FuncallNode node) {
        visitNode(node.expr());
        visitNodeList(node.arguments());
    }

    public void visit(ArefNode node) {
        visitNode(node.expr());
        visitNode(node.index());
    }

    public void visit(MemberNode node) {
        visitNode(node.expr());
    }

    public void visit(PtrMemberNode node) {
        visitNode(node.expr());
    }

    public void visit(DereferenceNode node) {
        visitNode(node.expr());
    }

    public void visit(AddressNode node) {
        visitNode(node.expr());
    }

    public void visit(CastNode node) {
        visitNode(node.expr());
    }

    public void visit(SizeofExprNode node) {
        visitNode(node.expr());
    }

    public void visit(SizeofTypeNode node) {
    }

    public void visit(VariableNode node) {
    }

    public void visit(IntegerLiteralNode node) {
    }

    public void visit(StringLiteralNode node) {
    }
}
