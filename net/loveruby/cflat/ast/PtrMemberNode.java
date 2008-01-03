package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.asm.*;

public class PtrMemberNode extends ExprNode implements LHSNode {
    public ExprNode expr;
    public String name;

    public PtrMemberNode(ExprNode expr, String name) {
        this.expr = expr;
        this.name = name;
    }

    public Type type() {
        return dereferedType().memberType(name);
    }

    public ComplexType dereferedType() {
        PointerType pt = expr.type().getPointerType();
        return pt.baseType().getComplexType();
    }

    public ExprNode expr() {
        return expr;
    }

    public String name() {
        return name;
    }

    public boolean isAssignable() {
        return true;
    }

    // LHS node requirement
    public boolean isConstantAddress() {
        return false;
    }

    // LHS node requirement
    public AsmEntity address() {
        throw new Error("PtrMemberNode#address");
    }

    public long offset() {
        return dereferedType().memberOffset(name);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
