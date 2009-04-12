package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;

public class DereferenceNode extends UnaryOpNode {
    public DereferenceNode(ExprNode n) {
        super("*", n);
    }

    public Type type() {
        return expr().type().baseType();
    }

    public boolean isAssignable() { return true; }
    public boolean isConstantAddress() { return false; }

    public <S,E> E accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }

    public void acceptLHS(ASTLHSVisitor visitor) {
        visitor.visitLHS(this);
    }
}
