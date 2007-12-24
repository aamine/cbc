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

    public void accept(DefinitionVisitor visitor) {
        visitor.visit(this);
    }

    public void defineIn(ToplevelScope s) {
        throw new Error("UndefinedVariable#defineIn called");
    }
}
