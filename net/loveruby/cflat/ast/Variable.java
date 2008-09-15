package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.asm.*;

abstract public class Variable extends Entity {
    protected long sequence;

    public Variable(boolean priv, TypeNode type, String name) {
        super(priv, type, name);
        sequence = -1;
    }

    public void setSequence(long seq) {
        this.sequence = seq;
    }

    public String symbol() {
        return (sequence < 0) ? name : (name + "." + sequence);
    }

    abstract public void setMemref(MemoryReference addr);
}
