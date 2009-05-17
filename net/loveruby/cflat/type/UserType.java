package net.loveruby.cflat.type;
import net.loveruby.cflat.ast.TypeNode;
import net.loveruby.cflat.ast.Location;

public class UserType extends NamedType {
    protected TypeNode real;

    public UserType(String name, TypeNode real, Location loc) {
        super(name, loc);
        this.real = real;
    }

    public Type realType() {
        return real.type();
    }

    public String toString() {
        return name;
    }

    //
    // Forward methods to real type.
    //

    public long size() { return realType().size(); }
    public long allocSize() { return realType().allocSize(); }
    public long alignment() { return realType().alignment(); }

    public boolean isVoid() { return realType().isVoid(); }
    public boolean isInt() { return realType().isInt(); }
    public boolean isInteger() { return realType().isInteger(); }
    public boolean isSigned() { return realType().isSigned(); }
    public boolean isPointer() { return realType().isPointer(); }
    public boolean isArray() { return realType().isArray(); }
    public boolean isAllocatedArray() { return realType().isAllocatedArray(); }
    public boolean isCompositeType() { return realType().isCompositeType(); }
    public boolean isStruct() { return realType().isStruct(); }
    public boolean isUnion() { return realType().isUnion(); }
    public boolean isUserType() { return true; }
    public boolean isFunction() { return realType().isFunction(); }

    public boolean isCallable() { return realType().isCallable(); }
    public boolean isScalar() { return realType().isScalar(); }

    public Type baseType() { return realType().baseType(); }

    public boolean isSameType(Type other) {
        return realType().isSameType(other);
    }

    public boolean isCompatible(Type other) {
        return realType().isCompatible(other);
    }

    public boolean isCastableTo(Type other) {
        return realType().isCastableTo(other);
    }

    public IntegerType getIntegerType() { return realType().getIntegerType(); }
    public CompositeType getCompositeType() { return realType().getCompositeType(); }
    public StructType getStructType() { return realType().getStructType(); }
    public UnionType getUnionType() { return realType().getUnionType(); }
    public ArrayType getArrayType() { return realType().getArrayType(); }
    public PointerType getPointerType() { return realType().getPointerType(); }
    public FunctionType getFunctionType() { return realType().getFunctionType(); }
}
