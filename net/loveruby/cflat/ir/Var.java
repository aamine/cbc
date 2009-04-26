package net.loveruby.cflat.ir;
import net.loveruby.cflat.entity.Entity;
import net.loveruby.cflat.asm.*;

public class Var extends Expr {
    protected Entity entity;

    public Var(Type type, Entity entity) {
        super(type);
        this.entity = entity;
    }

    public Type type() {
        if (super.type() == null) {
            throw new Error("Var is too big to load by 1 insn");
        }
        return super.type();
    }

    public String name() { return entity.name(); }
    public Entity entity() { return entity; }

    public boolean isConstantAddress() { return true; }

    public AsmOperand address() {
        return entity.address();
    }

    public MemoryReference memref() {
        return entity.memref();
    }

    public <S,E> E accept(IRVisitor<S,E> visitor) {
        return visitor.visit(this);
    }

    protected void _dump(Dumper d) {
        d.printMember("entity", entity.name());
    }
}
