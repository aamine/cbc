package net.loveruby.cflat.ir;
import net.loveruby.cflat.ast.Entity;
import net.loveruby.cflat.asm.AsmOperand;
import net.loveruby.cflat.asm.MemoryReference;

public class Var extends Expr {
    protected Entity entity;

    public Var(Entity entity) {
        super(entity.type());
        this.entity = entity;
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
}
