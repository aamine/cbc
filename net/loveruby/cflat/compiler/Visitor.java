package net.loveruby.cflat.compiler;
import net.loveruby.cflat.ast.*;
import java.util.*;

abstract public class Visitor implements ASTVisitor {
    public Visitor() {
    }

    protected void resolve(Node node) {
        node.accept(this);
    }

    protected void resolveNodeList(Iterator ns) {
        while (ns.hasNext()) {
            Node n = (Node)ns.next();
            resolve(n);
        }
    }

    //
    // Declarations
    //

    public void visit(DefinedVariable var) {
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
            if (var.hasInitializer()) {
                resolve(var.initializer());
            }
        }
        resolveNodeList(node.stmts());
    }

    public void visit(IfNode n) {
        resolve(n.cond());
        resolve(n.thenBody());
        if (n.elseBody() != null) {
            resolve(n.elseBody());
        }
    }

    public void visit(SwitchNode n) {
        resolve(n.cond());
        resolveNodeList(n.cases());
    }

    public void visit(CaseNode n) {
        resolveNodeList(n.values());
        resolve(n.body());
    }

    public void visit(WhileNode n) {
        resolve(n.cond());
        resolve(n.body());
    }

    public void visit(DoWhileNode n) {
        resolve(n.body());
        resolve(n.cond());
    }

    public void visit(ForNode n) {
        resolve(n.init());
        resolve(n.cond());
        resolve(n.incr());
        resolve(n.body());
    }

    public void visit(BreakNode n) {
    }

    public void visit(ContinueNode n) {
    }

    public void visit(GotoNode n) {
    }

    public void visit(LabelNode n) {
        resolve(n.stmt());
    }

    public void visit(ReturnNode n) {
        if (n.expr() != null) {
            resolve(n.expr());
        }
    }

    //
    // Expressions
    //

    public void visit(CondExprNode n) {
        resolve(n.cond());
        resolve(n.thenExpr());
        if (n.elseExpr() != null) {
            resolve(n.elseExpr());
        }
    }

    public void visit(LogicalOrNode node) {
        resolve(node.left());
        resolve(node.right());
    }

    public void visit(LogicalAndNode node) {
        resolve(node.left());
        resolve(node.right());
    }

    public void visit(AssignNode n) {
        resolve(n.lhs());
        resolve(n.rhs());
    }

    public void visit(OpAssignNode n) {
        resolve(n.lhs());
        resolve(n.rhs());
    }

    public void visit(BinaryOpNode n) {
        resolve(n.left());
        resolve(n.right());
    }

    public void visit(UnaryOpNode node) {
        resolve(node.expr());
    }

    public void visit(PrefixOpNode node) {
        resolve(node.expr());
    }

    public void visit(SuffixOpNode node) {
        resolve(node.expr());
    }

    public void visit(FuncallNode node) {
        resolve(node.expr());
        resolveNodeList(node.arguments());
    }

    public void visit(ArefNode node) {
        resolve(node.expr());
        resolve(node.index());
    }

    public void visit(MemberNode node) {
        resolve(node.expr());
    }

    public void visit(PtrMemberNode node) {
        resolve(node.expr());
    }

    public void visit(DereferenceNode node) {
        resolve(node.expr());
    }

    public void visit(AddressNode node) {
        resolve(node.expr());
    }

    public void visit(CastNode node) {
        resolve(node.expr());
    }

    public void visit(VariableNode node) {
    }

    public void visit(IntegerLiteralNode node) {
    }

    public void visit(CharacterLiteralNode node) {
    }

    public void visit(StringLiteralNode node) {
    }
}
