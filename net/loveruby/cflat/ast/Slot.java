package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;

public class Slot extends Node {
    protected TypeNode typeNode;
    protected String name;
    protected long offset;

    public Slot(TypeNode t, String n) {
        typeNode = t;
        name = n;
        offset = Type.sizeUnknown;
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

    public String name() {
        return name;
    }

    public long size() {
        return type().size();
    }

    public long allocSize() {
        return type().allocSize();
    }

    public long alignment() {
        return type().alignment();
    }

    public long offset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public Location location() {
        return typeNode.location();
    }

    protected void _dump(Dumper d) {
        d.printMember("name", name);
        d.printMember("typeNode", typeNode);
    }
}
