package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.asm.AsmOperand;
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

    public CompositeType baseType() {
        try {
            return expr.type().getCompositeType();
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

    public boolean isConstantAddress() {
        return false;
    }

    public Location location() {
        return expr.location();
    }

    protected void _dump(Dumper d) {
        d.printMember("expr", expr);
        d.printMember("member", member);
    }

    public MemberNode accept(ASTVisitor visitor) {
        return visitor.visit(this);
    }

    public void acceptLHS(ASTLHSVisitor visitor) {
        visitor.visitLHS(this);
    }
}
