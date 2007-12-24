package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.*;

public class CondExprNode extends IfNode {
    public CondExprNode(LabelPool lp, Node c, Node t, Node e) {
        super(lp, c, t, e);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
