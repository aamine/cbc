package net.loveruby.cflat.asm;

abstract public class Register extends AsmOperand {
    protected int size;
    protected String name;

    public Register(int size, String name) {
        this.size = size;
        this.name = name;
    }

    abstract public Register forType(Type t);

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

    /** size difference does NOT matter. */
    public boolean equals(Register reg) {
        return name.equals(reg.baseName());
    }

    public String baseName() {
        return name;
    }

    // default implementation
    public String name() {
        return name;
    }

    public void collectStatistics(AsmStatistics stats) {
        stats.registerUsed(this);
    }

    abstract public String toSource(SymbolTable table);
}
