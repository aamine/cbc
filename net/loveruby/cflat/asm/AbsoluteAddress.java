package net.loveruby.cflat.asm;

public class AbsoluteAddress extends AsmEntity {
    protected Register register;

    public AbsoluteAddress(Register reg) {
        this.register = reg;
    }

    public AsmEntity register() {
        return this.register;
    }

    public String toString() {
        return "*" + register.toString();
    }
}
