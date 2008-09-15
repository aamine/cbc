package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.asm.*;

public class UndefinedVariable extends Variable {
    public UndefinedVariable(TypeNode t, String name) {
        super(false, t, name);
    }

    public boolean isDefined() { return false; }
    public boolean isPrivate() { return false; }
    public boolean isInitialized() { return false; }

    public String symbol() {
        return name();
    }

    public void setMemref(MemoryReference mem) {
        throw new Error("UndefinedVariable#Memref");
    }

    public MemoryReference memref() {
        return new DirectMemoryReference(new Label(symbol()));
    }

    public AsmEntity address() {
        return new ImmediateValue(new Label(symbol()));
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
