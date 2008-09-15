package net.loveruby.cflat.asm;

public class AbsoluteAddress extends AsmOperand {
    protected Register register;

    public AbsoluteAddress(Register reg) {
        this.register = reg;
    }

    public AsmOperand register() {
        return this.register;
    }

    public String toString() {
        return "*" + register.toString();
    }
}
