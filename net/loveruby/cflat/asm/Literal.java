package net.loveruby.cflat.asm;

public interface Literal {
    public String toSource();
    public String toSource(SymbolTable table);
    public void collectStatistics(AsmStatistics stats);
    public boolean isZero();
    public Literal plus(long diff);
}
