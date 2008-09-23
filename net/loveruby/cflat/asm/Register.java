package net.loveruby.cflat.asm;
import net.loveruby.cflat.type.*;

public class Register extends AsmOperand {
    protected int size;
    protected String name;

    // platform dependent
    static final protected int naturalSize = 4;

    public Register(String name) {
        this(naturalSize, name);
    }

    public Register(int size, String name) {
        this.size = size;
        this.name = name;
    }

    public Register forType(Type t) {
        switch ((int)t.size()) {
        case 1:
        case 2:
        case 4:
            return new Register((int)t.size(), name);
        default:
            throw new Error("invalid register size: " + t.size());
        }
    }

    public boolean isRegister() {
        return true;
    }

    public boolean equals(Object other) {
        if (!(other instanceof Register)) return false;
        return equals((Register)other);
    }

    public int hashCode() {
        return name.hashCode();
    }

    /**
     * size difference does NOT matter.
     */
    public boolean equals(Register reg) {
        return name.equals(reg.baseName());
    }

    public String baseName() {
        return name;
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

    protected String lowerByteRegister(String baseName) {
        if (! hasLowerByteRegister(baseName)) {
            throw new Error("does not have lower-byte register: " + baseName);
        }
        return baseName.substring(0, 1) + "l";
    }

    protected boolean hasLowerByteRegister(String baseName) {
        if (baseName.equals("ax")) return true;
        if (baseName.equals("bx")) return true;
        if (baseName.equals("cx")) return true;
        if (baseName.equals("dx")) return true;
        return false;
    }

    public void collectStatistics(AsmStatistics stats) {
        stats.registerUsed(this);
    }

    public String toSource() {
        return "%" + name();
    }
}
