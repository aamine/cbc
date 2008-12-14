package net.loveruby.cflat.asm;

public interface Literal extends Comparable<Literal> {
    public String toSource();
    public String toSource(SymbolTable table);
    public void collectStatistics(AsmStatistics stats);
    public boolean isZero();
    public Literal plus(long diff);
    public int cmp(IntegerLiteral i);
    public int cmp(NamedSymbol sym);
    public int cmp(UnnamedSymbol sym);
    public int cmp(SuffixedSymbol sym);
}
