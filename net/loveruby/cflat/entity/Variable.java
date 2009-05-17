package net.loveruby.cflat.entity;
import net.loveruby.cflat.ast.TypeNode;
import net.loveruby.cflat.type.*;

abstract public class Variable extends Entity {
    public Variable(boolean priv, TypeNode type, String name) {
        super(priv, type, name);
    }
}
