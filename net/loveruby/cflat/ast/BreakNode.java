package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.*;

public class BreakNode extends StmtNode {
    public BreakNode(Location loc) {
        super(loc);
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

    public Location location() {
        return location;
    }

    protected void _dump(Dumper d) {
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
