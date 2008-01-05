package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.asm.AsmEntity;
import net.loveruby.cflat.exception.*;

public class MemberNode extends ExprNode implements LHSNode {
    protected ExprNode expr;
    protected String name;

    public MemberNode(ExprNode expr, String name) {
        this.expr = expr;
        this.name = name;
    }

    public Type type() {
        return baseType().memberType(name);
    }

    public ComplexType baseType() {
        try {
            return expr.type().getComplexType();
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

    public long offset() {
        return baseType().memberOffset(name);
    }

    public boolean isAssignable() {
        return true;
    }

    // LHS node requirement
    public boolean isConstantAddress() {
        return ((LHSNode)expr).isConstantAddress();
    }

    // LHS node requirement
    public AsmEntity address() {
        return ((LHSNode)expr).address().add(offset());
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
