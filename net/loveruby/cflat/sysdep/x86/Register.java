package net.loveruby.cflat.sysdep.x86;
import net.loveruby.cflat.asm.*;

class Register extends net.loveruby.cflat.asm.Register {
    RegKind kind;
    Type type;

    Register(RegKind kind, Type type) {
        this.kind = kind;
        this.type = type;
    }

    Register forType(Type t) {
        return new Register(kind, t);
    }

    public boolean isRegister() { return true; }

    public boolean equals(Object other) {
        return (other instanceof Register) && equals((Register)other);
    }

    /** size difference does NOT matter. */
    public boolean equals(Register reg) {
        return kind.equals(reg.kind);
    }

    public int hashCode() {
        return kind.hashCode();
    }

    RegKind kind() {
        return kind;
    }

    String baseName() {
        return kind.toString().toLowerCase();
    }

    public String toSource(SymbolTable table) {
        // GNU assembler dependent
        return "%" + typedName();
    }

    private String typedName() {
        switch (type) {
        case INT8: return lowerByteRegister();
        case INT16: return baseName();
        case INT32: return "e" + baseName();
        case INT64: return "r" + baseName();
        default:
            throw new Error("unknown register Type: " + type);
        }
    }

    private String lowerByteRegister() {
        switch (kind) {
        case AX:
        case BX:
        case CX:
        case DX:
            return baseName().substring(0, 1) + "l";
        default:
            throw new Error("does not have lower-byte register: " + kind);
        }
    }
}
