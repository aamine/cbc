package net.loveruby.cflat.type;

public class PointerTypeRef extends TypeRef {
    protected TypeRef baseType;

    public PointerTypeRef(TypeRef baseType) {
        super(baseType.location());
        this.baseType = baseType;
    }

    public boolean isPointer() {
        return true;
    }

    public TypeRef baseType() {
        return baseType;
    }

    public boolean equals(Object other) {
        if (! (other instanceof PointerTypeRef)) return false;
        return baseType.equals(((PointerTypeRef)other).baseType);
    }

    public String toString() {
        return baseType.toString() + "*";
    }
}
