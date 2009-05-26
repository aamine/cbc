package net.loveruby.cflat.asm;

public class AbsoluteAddress extends Operand {
    protected Register register;

    public AbsoluteAddress(Register reg) {
        this.register = reg;
    }

    public Operand register() {
        return this.register;
    }

    public void collectStatistics(Statistics stats) {
        register.collectStatistics(stats);
    }

    public String toSource(SymbolTable table) {
        return "*" + register.toSource(table);
    }

    public String dump() {
        return "(AbsoluteAddress " + register.dump() + ")";
    }
}
