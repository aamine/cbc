package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.*;

public class ContinueNode extends StmtNode {
    public ContinueNode(Location loc) {
        super(loc);
    }

    protected Label label;

    public void setTargetLabel(Label label) {
        this.label = label;
    }

    public Label targetLabel() {
        if (label == null) {
            throw new Error("continue target label is null");
        }
        return label;
    }

    protected void _dump(Dumper d) {
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
