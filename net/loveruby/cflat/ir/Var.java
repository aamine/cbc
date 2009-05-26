package net.loveruby.cflat.ir;
import net.loveruby.cflat.entity.Entity;
import net.loveruby.cflat.asm.Type;
import net.loveruby.cflat.asm.Operand;
import net.loveruby.cflat.asm.MemoryReference;

public class Var extends Expr {
    protected Entity entity;

    public Var(Type type, Entity entity) {
        super(type);
        this.entity = entity;
    }

    public boolean isVar() { return true; }

    public Type type() {
        if (super.type() == null) {
            throw new Error("Var is too big to load by 1 insn");
        }
        return super.type();
    }

    public String name() { return entity.name(); }
    public Entity entity() { return entity; }

    public Operand address() {
        return entity.address();
    }

    public MemoryReference memref() {
        return entity.memref();
    }

    // #@@range/addressNode{
    public Addr addressNode(Type type) {
        return new Addr(type, entity);
    }
    // #@@}

    public Entity getEntityForce() {
        return entity;
    }

    public <S,E> E accept(IRVisitor<S,E> visitor) {
        return visitor.visit(this);
    }

    protected void _dump(Dumper d) {
        d.printMember("entity", entity.name());
    }
}
