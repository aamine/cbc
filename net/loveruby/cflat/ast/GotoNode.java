package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.Label;

public class GotoNode extends Node {
    protected String target;
    protected Label label;

    public GotoNode(String target) {
        super();
        this.target = target;
    }

    public String target() {
        return target;
    }

    public void setTargetLabel(Label label) {
        this.label = label;
    }

    public Label targetLabel() {
        if (label == null) throw new Error("goto target label is null");
        return label;
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
