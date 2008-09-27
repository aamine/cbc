package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.asm.*;

abstract public class Variable extends Entity {
    protected MemoryReference memref;
    protected MemoryReference address;

    public Variable(boolean priv, TypeNode type, String name) {
        super(priv, type, name);
    }

    public boolean cannotLoad() {
        return type().isAllocatedArray();
    }

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
}
