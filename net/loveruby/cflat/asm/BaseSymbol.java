package net.loveruby.cflat.asm;

abstract public class BaseSymbol implements Symbol {
    public boolean isZero() {
        return false;
    }

    public void collectStatistics(AsmStatistics stats) {
        stats.symbolUsed(this);
    }
}
