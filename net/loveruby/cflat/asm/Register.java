package net.loveruby.cflat.asm;

abstract public class Register extends AsmOperand {
    public boolean isRegister() {
        return true;
    }

    public void collectStatistics(AsmStatistics stats) {
        stats.registerUsed(this);
    }

    abstract public String toSource(SymbolTable table);
}
