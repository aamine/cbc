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

    public long alignment() { return real().alignment(); }
    public long size() { return real().size(); }
    public boolean isReferable() { return real().isReferable(); }
    public boolean isInt() { return real().isInt(); }
    public boolean isInteger() { return real().isInteger(); }
    public boolean isNumeric() { return real().isNumeric(); }
    public boolean isPointer() { return real().isPointer(); }
    public boolean isArray() { return real().isArray(); }
    public boolean isStruct() { return real().isStruct(); }
    public boolean isUnion() { return real().isUnion(); }
    public boolean isUserType() { return true; }
    public boolean isFunction() { return real().isFunction(); }
}
