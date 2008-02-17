package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.asm.AsmEntity;
import net.loveruby.cflat.exception.*;

public class MemberNode extends ExprNode {
    protected ExprNode expr;
    protected String member;

    public MemberNode(ExprNode expr, String member) {
        this.expr = expr;
        this.member = member;
    }

    public Type type() {
        return baseType().memberType(member);
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

    public String member() {
        return member;
    }

    public long offset() {
        return baseType().memberOffset(member);
    }

    // #@@range/isAssignable{
    public boolean isAssignable() {
        return true;
    }
    // #@@}

    public boolean isConstant() {
        return expr.isConstantAddress();
    }

    public boolean isConstantAddress() {
        return expr.isConstantAddress();
    }

    public AsmEntity address() {
        return expr.address().add(offset());
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
