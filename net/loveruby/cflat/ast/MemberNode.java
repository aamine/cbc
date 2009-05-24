package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.Type;
import net.loveruby.cflat.type.CompositeType;
import net.loveruby.cflat.exception.*;

public class MemberNode extends LHSNode {
    private ExprNode expr;
    private String member;

    public MemberNode(ExprNode expr, String member) {
        this.expr = expr;
        this.member = member;
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

    protected Type origType() {
        return baseType().memberType(member);
    }

    public Location location() {
        return expr.location();
    }

    protected void _dump(Dumper d) {
        if (type != null) {
            d.printMember("type", type);
        }
        d.printMember("expr", expr);
        d.printMember("member", member);
    }

    public <S,E> E accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
