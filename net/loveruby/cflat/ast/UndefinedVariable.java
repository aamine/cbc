package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;

public class UndefinedVariable extends Variable {
    public UndefinedVariable(TypeNode t, String name) {
        super(false, t, name);
    }

    public boolean isDefined() { return false; }
    public boolean isPrivate() { return false; }
    public boolean isInitialized() { return false; }

    public String symbolString() {
        return name();
    }

    protected void _dump(Dumper d) {
        d.printMember("name", name);
        d.printMember("isPrivate", isPrivate());
        d.printMember("typeNode", typeNode);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
