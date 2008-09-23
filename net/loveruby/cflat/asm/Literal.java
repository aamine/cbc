package net.loveruby.cflat.asm;

abstract public class Literal {
    abstract public String toSource();
    abstract public void collectStatistics(AsmStatistics stats);
}
