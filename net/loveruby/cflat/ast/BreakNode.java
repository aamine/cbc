package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.Label;

public class BreakNode extends StmtNode {
    public BreakNode(Location loc) {
        super(loc);
    }

    public Location location() {
        return location;
    }

    protected void _dump(Dumper d) {
    }

    public <S,E> S accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
