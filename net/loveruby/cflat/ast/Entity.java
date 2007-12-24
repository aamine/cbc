package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.asm.*;

abstract public class Entity extends Definition {
    protected boolean isPrivate;
    protected TypeNode typeNode;
    protected long nRefered;

    public Entity(boolean priv, TypeNode t, String name) {
        super(name);
        isPrivate = priv;
        typeNode = t;
    }

    public boolean isEntity() {
        return true;
    }

    abstract public boolean isDefined();
    abstract public boolean isInitialized();

    public boolean isPrivate() {
        return isPrivate;
    }

    public TypeNode typeNode() {
        return typeNode;
    }

    public Type type() {
        return typeNode.type();
    }

    public long size() {
        return type().size();
    }

    public long alignment() {
        return 4;  // FIXME: platform dependent
    }

    public void refered() {
        nRefered++;
    }

    public boolean isRefered() {
        return (nRefered > 0);
    }

    abstract public void defineIn(ToplevelScope toplevel);
    abstract public AsmEntity address();
}
