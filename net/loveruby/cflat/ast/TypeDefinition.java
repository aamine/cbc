package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;

abstract public class TypeDefinition extends Definition {
    protected Location location;
    protected TypeNode typeNode;

    public TypeDefinition(Location loc, TypeRef ref, String name) {
        super(name);
        this.location = loc;
        this.typeNode = new TypeNode(ref);
    }

    public Location location() {
        return location;
    }

    public boolean isType() {
        return true;
    }

    public TypeNode typeNode() {
        return typeNode;
    }

    public TypeRef typeRef() {
        return typeNode.typeRef();
    }

    public Type type() {
        return typeNode.type();
    }

    abstract public void defineIn(TypeTable table);
}
