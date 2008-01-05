package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.asm.*;
import net.loveruby.cflat.exception.*;

public class PtrMemberNode extends ExprNode {
    public ExprNode expr;
    public String member;

    public PtrMemberNode(ExprNode expr, String member) {
        this.expr = expr;
        this.member = member;
    }

    public Type type() {
        return dereferedType().memberType(member);
    }

    public ComplexType dereferedType() {
        try {
            PointerType pt = expr.type().getPointerType();
            return pt.baseType().getComplexType();
        }
        catch (ClassCastException err) {
            throw new SemanticError(err.getMessage());
        }
    }

    public ExprNode expr() {
        return expr;
    }

    public String member() {
        return member;
    }

    public boolean isAssignable() {
        return true;
    }

    public boolean isConstantAddress() {
        return false;
    }

    public AsmEntity address() {
        throw new Error("PtrMemberNode#address");
    }

    public long offset() {
        return dereferedType().memberOffset(member);
    }

    public Location location() {
        return expr.location();
    }

    protected void _dump(Dumper d) {
        d.printMember("expr", expr);
        d.printMember("member", member);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
