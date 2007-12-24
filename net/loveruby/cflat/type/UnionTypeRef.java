package net.loveruby.cflat.type;

public class UnionTypeRef extends TypeRef {
    protected String name;

    public UnionTypeRef(String n) {
        name = n;
    }

    public boolean isUnion() {
        return true;
    }

    public boolean equals(Object other) {
        if (!(other instanceof UnionTypeRef)) return false;
        return name.equals(((UnionTypeRef)other).name);
    }

    public int hashCode() {
        return 9 ^ name.hashCode();
    }

    public String name() {
        return name;
    }
}
