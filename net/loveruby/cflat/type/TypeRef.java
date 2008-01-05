package net.loveruby.cflat.type;
import net.loveruby.cflat.ast.Location;

public abstract class TypeRef {
    protected Location location;

    public TypeRef(Location loc) {
        this.location = loc;
    }

    public Location location() {
        return location;
    }

    public boolean isVoid() { return false; }
    public boolean isSignedChar() { return false; }
    public boolean isSignedShort() { return false; }
    public boolean isSignedInt() { return false; }
    public boolean isSignedLong() { return false; }
    public boolean isUnsignedChar() { return false; }
    public boolean isUnsignedShort() { return false; }
    public boolean isUnsignedInt() { return false; }
    public boolean isUnsignedLong() { return false; }
    public boolean isArray() { return false; }
    public boolean isPointer() { return false; }
    public boolean isStruct() { return false; }
    public boolean isUnion() { return false; }
    public boolean isUserType() { return false; }
    public boolean isFunction() { return false; }

    public PointerTypeRef getPointerTypeRef() { return (PointerTypeRef)this; }
    public ArrayTypeRef getArrayTypeRef() { return (ArrayTypeRef)this; }
    public StructTypeRef getStructTypeRef() { return (StructTypeRef)this; }
    public UnionTypeRef getUnionTypeRef() { return (UnionTypeRef)this; }
    public FunctionTypeRef getFunctionTypeRef() { return (FunctionTypeRef)this;}
}
