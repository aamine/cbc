package net.loveruby.cflat.asm;
import net.loveruby.cflat.type.*;

public class Register extends AsmEntity {
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
        // FIXME: check basename.
        return baseName.substring(0, 1) + "l";
    }

    public String toString() {
        return "%" + name();
    }

    public AsmEntity add(long i) {
        return new CompositeAddress(i, this);
    }
}
