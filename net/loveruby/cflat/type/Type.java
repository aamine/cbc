package net.loveruby.cflat.type;
import net.loveruby.cflat.exception.*;

public abstract class Type {
    static final public long sizeUnknown = -1;

    abstract public long size();
    public long allocSize() { return size(); }
    public long alignment() { return allocSize(); }

    abstract public boolean isSameType(Type other);

    public boolean isVoid() { return false; }
    public boolean isInt() { return false; }
    public boolean isInteger() { return false; }
    public boolean isSigned()
            { throw new Error("#isSigned for non-integer type"); }
    public boolean isPointer() { return false; }
    public boolean isArray() { return false; }
    public boolean isCompositeType() { return false; }
    public boolean isStruct() { return false; }
    public boolean isUnion() { return false; }
    public boolean isUserType() { return false; }
    public boolean isFunction() { return false; }

    // Ability methods (unary)
    public boolean isAllocatedArray() { return false; }
    public boolean isIncompleteArray() { return false; }
    public boolean isScalar() { return false; }
    public boolean isCallable() { return false; }

    // Ability methods (binary)
    abstract public boolean isCompatible(Type other);
    abstract public boolean isCastableTo(Type target);

    public Type baseType() {
        throw new SemanticError("#baseType called for undereferable type");
    }

    // Cast methods
    public IntegerType getIntegerType() { return (IntegerType)this; }
    public PointerType getPointerType() { return (PointerType)this; }
    public FunctionType getFunctionType() { return (FunctionType)this; }
    public StructType getStructType() { return (StructType)this; }
    public UnionType getUnionType() { return (UnionType)this; }
    public CompositeType getCompositeType() { return (CompositeType)this; }
    public ArrayType getArrayType() { return (ArrayType)this; }
}
