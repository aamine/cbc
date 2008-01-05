package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.asm.*;
import net.loveruby.cflat.exception.*;

public class PtrMemberNode extends ExprNode {
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

    public String name() {
        return name;
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
        return dereferedType().memberOffset(name);
    }

    public Location location() {
        return expr.location();
    }

    protected void _dump(Dumper d) {
        d.printMember("expr", expr);
        d.printMember("name", name);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
