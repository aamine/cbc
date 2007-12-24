package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.asm.AsmEntity;

public class MemberNode extends Node implements LHSNode {
    protected Node expr;
    protected String name;

    public MemberNode(Node expr, String name) {
        this.expr = expr;
        this.name = name;
    }

    public Type type() {
        return baseType().memberType(name);
    }

    public ComplexType baseType() {
        return (ComplexType)expr.type();
    }

    public Node expr() {
        return expr;
    }

    public String name() {
        return name;
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    public long offset() {
        return baseType().memberOffset(name);
    }

    public boolean isConstantAddress() {
        return ((LHSNode)expr).isConstantAddress();
    }

    public AsmEntity address() {
        return expr.address().add(offset());
    }
}
