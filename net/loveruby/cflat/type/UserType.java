package net.loveruby.cflat.type;
import net.loveruby.cflat.ast.TypeNode;

public class UserType extends Type {
    protected String name;
    protected TypeNode real;

    public UserType(String name, TypeNode real) {
        this.name = name;
        this.real = real;
    }

    public String name() {
        return name;
    }

    public Type realType() {
        return real.type();
    }

    public TypeNode typeNode() {
        return real;
    }

    public String textize() {
        return name;
    }

    //
    // Forward methods to real type.
    //

    public long alignment() { return realType().alignment(); }
    public long size() { return realType().size(); }

    public boolean isInt() { return realType().isInt(); }
    public boolean isInteger() { return realType().isInteger(); }
    public boolean isPointer() { return realType().isPointer(); }
    public boolean isArray() { return realType().isArray(); }
    public boolean isComplexType() { return realType().isComplexType(); }
    public boolean isStruct() { return realType().isStruct(); }
    public boolean isUnion() { return realType().isUnion(); }
    public boolean isUserType() { return true; }
    public boolean isFunction() { return realType().isFunction(); }

    public boolean isDereferable() { return realType().isDereferable(); }
    public boolean isCallable() { return realType().isCallable(); }

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
    public ComplexType getComplexType() { return realType().getComplexType(); }
    public StructType getStructType() { return realType().getStructType(); }
    public UnionType getUnionType() { return realType().getUnionType(); }
    public ArrayType getArrayType() { return realType().getArrayType(); }
    public PointerType getPointerType() { return realType().getPointerType(); }
    public FunctionType getFunctionType() { return realType().getFunctionType(); }
}
