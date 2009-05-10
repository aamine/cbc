package net.loveruby.cflat.sysdep.x86;
import net.loveruby.cflat.asm.*;

public class Register extends net.loveruby.cflat.asm.Register {
    static final private int naturalSize = 4;

    public Register(String name) {
        super(naturalSize, name);
    }

    public Register(int size, String name) {
        super(size, name);
    }

    public Register forType(Type t) {
        switch (t.size()) {
        case 1:
        case 2:
        case 4:
            return new Register(t.size(), name);
        default:
            throw new Error("invalid register size: " + t.size());
        }
    }

    public String name() {
        switch (size) {
        case 1: return lowerByteRegister(name);
        case 2: return name;
        case 4: return "e" + name;
        default:
            throw new Error("invalid register size: " + size);
        }
    }

    private String lowerByteRegister(String baseName) {
        if (! hasLowerByteRegister(baseName)) {
            throw new Error("does not have lower-byte register: " + baseName);
        }
        return baseName.substring(0, 1) + "l";
    }

    private boolean hasLowerByteRegister(String baseName) {
        if (baseName.equals("ax")) return true;
        if (baseName.equals("bx")) return true;
        if (baseName.equals("cx")) return true;
        if (baseName.equals("dx")) return true;
        return false;
    }

    public String toSource(SymbolTable table) {
        return "%" + name();
    }
}
