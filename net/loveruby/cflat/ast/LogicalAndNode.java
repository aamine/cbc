package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.LabelPool;
import net.loveruby.cflat.asm.Label;

public class LogicalAndNode extends BinaryOpNode {
    protected LabelPool pool;
    protected Label endLabel;

    public LogicalAndNode(LabelPool lp, ExprNode left, ExprNode right) {
        super(left, "&&", right);
        pool = lp;
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    public Label endLabel() {
        if (endLabel == null) {
            endLabel = pool.newLabel();
        }
        return endLabel;
    }
}
