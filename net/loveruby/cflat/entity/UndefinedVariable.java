package net.loveruby.cflat.entity;
import net.loveruby.cflat.ast.TypeNode;

public class UndefinedVariable extends Variable {
    public UndefinedVariable(TypeNode t, String name) {
        super(false, t, name);
    }

    public boolean isDefined() { return false; }
    public boolean isPrivate() { return false; }
    public boolean isInitialized() { return false; }

    protected void _dump(net.loveruby.cflat.ast.Dumper d) {
        d.printMember("name", name);
        d.printMember("isPrivate", isPrivate());
        d.printMember("typeNode", typeNode);
    }

    public <T> T accept(EntityVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
