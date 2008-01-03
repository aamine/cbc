package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.*;

public class BreakNode extends Node {
    public BreakNode() {
        super();
    }

    protected Label label;

    public void setTargetLabel(Label label) {
        this.label = label;
    }

    public Label targetLabel() {
        if (label == null) {
            throw new Error("break label is null");
        }
        return label;
    }

    protected void _dump(Dumper d) {
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
