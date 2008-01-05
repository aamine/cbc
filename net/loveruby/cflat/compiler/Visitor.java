package net.loveruby.cflat.compiler;
import net.loveruby.cflat.ast.*;
import java.util.*;

public class Visitor implements ASTVisitor {
    protected void resolve(Node node) {
        node.accept(this);
    }

    protected void resolve2(Node l, Node r) {
        resolve(l);
        resolve(r);
    }

    protected void resolveNodeList(Iterator ns) {
        while (ns.hasNext()) {
            Node n = (Node)ns.next();
            resolve(n);
        }
    }

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

    public void visit(CondExprNode n) {
        resolve(n.cond());
        resolve(n.thenExpr());
        if (n.elseExpr() != null) {
            resolve(n.elseExpr());
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

    public void visit(LogicalOrNode node) {
        resolve2(node.left(), node.right());
    }

    public void visit(LogicalAndNode node) {
        resolve2(node.left(), node.right());
    }

    public void visit(ReturnNode n) {
        if (n.expr() != null) {
            resolve(n.expr());
        }
    }

    public void visit(AssignNode n) {
        resolve2(n.lhs(), n.rhs());
    }

    public void visit(PlusAssignNode n) {
        resolve2(n.lhs(), n.rhs());
    }

    public void visit(MinusAssignNode n) {
        resolve2(n.lhs(), n.rhs());
    }

    public void visit(MulAssignNode n) {
        resolve2(n.lhs(), n.rhs());
    }

    public void visit(DivAssignNode n) {
        resolve2(n.lhs(), n.rhs());
    }

    public void visit(ModAssignNode n) {
        resolve2(n.lhs(), n.rhs());
    }

    public void visit(AndAssignNode n) {
        resolve2(n.lhs(), n.rhs());
    }

    public void visit(OrAssignNode n) {
        resolve2(n.lhs(), n.rhs());
    }

    public void visit(XorAssignNode n) {
        resolve2(n.lhs(), n.rhs());
    }

    public void visit(LShiftAssignNode n) {
        resolve2(n.lhs(), n.rhs());
    }

    public void visit(RShiftAssignNode n) {
        resolve2(n.lhs(), n.rhs());
    }

    public void visit(GtNode n) {
        resolve2(n.left(), n.right());
    }

    public void visit(LtNode n) {
        resolve2(n.left(), n.right());
    }

    public void visit(GtEqNode n) {
        resolve2(n.left(), n.right());
    }

    public void visit(LtEqNode n) {
        resolve2(n.left(), n.right());
    }

    public void visit(EqNode n) {
        resolve2(n.left(), n.right());
    }

    public void visit(NotEqNode n) {
        resolve2(n.left(), n.right());
    }

    public void visit(BitwiseOrNode n) {
        resolve2(n.left(), n.right());
    }

    public void visit(BitwiseXorNode n) {
        resolve2(n.left(), n.right());
    }

    public void visit(BitwiseAndNode n) {
        resolve2(n.left(), n.right());
    }

    public void visit(RShiftNode n) {
        resolve2(n.left(), n.right());
    }

    public void visit(LShiftNode n) {
        resolve2(n.left(), n.right());
    }

    public void visit(PlusNode n) {
        resolve2(n.left(), n.right());
    }

    public void visit(MinusNode n) {
        resolve2(n.left(), n.right());
    }

    public void visit(MulNode n) {
        resolve2(n.left(), n.right());
    }

    public void visit(DivNode n) {
        resolve2(n.left(), n.right());
    }

    public void visit(ModNode n) {
        resolve2(n.left(), n.right());
    }

    public void visit(SuffixIncNode node) {
        resolve(node.expr());
    }

    public void visit(SuffixDecNode node) {
        resolve(node.expr());
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

    public void visit(IntegerLiteralNode node) {
    }

    public void visit(CharacterLiteralNode node) {
    }

    public void visit(StringLiteralNode node) {
    }

    public void visit(FuncallNode node) {
        resolve(node.expr());
        resolveNodeList(node.arguments());
    }

    public void visit(VariableNode node) {
    }

    public void visit(UnaryPlusNode node) {
        resolve(node.expr());
    }

    public void visit(UnaryMinusNode node) {
        resolve(node.expr());
    }

    public void visit(LogicalNotNode node) {
        resolve(node.expr());
    }

    public void visit(BitwiseNotNode node) {
        resolve(node.expr());
    }

    public void visit(DereferenceNode node) {
        resolve(node.expr());
    }

    public void visit(AddressNode node) {
        resolve(node.expr());
    }

    public void visit(PrefixIncNode node) {
        resolve(node.expr());
    }

    public void visit(PrefixDecNode node) {
        resolve(node.expr());
    }

    public void visit(CastNode node) {
        resolve(node.expr());
    }

    //
    // Declarations
    //

    public void visit(DefinedVariable var) {}
    public void visit(UndefinedVariable var) {}
    public void visit(DefinedFunction func) {}
    public void visit(UndefinedFunction func) {}
    public void visit(StructNode struct) {}
    public void visit(UnionNode union) {}
    public void visit(TypedefNode typedef) {}
}
