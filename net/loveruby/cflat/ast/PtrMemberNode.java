package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.asm.*;

public class PtrMemberNode extends Node implements LHSNode {
    public Node expr;
    public String name;

    public PtrMemberNode(Node n, String nm) {
        expr = n;
        name = nm;
    }

    public Type type() {
        return baseType().memberType(name);
    }

    public ComplexType baseType() {
        PointerType ptr = (PointerType)expr.type();
        return (ComplexType)ptr.base();
    }

    public Node expr() {
        return expr;
    }

    public String name() {
        return name;
    }

    public boolean isConstantAddress() {
        return false;
    }

    public AsmEntity address() {
        throw new Error("PtrMemberNode#address");
    }

    public long offset() {
        return baseType().memberOffset(name);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
