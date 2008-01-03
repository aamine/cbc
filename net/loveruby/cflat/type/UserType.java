package net.loveruby.cflat.type;
import net.loveruby.cflat.ast.TypeNode;

public class UserType extends Type {
    protected String name;
    protected TypeNode real;

    public UserType(String n, TypeNode t) {
        name = n;
        real = t;
    }

    public String name() {
        return name;
    }

    public Type real() {
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

    public long alignment() { return real().alignment(); }
    public long size() { return real().size(); }

    public boolean isInt() { return real().isInt(); }
    public boolean isInteger() { return real().isInteger(); }
    public boolean isPointer() { return real().isPointer(); }
    public boolean isArray() { return real().isArray(); }
    public boolean isComplexType() { return real().isComplexType(); }
    public boolean isStruct() { return real().isStruct(); }
    public boolean isUnion() { return real().isUnion(); }
    public boolean isUserType() { return true; }
    public boolean isFunction() { return real().isFunction(); }

    public boolean isDereferable() { return real().isDereferable(); }
    public boolean isCallable() { return real().isCallable(); }

    public Type baseType() { return real().baseType(); }

    public boolean isCompatible(Type other)
            { return real().isCompatible(other); }
    public boolean isCastableTo(Type other)
            { return real().isCastableTo(other); }

    public ComplexType getComplexType() { return real().getComplexType(); }
    public StructType getStructType() { return real().getStructType(); }
    public UnionType getUnionType() { return real().getUnionType(); }
    public ArrayType getArrayType() { return real().getArrayType(); }
    public PointerType getPointerType() { return real().getPointerType(); }
    public FunctionType getFunctionType() { return real().getFunctionType(); }
}
