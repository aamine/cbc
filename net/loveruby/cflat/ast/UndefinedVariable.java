package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.asm.*;

public class UndefinedVariable extends Variable {
    public UndefinedVariable(TypeNode t, String name) {
        super(true, t, name);
    }

    public boolean isDefined() { return false; }
    public boolean isPrivate() { return false; }
    public boolean isInitialized() { return false; }

    public String symbol() {
        return name();
    }

    public void defineIn(ToplevelScope s) {
        throw new Error("UndefinedVariable#defineIn called");
    }

    public void setAddress(AsmEntity addr) {
        throw new Error("UndefinedVariable#setAddress");
    }

    public AsmEntity address() {
        return new Reference(symbol());
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
