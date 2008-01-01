package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;

public class AddressNode extends UnaryOpNode {
    protected Type type;

    public AddressNode(Node n) {
        super(n);
    }

    public Type type() {
        if (type == null) throw new Error("type is null");
        return type;
    }

    /** This method is called in TypeChecker. */
    public void setType(Type type) {
        if (this.type != null) throw new Error("type set twice");
        this.type = type;
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
