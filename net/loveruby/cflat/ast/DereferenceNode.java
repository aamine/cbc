package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.asm.*;

public class DereferenceNode extends UnaryOpNode {
    public DereferenceNode(ExprNode n) {
        super(n);
    }

    public Type type() {
        return expr().type().baseType();
    }

    public boolean isAssignable() { return true; }
    public boolean isConstantAddress() { return false; }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
