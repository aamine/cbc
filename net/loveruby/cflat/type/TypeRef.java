package net.loveruby.cflat.type;

public abstract class TypeRef {
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
    public boolean isFunctionType() { return false; }
}
