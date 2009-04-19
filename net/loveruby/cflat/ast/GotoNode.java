package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.Label;

public class GotoNode extends StmtNode {
    protected String target;
    protected Label label;

    public GotoNode(Location loc, String target) {
        super(loc);
        this.target = target;
    }

    public GotoNode(Label target) {
        super(null);
        label = target;
    }

    public String target() {
        return target;
    }

    public Label label() {
        return label;
    }

    protected void _dump(Dumper d) {
        d.printMember("target", target);
    }

    public <S,E> S accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
