package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.Label;

public class GotoNode extends StmtNode {
    protected String target;
    protected Label label;

    public GotoNode(Location loc, String target) {
        super(loc);
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

    protected void _dump(Dumper d) {
        d.printMember("target", target);
    }

    public GotoNode accept(ASTVisitor visitor) {
        return visitor.visit(this);
    }
}
