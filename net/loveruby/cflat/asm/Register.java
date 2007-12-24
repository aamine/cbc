package net.loveruby.cflat.asm;
import net.loveruby.cflat.type.*;

public class Register extends AsmEntity {
    static public Register forType(Type t, String name) {
        switch ((int)t.size()) {
        case 1: return new Register(name);
        case 2: return new Register(name);
        case 4: return new Register("e" + name);
        case 8: return new Register("r" + name);
        default:
            throw new Error("invalid register size: " + t.size());
        }
    }

    static public Register widestRegister(String name) {
        return new Register("e" + name);
    }

    String name;

    public Register(String n) {
        name = n;
    }

    public String name() {
        return name;
    }

    public String toString() {
        return "%" + name;
    }

    public AsmEntity add(long n) {
        return new CompositeAddress(n, this);
    }
}
