package net.loveruby.cflat.asm;

public class AbsoluteAddress extends AsmOperand {
    protected Register register;

    public AbsoluteAddress(Register reg) {
        this.register = reg;
    }

    public AsmOperand register() {
        return this.register;
    }

    public void collectStatistics(AsmStatistics stats) {
        register.collectStatistics(stats);
    }

    public String toSource() {
        return "*" + register.toSource();
    }
}
