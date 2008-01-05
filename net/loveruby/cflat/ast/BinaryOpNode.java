package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.Type;

public class BinaryOpNode extends ExprNode {
    protected ExprNode left, right;
    protected String operator;
    protected Type type;

    public BinaryOpNode(ExprNode left, String op, ExprNode right) {
        super();
        this.left = left;
        this.operator = op;
        this.right = right;
    }

    public String operator() {
        return operator;
    }

    public Type type() {
        return (type != null) ? type : left.type();
    }

    public void setType(Type type) {
        if (this.type != null)
            throw new Error("BinaryOp#setType called twice");
        this.type = type;
    }

    public ExprNode left() {
        return left;
    }

    public void setLeft(ExprNode left) {
        this.left = left;
    }

    public ExprNode right() {
        return right;
    }

    public void setRight(ExprNode right) {
        this.right = right;
    }

    public Location location() {
        return left.location();
    }

    protected void _dump(Dumper d) {
        d.printMember("left", left);
        d.printMember("right", right);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
