package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;

abstract public class TypeDefinition extends Definition {
    public TypeDefinition(String name) {
        super(name);
    }

    public boolean isType() {
        return true;
    }

    abstract public TypeNode typeNode();
    abstract public TypeRef typeRef();
    abstract public void defineIn(TypeTable table);
}
