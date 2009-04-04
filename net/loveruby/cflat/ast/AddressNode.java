package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;

public class AddressNode extends UnaryOpNode {
    protected Type type;

    public AddressNode(ExprNode n) {
        super("&", n);
    }

    public void setExpr(ExprNode expr) {
        this.expr = expr;
    }

    public Type type() {
        if (type == null) throw new Error("type is null");
        return type;
    }

    /** Decides type of this node.
     * This method is called in TypeChecker. */
    public void setType(Type type) {
        if (this.type != null) throw new Error("type set twice");
        this.type = type;
    }

    public AddressNode accept(ASTVisitor visitor) {
        return visitor.visit(this);
    }
}
