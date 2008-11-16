package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;

abstract public class Variable extends Entity {
    public Variable(boolean priv, TypeNode type, String name) {
        super(priv, type, name);
    }

    public boolean cannotLoad() {
        return type().isAllocatedArray();
    }
}
