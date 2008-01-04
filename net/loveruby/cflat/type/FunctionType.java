package net.loveruby.cflat.type;
import net.loveruby.cflat.ast.Params;
import java.util.*;

public class FunctionType extends Type {
    protected Type returnType;
    protected Params paramTypes;

    public FunctionType(Type ret, Params partypes) {
        returnType = ret;
        paramTypes = partypes;
    }

    public boolean isFunction() { return true; }

    public boolean isSameType(Type other) {
        if (! other.isFunction()) return false;
        return equals(other.getFunctionType());
    }

    public Type returnType() {
        return returnType;
    }

    public boolean isVararg() {
        return paramTypes.isVararg();
    }

    public boolean acceptsArgc(long numArgs) {
        if (paramTypes.isVararg()) {
            return (numArgs >= paramTypes.minArgc());
        }
        else {
            return (numArgs == paramTypes.argc());
        }
    }

    /**
     * Returns iterator of mandatory parameter types.
     * This method does NOT include types for varargs.
     */
    public Iterator paramTypes() {
        return paramTypes.parameters();
    }

    public long alignment() {
        throw new Error("FunctionType#alignment called");
    }

    public long size() {
        throw new Error("FunctionType#size called");
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(returnType.toString());
        buf.append(" (*)(");
        Iterator params = paramTypes.parameters();
        while (params.hasNext()) {
            Type t = (Type)params.next();
            buf.append(t.toString());
        }
        buf.append(")");
        return buf.toString();
    }
}
