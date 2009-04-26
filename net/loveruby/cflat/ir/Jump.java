package net.loveruby.cflat.ir;
import net.loveruby.cflat.ast.Location;
import net.loveruby.cflat.asm.Label;

public class Jump extends Stmt {
    protected Label label;

    public Jump(Location loc, Label label) {
        super(loc);
        this.label = label;
    }

    public Label label() {
        return label;
    }

    public <S,E> S accept(IRVisitor<S,E> visitor) {
        return visitor.visit(this);
    }

    protected void _dump(Dumper d) {
        d.printMember("label", label);
    }
}
