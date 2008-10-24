package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.Label;

public class LogicalAndNode extends BinaryOpNode {
    protected Label endLabel;

    public LogicalAndNode(ExprNode left, ExprNode right) {
        super(left, "&&", right);
        this.endLabel = new Label();
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    public Label endLabel() {
        return endLabel;
    }
}
