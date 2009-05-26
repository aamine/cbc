package net.loveruby.cflat.asm;

abstract public class BaseSymbol implements Symbol {
    public boolean isZero() {
        return false;
    }

    public void collectStatistics(Statistics stats) {
        stats.symbolUsed(this);
    }

    public Literal plus(long n) {
        throw new Error("must not happen: BaseSymbol.plus called");
    }
}
