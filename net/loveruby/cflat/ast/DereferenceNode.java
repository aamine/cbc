package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.*;

public class DereferenceNode extends UnaryOpNode implements LHSNode {
    public DereferenceNode(Node n) {
        super(n);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    public boolean isConstantAddress() {
        return false;
    }

    public AsmEntity address() {
        throw new Error("DereferenceNode#address");
    }
}
