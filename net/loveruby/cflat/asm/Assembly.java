package net.loveruby.cflat.asm;

abstract public class Assembly {
    abstract public String toSource();

    public boolean isInstruction() {
        return false;
    }

    public boolean isLabel() {
        return false;
    }

    public void collectStatistics(AsmStatistics stats) {
        // does nothing by default.
    }
}
