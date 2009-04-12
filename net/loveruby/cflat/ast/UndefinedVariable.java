package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;

public class UndefinedVariable extends Variable {
    public UndefinedVariable(TypeNode t, String name) {
        super(false, t, name);
    }

    public boolean isDefined() { return false; }
    public boolean isPrivate() { return false; }
    public boolean isInitialized() { return false; }

    protected void _dump(Dumper d) {
        d.printMember("name", name);
        d.printMember("isPrivate", isPrivate());
        d.printMember("typeNode", typeNode);
    }

    public <S,E> S accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
