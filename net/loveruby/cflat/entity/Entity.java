package net.loveruby.cflat.entity;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.ast.TypeNode;
import net.loveruby.cflat.ast.Location;
import net.loveruby.cflat.asm.Symbol;
import net.loveruby.cflat.asm.AsmOperand;
import net.loveruby.cflat.asm.MemoryReference;

abstract public class Entity
        implements net.loveruby.cflat.ast.Dumpable {
    protected String name;
    protected boolean isPrivate;
    protected TypeNode typeNode;
    protected long nRefered;
    protected MemoryReference memref;
    protected MemoryReference address;

    public Entity(boolean priv, TypeNode type, String name) {
        this.name = name;
        this.isPrivate = priv;
        this.typeNode = type;
        this.nRefered = 0;
    }

    public String name() {
        return name;
    }

    public String symbolString() {
        return name();
    }

    abstract public boolean isDefined();
    abstract public boolean isInitialized();

    public boolean isParameter() { return false; }

    public boolean isPrivate() {
        return isPrivate;
    }

    public TypeNode typeNode() {
        return typeNode;
    }

    public Type type() {
        return typeNode.type();
    }

    public long allocSize() {
        return type().allocSize();
    }

    public long alignment() {
        return type().alignment();
    }

    public void refered() {
        nRefered++;
    }

    public boolean isRefered() {
        return (nRefered > 0);
    }

    abstract public boolean cannotLoad();

    public void setMemref(MemoryReference mem) {
        this.memref = mem;
    }

    public MemoryReference memref() {
        checkAddress();
        return memref;
    }

    public void setAddress(MemoryReference mem) {
        this.address = mem;
    }

    public AsmOperand address() {
        checkAddress();
        return address;
    }

    protected void checkAddress() {
        if (memref == null && address == null) {
            throw new Error("address did not resolved: " + name);
        }
    }

    public Location location() {
        return typeNode.location();
    }

    abstract public <T> T accept(EntityVisitor<T> visitor);

    public void dump(net.loveruby.cflat.ast.Dumper d) {
        d.printClass(this, location());
        _dump(d);
    }

    abstract protected void _dump(net.loveruby.cflat.ast.Dumper d);
}
