package net.loveruby.cflat.type;

public class StructTypeRef extends TypeRef {
    protected String name;

    public StructTypeRef(String n) {
        name = n;
    }

    public boolean isStruct() {
        return true;
    }

    public boolean equals(Object other) {
        if (!(other instanceof StructTypeRef)) return false;
        return name.equals(((StructTypeRef)other).name);
    }

    public int hashCode() {
        return 8 ^ name.hashCode();
    }

    public String name() {
        return name;
    }
}
